package se.kth.id2203

import se.kth.id2203.link.TcpLink
import se.sics.kompics.Init
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}
import se.sics.kompics.network.{Address, Network}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer

class ClientHost extends ComponentDefinition {

  val self = cfg.getValue[Address]("id2203.project.address")

  val timer = create(classOf[JavaTimer], Init.NONE)
  val net = create(classOf[NettyNetwork], new NettyInit(self))
  val pl = create(classOf[TcpLink], TcpLink.Init(self))
  val parent = create(classOf[ClientService], ClientService.Init(self))

  connect[Timer](timer -> parent)
  connect[Network](net -> pl)
  connect[PerfectLink](pl -> parent)

}
