package se.kth.id2203.bootstrapping

import java.util.UUID

import org.slf4j.LoggerFactory
import se.kth.id2203.broadcast.BasicBroadcast
import se.kth.id2203.failure.HeartbeatFailureDetector
import se.kth.id2203.overlay.{LookupTable, OverlayManager}
import se.kth.id2203.register.ReadImposeWriteConsultMajority
import se.kth.id2203.{AtomicRegister, BestEffortBroadcast, EventuallyPerfectFailureDetector, PerfectLink}
import se.sics.kompics.Start
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer

object Kernel {

  /**
    * @param self Address of self
    * @param delta Replication degree
    * @param period Keepalive interval
    */
  case class Init(self: Address, delta: Int, period: Long) extends se.sics.kompics.Init[Kernel]

}

class Kernel(init: Kernel.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[Kernel])

  val timer = requires[Timer]
  val boot = requires[Bootstrapping]
  val pl = requires[PerfectLink]

  val (self, delta, period) = (init.self, init.delta, init.period)

  boot uponEvent {
    case GetInitialAssignments(nodes: Set[Address]) => handle {
      val lut = LookupTable.generate(nodes, delta)
      log.debug("Generated initial node assignments.")
      trigger(InitialAssignments(lut) -> boot)
    }
    case Booted(lut) => handle {
      log.debug("Preparing new components.")

      val global = lut.getNodes
      val local: Set[Address] = lut.lookup(self)

      val lbb = create(classOf[BasicBroadcast], BasicBroadcast.Init(self, local))
      val gbb = create(classOf[BasicBroadcast], BasicBroadcast.Init(self, global))
      val htb = create(classOf[HeartbeatFailureDetector], HeartbeatFailureDetector.Init(self, global, period))
      val icm = create(classOf[ReadImposeWriteConsultMajority], ReadImposeWriteConsultMajority.Init(self, local.size))
      val ovl = create(classOf[OverlayManager], OverlayManager.Init(self, lut))

      connect[Timer](timer -> htb)
      connect[PerfectLink](pl -> htb)
      connect[PerfectLink](pl -> gbb)
      connect[PerfectLink](pl -> lbb)
      connect[PerfectLink](pl -> icm)
      connect[BestEffortBroadcast](lbb -> icm)
      connect[PerfectLink](pl -> ovl)
      connect[EventuallyPerfectFailureDetector](htb -> ovl)
      connect[BestEffortBroadcast](gbb -> ovl)
      connect[AtomicRegister](icm -> ovl)

      trigger(new Start -> lbb.control())
      trigger(new Start -> gbb.control())
      trigger(new Start -> htb.control())
      trigger(new Start -> icm.control())
      trigger(new Start -> ovl.control())

      val x = None
      x.nonEmpty
    }
  }
}