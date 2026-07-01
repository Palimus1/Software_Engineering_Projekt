package ludo.aview

import ludo.model.*
import ludo.controller.ControllerInterface
import scalafx.Includes.*
import scalafx.scene.{Cursor, Group}
import scalafx.scene.control.{Button, TextField}
import scalafx.scene.layout.{GridPane, StackPane, Pane, BorderPane, HBox, VBox}
import scala.util.{Failure, Success}
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Rectangle}
import scalafx.scene.text.Text
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.effect.{Blend, BlendMode, ColorInput, DropShadow}

object GuiLogic {
  
  private lazy val pieceImg = new Image("file:src/main/scala/ressources/PieceTemp.png")

  def createRootPane(state: GameState, controller: ControllerInterface): Pane = {
    val contentPane = state.phase match {
      case SetupPhase(step, data) => createSetupPane(step, data, controller)
      case _ => createGamePane(state, controller)
    }

    val undoBtn = new Button("UNDO") {
      style = "-fx-font-size: 14px; -fx-background-color: #6b6b6bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;"
      cursor = Cursor.Hand
      onAction = _ => controller.undo()
    }
    
    val redoBtn = new Button("REDO") {
      style = "-fx-font-size: 14px; -fx-background-color: #6b6b6bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;"
      cursor = Cursor.Hand
      onAction = _ => controller.redo()
    }

    val saveBtn = new Button("SAVE") {
      style = "-fx-font-size: 14px; -fx-background-color: #6b6b6bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;"
      cursor = Cursor.Hand
      onAction = _ => controller.save()
    }

    val loadBtn = new Button("LOAD") {
      style = "-fx-font-size: 14px; -fx-background-color: #6b6b6bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;"
      cursor = Cursor.Hand
      onAction = _ => controller.load()
    }
    
    val banners = createBanners(state)
    val bannerBox = new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = banners
      prefWidth = 500
      minWidth = 500
    }

    val btnBox = new HBox {
      spacing = 15
      alignment = Pos.CenterRight
      children = Seq(saveBtn, loadBtn, undoBtn, redoBtn)
    }

    val toolbar = new BorderPane {
      padding = Insets(10)
      left = bannerBox
      right = btnBox
      style = "-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0;"
    }

    val contentGroup = new Group(contentPane)
    val contentContainer = new StackPane {
      children = contentGroup
      alignment = Pos.Center
      minWidth = 0
      minHeight = 0
    }

    def updateScale(): Unit = {
      val w = contentContainer.width.value
      val h = contentContainer.height.value
      val bw = contentGroup.boundsInLocal.value.width
      val bh = contentGroup.boundsInLocal.value.height

      if (w > 0 && h > 0 && bw > 0 && bh > 0) {
        val scaleX = w / bw
        val scaleY = h / bh
        val scale = Math.min(Math.min(scaleX, scaleY) * 0.95, 1.5)
        contentGroup.scaleX = scale
        contentGroup.scaleY = scale
      }
    }

    contentContainer.width.onChange { (_, _, _) => updateScale() }
    contentContainer.height.onChange { (_, _, _) => updateScale() }
    contentGroup.boundsInLocal.onChange { (_, _, _) => updateScale() }

    new BorderPane {
      top = toolbar
      center = contentContainer
    }
  }

  private def createBanners(state: GameState): Seq[StackPane] = {
    val errorBanner = new StackPane {
      padding = Insets(5, 15, 5, 15)
      style = "-fx-background-color: #ffebee; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #ffcdd2; -fx-border-width: 1;"
      
      val errorMsg = state.lastError match {
        case Failure(_: NeedSixException) => "You need a 6 to leave the base!"
        case Failure(_: BlockedException) => "You cannot capture your own pieces!"
        case Failure(_: OvershootException) => "The move overshoots the target!"
        case Failure(_: InvalidPieceException) => "Invalid piece!"
        case Failure(_: AlreadyRolledException) => "You have already rolled! Please move a piece."
        case Failure(_: MustRollFirstException) => "You must roll first!"
        case Failure(_: GameOverException) => "The game is already over!"
        case Failure(_: BaseClearException) => "You must clear the start field!"
        case Failure(_: BaseLeaveException) => "You must move a piece out of the base!"
        case Failure(e) => Option(e.getMessage).getOrElse(e.getClass.getSimpleName)
        case Success(_) => ""
      }
      
      val displayMsg = Option(errorMsg).getOrElse("")
      visible = displayMsg.nonEmpty
      managed = displayMsg.nonEmpty
      children = new Text {
        text = displayMsg
        style = "-fx-fill: #c62828; -fx-font-weight: bold; -fx-font-size: 14px;"
      }
    }

    val winnerBanner = new StackPane {
      padding = Insets(5, 15, 5, 15)
      style = "-fx-background-color: #e8f5e9; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #c8e6c9; -fx-border-width: 1;"
      
      val winnerMsg = state.winner.map(p => p.name + " has won!").getOrElse("")
      
      visible = winnerMsg.nonEmpty
      managed = winnerMsg.nonEmpty
      children = new Text {
        text = winnerMsg
        style = "-fx-fill: #2e7d32; -fx-font-weight: bold; -fx-font-size: 14px;"
      }
    }

    val infoBanner = new StackPane {
      padding = Insets(5, 15, 5, 15)
      style = "-fx-background-color: #e3f2fd; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #90caf9; -fx-border-width: 1;"
      
      val infoMsg = state.message match {
        case Some(AllPiecesBlockedEvent(roll)) => s"Rolled a $roll, but all pieces are blocked! Next player."
        case Some(InvalidRollRetryEvent(roll, attemptsLeft)) => s"Rolled a $roll! Not a valid move. You have $attemptsLeft attempt(s) left."
        case Some(ThreeStrikesEvent(roll)) => s"Rolled a $roll. No moves possible three times. Next player's turn."
        case None => ""
      }
      
      visible = infoMsg.nonEmpty
      managed = infoMsg.nonEmpty
      children = new Text {
        text = infoMsg
        style = "-fx-fill: #1565c0; -fx-font-weight: bold; -fx-font-size: 14px;"
      }
    }
    
    Seq(winnerBanner, errorBanner, infoBanner)
  }

  private def createStyledButton(txt: String, baseColor: String, hoverColor: String, action: () => Unit, pad: String = "15 30"): Button = {
    new Button(txt) {
      style = s"-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: $pad; -fx-background-color: $baseColor; -fx-text-fill: white; -fx-background-radius: 10;"
      cursor = Cursor.Hand
      onMouseEntered = _ => style = s"-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: $pad; -fx-background-color: $hoverColor; -fx-text-fill: white; -fx-background-radius: 10;"
      onMouseExited = _ => style = s"-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: $pad; -fx-background-color: $baseColor; -fx-text-fill: white; -fx-background-radius: 10;"
      onAction = _ => action()
    }
  }

  private def createSetupPane(step: SetupStep, data: SetupData, controller: ControllerInterface): Pane = {
    val titleText = new Text {
      text = "Ludo Setup"
      style = "-fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 46px;"
      fill = Color.web("#2c3e50")
    }

    val contentBox = new VBox {
      spacing = 30
      alignment = Pos.Center
      padding = Insets(50)
      style = "-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 10);"
      maxWidth = 600
    }

    step match {
      case SetupStep.NumPlayers =>
        val label = new Text("Please select the number of players (1-4):") { 
          style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-fill: #34495e;" 
        }
        val btnBox = new HBox {
          spacing = 20
          alignment = Pos.Center
          children = (1 to 4).map { i =>
            createStyledButton(i.toString, "#3498db", "#2980b9", () => controller.doSetup(i.toString))
          }
        }
        contentBox.children = Seq(label, btnBox)

      case SetupStep.PlayerNames =>
        val playerNum = data.names.size + 1
        val label = new Text(s"Name for player $playerNum:") { 
          style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-fill: #34495e;" 
        }
        val inputField = new TextField {
          promptText = s"Player $playerNum"
          style = "-fx-font-size: 20px; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;"
          maxWidth = 350
        }
        val nextBtn = createStyledButton("Next ➔", "#2ecc71", "#27ae60", () => controller.doSetup(inputField.text.value), "10 25")
        inputField.onAction = _ => controller.doSetup(inputField.text.value)
        
        val row = new HBox {
          spacing = 15
          alignment = Pos.Center
          children = Seq(inputField, nextBtn)
        }
        contentBox.children = Seq(label, row)

      case SetupStep.FieldSize =>
        val label = new Text("Choose board size (Standard 40):") { 
          style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-fill: #34495e;" 
        }
        val inputField = new TextField {
          text = "40"
          style = "-fx-font-size: 20px; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;"
          maxWidth = 200
        }
        val nextBtn = createStyledButton("Next ➔", "#2ecc71", "#27ae60", () => controller.doSetup(inputField.text.value), "10 25")
        inputField.onAction = _ => controller.doSetup(inputField.text.value)
        
        val row = new HBox {
          spacing = 15
          alignment = Pos.Center
          children = Seq(inputField, nextBtn)
        }
        contentBox.children = Seq(label, row)

      case SetupStep.GameMode =>
        val label = new Text("Choose game mode:") { 
          style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-fill: #34495e;" 
        }
        val stdBtn = createStyledButton("Standard", "#9b59b6", "#8e44ad", () => controller.doSetup("standard"), "20 40")
        val blitzBtn = createStyledButton("Blitz", "#e67e22", "#d35400", () => controller.doSetup("blitz"), "20 40")
        val btnBox = new HBox {
          spacing = 30
          alignment = Pos.Center
          children = Seq(stdBtn, blitzBtn)
        }
        contentBox.children = Seq(label, btnBox)
    }

    val configStr = step match {
      case SetupStep.NumPlayers => "Configuration: [Nothing configured yet]"
      case SetupStep.PlayerNames => s"Configuration: ${data.numPlayers} Players | Names: [${data.names.mkString(", ")}]"
      case SetupStep.FieldSize => s"Configuration: ${data.numPlayers} Players | Names: [${data.names.mkString(", ")}]"
      case SetupStep.GameMode => s"Configuration: ${data.numPlayers} Players | Board: ${data.fieldSize} | Names: [${data.names.mkString(", ")}]"
    }

    val configText = new Text {
      text = configStr
      style = "-fx-font-size: 16px; -fx-font-style: italic;"
      fill = Color.web("#7f8c8d")
    }

    val mainContainer = new VBox {
      alignment = Pos.Center
      spacing = 40
      children = Seq(titleText, contentBox, configText)
    }

    new StackPane {
      style = "-fx-background-color: #ecf0f1;"
      children = mainContainer
    }
  }

  private def createGamePane(state: GameState, controller: ControllerInterface): Pane = {
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

    var minCol = 0
    var minRow = 0
    for (i <- 1 to layout.totalFields) {
      val (c, r) = layout.calculateGridCoordinates(i)
      minCol = Math.min(minCol, c)
      minRow = Math.min(minRow, r)
    }
    for (p <- state.players) {
      for (i <- 1 to 4) {
        val (tc, tr) = layout.calculateAlignedTargetCoordinates(p.startOffset, i - 1)
        minCol = Math.min(minCol, tc)
        minRow = Math.min(minRow, tr)
        val (bc, br) = layout.calculateAlignedBaseCoordinates(p.startOffset, i - 1)
        minCol = Math.min(minCol, bc)
        minRow = Math.min(minRow, br)
      }
      val (nc, nr) = layout.calculateNameCoordinates(p.startOffset)
      minCol = Math.min(minCol, nc)
      minRow = Math.min(minRow, nr)
    }
    val colOffset = if (minCol < 0) -minCol else 0
    val rowOffset = if (minRow < 0) -minRow else 0

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

      GridPane.setConstraints(fieldNode, col + colOffset, row + rowOffset)
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
        GridPane.setConstraints(tNode, tCol + colOffset, tRow + rowOffset)
        boardGrid.children.add(tNode)

        val (bCol, bRow) = layout.calculateAlignedBaseCoordinates(p.startOffset, i - 1)
        val bPieceOpt = p.pieces.find(piece => piece.id == i && piece.position == 0)
        
        val bNode = bPieceOpt match {
          case Some(piece) =>
            val clickable = isPieceClickable(state, p, piece)
            createOccupiedFieldNode(baseColor, baseColor, Color.DarkGray, piece.id.toString, fRadius, pRadius, fontSz, clickable, () => controller.doMove(piece.id))
          case None => createFieldNode(baseColor, Color.DarkGray, s"B$i", fRadius, fontSz)
        }
        GridPane.setConstraints(bNode, bCol + colOffset, bRow + rowOffset)
        boardGrid.children.add(bNode)
      }

      val (nCol, nRow) = layout.calculateNameCoordinates(p.startOffset)
      val nameText = new Text {
        text = p.name
        style = s"-fx-font-weight: bold; -fx-font-size: ${fontSz}px;"
        fill = baseColor
      }
      GridPane.setConstraints(nameText, nCol + colOffset, nRow + rowOffset, 2, 1)
      GridPane.setHalignment(nameText, javafx.geometry.HPos.CENTER)
      boardGrid.children.add(nameText)
    }
    
    val (turnInfo, diceActionGroup) = createControlElements(state, controller)

    val controlPanel = new HBox {
      spacing = 15
      padding = Insets(5)
      alignment = Pos.Center
      children = Seq(turnInfo, diceActionGroup)
    }

    val boardGroup = new Group(boardGrid)
    
    val boardContainer = new StackPane {
      children = Seq(boardGroup)
      alignment = Pos.Center
      padding = Insets(5)
    }

    new BorderPane {
      center = boardContainer
      bottom = controlPanel
    }
  }

  private def createControlElements(state: GameState, controller: ControllerInterface): (Text, VBox) = {
    val currPlayerColor = getPlayerColor(state.currentPlayer.color)
    
    val turnInfo = new Text {
      text = s"Turn: ${state.currentPlayer.name}"
      style = "-fx-font-weight: bold; -fx-font-size: 20px;"
      fill = currPlayerColor
    }

    val rolledValue = state.diceRoll.orElse {
      state.message match {
        case Some(AllPiecesBlockedEvent(roll)) => Some(roll)
        case Some(InvalidRollRetryEvent(roll, _)) => Some(roll)
        case Some(ThreeStrikesEvent(roll)) => Some(roll)
        case _ => None
      }
    }

    val diceContent = rolledValue match {
      case Some(v) if v >= 1 && v <= 6 => createDiceDots(v)
      case _ => new Group()
    }

    val diceBox = new StackPane {
      cursor = Cursor.Hand
      onMouseClicked = _ => controller.rollDice()
      children = Seq(
        new Rectangle {
          width = 45
          height = 45
          fill = if (rolledValue.isDefined) Color.White else Color.LightGray
          stroke = Color.DarkGray
          strokeWidth = 2
          arcWidth = 10
          arcHeight = 10
        },
        diceContent
      )
    }

    val diceActionGroup = new VBox {
      spacing = 10
      alignment = Pos.Center
      children = Seq(diceBox)
    }

    (turnInfo, diceActionGroup)
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
    val fieldSize = radiusVal * 2 + 10
    new StackPane {
      prefWidth = fieldSize
      prefHeight = fieldSize
      minWidth = fieldSize
      minHeight = fieldSize
      maxWidth = fieldSize
      maxHeight = fieldSize
      children = Seq(
        new Circle {
          radius = radiusVal
          fill = bgColor
          stroke = strokeColor
          strokeWidth = if (strokeColor == Color.DarkGray) 2 else 5
        }
      )
    }
  }

  private def createOccupiedFieldNode(bgColor: Color, pieceColor: Color, strokeColor: Color, label: String, fRadius: Double, pRadius: Double, fontSz: Int, isClickable: Boolean, onClick: () => Unit): StackPane = {
    val fieldSize = fRadius * 2 + 10
    val pieceOpacity = if (isClickable) 1.0 else 0.4 
    val stack = new StackPane {
      prefWidth = fieldSize
      prefHeight = fieldSize
      minWidth = fieldSize
      minHeight = fieldSize
      maxWidth = fieldSize
      maxHeight = fieldSize
      children = Seq(
        new Circle { radius = fRadius; fill = bgColor; stroke = strokeColor; strokeWidth = if (strokeColor == Color.DarkGray) 2 else 5 },
        new ImageView(pieceImg) {
          fitWidth = pRadius * 3.8
          fitHeight = pRadius * 3.8
          preserveRatio = true
          opacity = pieceOpacity
          effect = new DropShadow {
            color = Color.Black
            radius = 1.5
            spread = 1.0
            offsetX = 0.0
            offsetY = 0.0
            input = new Blend {
              mode = BlendMode.SrcAtop
              topInput = new ColorInput {
                paint = pieceColor
                x = 0
                y = 0
                width = pRadius *4
                height = pRadius * 4
              }
            }
          }
        }
      )
    }
    if (isClickable) {
      stack.cursor = Cursor.Hand
      stack.onMouseClicked = _ => onClick()
    }
    stack
  }

  private def createDiceDots(value: Int): Group = {
    val dotRadius = 3.5
    val dotColor = if (value == 6) Color.web("#e74c3c") else Color.Black

    def dot(cx: Double, cy: Double) = new Circle {
      centerX = cx
      centerY = cy
      radius = dotRadius
      fill = dotColor
    }

    val tl = dot(12, 12)
    val tr = dot(33, 12)
    val bl = dot(12, 33)
    val br = dot(33, 33)
    val ml = dot(12, 22.5)
    val mr = dot(33, 22.5)
    val c  = dot(22.5, 22.5)

    val dots = value match {
      case 1 => Seq(c)
      case 2 => Seq(bl, tr)
      case 3 => Seq(bl, c, tr)
      case 4 => Seq(tl, tr, bl, br)
      case 5 => Seq(tl, tr, c, bl, br)
      case 6 => Seq(tl, ml, bl, tr, mr, br)
      case _ => Seq()
    }

    new Group {
      children = dots
    }
  }
}