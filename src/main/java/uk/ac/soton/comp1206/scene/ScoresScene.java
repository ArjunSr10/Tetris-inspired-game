package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.Collections;
import java.util.Optional;
import javafx.scene.layout.BorderPane;
import uk.ac.soton.comp1206.component.ScoresList;
import javafx.collections.ObservableList;

/**
 * The ScoresScene class represents the scene for displaying local and online high scores.
 * It provides functionality to load, display, and update high scores
 */
public class ScoresScene extends BaseScene{

    /**
     * The game associated with this ScoresScene
     */
    private final Game game;

    /**
     * The local scores list for this scene.
     */
    private final ScoresList localScoresList;

    /**
     * The remote scores list for this scene
     */
    private final ScoresList remoteScoresList;

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * The timeline for the score scene
     */
    private Timeline scoreSceneTimeline;

    /**
     * The challenge scene associated with this ScoresScene
     */
    private final ChallengeScene challengeScene;

    /**
     * Observable list property to hold the scores
     */
    private final ListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>(FXCollections.observableArrayList());

    /**
     * Observable list property to hold the remote scores
     */
    private final ListProperty<Pair<String, Integer>> remoteScores = new SimpleListProperty<>(FXCollections.observableArrayList());

    /**
     * The communicator for handling communication with the server
     */
    private final Communicator communicator;

    /**
     * Constructs a ScoreScene object
     * @param gameWindow the game window associated with the scene
     * @param game the game object
     * @param challengeScene the challenge scene object
     */
    public ScoresScene(GameWindow gameWindow, Game game, ChallengeScene challengeScene){
        super(gameWindow);
        logger.info("Creating Scores Scene");
        this.game = game;
        this.localScoresList = new ScoresList();
        this.remoteScoresList = new ScoresList();
        this.challengeScene = challengeScene;
        // Bind the ScoresList scores to the ScoresScene scores list
        localScoresList.scoresProperty().bind(localScores);
        remoteScoresList.scoresProperty().bind(remoteScores);

        // Initialize the Communicator
        this.communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");
        // Add a listener to receive high scores from the server
        communicator.addListener(this::receiveOnlineScores);
    }

    /**
     * Builds the UI components of the scene
     */
    public void build(){
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        root.getStylesheets().add(getClass().getResource("/style/game.css").toExternalForm()); // Link the CSS file

        BorderPane mainPane = new BorderPane(); // Use BorderPane as the main layout container
        mainPane.setMaxWidth(gameWindow.getWidth());
        mainPane.setMaxHeight(gameWindow.getHeight());
        mainPane.getStyleClass().add("menu-background"); // Apply CSS class

        // Create a VBox to hold the "Game Over!" label and title image
        VBox titleContainer = new VBox(20); // Spacing between children
        titleContainer.setAlignment(Pos.CENTER);

        // Create an ImageView for the title image
        ImageView titleImage = new ImageView(new Image(getClass().getResourceAsStream("/images/TetrECS.png")));
        titleImage.setFitWidth(400); // Set the width of the image
        titleImage.setPreserveRatio(true); // Preserve the aspect ratio
        titleImage.getStyleClass().add("bigtitle"); // Apply the "bigtitle" style class

        // Label for "Game Over!"
        Label gameOverLabel = new Label("Game Over!");
        gameOverLabel.setStyle("-fx-font-size: 50px; -fx-font-family: 'Orbitron';"); // Apply font size, family, and text fill
        gameOverLabel.setTextFill(Color.YELLOW);

        // Add the title image and "Game Over!" label to the VBox
        titleContainer.getChildren().addAll(titleImage, gameOverLabel);
        mainPane.setTop(titleContainer); // Set the VBox with title image and "Game Over!" label to the top of the BorderPane

        // Create a VBox to hold local scores
        VBox scoresContainer = new VBox(10); // Spacing between children
        scoresContainer.setAlignment(Pos.CENTER_LEFT); // Align children to the left

        VBox remoteScoresContainer = new VBox(10); // Spacing between children
        remoteScoresContainer.setAlignment(Pos.CENTER_RIGHT); // Align children to the right

        // Create a label for local scores
        Label localScoresLabel = new Label("Local Scores:");
        localScoresLabel.setStyle("-fx-font-size: 20px; -fx-font-family: 'Orbitron';"); // Apply font size and family directly
        localScoresLabel.setTextFill(Color.WHITE); // Set text color to white

        // Create a label for remote scores
        Label remoteScoresLabel = new Label("Online Scores:");
        remoteScoresLabel.setStyle("-fx-font-size: 20px; -fx-font-family: 'Orbitron';"); // Apply font size and family directly
        remoteScoresLabel.setTextFill(Color.WHITE); // Set text color to white

        // Set margin for the top of the label and scores list for local scores
        VBox.setMargin(localScoresLabel, new Insets(70, 20, 0, 130));
        VBox.setMargin(localScoresList, new Insets(0, 20, 0, 130));

        // Set margin for the top of the label and scores list for remote scores
        VBox.setMargin(remoteScoresLabel, new Insets(70, 85, 0, 130));
        VBox.setMargin(remoteScoresList, new Insets(0, 20, 0, 130));

        scoresContainer.getChildren().addAll(localScoresLabel, localScoresList);
        remoteScoresContainer.getChildren().addAll(remoteScoresLabel, remoteScoresList);
        mainPane.setLeft(scoresContainer); // Set scores container to the left center of the BorderPane
        mainPane.setRight(remoteScoresContainer); // Set remote scores container to the right center of the BorderPane

        root.getChildren().add(mainPane); // Add BorderPane to the root node

        // Load scores from file
        loadScores("/Users/arjunsrinivasan/Documents/Programming II/Coursework/coursework/src/main/java/uk/ac/soton/comp1206/scores.txt");

        // Load online scores
        loadOnlineScores();

    }

    /**
     * Initializes the ScoresScene by setting the loaded scores to the ScoresList,
     * checking if the current game score beats any high scores and prompting the user
     * if the current score is among the top 10 local or remote scores.
     */
    @Override
    public void initialise() {
        // Set the loaded scores to the ScoresList
        localScoresList.setScores(localScores.get());

        // Check if current game score beats any high scores
        int gameScore = game.scoreProperty().get();

        boolean isNewHighScore = localScores.isEmpty(); // Set to true if there are no scores

        // Check if there are fewer than 10 scores
        if (localScores.size() < 10){
            isNewHighScore = true;
        }
        else{
            // Check if the new score is greater than any of the existing scores
            for (Pair<String, Integer> score : localScores) {
                if (gameScore > score.getValue()) {
                    isNewHighScore = true;
                    break;
                }
            }
        }

        // If the current score is among the top 10 local or remote scores, prompt the user for their name
        if (isNewHighScore || isNewHighScore(gameScore)) {
            logger.info("New High Score!");
            insertScore(gameScore);
        }
        else{
            // Otherwise just reveal the scores by animation straight away, and start the timeline for the menu scene to appear next
            revealScores();
            startScoreSceneTimeline();
        }

    }


    /**
     * Getter for the localScores property
     * @return the local scores list
     */
    public ListProperty<Pair<String, Integer>> localScoresProperty(){
        return localScores;
    }

    /**
     * Method to reveal the scores with animation
     */
    public void revealScores(){
        logger.info("Revealing local and remote scores");
        localScoresList.reveal();
        // Set visibility to true to ensure it's visible
        localScoresList.setVisible(true);

        remoteScoresList.reveal();
        // Set visibility to true to ensure it's visible
        remoteScoresList.setVisible(true);
    }

    /**
     * Updates scores list
     * @param scores the local scores list
     */
    public void updateScoresList(ObservableList<Pair<String, Integer>> scores) {
        localScores.set(scores);
    }

    /**
     * Load high scores from a file and populate an ordered list.
     * A simple format of newline separated name:score is assumed.
     * Update the ScoresScene score list with the loaded scores (which will update the ScoresList).
     * @param filePath the path to the file containing high scores
     */
    public void loadScores(String filePath) {

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            ObservableList<Pair<String, Integer>> loadedScores = FXCollections.observableArrayList();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                // Check if the line has the expected format (name:score)
                if (parts.length == 2) {
                    String name = parts[0].trim(); // Extract the name
                    int score = Integer.parseInt(parts[1].trim()); // Extract the scores
                    loadedScores.add(new Pair<>(name, score)); // Add the name and score to the loaded scores list
                }
            }

            localScores.set(loadedScores); // Set the loaded scores to the localScores list

        } catch (IOException e) {
            System.err.println("Error loading scores from file: " + e.getMessage());
        }
    }

    /**
     * Write an ordered list of scores into a file, using the format name:score.
     * @param filePath the path to the file where scores will be written
     * @param newScore the list of scores to write
     */
    public void writeScores(String filePath, Pair<String, Integer> newScore) {
        try {
            // Use FileWriter with append mode set to true
            FileWriter fileWriter = new FileWriter(filePath, true);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            // Write the new score
            writer.write(newScore.getKey() + ":" + newScore.getValue());
            writer.write("\n");

            // Close the writer
            writer.close();

            // Load the updated scores from the file
            loadScores(filePath);

            logger.info("Updating local scores");
            // Update the scores list in the game
            localScoresList.setScores(localScores.get());

        } catch (IOException e) {
            System.err.println("Error writing scores to file: " + e.getMessage());
        }
    }

    /**
     * Write a default list of scores to the given BufferedWriter.
     * @param writer the BufferedWriter to write to
     * @throws IOException if an I/O error occurs
     */
    private void writeDefaultScores(BufferedWriter writer) throws IOException {
        // Write default scores
        writer.write("Player1:100\n");
        writer.write("Player2:150\n");
        writer.write("Player3:200\n");
    }

    /**
     * Check if the provided score beats any of the high scores.
     * If so, prompt the user for their name and insert the score into the list at the correct position.
     * @param score the score to check and insert
     */
    private void insertScore(int score) {
        // Hide the scores list before showing the dialog
        localScoresList.setVisible(false);
        remoteScoresList.setVisible(false);

        TextInputDialog dialog = new TextInputDialog("Player");
        dialog.setTitle("New High Score!");
        dialog.setHeaderText("Congratulations! You achieved a new high score!");
        dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            Pair<String, Integer> newScore = new Pair<>(name, score);
            logger.info("Inserting new score into local scores list");
            localScores.add(newScore);

            // Sort the scores after insertion
            sortScores();

            // Keep only the top 10 scores
            if (localScores.size() > 10) {
                localScores.remove(10, localScores.size());
            }

            // Update the scores list in the scores scene
            updateScoresList(localScores.get());

            // Write the score to the online server
            submitNewHighScore(newScore.getKey(), newScore.getValue());

            logger.info("Writing score to the local server");
            // Write the score to the local server
            writeScores("/Users/arjunsrinivasan/Documents/Programming II/Coursework/coursework/src/main/java/uk/ac/soton/comp1206/scores.txt", newScore);

            revealScores();
            // Start the timeline for the score scene after inserting the score
            startScoreSceneTimeline();
        });
    }

    private void startScoreSceneTimeline() {
        // Start the timeline for the score scene
        scoreSceneTimeline = new Timeline(new KeyFrame(Duration.seconds(15), event -> {
            // Reset the game after the timeline runs out
            challengeScene.setupGame();
            // Clean up game resources before a new game is started when the timer runs out
            game.stop();
            // Load the menu scene when the timer runs out
            gameWindow.startMenu();
        }));
        scoreSceneTimeline.play();
    }

    /**
     * Sort the list of scores in descending order based on score value.
     */
    private void sortScores() {
        ObservableList<Pair<String, Integer>> sortedScores = FXCollections.observableArrayList(localScores.get());
        Collections.sort(sortedScores, (s1, s2) -> s2.getValue().compareTo(s1.getValue()));
        localScores.set(sortedScores);
    }

    /**
     * Load online scores by sending a request to the server.
     * The server will respond with the high scores list.
     */
    public void loadOnlineScores() {
        // Send a request for high scores to the server
        communicator.send("HISCORES");
    }

    /**
     * Method to receive online scores from the server
     * @param message the high scores list, or the new score to be inserted
     */
    private void receiveOnlineScores(String message){
        // Split the message by newline characters
        String[] lines = message.split("\n");

        // Remove the "HISCORES" or "NEWSCORE" part from the first line
        if (lines.length > 0) {
            if (lines[0].startsWith("HISCORES")) {
                lines[0] = lines[0].replaceFirst("^HISCORES\\s*", ""); // Remove "HISCORES" from the start of the line
            } else if (lines[0].startsWith("NEWSCORE")) {
                lines[0] = lines[0].replaceFirst("^NEWSCORE\\s*", ""); // Remove "NEWSCORE" from the start of the line
            }
        }

        // Join the lines back together
        String modifiedMessage = String.join("\n", lines);

        // Parse the modified message and update the remoteScores property accordingly
        String[] scores = modifiedMessage.split("\n");
        ObservableList<Pair<String, Integer>> onlineScores = FXCollections.observableArrayList();

        for (String score: scores){
            String[] parts = score.split(":");
            if (parts.length == 2){
                String name = parts[0].trim();
                int scoreValue = Integer.parseInt(parts[1].trim());
                onlineScores.add(new Pair<>(name, scoreValue));
            }
        }

        // Update the remoteScores property with the received scores
        Platform.runLater(() -> {
            logger.info("Adding score to remote scores list");
            remoteScores.addAll(onlineScores); // Add the new scores
            remoteScoresList.setScores(remoteScores.get()); // Update remote scores list
        });
    }

    private boolean isNewHighScore(int score) {
        for (Pair<String, Integer> remoteScore : remoteScores) {
            if (score > remoteScore.getValue()) {
                return true; // If the user's score beats any score in the remote scores list, return true
            }
        }
        return false; // If the user's score doesn't beat any score in the remote scores list, return false
    }


    /**
     * Load online scores with unique player names by sending a request to the server.
     * The server will respond with the high scores list containing only unique player names.
     */
    public void loadOnlineScoresUnique() {
        // Send a request for high scores with unique player names to the server
        communicator.send("HISCORES UNIQUE");
    }

    /**
     * Load online scores with default high scores by sending a request to the server.
     * The server will respond with the default high scores list.
     * This is useful for testing purposes.
     */
    public void loadDefaultOnlineScores() {
        // Send a request for default high scores to the server
        communicator.send("HISCORES DEFAULT");
    }

    /**
     * Submit a new high score to the server.
     * @param name the name of the player.
     * @param score the score achieved by the player.
     */
    public void submitNewHighScore(String name, int score) {
        // Send the new high score to the server
        String message = "HISCORE " + name + ":" + score;
        communicator.send(message);
    }


}
