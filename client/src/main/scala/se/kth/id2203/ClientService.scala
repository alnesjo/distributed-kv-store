package se.kth.id2203

import java.util
import java.util.concurrent.Future

import com.google.common.util.concurrent.SettableFuture
import org.slf4j.LoggerFactory
import se.kth.id2203.kvstore._
import se.kth.id2203.kvstore._
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timeout, Timer}
import se.sics.kompics.{Kompics, KompicsEvent, Start}

object ClientService {

  case class Init(self: Address) extends se.sics.kompics.Init[ClientService]

}

class ClientService(init: ClientService.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[ClientService])

  val timer = requires[Timer]
  val pl = requires[PerfectLink]

  val self = init.self
  val master = cfg.getValue[Address]("id2203.project.bootstrap-address")
  val timeout = 2 * cfg.getValue[Long]("id2203.project.keepAlivePeriod")

  var connected: Option[Ack] = None
  val pendingGet = new util.TreeMap[Identifier, SettableFuture[GetRespond]]
  val pendingPut = new util.TreeMap[Identifier, SettableFuture[PutRespond]]

  ctrl uponEvent {
    case _: Start => handle {
      log.debug(s"Starting client on $self. Waiting to connect...")
      val st: ScheduleTimeout = new ScheduleTimeout(timeout)
      st.setTimeoutEvent(new ConnectTimeout(st))
      trigger(PL_Send(master, Connect(Identifier(st.getTimeoutEvent.getTimeoutId, self))) -> pl)
      trigger(st -> timer)
    }
  }

  pl uponEvent {
    case PL_Deliver(_, ack@Ack(id, clusterSize)) => handle {
      log.debug(s"Client connected to $master, cluster size is $clusterSize")
      connected = Some(ack)
      val c: Console = new Console(ClientService.this)
      val tc: Thread = new Thread(c)
      tc.start()
    }
    case PL_Deliver(_, op@GetRespond(id, _, Ok)) => handle {
      log.debug(s"Got OperationRespond: $op")
      val sf: SettableFuture[GetRespond] = pendingGet.remove(id)
      if (sf != null) sf.set(op)
      else log.debug(s"ID $id was not pending! Ignoring response.")
    }
    case PL_Deliver(_, op@PutRespond(id, status)) => handle {
      log.debug(s"Got OperationRespond: $op")
      val sf: SettableFuture[PutRespond] = pendingPut.remove(id)
      if (sf != null) sf.set(op)
      else log.debug(s"ID $id was not pending! Ignoring response.")
    }
  }

  timer uponEvent {
    case event: ConnectTimeout => handle {
      connected match {
        case None =>
          log.debug(s"Connection to server $master did not succeed. Shutting down...")
          Kompics.asyncShutdown()
        case Some(Ack(id,_)) if id.uuid != event.getTimeoutId =>
          log.error("Received wrong response id earlier! System may be inconsistent. Shutting down...")
          System.exit(1)
        case _ =>
      }
    }
  }

  loopbck uponEvent {
    case owf: GetWithFuture => handle {
      trigger(PL_Send(master, owf.op) -> pl)
      pendingGet.put(owf.op.id, owf.f)
    }
    case owf: PutWithFuture => handle {
      trigger(PL_Send(master, owf.op) -> pl)
      pendingPut.put(owf.op.id, owf.f)
    }
  }

  def get(key: String): Future[GetRespond] = {
    val op = GetInvoke(Identifier.fromSource(self), key)
    val owf = GetWithFuture(op)
    trigger(owf -> onSelf)
    owf.f
  }

  def put(key: String, value: String): Future[PutRespond] = {
    val op = PutInvoke(Identifier.fromSource(self), key, value)
    val owf = PutWithFuture(op)
    trigger(owf -> onSelf)
    owf.f
  }

  case class GetWithFuture(op: GetInvoke) extends KompicsEvent {
    val f: SettableFuture[GetRespond] = SettableFuture.create()
  }

  case class PutWithFuture(op: PutInvoke) extends KompicsEvent {
    val f: SettableFuture[PutRespond] = SettableFuture.create()
  }

  class ConnectTimeout(val st: ScheduleTimeout) extends Timeout(st) {
  }

}
