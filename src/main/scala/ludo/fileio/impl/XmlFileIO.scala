package ludo.fileio.impl

import ludo.fileio.FileIOInterface
import ludo.model.memento.GameStateMemento

import java.io.{File, PrintWriter}
import scala.xml.{PrettyPrinter, XML}

class XmlFileIO extends FileIOInterface {

  private val fileName = "game.xml"
  private val prettyPrinter = new PrettyPrinter(120, 2)

  override def save(memento: GameStateMemento): Unit = {
    val xmlString = prettyPrinter.format(memento.toXml)

    val writer = new PrintWriter(new File(fileName))
    try {
      writer.write("<?xml version='1.0' encoding='UTF-8'?>\n")
      writer.write(xmlString)
    } finally {
      writer.close()
    }
  }

  override def load(): GameStateMemento = {
    val xml = XML.loadFile(fileName)
    GameStateMemento.fromXml(xml)
  }
}
