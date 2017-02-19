package se.kth.id2203.shared

import org.scalatest._
import scala.collection.mutable
import se.sics.kompics.simulator._

class DistributedKeyValueStoreSpec extends FlatSpec with Matchers {

  "A distributed key-value store" should "(description of some desired behaviour goes here) ..." in {
    // Placeholder testing of stack, TODO replace with real tests
    val stack = new mutable.Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should be (2)
    stack.pop() should be (1)
  }

  it should "(also) ..." in {
    // Placeholder testing of stack, TODO replace with real tests
    val emptyStack = new mutable.Stack[Int]
    a [NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    }
  }

}