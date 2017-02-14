package se.kth.id2203

import se.sics.kompics.KompicsEvent
import se.sics.kompics.timer.{ScheduleTimeout, Timeout}

package object failure {

  case class CheckTimeout(timeout: ScheduleTimeout) extends Timeout(timeout)

  case class HeartbeatReply(seq: Int) extends KompicsEvent
  case class HeartbeatRequest(seq: Int) extends KompicsEvent

}
