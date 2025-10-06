package uk.ac.soton.comp1206.event;

/**
 * The Game listener is used to link the Game and the GameBoard
 */
public interface GameListener {
    /**
     * Handle an event when the game updates
     */
    void onGameUpdate();
}
