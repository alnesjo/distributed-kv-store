package se.kth.id2203.broadcast

import se.kth.id2203.link.Deliver
import se.sics.kompics.sl.Port

class ReliableBroadcast extends Port {

  indication[Deliver]
  request[Broadcast]

}