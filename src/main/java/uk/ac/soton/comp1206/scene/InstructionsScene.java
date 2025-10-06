package uk.ac.soton.comp1206.scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The scene displaying game instructions, and dynamically generated pieces
 */

public class InstructionsScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Creates an instance of InstructionsScene
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow){
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }

    /**
     * Builds the scene by adding UI elements
     */
    public void build(){
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        root.getStylesheets().add(getClass().getResource("/style/game.css").toExternalForm());

        var instructionsPane = new StackPane();
        instructionsPane.setMaxWidth(gameWindow.getWidth());
        instructionsPane.setMaxHeight(gameWindow.getHeight());
        instructionsPane.setAlignment(Pos.CENTER); // Center the content
        root.getChildren().add(instructionsPane);
        instructionsPane.getStyleClass().add("menu-background");

        // Create a VBox to arrange elements vertically
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER); // Center align the VBox
        instructionsPane.getChildren().add(vbox);

        // Create a title label for instructions
        Label titleLabel = new Label("Instructions");
        titleLabel.getStyleClass().add("title");
        titleLabel.setTextFill(Color.WHITE); // Set the text color to white
        titleLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Regular.ttf"), 5)); // Adjust font size

        // Create text with instructions
        Label instructionsLabel = new Label("TetrECS is a fast-paced gravity-free block placement game, where you must survive by clearing rows through careful placement of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!");
        instructionsLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Regular.ttf"), 10));
        instructionsLabel.setTextFill(Color.WHITE); // Set the text color to white
        instructionsLabel.setWrapText(true); // Enable text wrapping
        instructionsLabel.setMaxWidth(gameWindow.getWidth() * 0.8); // Set the maximum width of the text label to 80% of the screen width

        // Add the title label, and instructions label to the VBox
        vbox.getChildren().addAll(titleLabel, instructionsLabel);

        // Add image with game instructions
        ImageView instructionsImage = new ImageView(new Image(getClass().getResourceAsStream("/images/Instructions.png")));
        double imageSize = Math.min(gameWindow.getWidth(), gameWindow.getHeight()) * 0.6;
        instructionsImage.setFitWidth(imageSize);
        instructionsImage.setFitHeight(imageSize);
        instructionsImage.setPreserveRatio(true); // Preserve the aspect ratio
        vbox.getChildren().add(instructionsImage);

        // Create a title label for instructions
        Label gamePiecesLabel = new Label("Game Pieces");
        gamePiecesLabel.getStyleClass().add("title");
        gamePiecesLabel.setTextFill(Color.WHITE); // Set the text color to white
        gamePiecesLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Regular.ttf"), 5)); // Adjust font size
        vbox.getChildren().add(gamePiecesLabel);

        // Create a grid pane to display all 15 pieces
        GridPane pieceGrid = new GridPane();
        pieceGrid.setAlignment(Pos.CENTER);
        pieceGrid.setHgap(20);
        pieceGrid.setVgap(20);
        vbox.getChildren().add(pieceGrid);

        // Loop through each piece index (from 0 to 14)
        for (int index = 0; index < 15; index++) {
            // Create a GamePiece instance for the current index
            GamePiece gamePiece = GamePiece.createPiece(index);

            // Create a PieceBoard instance for the current piece
            PieceBoard pieceBoard = new PieceBoard(gameWindow.getWidth() * 0.05, gameWindow.getHeight() * 0.05);

            // Display the current piece on the piece board
            pieceBoard.displayPiece(gamePiece);

            // Add the piece board to the grid pane at the corresponding row and column
            int row = index / 5;
            int col = index % 5;
            pieceGrid.add(pieceBoard, col, row);
        }

    }

    /**
     * Initializes the scene by adding a keyboard listener
     */
    @Override
    public void initialise() {
        // Add keyboard listener to listen for Escape key press
        getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                // Load the menu scene
                gameWindow.loadScene(new MenuScene(gameWindow));
            }
        });

    }
}
