package ludo.util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ObserverSpec extends AnyWordSpec with Matchers {
  "An Observable" should {
    "notify its observers" in {
      var updated = false
      val observable = new Observable {}
      val observer = new Observer {
        override def update(): Unit = updated = true
      }

      observable.add(observer)
      observable.notifyObservers()
      updated.shouldBe(true)
    }

    "remove observers correctly" in {
      var counter = 0
      val observable = new Observable {}
      val observer = new Observer {
        override def update(): Unit = counter += 1
      }

      observable.add(observer)
      observable.remove(observer)
      observable.notifyObservers()
      counter.shouldBe(0)
    }
  }
}