import _root_.ludo.util.{Command, Observable, Observer, UndoManager}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ObserverSpec extends AnyWordSpec with Matchers {

  "An Observable" should {
    "notify its observers" in {
      var updated = false
      val observable = new Observable
      val observer = new Observer {
        override def update(): Unit = updated = true
      }

      observable.add(observer)
      observable.notifyObservers()

      updated shouldBe true
    }

    "remove observers correctly" in {
      var counter = 0
      val observable = new Observable
      val observer = new Observer {
        override def update(): Unit = counter += 1
      }

      observable.add(observer)
      observable.remove(observer)
      observable.notifyObservers()

      counter shouldBe 0
    }
  }

  "An UndoManager" should {
    "execute, undo, redo and clear commands" in {
      var value = 0
      val command = new Command {
        override def doStep(): Unit = value += 1
        override def undoStep(): Unit = value -= 1
        override def redoStep(): Unit = value += 1
      }
      val undoManager = new UndoManager

      undoManager.doStep(command)
      value shouldBe 1

      undoManager.undoStep()
      value shouldBe 0

      undoManager.redoStep()
      value shouldBe 1

      undoManager.clear()
      undoManager.undoStep()
      value shouldBe 1
    }

    "do nothing when undo or redo stacks are empty" in {
      val undoManager = new UndoManager

      noException should be thrownBy undoManager.undoStep()
      noException should be thrownBy undoManager.redoStep()
    }
  }
}
