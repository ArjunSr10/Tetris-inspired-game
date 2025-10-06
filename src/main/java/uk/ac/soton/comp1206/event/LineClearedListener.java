package uk.ac.soton.comp1206.event;
import java.util.Set;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * Interface for listening to line cleared events.
 */
public interface LineClearedListener {
    /**
     * Called when lines are cleared in the game.
     * @param coordinates the coordinates of the blocks that were cleared
     */
    void onLinesCleared(Set<GameBlockCoordinate> coordinates);
}
