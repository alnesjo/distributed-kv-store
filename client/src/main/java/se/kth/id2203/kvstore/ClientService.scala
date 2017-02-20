package se.kth.id2203.kvstore

import java.util
import java.util.UUID
import java.util.concurrent.Future

import com.google.common.base.Optional
import com.google.common.util.concurrent.SettableFuture
import org.slf4j.LoggerFactory
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.overlay.{Connect, Route}
import se.sics.kompics.{Kompics, KompicsEvent, Start}
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timeout, Timer}

object ClientService {

  case class Init(self: Address) extends se.sics.kompics.Init[ClientService]

}

class ClientService(init: ClientService.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[ClientService])

  val timer = requires[Timer]
  val pl = requires(PerfectLink)

  val self = init.self
  val master = cfg.getValue[Address]("id2203.project.bootstrap-address")
  val timeout = 2 * cfg.getValue[Long]("id2203.project.keepAlivePeriod")

  var connected: Optional[Connect.Ack] = Optional.absent()
  val pending = new util.TreeMap[UUID, SettableFuture[OperationRespond]]

  ctrl uponEvent {
    case _: Start => handle {
      log.debug(s"Starting client on $self. Waiting to connect...")
      val st: ScheduleTimeout = new ScheduleTimeout(timeout)
      st.setTimeoutEvent(new ConnectTimeout(st))
      trigger(PL_Send(master, new Connect(st.getTimeoutEvent.getTimeoutId)) -> pl)
      trigger(st -> timer)
    }
  }

  pl uponEvent {
    case PL_Deliver(_, ack@Connect.Ack(id, clusterSize)) => handle {
      log.debug(s"Client connected to $master, cluster size is $clusterSize")
      connected = Optional.of(ack)
      val c: Console = new Console(ClientService.this)
      val tc: Thread = new Thread(c)
      tc.start()
    }
    case PL_Deliver(_, op@OperationRespond(id, status)) => handle {
      log.debug(s"Got OperationRespond: $op")
      val sf: SettableFuture[OperationRespond] = pending.remove(id)
      if (sf != null) sf.set(op)
      else log.debug(s"ID $id was not pending! Ignoring response.")
    }
  }

  timer uponEvent {
    case event: ConnectTimeout => handle {
      if (!connected.isPresent) {
        log.debug(s"Connection to server $master did not succeed. Shutting down...")
        Kompics.asyncShutdown()
      } else {
        val cack = connected.get
        if (!(cack.id == event.getTimeoutId)) {
          log.debug("Received wrong response id earlier! System may be inconsistent. Shutting down...")
          System.exit(1)
        }
      }
    }
  }

  loopbck uponEvent {
    case event: OpWithFuture => handle {
      trigger(PL_Send(master, Route(event.op.key, event.op)) -> pl)
      pending.put(event.op.id, event.f)
    }
  }

  def op(key: String): Future[OperationRespond] = {
    val op = OperationInvoke(key)
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
