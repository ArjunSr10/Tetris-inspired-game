package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Next Piece listener is used to handle the event when a new piece is generated
 */
public interface NextPieceListener {

    /**
     * Called when a new piece is generated.
     * @param currentPiece the current game piece
     * @param nextPiece the next game piece
     */
    void onNextPiece(GamePiece currentPiece, GamePiece nextPiece);
}
