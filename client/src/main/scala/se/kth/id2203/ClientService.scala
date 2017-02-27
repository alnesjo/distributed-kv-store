package se.kth.id2203

import java.util
import java.util.UUID
import java.util.concurrent.Future

import com.google.common.util.concurrent.SettableFuture
import org.slf4j.LoggerFactory
import se.kth.id2203.kvstore.{OperationInvoke, OperationRespond}
import se.kth.id2203.link.NetworkMessage
import se.kth.id2203.overlay.{Ack, Connect, RouteMessage}
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timeout, Timer}
import se.sics.kompics.{Kompics, KompicsEvent, Start}

object ClientService {

  case class Init(self: Address) extends se.sics.kompics.Init[ClientService]

}

class ClientService(init: ClientService.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[ClientService])

  val timer = requires[Timer]
  val net = requires[Network]

  val self = init.self
  val master = cfg.getValue[Address]("id2203.project.bootstrap-address")
  val timeout = 2 * cfg.getValue[Long]("id2203.project.keepAlivePeriod")

  var connected: Option[Ack] = None
  val pending = new util.TreeMap[String, SettableFuture[OperationRespond]]

  ctrl uponEvent {
    case _: Start => handle {
      log.debug(s"Starting client on $self. Waiting to connect...")
      val st: ScheduleTimeout = new ScheduleTimeout(timeout)
      st.setTimeoutEvent(new ConnectTimeout(st))
      trigger(NetworkMessage(self, master, Transport.TCP, Connect(st.getTimeoutEvent.getTimeoutId.toString)) -> net)
      trigger(st -> timer)
    }
  }

  net uponEvent {
    case NetworkMessage(_, _, _, ack@Ack(id, clusterSize)) => handle {
      log.debug(s"Client connected to $master, cluster size is $clusterSize")
      connected = Some(ack)
      val c: Console = new Console(ClientService.this)
      val tc: Thread = new Thread(c)
      tc.start()
    }
    case NetworkMessage(_, _, _, op@OperationRespond(id, status)) => handle {
      log.debug(s"Got OperationRespond: $op")
      val sf: SettableFuture[OperationRespond] = pending.remove(id)
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
        case Some(ack) if ack.id != event.getTimeoutId.toString =>
          log.error("Received wrong response id earlier! System may be inconsistent. Shutting down...")
          System.exit(1)
        case _ =>
      }
    }
  }

  loopbck uponEvent {
    case owf: OpWithFuture => handle {
      trigger(NetworkMessage(self, master, Transport.TCP, RouteMessage(owf.op.key, owf.op)) -> net)
      pending.put(owf.op.id, owf.f)
    }
  }

  def op(key: String): Future[OperationRespond] = {
    val op = OperationInvoke(UUID.randomUUID.toString, key)
    val owf = OpWithFuture(op)
    trigger(owf -> onSelf)
    owf.f
  }

  case class OpWithFuture(op: OperationInvoke) extends KompicsEvent {
    val f: SettableFuture[OperationRespond] = SettableFuture.create()
  }

  class ConnectTimeout(val st: ScheduleTimeout) extends Timeout(st) {
  }

}
