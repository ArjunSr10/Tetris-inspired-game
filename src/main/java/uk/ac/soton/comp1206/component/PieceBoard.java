package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The PieceBoard class represents the board where the next game piece, and next
 * game piece in advance are displayed. It extends GameBoard and provides
 * methods to display the upcoming game piece, set the indicator position, paint
 * the indicator, and clear the grid.
 */

public class PieceBoard extends GameBoard{

    /**
     * x - coordinate of indicator position
     */
    private int indicatorX = 1;

    /**
     * y - coordinate of indicator position
     */
    private int indicatorY = 1;

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * Constructs a PieceBoard with the specified width and height.
     * @param width the width of the PieceBoard
     * @param height the height of the PieceBoard
     */
    public PieceBoard(double width, double height){
        super(3, 3, width, height); // Create a 3*3 grid
    }

    /**
     * Displays the given game piece on the PieceBoard.
     * @param gamePiece the game piece to display
     */
    public void displayPiece(GamePiece gamePiece){

        logger.info("Displaying " + gamePiece.toString() + " PieceBoard");
        // Clear the grid before displaying the new piece
        clearGrid();

        int value = gamePiece.getValue();
        int [][]blocks = gamePiece.getBlocks();

        // Calculate the position of the middle square
        int middleX = super.grid.getCols() / 2;
        int middleY = super.grid.getRows() / 2;

        // Loop through the piece matrix and set blocks accordingly
        for (int blockX = 0; blockX < blocks.length; blockX++) {
            for (int blockY = 0; blockY < blocks[blockX].length; blockY++) {
                int gridX = middleX - (blocks.length / 2) + blockX; // Adjust X position relative to middle square
                int gridY = middleY - (blocks[blockX].length / 2) + blockY; // Adjust Y position relative to middle square

                // Check if within bounds
                if (gridX >= 0 && gridX < super.grid.getCols() && gridY >= 0 && gridY < super.grid.getRows()) {
                    int blockValue = blocks[blockX][blockY];
                    if (blockValue > 0) {
                        super.grid.set(gridX, gridY, value);
                    }
                }
            }
        }

        // Set indicator position and paint indicator
        setIndicator(middleX, middleY);
        // Colour the indicator
        paintIndicator();
    }

    /**
     * Sets the position of the indicator.
     * @param x the x-coordinate of the indicator position
     * @param y the y-coordinate of the indicator position
     */
    public void setIndicator(int x, int y){
        this.indicatorX = x;
        this.indicatorY = y;
    }

    /**
     * Paints the indicator on the middle square.
     */
    private void paintIndicator() {
        // Gets middle block, specified by the indicator x and y positions
        var block = (GameBlock) super.getBlock(indicatorX, indicatorY);
        if (block != null) {
            block.paintIndicator();
        }
    }

    /**
     * Clears the grid by setting all blocks to empty.
     */
    public void clearGrid(){
        for (int x = 0; x < super.grid.getCols(); x++) { // Iterates through columns
            for (int y = 0; y < super.grid.getRows(); y++) { // Iterators though rows
                super.grid.set(x, y, 0); // Sets block to be empty
            }
        }
    }
}
