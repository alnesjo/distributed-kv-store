package se.kth.id2203

import se.kth.id2203.bootstrapping.{BootstrapMaster, BootstrapSlave, Bootstrapping, Kernel}
import se.kth.id2203.link.TcpLink
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

  val tcp = create(classOf[TcpLink], TcpLink.Init(self))
  val boot = master match {
    case Some(address) =>
      create(classOf[BootstrapSlave], BootstrapSlave.Init(self, address, keepAlivePeriod))
    case None =>
      create(classOf[BootstrapMaster], BootstrapMaster.Init(self, bootThreshold, keepAlivePeriod))
  }
  val krnl = create(classOf[Kernel], Kernel.Init(self, 3))

  connect[Network](net -> tcp)
  connect[PerfectLink](tcp -> boot)
  connect[Timer](timer -> boot)
  connect[Timer](timer -> krnl)
  connect[Bootstrapping](boot -> krnl)
  connect[PerfectLink](tcp -> krnl)

}
