package ludo.fileio.impl

import ludo.fileio.FileIOInterface
import ludo.model.memento.*
import play.api.libs.json.*

import java.io.{File, PrintWriter}
import scala.io.Source

class JsonFileIO extends FileIOInterface {

  private val fileName = "game.json"

  override def save(memento: GameStateMemento): Unit = {
    val json = Json.prettyPrint(Json.toJson(memento))

    val writer = new PrintWriter(new File(fileName))
    try {
      writer.write(json)
    } finally {
      writer.close()
    }
  }

  override def load(): GameStateMemento = {
    val source = Source.fromFile(fileName)
    try {
      val jsonString = source.mkString
      val json = Json.parse(jsonString)
      json.as[GameStateMemento]
    } finally {
      source.close()
    }
  }
}