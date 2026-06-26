package ludo.fileio

import ludo.model.memento.GameStateMemento

trait FileIOInterface {
  def save(memento: GameStateMemento): Unit
  def load(): GameStateMemento
}
