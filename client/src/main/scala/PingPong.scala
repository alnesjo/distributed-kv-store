import se.sics.kompics.sl._
import se.sics.kompics.{Kompics, KompicsEvent, Start, Init => JInit}

//-----------------------------------------------------
//STEP 1: Define the basic events for the service (with counters)
case class Ping(i: Int) extends KompicsEvent;
case class Pong(i: Int) extends KompicsEvent;
//-----------------------------------------------------
//STEP 2: Specify the Port that defines the interface for the service
object PingPongPort extends Port {
  request[Ping];
  indication[Pong];
};
//-----------------------------------------------------
//STEP 3: Implement basic service components that run the service

class PingerC extends ComponentDefinition {
  //STEP 3A : Bind service interfaces
  val ppp = requires(PingPongPort);

  //STEP 3B: Define event handlers that implement service logic
  ctrl uponEvent {
    case _: Start => handle {
      println("Triggering initial ping.")
      trigger(Ping(0), ppp);
    }
  }
  ppp uponEvent {
    case Pong(i) => handle {
      println(s"Got a Pong($i)!");
      if (i < 10) {
        trigger(Ping(i+1), ppp);
      } else {
        Kompics.asyncShutdown();
      }
    }
  }
};

class PongerC extends ComponentDefinition {
  //STEP 3A : Bind service interfaces
  val ppn = provides(PingPongPort);
  //STEP 3B: Define event handlers that implement service logic
  ppn uponEvent {
    case Ping(i) => handle {
      println(s"Got a Ping($i)!");
      trigger(Pong(i), ppn);
    }
  }
};

//-----------------------------------------------------
//STEP 4: Instantiate components and execute the service

class PingService extends ComponentDefinition {
  //STEP 4A: Instantiate Components
  val ponger = create(classOf[PongerC], JInit.NONE);
  val pinger = create(classOf[PingerC], JInit.NONE);
  connect(PingPongPort)(ponger -> pinger);
};

//STEP 4C: Execute the service
object Main extends App {
  Kompics.createAndStart(classOf[PingService]);
  Kompics.waitForTermination();
}