package se.kth.id2203.bootstrapping

import org.slf4j.LoggerFactory
import se.kth.id2203.broadcast.BasicBroadcast
import se.kth.id2203.failure.HeartbeatFailureDetector
import se.kth.id2203.kvstore._
import se.kth.id2203.register.{PartitionedAtomicRegister, ReadImposeWriteConsultMajority}
import se.kth.id2203._
import se.sics.kompics.{Positive, Start}
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer

import scala.util.Random

object Kernel {

  /**
    * @param self Address of self
    * @param delta Replication degree
    * @param period Keepalive interval
    */
  case class Init(self: Address, delta: Int, period: Long) extends se.sics.kompics.Init[Kernel]

}

/**
  * Creates and starts core components and forwards elementary ports.
  * @see [[se.kth.id2203.bootstrapping.Kernel.Init Init]]
  */
class Kernel(init: Kernel.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[Kernel])

  val timer = requires[Timer]
  val boot = requires[Bootstrapping]
  val pl = requires[PerfectLink]
  val epfd = requires[EventuallyPerfectFailureDetector]
  var kvs = requires[KeyValueStore]

  val (self, delta, period) = (init.self, init.delta, init.period)

  var luto = Option.empty[LookupTable]

  boot uponEvent {
    case GetInitialAssignments(nodes: Set[Address]) => handle {
      val lut = LookupTable.generate(nodes, delta)
      log.debug("Generated initial node assignments.")
      trigger(InitialAssignments(lut) -> boot)
    }
    case Booted(lut) => handle {
      val global = lut.nodes
      luto = Some(lut)
      log.debug("Preparing new components.")

      val gbb = create(classOf[BasicBroadcast], BasicBroadcast.Init(self, global))
      val htb = create(classOf[HeartbeatFailureDetector], HeartbeatFailureDetector.Init(self, global, period))
      val icm = create(classOf[ReadImposeWriteConsultMajority], ReadImposeWriteConsultMajority.Init(self, lut))
      val ss = create(classOf[StoreService], StoreService.Init(self, lut))

      connect[Timer](timer -> htb)
      connect[PerfectLink](pl -> htb)
      connect[EventuallyPerfectFailureDetector](htb -> getComponentCore)
      connect[PerfectLink](pl -> gbb)
      connect[PerfectLink](pl -> icm)
      connect[BestEffortBroadcast](gbb -> icm)
      connect[PartitionedAtomicRegister](icm -> ss)
      connect[KeyValueStore](ss -> getComponentCore)

      trigger(new Start -> gbb.control())
      trigger(new Start -> htb.control())
      trigger(new Start -> icm.control())
      trigger(new Start -> ss.control())
    }
  }

  pl uponEvent {
    case PL_Deliver(_, Connect(id)) => handle {
      luto match {
        case Some(lut) =>
          log.info(s"Acknowledging connection request from ${id.src}.")
          trigger(PL_Send(id.src, Ack(id, lut.nodes.size)) -> pl)
        case None =>
          log.info(s"Rejected connection request since system is not ready yet.")
      }
    }
    case PL_Deliver(_, inv: Invocation) => handle {
      luto match {
        case Some(lut) =>
          val key = lut.lookup(inv.key)
          val replicators = lut.replicators(key)
          if (replicators contains self) {
            log.info(s"Handling operation invocation on partition $key.")
            trigger(inv -> kvs)
          } else {
            log.info(s"Forwarding operation invocation to a random replicator of partition $key.")
            val dst = Random.shuffle(replicators.toList).headOption match {
              case Some(address) => address
              case None => ???
              // Entire replication group is suspected by FD.
              // Terminate/inform client/retry by sending to self?
            }
            trigger(PL_Send(dst, inv) -> pl)
          }
        case None => // Not ready for requests
      }
    }
  }

  kvs uponEvent {
    case res: Response => handle {
      trigger(PL_Send(res.id.src, res) -> pl)
    }
  }

  epfd uponEvent {
    case EP_Suspect(node: Address) => handle {
      luto.map(_ - node)
    }
    case EP_Restore(node: Address) => handle {
      luto.map(_ + node)
    }
  }
}