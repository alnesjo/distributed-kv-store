package se.kth.id2203

import se.kth.id2203.bootstrapping.{Bootstrapping, BootstrapSlave, BootstrapMaster}
import se.kth.id2203.shared.StoreService
import se.kth.id2203.overlay.{Routing, VSOverlayManager}
import se.sics.kompics.Init
import se.sics.kompics.network.{Address, Network}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer

class Parent extends ComponentDefinition {

  val net = requires[Network]
  val timer = requires[Timer]

  val self = cfg.getValue[Address]("id2203.project.address")
  val other = cfg.readValue[Address]("id2203.project.bootstrap-address")
  val boot = create(other match {
    case Some(_) =>
      classOf[BootstrapSlave]
    case None =>
      classOf[BootstrapMaster]
  }, Init.NONE)
  val overlay = create(classOf[VSOverlayManager], Init.NONE)
  val store = create(classOf[StoreService], Init.NONE)

  connect[Timer](timer -> boot)
  connect[Network](net -> boot)
  connect(Bootstrapping)(boot -> overlay)
  connect[Network](net -> overlay)
  connect(Routing)(overlay -> store)
  connect[Network](net -> store)

}
