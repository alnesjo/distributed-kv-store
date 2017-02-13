package se.kth.id2203

import se.sics.kompics.Init
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}
import se.sics.kompics.network.{Address, Network}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer

class Host extends ComponentDefinition {

  val self = cfg.getValue[Address]("id2203.project.address")

  val timer = create(classOf[JavaTimer], Init.NONE)
  val net = create(classOf[NettyNetwork], new NettyInit(self))
  val parent = create(classOf[Parent], Init.NONE)

  connect[Timer](timer -> parent)
  connect[Network](net -> parent)

}
