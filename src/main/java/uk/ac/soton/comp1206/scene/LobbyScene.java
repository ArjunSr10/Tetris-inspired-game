package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The LobbyScene class represents the scene where players can interact in a lobby before starting a game.
 * It provides features like chatting, creating new channels, and joining existing channels.
 */
public class LobbyScene extends BaseScene{

    /**
     * The communicator for handling communication with the server
     */
    private Communicator communicator;

    /**
     * The game object associated with the lobby scene
     */
    protected Game game;

    /**
     * Container for displaying the list of available channels
     */
    private VBox channelListContainer;

    /**
     * List of users in the current channel
     */
    private List<String> channelUsers = new ArrayList<>();

    /**
     * Text area for displaying chat messages
     */
    private TextArea chatArea;

    /**
     * Text field for entering chat messages
     */
    private TextField messageField;

    /**
     * Button for starting the game
     */
    private Button startGameButton;

    /**
     * Button for leaving the current channel
     */
    private Button leaveChannelButton;

    /**
     * Button for creating a new channel
     */
    private Button newChannelButton;

    /**
     * Button for quitting the lobby
     */
    private Button quitButton;

    /**
     * Container for message input components (message field and send buttons).
     */
    private HBox messageBox;
    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Constructs a new LobbyScene object
     * @param gameWindow the GameWindow object associated with this class
     */
    public LobbyScene(GameWindow gameWindow){
        super(gameWindow);
        logger.info("Creating Lobby Scene");
        this.communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");
    }

    /**
     * Builds UI components of the lobby scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        root.getStylesheets().add(getClass().getResource("/style/game.css").toExternalForm()); // Link the CSS file

        BorderPane mainPane = new BorderPane(); // Use BorderPane as the main layout container
        mainPane.setMaxWidth(gameWindow.getWidth());
        mainPane.setMaxHeight(gameWindow.getHeight());
        mainPane.getStyleClass().add("menu-background"); // Apply CSS class

        // Create a VBox to hold the main UI components
        VBox mainContainer = new VBox(10);
        mainContainer.setAlignment(Pos.TOP_LEFT);

        // Build lobby scene UI, including the VBox to contain the available channels, as well as initializing the chat area and message field
        channelListContainer = new VBox(10);
        chatArea = new TextArea();
        chatArea.setStyle("-fx-control-inner-background: rgba(0, 0, 0, 1); -fx-text-fill: white;");
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        messageField = new TextField();
        messageField.setPromptText("Type your message here");

        messageField.setPrefWidth(100);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());

        messageBox = new HBox(5);
        messageBox.getChildren().addAll(messageField, sendButton);

        // Start timer to request current channels from the server
        startChannelRequestTimer();
        
        // Create a VBox to hold the buttons
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.TOP_CENTER); // Align buttons to top left
        
        // Add a button to start a new channel
        newChannelButton = new Button("Start New Channel");
        newChannelButton.setStyle("-fx-font-family: 'Orbitron'; -fx-font-size: 20;");
        newChannelButton.setTextFill(Color.BLACK);
        newChannelButton.setOnAction(e -> startNewChannel());
        buttonContainer.getChildren().add(newChannelButton);

        // Add a button to quit or exit the lobby
        quitButton = new Button("Quit");
        quitButton.setOnAction(e -> quit());
        quitButton.setStyle("-fx-font-family: 'Orbitron'; -fx-background-color: red; -fx-font-size: 20;");
        quitButton.setTextFill(Color.BLACK);
        buttonContainer.getChildren().add(quitButton);
        
        // Add button container to main container
        mainContainer.getChildren().add(buttonContainer);
        mainPane.setTop(mainContainer);

        // Add a button to start the game (if the user is the host)
        startGameButton = new Button("Start Game");

        // Add a button to leave the channel
        leaveChannelButton = new Button("Leave Channel");
        leaveChannelButton.setOnAction(e -> leaveChannel());

        VBox channelsContainer = new VBox(10);
        channelsContainer.setAlignment(Pos.TOP_LEFT);

        // Create a label for the Channels title
        Label channelsTitle = new Label("Channels:");
        channelsTitle.setStyle("-fx-font-size: 20px; -fx-font-family: 'Orbitron';"); // Apply font size and family directly
        channelsTitle.setTextFill(Color.WHITE);

        channelsContainer.getChildren().addAll(channelsTitle, channelListContainer);
        mainPane.setLeft(channelsContainer);

        root.getChildren().add(mainPane);
    }

    /**
     * Initializes the lobby scene
     */
    @Override
    public void initialise() {
        // Add listener to the communicator to handle incoming messages
        communicator.addListener(this::handleIncomingMessage);

        // Set a key pressed event listener on the root node
        root.setOnKeyPressed(event -> {
            // Check if the pressed key is the Escape key
            if (event.getCode() == KeyCode.ESCAPE) {
                logger.info("Escape key pressed, exit lobby scene");
                // Call the quit() method if the Escape key is pressed
                quit();
            }
        });
    }

    /**
     * Starts a repeating timer to request channels every few seconds
     */
    private void startChannelRequestTimer() {
        // Start a repeating timer to request channels every few seconds
        // For example, here it requests channels every 5 seconds
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), e -> {
                    listChannels();
                    channelListContainer.getChildren().clear();
                })
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Handles incoming messages received from the server
     * @param message the message received from the server
     */
    private void handleIncomingMessage(String message) {
        // Extract the command from the message
        StringBuilder commandBuilder = new StringBuilder();
        int i = 0;
        while (i < message.length() && Character.isUpperCase(message.charAt(i))) {
            commandBuilder.append(message.charAt(i));
            i++;
        }
        String command = commandBuilder.toString();

        // Parse the incoming message and split around the new line
        String[] parts = message.split("\n");

        switch (command) {
            case "CHANNELS":
                logger.info("Updating channels list");
                // Update UI to show available channels
                Platform.runLater(() -> updateChannelList(parts));
                break;
            case "JOIN":
                logger.info("Joining channel");
                // Handle join message
                Platform.runLater(() -> handleJoinMessage(message));
                break;
            case "ERROR":
                logger.info("An error has occurred, an action was not possible");
                // Handle error message
                Platform.runLater(() -> handleErrorMessage(message));
                break;
            case "MSG":
                logger.info("Chat area updated");
                // Update UI to show received message
                String[] msgParts = message.split(":", 2);
                String playerName = msgParts[0].substring(4); // Extract player name
                String msgContent = msgParts[1]; // Extract message content
                Platform.runLater(() -> updateChatArea(playerName + ": " + msgContent)); // Update chat area
                break;
            case "NICK":
                logger.info("Name changed");
                // Handle nick message
                Platform.runLater(() -> handleNicknameChange(message));
                break;
            case "PARTED":
                logger.info("Left channel");
                // Handle parted message
                Platform.runLater(() -> handlePartedMessage());
                break;
            case "USERS":
                // Update UI to show channel users
                Platform.runLater(() -> updateChannelUsers(parts));
                break;
        }
    }

    /**
     * Updates the list of available channels in the UI
     * @param channels the list of available channels received from the server
     */
    private void updateChannelList(String[] channels) {

        // Iterate through each channel in the channels array
        for (String channel : channels) {
            // Modify the channel string to remove the "CHANNELS " prefix
            String modifiedStringChannel = channel.replace("CHANNELS ", "");

            // Create a new button for the modified channel
            Button channelButton = new Button(modifiedStringChannel);
            // Set the style of the button
            channelButton.setStyle("-fx-font-family: 'Orbitron'; -fx-background-color: black; -fx-font-size: 20;");
            channelButton.setTextFill(Color.WHITE);
            // Set an action event for the button to join the channel when clicked
            channelButton.setOnAction(e -> joinChannel(modifiedStringChannel));

            // Create a VBox to hold the channel button
            VBox buttonContainer = new VBox(10);
            buttonContainer.setAlignment(Pos.TOP_LEFT);
            buttonContainer.getChildren().add(channelButton);

            // Add the button container to the channel list container
            Platform.runLater(() -> {
                channelListContainer.getChildren().add(buttonContainer);
            });
        }
    }

    /**
     * Updates the list of channel users in the UI
     * @param users the list of channel users received from the server
     */
    private void updateChannelUsers(String[] users) {
        // Clear existing channel users list
        channelUsers.clear();

        HBox usersContainer = new HBox(5); // Set spacing between user labels

        // Display users in the channel
        for (int i = 0; i < users.length; i++) {
            String user = users[i];
            // Modify the user string to remove the "USERS " prefix
            String modifiedStringUser = user.replace("USERS ", "");
            // Create a label for the modified user string
            Label userLabel = new Label(modifiedStringUser);
            // Set the style for the user label
            userLabel.setStyle("-fx-font-size: 20px; -fx-font-family: 'Orbitron';"); // Apply font size and family directly
            userLabel.setTextFill(Color.WHITE);
            // Add the user label to the users container
            usersContainer.getChildren().add(userLabel);
        }

        // Get the main layout container (BorderPane) from the root
        BorderPane mainPane = (BorderPane) root.getChildren().get(0);
        // Set the users container to the right side of the main layout container
        mainPane.setRight(usersContainer);
    }

    /**
     * Updates the chat area with a new message
     * @param message the message to be displayed in the chat area
     */
    private void updateChatArea(String message) {
        // Append the received message to the chat area
        chatArea.appendText(message + "\n");
    }

    /**
     * Handles the PARTED command received from the server.
     * Clears the chat area and hides UI components related to channel interactions
     */
    private void handlePartedMessage() {
        // Clear the chat area
        chatArea.clear();
        // Hide the chat area, message box, start game button, and leave channel button
        chatArea.setVisible(false);
        messageBox.setVisible(false);
        startGameButton.setVisible(false);
        leaveChannelButton.setVisible(false);
        // Clear the channel users list
        channelUsers.clear();

        // Get the main layout container (BorderPane) from the root
        BorderPane mainPane = (BorderPane) root.getChildren().get(0);
        // Remove the users list from the right side of the main layout container
        mainPane.setRight(null);
    }

    /**
     * Handles the JOIN command received from the server.
     * Displays UI components related to channel interaction.
     * @param message the JOIN message received from the server
     */
    private void handleJoinMessage(String message) {
        chatArea.setVisible(true);
        messageBox.setVisible(true);
        startGameButton.setVisible(true);
        leaveChannelButton.setVisible(true);
        // Create container to hold chat components and buttons
        VBox chatAndButtonsContainer = new VBox(10);

        // Add chat components and buttons to the container
        chatAndButtonsContainer.getChildren().addAll(chatArea, messageBox, startGameButton, leaveChannelButton);

        // Set alignment of container to top right
        chatAndButtonsContainer.setAlignment(Pos.TOP_RIGHT);

        // Add container to main layout container (BorderPane)
        BorderPane mainPane = (BorderPane) root.getChildren().get(0);
        mainPane.setBottom(chatAndButtonsContainer);
    }

    /**
     * Handles error messages received from the server.
     * Displays an error alert with the error message.
     * @param message the error message received from the server
     */
    private void handleErrorMessage(String message) {
        // extract the error message from the received message
        String errorMessage = message.replace("ERROR", "");
        // Create an alert to display the error message
        Alert alert = new Alert(Alert.AlertType.ERROR);
        // Set the title of the new alert
        alert.setTitle("Error");
        // Set the header text of the alert to null (no header text)
        alert.setHeaderText(null); // No header text
        // Set the content text of the alert to the error message
        alert.setContentText(errorMessage);
        // Show the alert and wait for the user to acknowledge it
        alert.showAndWait();
    }

    /**
     * Handles nickname change messages received from the server
     * Updates the chat area to reflect the nickname change.
     * @param message the nickname change message received from the server.
     */
    private void handleNicknameChange(String message) {
        if (message.contains(":")){
            // Extract the old and new nicknames from the message
            String[] parts = message.split(":");
            String oldName = parts[0].substring(5); // Skip "NICK " prefix
            String newName = parts[1];

            // Update the UI to reflect the nickname change
            chatArea.appendText(oldName + " has changed their nickname to " + newName + "\n");
        }
    }

    /**
     * Sends a message to the server to leave the current channel.
     */
    private void leaveChannel() {
        communicator.send("PART");
    }

    /**
     * Sends a message to the server to request the list of all current channels.
     */
    private void listChannels(){
        communicator.send("LIST");
    }

    /**
     * Sends a message to the server to join the specified channel
     * @param channel the name of the channel to join
     */
    private void joinChannel(String channel) {
        communicator.send("JOIN " + channel);

    }

    /**
     * Sends a message to the server to quit or disconnect from the lobby
     */
    private void quit() {
        communicator.send("QUIT");
        // Load the menu scene, as the user as quit the lobby scene
        gameWindow.loadScene(new MenuScene(gameWindow));
    }

    /**
     * Sends a message to the server.
     * If the message starts with "/nick". it changes the user's nickname.
     * Otherwise, it sends the message to the current channel.
     */
    private void sendMessage() {
        // Get the message from the message field
        String message = messageField.getText();

        // Check if the message starts with "/nick"
        if (message.startsWith("/nick ")) {
            // Extract the new nickname
            String newNickname = message.substring(6);
            // Send the nick command to the server
            communicator.send("NICK " + newNickname);
        } else {
            // Send the message to the channel
            communicator.send("MSG " + message);
        }

        // Clear the message field after sending
        messageField.clear();
    }

    /**
     * Prompts the user to enter a name for the new channel and sends the request
     */
    private void startNewChannel() {
        // Prompt the user for a channel name
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Start New Channel");
        dialog.setHeaderText("Enter the name for the new channel:");
        dialog.setContentText("Channel Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(channelName -> {
            // Submit the new channel name using the communicator
            communicator.send("CREATE " + channelName);
        });
    }

    /**
     * Sends a message to the server to request the list of users in the current channel.
     */
    private void requestChannelUsers(){
        // Send a message to request the list of users in the channel
        communicator.send("USERS");
    }
}
