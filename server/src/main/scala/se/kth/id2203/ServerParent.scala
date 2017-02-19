package se.kth.id2203

import se.kth.id2203.bootstrapping.{BootstrapMaster, BootstrapSlave, Bootstrapping}
import se.kth.id2203.link.TcpLink
import se.kth.id2203.overlay.VSOverlayManager
import se.sics.kompics.Init
import se.sics.kompics.network.{Address, Network}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer

object ServerParent {

  case class Init(self: Address) extends se.sics.kompics.Init[ServerParent]

}

class ServerParent(init: ServerParent.Init) extends ComponentDefinition {

  val net = requires[Network]
  val timer = requires[Timer]

  val self = init.self
  val master = cfg.readValue[Address]("id2203.project.bootstrap-address")
  val bootThreshold = cfg.getValue[Int]("id2203.project.bootThreshold")
  val keepAlivePeriod = cfg.getValue[Long]("id2203.project.keepAlivePeriod")


  val pl = create(classOf[TcpLink], TcpLink.Init(self))
  val boot = master match {
    case Some(_) =>
      create(classOf[BootstrapSlave], Init.NONE)
    case None =>
      create(classOf[BootstrapMaster], BootstrapMaster.Init(self, bootThreshold, keepAlivePeriod))
  }
  val overlay = create(classOf[VSOverlayManager], Init.NONE)
  //val store = create(classOf[KeyValueStore], Init.NONE)

  connect[Network](net -> pl)
  connect[Timer](timer -> boot)
  connect(PerfectLink)(pl -> boot)
  connect(Bootstrapping)(boot -> overlay)
  connect[Network](net -> overlay)
  //connect(Routing)(overlay -> store)
  //connect[Network](net -> store)

}
