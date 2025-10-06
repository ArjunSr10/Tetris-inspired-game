package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.scene.ChallengeScene;

import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    /**
     * List of listeners to notify when the game state updates
     */
    private List<GameListener> listeners = new ArrayList<>();

    /**
     * List of listeners to notify when the next piece updates
     */
    private List<NextPieceListener> nextPieceListeners = new ArrayList<>();

    /**
     * List of LineClearedListeners to notify when lines are cleared.
     */
    private List<LineClearedListener> lineClearedListeners = new ArrayList<>();

    /**
     * Listener for game loop events
     */
    private GameLoopListener gameLoopListener;
    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Executor service for scheduling tasks
     */
    private ScheduledExecutorService executorService;

    /**
     * The delay for the game timer
     */
    private int timerDelay;

    /**
     * The next game piece to be played
     */
    private GamePiece currentPiece;

    /**
     * The piece that will appear after the current piece
     */
    private GamePiece followingPiece;

    /**
     * Multimedia object for game sounds and music
     */
    private Multimedia multimedia;

    /**
     * Random number generator
     */
    private Random random = new Random();

    /**
     * The score of the game
     */
    private IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * The level of the game
     */
    private IntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * The number of lives of the game
     */
    private IntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * The multiplier of the game
     */
    private IntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * Listener for next piece events
     */
    private NextPieceListener nextPieceListener;

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        // Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

        // Creates new multimedia instance to play audio files, and background music
        multimedia = new Multimedia();

    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        nextPiece(); // Initialise the current piece
        followingPiece = spawnPiece(); // Initialise the following piece
        startTimer(); // Starts game loop timer
    }

    /**
     * Start the timer with the initial delay.
     */
    private void startTimer() {
        timerDelay = getTimerDelay();
        logger.info("Timer is now " + ((double) getTimerDelay() / 1000) + " seconds");
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::gameLoop, timerDelay, timerDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Reset the timer with the updated delay.
     */
    public void resetTimer() {
        executorService.shutdown();
        startTimer();
    }

    /**
     * Game loop method called by the timer.
     * Decrements the timer, loses a life, and discards the current piece when the timer reaches zero.
     */
    private void gameLoop() {
        timerDelay = getTimerDelay();

        if (timerDelay <= 0) {
            logger.info("Timer reached zero. Losing a life and discarding current piece.");
            loseLife();
            currentPiece = followingPiece;
            followingPiece = spawnPiece();
            resetTimer();
        }

        // Invoke the GameLoopListener to update the UI timer
        if (gameLoopListener != null) {
            gameLoopListener.onGameLoop();
        }
    }

    /**
     * Lose a life, by subtracting one from the lives property
     */
    public void loseLife() {
        int remainingLives = lives.get();
        // Reduce number of lives if the remaining lives is greater than or equal to 0
        if (remainingLives >= 0) {
            // Reduce the number of lives by one
            lives.set(remainingLives - 1);
            multimedia.playLoseLifeSound();
        }
    }

    /**
     * Replaces the current piece with a new piece, and generates next piece in advance
     * @return The next piece to be played
     */
    public GamePiece nextPiece(){
        // Move the following piece to the current piece
        currentPiece = followingPiece;

        // Generate a new following piece
        followingPiece = spawnPiece();

        logger.info("The next piece is: {}",currentPiece);

        // Notify next piece listener when a new piece is generated, passing both current and following pieces
        if (nextPieceListener != null) {
            nextPieceListener.onNextPiece(currentPiece, followingPiece);
        }
        return currentPiece;
    }


    /**
     * Creates a random GamePiece to be played in the turn
     * @return the random piece generated to be played
     */
    public GamePiece spawnPiece(){
        // Maximum number of pieces obtained
        var maxPieces = GamePiece.PIECES;
        // Generates random number, based on the maxPieces number
        var randomPiece = random.nextInt(maxPieces);
        logger.info("Picking random piece: {}",randomPiece);
        // Creates random piece based on the random value generated
        var piece = GamePiece.createPiece(randomPiece);
        return piece;
    }

    /**
     * Handles actions that need to take place after a piece is played.
     * This includes clearing lines, and updating the multiplier and score
     */
    public void afterPiece() {
        Set<GameBlockCoordinate> coordinateSet = new HashSet<>(); // Set to store coordinates of cleared blocks
        var lineCounter = 0; // Counter for number of lines cleared
        var clearedBlocksCounter = 0; // Counter for number of blocks cleared

        // Clear columns
        for (var x= 0; x < grid.getCols(); x++) {
            ArrayList<GameBlockCoordinate> columnArrayList = new ArrayList<>(); // List to store coordinates of blocks in a column
            for (var y = 0; y < grid.getRows(); y++) {
                columnArrayList.add(new GameBlockCoordinate(x, y)); // Add coordinates to the list
            }
            if (allOnes(columnArrayList)) { // Check if all blocks in the column are filled
                coordinateSet.addAll(columnArrayList); // Add coordinates to the set
                lineCounter++;
            }
        }

        // Clear rows
        for (var y = 0; y < grid.getRows(); y++) {
            ArrayList<GameBlockCoordinate> rowArrayList = new ArrayList<>(); // List to store coordinates of blocks in a row
            for (var x = 0; x < grid.getCols(); x++) {
                rowArrayList.add(new GameBlockCoordinate(x, y)); // Add coordinates to the list
            }
            if (allOnes(rowArrayList)) { // Check if all blocks in the row are filled
                coordinateSet.addAll(rowArrayList); // Add coordinates to the set
                lineCounter++;
            }
        }

        // Clear cells
        for (GameBlockCoordinate gameBlockCoordinate : coordinateSet) {
            logger.info("Clearing block at coordinates {},{}", gameBlockCoordinate.getX(), gameBlockCoordinate.getY());
            grid.set(gameBlockCoordinate.getX(), gameBlockCoordinate.getY(), 0); // Clear block in the grid
            multimedia.playClearLineSound();
            clearedBlocksCounter++;
        }

        // Notify listeners about lines cleared
        notifyLineClearedListeners(coordinateSet);

        // Update score based on number of lines cleared and blocks cleared
        score(lineCounter, clearedBlocksCounter);

        // Check if lines were cleared to update the multiplier
        if(lineCounter > 0) {
            multiplier.set(multiplier.get() + 1);
            logger.info("Multiplier increased by 1");
        }
        else{
            multiplier.set(1); // Reset multiplier if lines were cleared in the turn
            logger.info("Multiplier set back to 1");
        }

        // Check if the level needs to be increased based on the score

        if((score.get() / 1000) != level.get()){
            level.set(level.get() + 1);
        }

    }

    /**
     * Checks if all the grid values in a particular line are 1s (so they are coloured blocks)
     * @param lineArrayList arrayList that contains the GameBlockCoordinates for a particular line
     * @return boolean value determining whether a line should be cleared, false if it shouldn't and true if it should
     */
    public boolean allOnes(ArrayList<GameBlockCoordinate> lineArrayList) {
        for (GameBlockCoordinate gameBlockCoordinate : lineArrayList) {
            if (grid.get(gameBlockCoordinate.getX(), gameBlockCoordinate.getY()) < 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {

        logger.info("Initialising game");
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if(grid.canPlayPiece(currentPiece,x,y)){
            //Can play the piece
            grid.playPiece(currentPiece,x,y);
            multimedia.playPlacePieceSound(); // Play sound for placing piece
            ChallengeScene.setTimerDuration(getTimerDelay());
            afterPiece();
            nextPiece();
            resetTimer(); // Reset the timer with the updated delay
        }
        else{
            //Can't play the piece
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the piece that will appear after the current piece
     * @return the following piece
     */
    public GamePiece getFollowingPiece() {
        return followingPiece;
    }

    /**
     * Access the score property
     * @return the current score
     */
    public IntegerProperty scoreProperty() {
        return score;
    }

    /**
     * Access the level property
     * @return the current level
     */
    public IntegerProperty levelProperty() {
        return level;
    }

    /**
     * Access the lives property
     * @return the current number of lives
     */
    public IntegerProperty livesProperty() {
        return lives;
    }

    /**
     * Access the multiplier property
     * @return the current multiplier
     */
    public IntegerProperty multiplierProperty() {
        return multiplier;
    }

    /**
     * Calculates the score based on the number of lines cleared and the number of cleared
     * blocks, and updates the current score accordingly
     * @param numberOfLines the number of lines cleared in the turn
     * @param numberOfClearedBlocks the number of blocks cleared in the turn
     */
    public void score(int numberOfLines, int numberOfClearedBlocks){
        // Calculate the score based on the formula
        int newScore = numberOfLines * numberOfClearedBlocks * 10 * multiplier.get();

        // Add the calculated score to the current score
        score.set(score.get() + newScore);
    }

    /**
     * Adds a GameListener to the list of listeners
     * @param listener the GameListener to be added
     */
    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all registered listeners that a game update has occurred
     */
    private void notifyListeners() {
        for (GameListener listener : listeners) {
            listener.onGameUpdate();
        }
    }

    /**
     * Set the next piece listener.
     * @param listener the next piece listener to set
     */
    public void setNextPieceListener(NextPieceListener listener) {
        this.nextPieceListener = listener;
    }

    /**
     * Rotate the next piece clockwise
     */
    public void rotateCurrentPiece() {
        if (currentPiece != null) {
            logger.info("Rotating next piece clockwise");
            currentPiece.rotate();
            multimedia.playRotatePieceSound(); // Play sound for rotating piece
            notifyListeners(); // Notify listeners about the change
        }
    }

    /**
     * Rotate the next piece anti-clockwise
     */
    public void rotateCurrentPiece(int rotations) {
        if (currentPiece != null) {
            logger.info("Rotating next piece anti - clockwise");
            currentPiece.rotate(rotations);
            multimedia.playRotatePieceSound(); // Play sound for rotating piece
            notifyListeners(); // Notify listeners about the change
        }
    }

    /**
     * Swap the current piece with the following piece.
     */
    public void swapCurrentPiece() {
        GamePiece temp = currentPiece;
        currentPiece = followingPiece;
        logger.info("Swapping next piece and next piece in advance");
        followingPiece = temp;

        multimedia.playSwapPieceSound(); // Play sound for swapping piece

        // Notify next piece listener when the pieces are swapped, passing both current and following pieces
        if (nextPieceListener != null) {
            nextPieceListener.onNextPiece(currentPiece, followingPiece);
        }
    }

    /**
     * Retrieve the next piece to be played
     * @return the GamePiece to be played next
     */
    public GamePiece getCurrentPiece(){
        return currentPiece;
    }

    /**
     * Add a LineClearedListener to the game.
     * @param listener the listener to add
     */
    public void addLineClearedListener(LineClearedListener listener) {
        lineClearedListeners.add(listener);
    }

    /**
     * Notify all LineClearedListeners that lines have been cleared.
     * @param coordinates the coordinates of the blocks that were cleared
     */
    private void notifyLineClearedListeners(Set<GameBlockCoordinate> coordinates) {
        for (LineClearedListener listener : lineClearedListeners) {
            listener.onLinesCleared(coordinates);
        }
    }

    /**
     * Gets the timer delay based on the current level.
     * The delay decreases as the level increases.
     *
     * @return the timer delay in milliseconds
     */
    public int getTimerDelay() {
        // Calculate the delay based on the current level
        int delay = Math.max(2500, 12000 - (500 * level.get()));

        return delay;
    }

    /**
     * Stop the game and clean up resources.
     */
    public void stop() {
        // Reset the score, level, lives, and multiplier
        score.set(0);
        level.set(0);
        lives.set(3);
        multiplier.set(1);
    }
}
