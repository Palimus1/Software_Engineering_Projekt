package ludo.aview

import ludo.model.*
import ludo.controller.ControllerInterface
import scalafx.Includes.*
import scalafx.scene.Cursor
import scalafx.scene.control.Button
import scalafx.scene.layout.{GridPane, StackPane, Pane, BorderPane, HBox}
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Rectangle}
import scalafx.scene.text.Text
import scalafx.geometry.{Insets, Pos}

object GuiLogic {

  def createRootPane(state: GameState)(using controller: ControllerInterface): Pane = {
    val fields = state.config.fieldSize
    val (fRadius, pRadius, gap, fontSz) = fields match {
      case n if n <= 16 => (24.0, 16.0, 15.0, 16)
      case n if n <= 36 => (20.0, 14.0, 10.0, 14)
      case n if n <= 64 => (15.0, 10.0, 7.0, 12)
      case _            => (11.0, 7.0, 4.0, 10)
    }

    val boardGrid = new GridPane {
      hgap = gap
      vgap = gap
      padding = Insets(gap * 2)
      alignment = Pos.Center
    }

    val layout = new BoardLayoutCalculator(state.config.fieldSize)

    for (i <- 1 to layout.totalFields) {
      val (col, row) = layout.calculateGridCoordinates(i)
      val fieldColor = getFieldBaseColor(i, state)
      val strokeColor = getFieldStrokeColor(i, state)

      val occupants = for {
        p <- state.players
        piece <- p.pieces
        globalPos <- state.getGlobalPosition(p, piece) if globalPos == i
      } yield (p, piece)

      val fieldNode = if (occupants.nonEmpty) {
        val (p, piece) = occupants.head
        val clickable = isPieceClickable(state, p, piece)
        createOccupiedFieldNode(fieldColor, getPlayerColor(p.color), strokeColor, piece.id.toString, fRadius, pRadius, fontSz, clickable, () => controller.doMove(piece.id))
      } else {
        createFieldNode(fieldColor, strokeColor, i.toString, fRadius, fontSz)
      }

      GridPane.setConstraints(fieldNode, col, row)
      boardGrid.children.add(fieldNode)
    }

    for (p <- state.players) {
      val baseColor = getPlayerColor(p.color)

      for (i <- 1 to 4) {
        val (tCol, tRow) = layout.calculateAlignedTargetCoordinates(p.startOffset, i - 1)
        val tPieceOpt = p.pieces.find(piece => piece.position == layout.totalFields + i)

        val tNode = tPieceOpt match {
          case Some(piece) =>
            val clickable = isPieceClickable(state, p, piece)
            createOccupiedFieldNode(baseColor, baseColor, Color.DarkGray, piece.id.toString, fRadius, pRadius, fontSz, clickable, () => controller.doMove(piece.id))
          case None => createFieldNode(baseColor, Color.DarkGray, s"T$i", fRadius, fontSz)
        }
        GridPane.setConstraints(tNode, tCol, tRow)
        boardGrid.children.add(tNode)

        val (bCol, bRow) = layout.calculateAlignedBaseCoordinates(p.startOffset, i - 1)
        val bPieceOpt = p.pieces.find(piece => piece.id == i && piece.position == 0)

        val bNode = bPieceOpt match {
          case Some(piece) =>
            val clickable = isPieceClickable(state, p, piece)
            createOccupiedFieldNode(baseColor, baseColor, Color.DarkGray, piece.id.toString, fRadius, pRadius, fontSz, clickable, () => controller.doMove(piece.id))
          case None => createFieldNode(baseColor, Color.DarkGray, s"B$i", fRadius, fontSz)
        }
        GridPane.setConstraints(bNode, bCol, bRow)
        boardGrid.children.add(bNode)
      }

      val (nCol, nRow) = layout.calculateNameCoordinates(p.startOffset)
      val nameText = new Text {
        text = p.name
        style = s"-fx-font-weight: bold; -fx-font-size: ${fontSz}px;"
        fill = baseColor
      }
      GridPane.setConstraints(nameText, nCol, nRow, 2, 1)
      GridPane.setHalignment(nameText, javafx.geometry.HPos.CENTER)
      boardGrid.children.add(nameText)
    }

    val controlPanel = createControlPanel(state)

    new BorderPane {
      center = boardGrid
      bottom = controlPanel
    }
  }

  private def createControlPanel(state: GameState)(using controller: ControllerInterface): HBox = {
    val currPlayerColor = getPlayerColor(state.currentPlayer.color)

    val turnInfo = new Text {
      text = s"Am Zug: ${state.currentPlayer.name}"
      style = "-fx-font-weight: bold; -fx-font-size: 20px;"
      fill = currPlayerColor
    }

    val rollButton = new Button("🎲") {
      style = "-fx-font-size: 24px; -fx-font-weight: bold;"
      cursor = Cursor.Hand
      onAction = _ => controller.rollDice()
    }

    val diceBox = new StackPane {
      children = Seq(
        new Rectangle {
          width = 45
          height = 45
          fill = Color.White
          stroke = Color.DarkGray
          strokeWidth = 2
          arcWidth = 10
          arcHeight = 10
        },
        new Text {
          text = state.diceRoll.map(_.toString).getOrElse("-")
          style = "-fx-font-weight: bold; -fx-font-size: 24px;"
          fill = if (state.diceRoll.contains(6)) Color.web("#e74c3c") else Color.Black
        }
      )
    }

    new HBox {
      spacing = 40
      padding = Insets(20)
      alignment = Pos.Center
      children = Seq(turnInfo, rollButton, diceBox)
    }
  }

  private def getPlayerColor(pc: PlayerColor): Color = pc match {
    case PlayerColor.Blue => Color.web("#3498db")
    case PlayerColor.Red => Color.web("#e74c3c")
    case PlayerColor.Green => Color.web("#2ecc71")
    case PlayerColor.Yellow => Color.web("#f1c40f")
  }

  private def getFieldBaseColor(fieldIndex: Int, state: GameState): Color = {
    state.players.find(p => (p.startOffset + 1) == fieldIndex) match {
      case Some(p) => getPlayerColor(p.color)
      case None => Color.White
    }
  }

  private def getFieldStrokeColor(fieldIndex: Int, state: GameState): Color = {
    state.players.find { p =>
      val entryPos = if (p.startOffset == 0) state.config.fieldSize else p.startOffset
      entryPos == fieldIndex
    } match {
      case Some(p) => getPlayerColor(p.color)
      case None => Color.DarkGray
    }
  }

  private def isPieceClickable(state: GameState, player: Player, piece: Piece): Boolean = {
    if (player != state.currentPlayer) return false
    state.diceRoll match {
      case Some(roll) =>
        if (piece.position == 0 && roll != 6) false
        else true
      case None => false
    }
  }

  private def createFieldNode(bgColor: Color, strokeColor: Color, label: String, radiusVal: Double, fontSz: Int): StackPane = {
    new StackPane {
      children = Seq(
        new Circle {
          radius = radiusVal
          fill = bgColor
          stroke = strokeColor
          strokeWidth = if (strokeColor == Color.DarkGray) 2 else 5
        },
        new Text {
          text = label
          style = s"-fx-font-weight: bold; -fx-font-size: ${fontSz}px;"
          fill = if (bgColor == Color.White || bgColor == Color.web("#f1c40f")) Color.Black else Color.White
        }
      )
    }
  }

  private def createOccupiedFieldNode(bgColor: Color, pieceColor: Color, strokeColor: Color, label: String, fRadius: Double, pRadius: Double, fontSz: Int, isClickable: Boolean, onClick: () => Unit): StackPane = {
    val pieceOpacity = if (isClickable) 1.0 else 0.4
    val stack = new StackPane {
      children = Seq(
        new Circle { radius = fRadius; fill = bgColor; stroke = strokeColor; strokeWidth = if (strokeColor == Color.DarkGray) 2 else 5 },
        new Circle { radius = pRadius; fill = pieceColor; stroke = Color.Black; strokeWidth = 2; opacity = pieceOpacity },
        new Text {
          text = label
          style = s"-fx-font-weight: bold; -fx-font-size: ${fontSz}px;"
          fill = if (pieceColor == Color.White || pieceColor == Color.web("#f1c40f")) Color.Black else Color.White
          opacity = pieceOpacity
        }
      )
    }
    if (isClickable) {
      stack.cursor = Cursor.Hand
      stack.onMouseClicked = _ => onClick()
    }
    stack
  }
}