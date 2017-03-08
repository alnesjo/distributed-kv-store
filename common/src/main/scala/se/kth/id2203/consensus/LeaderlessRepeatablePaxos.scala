package se.kth.id2203.consensus

import se.kth.id2203._
import se.sics.kompics.network.Address
import se.sics.kompics.sl._

object LeaderlessRepeatablePaxos {

  case class Init(self: Address, topology: Set[Address]) extends se.sics.kompics.Init[LeaderlessRepeatablePaxos]

}

class LeaderlessRepeatablePaxos(init: LeaderlessRepeatablePaxos.Init) extends ComponentDefinition {

  implicit def addComparators[A](x: A)(implicit o: math.Ordering[A]): o.Ops = o.mkOrderingOps(x)

  //Port Subscriptions for Paxos

  val consensus = provides[Consensus]
  val beb = requires[BestEffortBroadcast]
  val plink = requires[PerfectLink]

  //Internal State of Paxos
  val (selfRank, nrNodes) = (init.self.##, init.topology.size)

  //Proposer State
  var ts = 0
  var pv = Option.empty[Any]
  var promises = List.empty[((Int, Int), Option[Any])]
  var numOfAccepts = 0
  var decided = false

  //Acceptor State
  var promisedBallot = (0, 0)
  var acceptedBallot = (0, 0)
  var av = Option.empty[Any]

  def propose() = {
    if (!decided) {
      ts += 1
      numOfAccepts = 0
      promises = List.empty
      trigger(BEB_Broadcast(Prepare((ts,selfRank))) -> beb)
    }
  }

  consensus uponEvent {
    case C_Propose(v) => handle {
      pv = Some(v)
      propose()
    }
  }


  beb uponEvent {
    case BEB_Deliver(p, Prepare(ballot)) => handle {
      if (promisedBallot < ballot) {
        promisedBallot = ballot
        trigger(PL_Send(p, Promise(promisedBallot, acceptedBallot, av)) -> plink)
      } else {
        trigger(PL_Send(p, Nack(ballot)) -> plink)
      }
    }
    case BEB_Deliver(p, Accept(ballot, v)) => handle {
      if (promisedBallot <= ballot) {
        acceptedBallot = ballot
        promisedBallot = ballot
        av = Some(v)
        trigger(PL_Send(p, Accepted(ballot)) -> plink)
      } else {
        trigger(PL_Send(p, Nack(ballot)) -> plink)
      }
    }
    case BEB_Deliver(_, Decided(v)) => handle {
      if (!decided) {
        trigger(C_Decide(v) -> consensus)
        decided = true
      }
    }
  }

  plink uponEvent {
    case PL_Deliver(_, Promise(b,a,v)) => handle {
      if ((ts, selfRank) == b) {
        promises :+= (a -> v)
        if (promises.size == (nrNodes+1)/2) {
          val (maxBallot, value) = promises.sortBy(_._1).last
          if (value.nonEmpty) {
            pv = value
          }
          trigger(BEB_Broadcast(Accept((ts,selfRank),pv)) -> beb)
        }
      }
    }

    case PL_Deliver(_, Accepted(ballot)) => handle {
      if ((ts, selfRank) == ballot) {
        numOfAccepts += 1
        if (numOfAccepts == (nrNodes+1)/2) {
          trigger(BEB_Broadcast(Decided(pv)) -> beb)
        }
      }
    }
    case PL_Deliver(_, Nack(ballot)) => handle {
      if ((ts, selfRank) == ballot) {
        propose()
      }
    }
  }

}