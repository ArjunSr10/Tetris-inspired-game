package uk.ac.soton.comp1206.event;

/**
 * The Game Loop listener is used to handle the event when a game timer reaches zero
 */
public interface GameLoopListener {

    /**
     * Handle the event when the timer reaches zero
     */
    void onGameLoop();
}
