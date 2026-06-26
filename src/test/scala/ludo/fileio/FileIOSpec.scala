import _root_.ludo.fileio.FileIOInterface
import _root_.ludo.fileio.impl.{JsonFileIO, XmlFileIO}
import _root_.ludo.model.*
import _root_.ludo.model.memento.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*

import java.io.File
import scala.io.Source
import scala.xml.XML

class FileIOSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  private val jsonFile = new File("game.json")
  private val xmlFile = new File("game.xml")

  private val sampleMemento = GameStateMemento(
    players = List(
      PlayerMemento(
        name = "Alice",
        color = "Blue",
        pieces = List(
          PieceMemento(1, "Blue", 0),
          PieceMemento(2, "Blue", 5),
          PieceMemento(3, "Blue", 41),
          PieceMemento(4, "Blue", 44)
        ),
        startOffset = 0
      ),
      PlayerMemento(
        name = "Bob",
        color = "Red",
        pieces = List(
          PieceMemento(1, "Red", 10),
          PieceMemento(2, "Red", 0),
          PieceMemento(3, "Red", 0),
          PieceMemento(4, "Red", 0)
        ),
        startOffset = 20
      )
    ),
    fieldSize = 40,
    numPlayers = 2,
    winStrategy = "standard",
    currentPlayerIndex = 1,
    winnerColor = Some("Blue"),
    diceRoll = Some(6),
    rollAttempt = 2,
    phase = "moving"
  )

  private val emptyOptionMemento = sampleMemento.copy(
    winnerColor = None,
    diceRoll = None,
    rollAttempt = 0,
    phase = "rolling"
  )

  override protected def beforeEach(): Unit = {
    deleteGeneratedFiles()
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    try super.afterEach()
    finally deleteGeneratedFiles()
  }

  private def deleteGeneratedFiles(): Unit = {
    if (jsonFile.exists()) jsonFile.delete()
    if (xmlFile.exists()) xmlFile.delete()
  }

  "JsonFileIO" should {
    "save and load a GameStateMemento" in {
      val fileIO: FileIOInterface = new JsonFileIO

      fileIO.save(sampleMemento)
      val loaded = fileIO.load()

      jsonFile.exists() shouldBe true
      loaded shouldBe sampleMemento
    }

    "write readable JSON content" in {
      val fileIO = new JsonFileIO

      fileIO.save(sampleMemento)
      val source = Source.fromFile(jsonFile)
      val content = try source.mkString finally source.close()

      content should include("\"players\"")
      content should include("\"Alice\"")
      content should include("\"winnerColor\" : \"Blue\"")
    }
  }

  "XmlFileIO" should {
    "save and load a GameStateMemento" in {
      val fileIO: FileIOInterface = new XmlFileIO

      fileIO.save(sampleMemento)
      val loaded = fileIO.load()

      xmlFile.exists() shouldBe true
      loaded shouldBe sampleMemento
    }

    "write readable XML content" in {
      val fileIO = new XmlFileIO

      fileIO.save(sampleMemento)
      val xml = XML.loadFile(xmlFile)

      xml.label shouldBe "gameState"
      (xml \\ "player").size shouldBe 2
      (xml \\ "name").map(_.text.trim).toList should contain("Alice")
      (xml \ "winnerColor").text.trim shouldBe "Blue"
    }
  }

  "GameStateMemento JSON formats" should {
    "convert to JSON and back with Some values" in {
      val json = Json.toJson(sampleMemento)

      (json \ "fieldSize").as[Int] shouldBe 40
      json.as[GameStateMemento] shouldBe sampleMemento
    }

    "convert to JSON and back with None values" in {
      val json = Json.toJson(emptyOptionMemento)

      json.as[GameStateMemento] shouldBe emptyOptionMemento
    }

    "validate JSON explicitly" in {
      val jsonString = Json.toJson(sampleMemento).toString()

      Json.parse(jsonString).validate[GameStateMemento] match {
        case JsSuccess(value, _) => value shouldBe sampleMemento
        case JsError(errors)    => fail(s"Could not parse memento JSON: $errors")
      }
    }
  }

  "GameStateMemento XML conversion" should {
    "convert to XML and back with Some values" in {
      val xml = sampleMemento.toXml

      xml.label shouldBe "gameState"
      (xml \ "winStrategy").text.trim shouldBe "standard"
      GameStateMemento.fromXml(xml) shouldBe sampleMemento
    }

    "convert to XML and back with None values" in {
      val xml = emptyOptionMemento.toXml

      (xml \ "winnerColor").text.trim shouldBe ""
      (xml \ "diceRoll").text.trim shouldBe ""
      GameStateMemento.fromXml(xml) shouldBe emptyOptionMemento
    }
  }

  "PlayerMemento XML conversion" should {
    "convert to XML and back" in {
      val player = sampleMemento.players.head
      val xml = player.toXml

      xml.label shouldBe "player"
      (xml \\ "piece").size shouldBe 4
      PlayerMemento.fromXml(xml) shouldBe player
    }
  }

  "PieceMemento XML conversion" should {
    "convert to XML and back" in {
      val piece = PieceMemento(2, "Green", 13)
      val xml = piece.toXml

      xml.label shouldBe "piece"
      (xml \ "id").text.trim shouldBe "2"
      PieceMemento.fromXml(xml) shouldBe piece
    }
  }

  "GameState and FileIO together" should {
    "persist and restore a real GameState through JSON mementos" in {
      val state = GameState.create(List("Alice", "Bob"), BoardConfig(40, 2, QuickWinStrategy)).copy(
        currentPlayerIndex = 1,
        diceRoll = Some(6),
        rollAttempt = 0,
        phase = MovingPhase
      )
      val fileIO = new JsonFileIO

      fileIO.save(state.createMemento())
      val restored = GameState.fromMemento(fileIO.load())

      restored.players shouldBe state.players
      restored.config shouldBe state.config
      restored.currentPlayerIndex shouldBe state.currentPlayerIndex
      restored.diceRoll shouldBe state.diceRoll
      restored.rollAttempt shouldBe state.rollAttempt
      restored.phase shouldBe state.phase
    }


    "persist and restore a real GameState in game over phase through JSON mementos" in {
      val baseState = GameState.create(List("Alice", "Bob"), BoardConfig(40, 2))
      val state = baseState.copy(
        winner = Some(baseState.players.head),
        diceRoll = Some(6),
        phase = GameOverPhase
      )
      val fileIO = new JsonFileIO

      fileIO.save(state.createMemento())
      val loadedMemento = fileIO.load()
      val restored = GameState.fromMemento(loadedMemento)

      loadedMemento.phase shouldBe "gameover"
      restored.phase shouldBe GameOverPhase
      restored.winner shouldBe Some(baseState.players.head)
      restored.diceRoll shouldBe Some(6)
    }

    "persist and restore a real GameState through XML mementos" in {
      val state = GameState.create(List("Alice", "Bob"), BoardConfig(40, 2)).copy(
        currentPlayerIndex = 0,
        rollAttempt = 1,
        phase = RollingPhase
      )
      val fileIO = new XmlFileIO

      fileIO.save(state.createMemento())
      val restored = GameState.fromMemento(fileIO.load())

      restored.players shouldBe state.players
      restored.config shouldBe state.config
      restored.currentPlayerIndex shouldBe state.currentPlayerIndex
      restored.rollAttempt shouldBe state.rollAttempt
      restored.phase shouldBe state.phase
      restored.message shouldBe Some(InvalidRollRetryEvent(0, 2))
    }
  }
}
