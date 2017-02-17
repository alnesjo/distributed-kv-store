package se.kth.id2203.simulation

import org.slf4j.LoggerFactory

class ScenarioClient extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[BootstrapMaster])
  // Ports
  val pl = requires(PerfectLink)
  val timer = requires[Timer]

  // Fields
  
}