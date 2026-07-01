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
        var state = GameState.createSetup() 
        state.phase.name shouldBe "setup"
        
        val s1 = state.phase.handleSetup(state, "2").get
        s1.phase shouldBe a[SetupPhase]
        s1.phase.asInstanceOf[SetupPhase].step shouldBe SetupStep.PlayerNames
        s1.phase.asInstanceOf[SetupPhase].data.numPlayers shouldBe 2
        
        val s2 = s1.phase.handleSetup(s1, "Alice").get
        s2.phase.asInstanceOf[SetupPhase].step shouldBe SetupStep.PlayerNames
        s2.phase.asInstanceOf[SetupPhase].data.names shouldBe List("Alice")
        
        val s3 = s2.phase.handleSetup(s2, "   ").get 
        s3.phase.asInstanceOf[SetupPhase].step shouldBe SetupStep.FieldSize
        s3.phase.asInstanceOf[SetupPhase].data.names shouldBe List("Alice", "Player 2")
        
        val s4 = s3.phase.handleSetup(s3, "10").get
        s4.phase.asInstanceOf[SetupPhase].step shouldBe SetupStep.GameMode
        s4.phase.asInstanceOf[SetupPhase].data.fieldSize shouldBe 10
        
        val s5 = s4.phase.handleSetup(s4, "blitz").get
        s5.phase shouldBe RollingPhase
        s5.config.fieldSize shouldBe 10
        s5.config.numPlayers shouldBe 2
        s5.config.winStrategy shouldBe QuickWinStrategy
        s5.players.map(_.name) shouldBe List("Alice", "Player 2")
      }
      
      "handle edge cases in inputs" in {
        var state = GameState.createSetup()
        
        val s1 = state.phase.handleSetup(state, "0").get
        s1.phase.asInstanceOf[SetupPhase].data.numPlayers shouldBe 1
        
        var st2 = GameState.createSetup()
        val s1_2 = st2.phase.handleSetup(st2, "5").get
        s1_2.phase.asInstanceOf[SetupPhase].data.numPlayers shouldBe 4
        
        var sfs = s1.phase.handleSetup(s1, "Alice").get 
        val sfs2 = sfs.phase.handleSetup(sfs, "3").get
        sfs2.phase.asInstanceOf[SetupPhase].data.fieldSize shouldBe 4
        
        val sfs3 = sfs.phase.handleSetup(sfs, "15").get
        sfs3.phase.asInstanceOf[SetupPhase].data.fieldSize shouldBe 16
        
        val sFinal = sfs3.phase.handleSetup(sfs3, "anything").get
        sFinal.config.winStrategy shouldBe StandardWinStrategy
      }
    }
  }
}
