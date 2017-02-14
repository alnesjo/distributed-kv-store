package se.kth.id2203

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address

package object broadcast {
  case class Source(src: Address, payload: KompicsEvent) extends KompicsEvent
}
