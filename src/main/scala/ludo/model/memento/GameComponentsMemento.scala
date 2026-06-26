package ludo.model.memento

import play.api.libs.json.*
import scala.xml.{Elem, Node}

case class GameStateMemento(
                             players: List[PlayerMemento],
                             fieldSize: Int,
                             numPlayers: Int,
                             winStrategy: String,
                             currentPlayerIndex: Int,
                             winnerColor: Option[String],
                             diceRoll: Option[Int],
                             rollAttempt: Int,
                             phase: String
                           ){

  def toXml: Elem = {
    <gameState>
      <players>{players.map(_.toXml)}</players>
      <fieldSize>{fieldSize}</fieldSize>
      <numPlayers>{numPlayers}</numPlayers>
      <winStrategy>{winStrategy}</winStrategy>
      <currentPlayerIndex>{currentPlayerIndex}</currentPlayerIndex>
      <winnerColor>{winnerColor.getOrElse("")}</winnerColor>
      <diceRoll>{diceRoll.map(_.toString).getOrElse("")}</diceRoll>
      <rollAttempt>{rollAttempt}</rollAttempt>
      <phase>{phase}</phase>
    </gameState>
  }
}

object GameStateMemento {

  given OFormat[GameStateMemento] = Json.format[GameStateMemento]
  
  def fromXml(node: Node): GameStateMemento = {
    GameStateMemento(
      players = (node \ "players" \ "player").map(PlayerMemento.fromXml).toList,
      fieldSize = (node \ "fieldSize").text.trim.toInt,
      numPlayers = (node \ "numPlayers").text.trim.toInt,
      winStrategy = (node \ "winStrategy").text.trim,
      currentPlayerIndex = (node \ "currentPlayerIndex").text.trim.toInt,
      winnerColor = optionalText(node, "winnerColor"),
      diceRoll = optionalText(node, "diceRoll").map(_.toInt),
      rollAttempt = (node \ "rollAttempt").text.trim.toInt,
      phase = (node \ "phase").text.trim
    )
  }

  private def optionalText(node: Node, label: String): Option[String] = {
    val value = (node \ label).text.trim
    if (value.isEmpty) None else Some(value)
  }
}

case class PlayerMemento(
                          name: String,
                          color: String,
                          pieces: List[PieceMemento],
                          startOffset: Int
                        ){

  def toXml: Elem = {
    <player>
      <name>{name}</name>
      <color>{color}</color>
      <pieces>{pieces.map(_.toXml)}</pieces>
      <startOffset>{startOffset}</startOffset>
    </player>
  }
}

object PlayerMemento {
  given OFormat[PlayerMemento] = Json.format[PlayerMemento]

  def fromXml(node: Node): PlayerMemento = {
    PlayerMemento(
      name = (node \ "name").text.trim,
      color = (node \ "color").text.trim,
      pieces = (node \ "pieces" \ "piece").map(PieceMemento.fromXml).toList,
      startOffset = (node \ "startOffset").text.trim.toInt
    )
  }
}

case class PieceMemento(
                         id: Int,
                         color: String,
                         position: Int
                       ){

  def toXml: Elem = {
    <piece>
      <id>{id}</id>
      <color>{color}</color>
      <position>{position}</position>
    </piece>
  }
}


object PieceMemento {
  given OFormat[PieceMemento] = Json.format[PieceMemento]

  def fromXml(node: Node): PieceMemento = {
    PieceMemento(
      id = (node \ "id").text.trim.toInt,
      color = (node \ "color").text.trim,
      position = (node \ "position").text.trim.toInt
    )
  }
}