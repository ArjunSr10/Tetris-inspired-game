package uk.ac.soton.comp1206.media;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.MenuScene;

/**
 * Manages multimedia playback including audio files background music
 */
public class Multimedia {

    /**
     * Player for audio files
     */
    private MediaPlayer audioPlayer;

    /**
     * Player for background music
     */
    private MediaPlayer musicPlayer;

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Constructs a new Multimedia object.
     * Initializes audioPlayer and musicPlayer with null.
     */
    public Multimedia() {
        // Initialize the audio and music players with null
        audioPlayer = null;
        musicPlayer = null;
    }

    /**
     * Plays an audio file
     * @param filePath the path to the audio file to be played
     */
    public void playAudio(String filePath) {
        Media audioMedia = new Media(getClass().getResource(filePath).toExternalForm());
        // Dispose of the previous audio player if it exists
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer.dispose();
        }
        audioPlayer = new MediaPlayer(audioMedia);
        audioPlayer.play();
    }

    /**
     * Plays background music
     * @param filePath the path to the music file to be played
     */
    public void playBackgroundMusic(String filePath) {
        Media musicMedia = new Media(getClass().getResource(filePath).toExternalForm());
        // Dispose of the previous music player if it exists
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
        }
        musicPlayer = new MediaPlayer(musicMedia);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop music indefinitely
        logger.info("Playing background music");
        musicPlayer.play();
    }

    /**
     * Plays the sound for placing a game piece.
     */
    public void playPlacePieceSound() {
        playAudio("/sounds/place.wav");
    }

    /**
     * Plays the sound for rotating a game piece.
     */
    public void playRotatePieceSound() {
        playAudio("/sounds/rotate.wav");
        logger.info("Playing rotate piece sound");
    }

    /**
     * Plays the sound for swapping a game piece.
     */
    public void playSwapPieceSound() {

        playAudio("/sounds/rotate.wav");
        logger.info("Playing swap piece sound");
    }

    /**
     * Plays the sound for losing a life.
     */
    public void playLoseLifeSound(){
        playAudio("/sounds/lifelose.wav");
        logger.info("Playing lose life sound");
    }

    /**
     * Plays the sound for clearing lines.
     */
    public void playClearLineSound(){
        playAudio("/sounds/clear.wav");
        logger.info("Playing clear line sound");
    }

    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
}

