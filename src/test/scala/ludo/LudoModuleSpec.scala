import _root_.ludo.LudoModule
import _root_.ludo.controller.ControllerInterface
import _root_.ludo.fileio.FileIOInterface
import _root_.ludo.fileio.impl.JsonFileIO
import _root_.ludo.model.{BoardConfig, GameState}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LudoModuleSpec extends AnyWordSpec with Matchers {

  "A LudoModule" should {
    "provide a ControllerInterface and FileIOInterface when its givens are imported" in {
      val state = GameState.create(List("Alice", "Bob"), BoardConfig(40, 2))
      val module = new LudoModule(state)

      import module.given

      val injectedController = summon[ControllerInterface]
      val injectedFileIO = summon[FileIOInterface]

      injectedController should not be null
      injectedController.gameState shouldBe state
      injectedFileIO shouldBe a[JsonFileIO]
    }
  }
}
