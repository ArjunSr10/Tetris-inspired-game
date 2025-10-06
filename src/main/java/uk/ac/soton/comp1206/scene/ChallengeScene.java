package uk.ac.soton.comp1206.scene;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener, LineClearedListener, GameLoopListener {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Holds the PieceBoard display of the next piece to be played
     */
    private PieceBoard currentPieceBoard;

    /**
     * Holds the PieceBoard for the next piece to be displayed in advance
     */
    private PieceBoard followingPieceBoard;

    /**
     * Instance of the multimedia class to play audio files and background music
     */
    private Multimedia multimedia;

    /**
     * Game attribute used to access the game state
     */
    protected Game game;

    /**
     * GameBoard attribute used to access the game board
     */
    private GameBoard board;

    /**
     * Current x coordinate of the aim position
     */
    private int aimX;

    /**
     * Current y coordinate of the aim position
     */
    private int aimY;

    /**
     * Previous x coordinate of the aim position
     */
    private int previousAimX;

    /**
     * Previous y coordinate of the aim position
     */
    private int previousAimY;

    /**
     * Used to display the TimerBar as rectangle at the bottom of this scene
     */
    private Rectangle timerBar;

    /**
     * Timeline object responsible for animating the timer display
     */
    private Timeline timerAnimation;

    /**
     * Timer duration in milliseconds
     */
    private static int timerDuration;

    /**
     * Initialize the high score
     */
    private int highScore = 0;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
        multimedia = new Multimedia();
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        root.getStylesheets().add(getClass().getResource("/style/game.css").toExternalForm()); // Link the CSS file

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background"); // Apply the "challenge-background" style class
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        board.getStyleClass().add("gameBox"); // Add a custom style class for the game board
        mainPane.setCenter(board);

        // Register the GameBoard instance as a listener
        game.addListener(board);

        // Create a VBox to hold the labels
        VBox labelsBox = new VBox();
        labelsBox.setSpacing(10); // Set spacing between labels

        // Create labels to display score, level, multiplier, and lives
        Label scoreLabel = new Label("Score: ");
        Label levelLabel = new Label("Level: ");
        Label multiplierLabel = new Label("Multiplier: ");
        Label livesLabel = new Label("Lives: ");

        // Bind labels to corresponding properties in the game
        scoreLabel.textProperty().bind(Bindings.concat("Score: ", game.scoreProperty().asString()));
        levelLabel.textProperty().bind(Bindings.concat("Level: ", game.levelProperty().asString()));
        multiplierLabel.textProperty().bind(Bindings.concat("Multiplier: ", game.multiplierProperty().asString()));
        livesLabel.textProperty().bind(Bindings.concat("Lives: ", game.livesProperty().asString()));

        // Apply CSS styles to the labels
        scoreLabel.getStyleClass().add("score");
        levelLabel.getStyleClass().add("level");
        multiplierLabel.getStyleClass().add("multiplier");
        livesLabel.getStyleClass().add("lives");

        // Set text fill colors of labels
        scoreLabel.setTextFill(Color.YELLOW);
        levelLabel.setTextFill(Color.ORANGE);
        multiplierLabel.setTextFill(Color.WHITE);
        livesLabel.setTextFill(Color.YELLOW);

        BorderPane topPane = new BorderPane();
        topPane.setLeft(scoreLabel);
        topPane.setRight(livesLabel);

        // Add the labels and the game board to the mainPane
        mainPane.setTop(topPane);
        mainPane.setCenter(board);

        logger.info("Adding, score, and lives labels to the Challenge Scene");

        // Create a new PieceBoard instance for the next piece
        this.currentPieceBoard = new PieceBoard(100, 100);
        currentPieceBoard.getStyleClass().add("gameBox");

        // Create a new PieceBoard instance for following piece
        this.followingPieceBoard = new PieceBoard(75, 75);
        followingPieceBoard.getStyleClass().add("gameBox");

        // Create a VBox to hold the incoming title, next piece board, and following piece board
        VBox incomingBox = new VBox();
        incomingBox.setAlignment(Pos.CENTER);
        incomingBox.setSpacing(10);

        // Call getHighScore() to retrieve the high score label
        Label highScoreLabel = getHighScore();
        highScoreLabel.getStyleClass().add("hiscore");
        highScoreLabel.setTextFill(Color.ORANGE);

        // Check if highScoreLabel is not null before adding it to the UI
        if (highScoreLabel != null) {
            incomingBox.getChildren().add(highScoreLabel);
        }
        // Add the multiplier label to the VBox
        incomingBox.getChildren().add(multiplierLabel);
        // Add the level label to the VBox
        incomingBox.getChildren().add(levelLabel);


        // Add the title for the following piece board
        Label incomingTitle = new Label("Incoming");
        incomingTitle.getStyleClass().add("title");
        incomingTitle.setTextFill(Color.ORANGE); // Set text fill color to orange
        incomingTitle.setStyle("-fx-font-size: 20;"); // Set font size
        incomingBox.getChildren().add(incomingTitle);

        // Add the next piece board to the VBox
        incomingBox.getChildren().add(currentPieceBoard);

        // Add the following piece board to the VBox
        incomingBox.getChildren().add(followingPieceBoard);

        // Add incomingBox to mainPane on the right-hand side
        mainPane.setRight(incomingBox);

        logger.info("Added high score, multiplier, and level label to the challenge scene, as well as the current piece board and following piece board");

        // Handle block on gameBoard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        // Set up a left-click listener for the current piece board
        currentPieceBoard.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                rotateNextPiece();
            }
        });

        // Add event handler to the following piece board for left click
        followingPieceBoard.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                game.swapCurrentPiece(); // Call swapCurrentPiece method when left click is detected
            }
        });

        // Create the timer bar
        timerBar = new Rectangle();
        timerBar.setWidth(gameWindow.getWidth());
        timerBar.setHeight(20); // Set height of the timer bar
        timerBar.setFill(Color.GREEN); // Set initial color of the timer bar

        BorderPane.setAlignment(timerBar, Pos.BOTTOM_LEFT);
        mainPane.setBottom(timerBar);

        // Initialize the timer animation
        timerAnimation = new Timeline();
        timerAnimation.setCycleCount(Timeline.INDEFINITE); // Repeat indefinitely
        KeyFrame keyFrame = new KeyFrame(Duration.millis(10), event -> updateTimerBar());
        timerAnimation.getKeyFrames().add(keyFrame);

        // Play background music for the game
        multimedia.playBackgroundMusic("/music/game.wav");

    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {

        game.blockClicked(gameBlock);
    }

    /**
     * Set up the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
        game.setNextPieceListener(this); // Register this scene as a listener for next piece events
        game.addLineClearedListener(this); // Register this scene as a listener for line cleared events
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");

        aimX = 0;
        aimY = 0;

        // Set the initial timer duration (in milliseconds)
        timerDuration = 12000;

        // Start the timer animation
        timerAnimation.play();

        // Set up key event handling
        getScene().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                // Rotate next piece clockwise (E, C, or CLOSE_BRACKET key)
                case E, C, CLOSE_BRACKET -> rotateNextPiece();
                // Rotate next piece counterclockwise (Q, Z, or OPEN_BRACKET key)
                case Q, Z, OPEN_BRACKET -> rotateNextPiece(3);
                // Swap current piece (R or SPACE key)
                case R, SPACE -> game.swapCurrentPiece();
                // Exit challenge (ESCAPE key)
                case ESCAPE -> {
                    logger.info("Leaving challenge scene, as escape key has been pressed");
                    game.stop(); // Stop the game
                    multimedia.stopBackgroundMusic();
                    // Stop the timer animation
                    timerAnimation.stop();
                    // Reset the game state to its initial state
                    setupGame();
                    gameWindow.loadScene(new MenuScene(gameWindow));
                }
                // Move aim left (LEFT or A key)
                case LEFT, A -> {
                    moveAimLeft();
                    logger.info("Moving aim left");
                }
                // Move aim right (RIGHT or D key)
                case RIGHT, D -> {
                    logger.info("Moving aim right");
                    moveAimRight();
                }
                // Move aim down (DOWN or S key)
                case DOWN, S -> {
                    logger.info("Moving aim down");
                    moveAimDown();
                }
                // Move aim up (UP or W key)
                case UP, W -> {
                    logger.info("Moving aim up");
                    moveAimUp();
                }
                // Drop the piece (ENTER or X key)
                case ENTER, X -> {
                    logger.info("Dropping piece using key enter or x");
                    dropPiece();
                }
            }
        });

        game.setNextPieceListener(this); // Register this scene as a listener
        game.start();

        // Manually call onNextPiece after starting the game
        onNextPiece(null, game.getFollowingPiece());

        // Initialize previous aim position
        previousAimX = aimX;
        previousAimY = aimY;

        // Initially update aim position
        updateAimPosition();

    }

    /**
     * Handles the event when the next piece is received
     *
     * @param currentPiece the current game piece
     * @param nextPiece the next game piece
     */
    public void onNextPiece(GamePiece currentPiece, GamePiece nextPiece){
        // Display the current piece on the current piece board
        if (currentPiece != null) {
            logger.info("Next piece received: {}", currentPiece);
            currentPieceBoard.displayPiece(currentPiece);
        } else {
            logger.warn("Received null current piece");
        }

        // Display the next piece in advance on the following piece board
        if (nextPiece != null) {
            logger.info("Next piece in advance received: {}", nextPiece);
            followingPieceBoard.displayPiece(nextPiece);
        } else {
            logger.warn("Received null next piece");
        }
    }

    /**
     * Rotate the next piece clockwise when the current piece board is left-clicked, or the appropriate keys are pressed
     */
    private void rotateNextPiece() {
        game.rotateCurrentPiece();
        currentPieceBoard.displayPiece(game.getCurrentPiece());
    }

    /**
     * Rotate the next piece anti - clockwise when the appropriate keys are pressed
     */
    private void rotateNextPiece(int rotations) {
        game.rotateCurrentPiece(rotations);
        currentPieceBoard.displayPiece(game.getCurrentPiece());
    }

    /**
     * Method to move the aim position upwards when the appropriate keys are pressed
     */
    private void moveAimUp() {
        if (aimY > 0) {
            aimY--;
            updateAimPosition();
        }
    }

    /**
     * Method to move the aim position to the left when the appropriate keys are pressed
     */
    private void moveAimLeft() {
        if (aimX > 0) {
            aimX--;
            updateAimPosition();
        }
    }

    /**
     * Method to move the aim position to the right when the appropriate keys are pressed
     */
    private void moveAimRight() {
        if (aimX < game.getGrid().getCols() - 1) {
            aimX++;
            updateAimPosition();
        }
    }

    /**
     * Method to move the aim position to downwards when the appropriate keys are pressed
     */
    private void moveAimDown() {
        if (aimY < game.getGrid().getRows() - 1) {
            aimY++;
            updateAimPosition();
        }
    }

    /**
     * Drops the piece at the current aim position
     */
    private void dropPiece() {
        // Check if the piece can be placed at the current aim position
        if (game.getGrid().canPlayPiece(game.getCurrentPiece(), aimX, aimY)) {
            // Place the piece at the current aim position
            game.getGrid().playPiece(game.getCurrentPiece(), aimX, aimY);
            multimedia.playPlacePieceSound(); // Play sound for placing piece
            game.afterPiece();
            game.nextPiece();
            game.resetTimer();

            timerDuration = game.getTimerDelay();
        }
    }

    /**
     * Update the visual representation of the aim position on the game board
     */
    private void updateAimPosition() {
        // Store the color of the previous aim block
        Color previousColor = board.getBlock(previousAimX, previousAimY).getColor();

        Color transparentBlack = Color.rgb(100, 100, 100, 0.3); // Transparent black color (RGB: 0, 0, 0), Alpha: 0.5

        // Get the color of the block at the aim position
        Color currentColor = board.getBlock(aimX, aimY).getColor();

        // Check if the current color is not transparent black
        if (!currentColor.equals(transparentBlack)) {
            // Darken the color of the block while retaining its hue
            Color lighterColor = currentColor.deriveColor(0, 1.0, 0.5, 1.0);

            // Set the new color to the block at the aim position
            board.getBlock(aimX, aimY).setColor(lighterColor);
        }
        else{
            // Set the color of the aim position to very dark grey
            Color aimColor = Color.rgb(60, 60, 60); // RGB values for very dark grey
            board.getBlock(aimX, aimY).setAimColor(aimColor);
        }

        // If the previous tile the aim position was on was transparent black
        if (previousColor.equals(transparentBlack)){
            // Reset the colour of the previous tile to transparent black, as the aim position has moved off it
            board.getBlock(previousAimX, previousAimY).setEmptyTileColor();
        }
        // Otherwise if the previous tile the aim position was on was not transparent black, so it was coloured
        else{
            // Reset the colour of the previous tile to that color
            board.getBlock(previousAimX, previousAimY).setColor(previousColor);
        }

        // Update previous aim position
        previousAimX = aimX;
        previousAimY = aimY;
    }

    /**
     * Receive notification when lines are cleared in the game.
     * @param coordinates the coordinates of the blocks that were cleared
     */
    @Override
    public void onLinesCleared(Set<GameBlockCoordinate> coordinates) {
        // Pass the cleared blocks to the fadeOut method in the GameBoard class
        board.fadeOut(coordinates);
    }

    /**
     * Update the timer bar based on the remaining time.
     */
    private void updateTimerBar() {
        // Get the timer delay from the Game class
        int timerDelay = game.getTimerDelay();

        // Calculate the remaining time as a percentage of the initial duration
        double remainingTimePercentage = (double) timerDuration / timerDelay;

        // Update the width of the timer bar based on the remaining time percentage
        timerBar.setWidth(gameWindow.getWidth() * remainingTimePercentage);

        // Change the color of the timer bar based on urgency
        if (timerDuration <= 0.2 * timerDelay) {
            timerBar.setFill(Color.RED); // Red color for high urgency
        } else if (timerDuration <= 0.6 * timerDelay) {
            timerBar.setFill(Color.YELLOW); // Yellow color for medium urgency
        } else {
            timerBar.setFill(Color.GREEN); // Green color for low urgency
        }

        // Decrement the timer duration
        timerDuration -= 10; // Decrease by 100 milliseconds

        // Check if the timer has reached zero
        if (timerDuration <= 0) {
            logger.info("Timer has reached zero, lose a life, and current piece has been discarded");
            // Lose a life
            game.loseLife();

            // Reset multiplier back to 1
            game.multiplierProperty().set(1);

            // Reset the timer duration
            timerDuration = timerDelay;

            // Discard the current piece and replace it with the following piece
            game.nextPiece();

            // Reset the timer after a piece is discarded
            game.resetTimer();

            // End the game if lives are below 0
            if (game.livesProperty().get() < 0) {
                shutdownGame();
            }
        }
    }

    /**
     * Receive notification when the game loop starts.
     */
    @Override
    public void onGameLoop() {
        // Reset the timer duration when the game loop starts
        timerDuration = 12000;
    }

    /**
     * Update the timer duration.
     * @param newTimerDuration the new value of timerDuration
     */
    public static void setTimerDuration(int newTimerDuration) {
        timerDuration = newTimerDuration;
    }

    /**
     * Get the top high score when starting a game in the ChallengeScene and display it in the UI.
     */
    private Label getHighScore() {
        // Path to the scores.txt file
        String filePath = "/Users/arjunsrinivasan/Documents/Programming II/Coursework/coursework/src/main/java/uk/ac/soton/comp1206/scores.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int maxScore = 0;

            // Read each line of the file
            while ((line = reader.readLine()) != null) {
                // Split the line into name and score parts
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    // Parse the score
                    int score = Integer.parseInt(parts[1].trim());
                    // Update maxScore if the current score is higher
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
            }

            // Update the high score in the UI
            highScore = maxScore;
            Label highScoreLabel = new Label("High Score: " + highScore);
            return highScoreLabel;

        } catch (IOException e) {
            // Handle IO exception
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Shuts down the game, stopping background music and timer animation, then loads the Scores Scene
     */
    public void shutdownGame(){
        multimedia.stopBackgroundMusic();
        // Stop the timer animation
        timerAnimation.stop();
        // Load the ScoresScene directly after stopping the game
        gameWindow.scoreScene(game, this);
    }

}
