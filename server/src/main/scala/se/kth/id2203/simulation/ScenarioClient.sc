package se.kth.id2203.simulation

import java.util.UUID

import com.sun.jndi.cosnaming.IiopUrl.Address
import org.slf4j.LoggerFactory
import se.sics.kompics.Start
import se.sics.kompics.sl.handle
import se.kth.id2203.kvstore
import java.util.UUID

class ScenarioClient extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[BootstrapMaster])
  // Ports
  val pl = requires(PerfectLink)
  val timer = requires[Timer]

  // Fields
  val self = cfg.getValue[Address]("id2203.project.address")
  val server = cfg.getValue[Address]("id2203.project.bootstrap-address")
  // TODO is this the correct way of doing this?
  val res: SimulationResult
  val pending = Map[UUID, String]()

  ctrl uponEvent {
    case _: Start => handle {
      var messages: Int = res.get("messages").toInt
      for(i <- 0 until messages) {
        var op = new OperationInvoke("test" + i)
        var rm = new Route(op.id, op)
        trigger(PL_Send(server, rm) -> pl)
        pending += (op.id -> op.key)
        LOG.info("Sending {}", op)
        res.put(op.key, "SENT")
      }
    }
  }

  pl uponEvent {
    case PL_Deliver(src, opRespond: OperationRespond) => handle {
      LOG.debug("Got OpResponse: {}", content)
      var key: String = opRespond.id
      if (pending.contains(key)) {
        pending -= key
        res.put(key, opRespond.status)
      }
      else {
        LOG.warn("ID {} was not pending! Ignoring response.", opRespond.id);
      }
    }
  }
}