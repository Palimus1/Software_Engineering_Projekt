package ludo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import ludo.model.{GameState, BoardConfig}
import ludo.controller.ControllerInterface

class LudoModuleSpec extends AnyWordSpec with Matchers {
  "A LudoModule" should {
    "provide a ControllerInterface when imported" in {
      // 1. Wir machen einen leeren GameState
      val state = GameState(List(), BoardConfig(40, 2))

      // 2. Wir erstellen das Modul
      val module = new LudoModule(state)

      // 3. Wir aktivieren die Dependency Injection
      import module.given

      // 4. Wir prüfen, ob Scala uns jetzt automatisch einen Controller geben kann
      val injectedController = summon[ControllerInterface]

      // Wenn der Controller existiert, funktioniert das Modul!
      injectedController.shouldNot(be(null))
    }
  }
}