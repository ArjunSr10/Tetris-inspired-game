package uk.ac.soton.comp1206.scene;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.media.Multimedia;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Multimedia object used to play background menu music
     */
    private Multimedia multimedia;

    /**
     * Static flag to track if the fade transition has been played
     */
    private static boolean fadeTransitionPlayed = false;

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");

        multimedia = new Multimedia();

    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        root.getStylesheets().add(getClass().getResource("/style/game.css").toExternalForm()); // Link the CSS file

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        // Create a black background to transition from
        var blackBackground = new StackPane();
        blackBackground.setStyle("-fx-background-color: black;");
        blackBackground.setMaxSize(gameWindow.getWidth(), gameWindow.getHeight());
        menuPane.getChildren().add(blackBackground);

        // Create an ImageView for the ECSGames image
        ImageView firstImage = new ImageView(new Image(getClass().getResourceAsStream("/images/ECSGames.png")));
        double imageSize = Math.min(gameWindow.getWidth(), gameWindow.getHeight()) * 0.6;
        firstImage.setFitWidth(imageSize);
        firstImage.setFitHeight(imageSize);
        firstImage.setPreserveRatio(true); // Preserve the aspect ratio
        blackBackground.getChildren().add(firstImage);

        // Create a FadeTransition for the first image (fade out)
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), firstImage);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setNode(firstImage); // Apply the transition to the first image
        fadeOut.setOnFinished(event -> {
            menuPane.getChildren().remove(blackBackground);
            // Now that the first image has faded out, show the menuPane
        });

        /// Create an ImageView for the title image
        ImageView titleImage = new ImageView(new Image(getClass().getResourceAsStream("/images/TetrECS.png")));
        titleImage.setFitWidth(400); // Set the width of the image
        titleImage.setPreserveRatio(true); // Preserve the aspect ratio
        titleImage.getStyleClass().add("bigtitle"); //Apply the "bigtitle" style class
        mainPane.setTop(titleImage);

        // Create buttons for menu options
        var playButton = new Button("Play");
        playButton.getStyleClass().add("menuItem");

        var howToPlayButton = new Button("How To Play");
        howToPlayButton.getStyleClass().add("menuItem");
        howToPlayButton.setOnAction(this::showInstructions);

        var multiplayerButton = new Button("Multiplayer");
        multiplayerButton.getStyleClass().add("menuItem");
        multiplayerButton.setOnAction(this::showMultiPlayerLobby);

        // Create a VBox to hold the title image and buttons with spacing
        VBox titleBox = new VBox(30); // Spacing between title and buttons
        titleBox.getChildren().addAll(titleImage, playButton, howToPlayButton, multiplayerButton);
        titleBox.setAlignment(Pos.CENTER); // Center align the VBox

        mainPane.setCenter(titleBox); // Set the VBox in the center of the BorderPane

        // Create rocking animation for title image
        RotateTransition rockingAnimation = new RotateTransition(Duration.seconds(2), titleImage);
        rockingAnimation.setFromAngle(-5); // Set the starting angle
        rockingAnimation.setToAngle(5); // Set the ending angle
        rockingAnimation.setCycleCount(RotateTransition.INDEFINITE); // Repeat indefinitely
        rockingAnimation.setAutoReverse(true); // Reverse the animation direction
        rockingAnimation.play(); // Start the animation

        //Bind the button action to the startGame method in the menu
        playButton.setOnAction(this::startGame);

        // Play background music for the menu
        multimedia.playBackgroundMusic("/music/menu.mp3");

        // Play the fade out transition for the first image only if it hasn't been played before
        if (!fadeTransitionPlayed) {
            fadeOut.play();
            fadeTransitionPlayed = true;
        } else {
            // If the fade transition has already been played, remove the black background immediately
            menuPane.getChildren().remove(blackBackground);
        }


    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        // Add keyboard listener to listen for Escape key press
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                // Exit the application
                System.exit(0);
            }
        });
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {

        // Stop the background music before loading the challenge scene
        multimedia.stopBackgroundMusic();
        gameWindow.startChallenge();

    }

    private void showInstructions(ActionEvent event){

        // Stop background music before switching to instructions scene
        multimedia.stopBackgroundMusic();
        gameWindow.loadScene(new InstructionsScene(gameWindow));
    }

    private void showMultiPlayerLobby(ActionEvent event){
        // Stop background music before switching to instructions scene
        multimedia.stopBackgroundMusic();
        gameWindow.startMultiplayerLobby();
    }
}
