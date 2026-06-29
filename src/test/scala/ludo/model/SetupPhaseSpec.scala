package ludo.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.util.{Success, Failure}

class SetupPhaseSpec extends AnyWordSpec with Matchers {
  "A SetupPhase" when {
    "handling unsupported actions" should {
      val state = GameState.createSetup()
      
      "return SetupInProgressException for handleRoll" in {
        state.phase.handleRoll(state, 6).isFailure shouldBe true
        state.phase.handleRoll(state, 6).failed.get shouldBe a[SetupInProgressException]
      }
      
      "return SetupInProgressException for handleMove" in {
        state.phase.handleMove(state, 1).isFailure shouldBe true
        state.phase.handleMove(state, 1).failed.get shouldBe a[SetupInProgressException]
      }
    }
    
    "handling handleSetup in normal phases" should {
      val state = GameState.create(List("A", "B"), BoardConfig(40, 2))
      
      "return NotSetupPhaseException in RollingPhase" in {
        val st = state.copy(phase = RollingPhase)
        st.phase.handleSetup(st, "test").failed.get shouldBe a[NotSetupPhaseException]
      }
      
      "return NotSetupPhaseException in MovingPhase" in {
        val st = state.copy(phase = MovingPhase)
        st.phase.handleSetup(st, "test").failed.get shouldBe a[NotSetupPhaseException]
      }
      
      "return NotSetupPhaseException in GameOverPhase" in {
        val st = state.copy(phase = GameOverPhase)
        st.phase.handleSetup(st, "test").failed.get shouldBe a[NotSetupPhaseException]
      }
    }
    
    "processing setup steps" should {
      "progress through NumPlayers -> PlayerNames -> FieldSize -> GameMode" in {
        var state = GameState.createSetup() // Starts at NumPlayers
        state.phase.name shouldBe "setup"
        
        // 1. NumPlayers: input "2"
        val s1 = state.phase.handleSetup(state, "2").get
        s1.phase shouldBe a[SetupPhase]
        s1.phase.asInstanceOf[SetupPhase].step shouldBe SetupStep.PlayerNames
        s1.phase.asInstanceOf[SetupPhase].data.numPlayers shouldBe 2
        
        // 2. PlayerNames (Player 1)
        val s2 = s1.phase.handleSetup(s1, "Alice").get
        s2.phase.asInstanceOf[SetupPhase].step shouldBe SetupStep.PlayerNames
        s2.phase.asInstanceOf[SetupPhase].data.names shouldBe List("Alice")
        
        // 3. PlayerNames (Player 2)
        val s3 = s2.phase.handleSetup(s2, "   ").get // Test empty name fallback
        s3.phase.asInstanceOf[SetupPhase].step shouldBe SetupStep.FieldSize
        s3.phase.asInstanceOf[SetupPhase].data.names shouldBe List("Alice", "Player 2")
        
        // 4. FieldSize: input "10"
        val s4 = s3.phase.handleSetup(s3, "10").get
        s4.phase.asInstanceOf[SetupPhase].step shouldBe SetupStep.GameMode
        s4.phase.asInstanceOf[SetupPhase].data.fieldSize shouldBe 10
        
        // 5. GameMode: input "blitz" -> Completes setup!
        val s5 = s4.phase.handleSetup(s4, "blitz").get
        s5.phase shouldBe RollingPhase
        s5.config.fieldSize shouldBe 10
        s5.config.numPlayers shouldBe 2
        s5.config.winStrategy shouldBe QuickWinStrategy
        s5.players.map(_.name) shouldBe List("Alice", "Player 2")
      }
      
      "handle edge cases in inputs" in {
        var state = GameState.createSetup()
        
        // NumPlayers out of bounds (< 1)
        val s1 = state.phase.handleSetup(state, "0").get
        s1.phase.asInstanceOf[SetupPhase].data.numPlayers shouldBe 1
        
        var st2 = GameState.createSetup()
        // NumPlayers out of bounds (> 4)
        val s1_2 = st2.phase.handleSetup(st2, "5").get
        s1_2.phase.asInstanceOf[SetupPhase].data.numPlayers shouldBe 4
        
        // FieldSize invalid (too small)
        var sfs = s1.phase.handleSetup(s1, "Alice").get // Name
        val sfs2 = sfs.phase.handleSetup(sfs, "3").get // Size 3 -> rounded to 4
        sfs2.phase.asInstanceOf[SetupPhase].data.fieldSize shouldBe 4
        
        // FieldSize odd number
        val sfs3 = sfs.phase.handleSetup(sfs, "15").get // Size 15 -> rounded to 16
        sfs3.phase.asInstanceOf[SetupPhase].data.fieldSize shouldBe 16
        
        // GameMode default
        val sFinal = sfs3.phase.handleSetup(sfs3, "anything").get
        sFinal.config.winStrategy shouldBe StandardWinStrategy
      }
    }
  }
}
