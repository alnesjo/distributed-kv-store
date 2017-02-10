package se.kth.id2203.link

import se.sics.kompics.sl.Port

class PerfectLink extends Port {

  indication[Deliver]
  request[Send]

}
