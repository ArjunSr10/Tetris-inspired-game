package uk.ac.soton.comp1206.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.animation.AnimationTimer;


/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    /**
     * Used to determine whether a block is currently hovered over or not
     */
    private boolean hovered = false;

    /**
     * An AnimationTimer used for fading out a block
     */
    private AnimationTimer fadeOutTimer;
    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);

        // Add mouse entered and exited event handlers, for when the mouse hovers over a block
        setOnMouseEntered(event -> gameBoard.blockEntered(event, this));
        setOnMouseExited(event -> gameBoard.blockExited(event, this));
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        // Check if the block is an empty tile
        if(value.get() == 0) {
            // Check if the block is currently hovered over
            if (hovered){
                // Darkens the colour of the empty block to indicate that it's hovered over
                paintHighlighted(COLOURS[value.get()]);
            }
            else{
                // If the block isn't hovered over than just paint the block empty;
                paintEmpty();
            }
        } else {
            //If the block is not empty, paint with the colour represented by the value
            if (hovered) {
                // Like before darken the colour of the coloured block to indicate that it's hovered over
                paintHighlighted(COLOURS[value.get()]);
            } else {
                // If not just paint it its normal colour
                paintColor(COLOURS[value.get()]);
            }
        }
    }

    /**
     * Paint this canvas with the given colour, with highlighting for hovered blocks.
     * @param colour the colour to paint
     */
    private void paintHighlighted(Paint colour) {
        var gc = getGraphicsContext2D();

        // Clear the canvas
        gc.clearRect(0, 0, width, height);

        if (value.get() == 0) {
            // Darken the transparent black color for highlighting empty blocks
            Color darkerEmptyColor = Color.rgb(0, 0, 0, 0.5);
            gc.setFill(darkerEmptyColor);
            gc.fillRect(0, 0, width, height);
        } else {
            // Darken the original colour for highlighting
            Color darkerColor = colour instanceof Color ? darkenColor((Color) colour) : (Color) colour;
            gc.setFill(darkerColor);
            gc.fillRect(0, 0, width, height);
        }
    }

    /**
     * Darken a color by reducing its RGB values.
     * @param color the color to darken
     * @return the darkened color
     */
    private Color darkenColor(Color color) {
        // Reduce each RGB value by a fixed amount to darken the color
        double r = Math.max(0, color.getRed() - 0.2);
        double g = Math.max(0, color.getGreen() - 0.2);
        double b = Math.max(0, color.getBlue() - 0.2);
        return Color.color(r, g, b);
    }


    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        // Clear canvas
        gc.clearRect(0, 0, width, height);

        // Draw subtle white border
        gc.setStroke(Color.rgb(255, 255, 255, 0.5)); // White with low opacity
        gc.setLineWidth(2.0); // Set border width
        gc.strokeRect(0, 0, width, height);

        // Fill with transparent black
        gc.setFill(Color.rgb(100, 100, 100, 0.3)); // Transparent black with a lighter edge
        gc.fillRect(0, 0, width, height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        // Clear canvas
        gc.clearRect(0, 0, width, height);

        // Paint the original colour
        gc.setFill(colour);
        gc.fillRect(0, 0, width, height);

        // Create a gradient fill with a slightly darker shade
        LinearGradient gradient = new LinearGradient(0, 0, width, height, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(0, 0, 0, 0)), new Stop(0.3, Color.rgb(0, 0, 0, 0.1)),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.3)));

        // Set the composite mode to multiply to darken the original color with the gradient
        gc.setGlobalBlendMode(BlendMode.MULTIPLY);

        // Fill the gradient on top of the original colour
        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);

        // Reset the blend mode to normal
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing its colour
     * @return value determining colour
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Set the color of this block.
     * @param color the color to set
     */
    public void setColor(Color color) {
        // Call paintColor method with the specified color
        paintColor(color);
    }

    /**
     * Sets the aim position's colour
     * @param aimColor the colour to set the aim position
     */
    public void setAimColor(Color aimColor){
        paintColorEmpty(aimColor);
    }

    /**
     * Reset the colour of an empty tile, after the aim position moves off this tile to a different tile, and this tile hasn't had a block placed on it
     */
    public void setEmptyTileColor(){
        paintEmpty();
    }

    /**
     * Paint this canvas with the given colour for an aim tile
     * @param colour the colour to paint
     */
    private void paintColorEmpty(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0, 0, width, height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0, 0, width, height);
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Get the color of this block.
     * @return the color of the block
     */
    public Color getColor() {
        // Get the color based on the value property
        int index = value.get();
        if (index == 0) {
            // Return transparent black if index is 0
            return Color.rgb(100, 100, 100, 0.3);
        } else {
            // Otherwise, return the actual color
            return COLOURS[index];
        }
    }

    /**
     * Paint this canvas with a white circle as indicator
     */
    public void paintIndicator() {
        var gc = getGraphicsContext2D();

        // Clear the canvas
        gc.clearRect(0, 0, width, height);

        paint();

        // Draw a smaller transparent white circle as the indicator
        double circleSize = Math.min(width, height) * 0.5; // Size of the circle relative to block size
        double circleX = (width - circleSize) / 2; // X position of the circle
        double circleY = (height - circleSize) / 2; // Y position of the circle
        double circleAlpha = 0.8; // Transparency of the circle

        gc.setFill(Color.rgb(255, 255, 255, circleAlpha));
        gc.fillOval(circleX, circleY, circleSize, circleSize);
    }

    /**
     * Set whether this block is currently being hovered over.
     * @param hovered true if hovered, false otherwise
     */
    public void setHovered(boolean hovered) {
        // Only update the hovered state if the block does not belong to PieceBoard
        // This is because the PieceBoards display the next piece and the next piece in advance, hence we don't want the user to be able to hover over these blocks
        if (!(gameBoard instanceof PieceBoard)) {
            this.hovered = hovered;
            paint();
        }
    }

    /**
     * Start fading out the block
     */
    public void fadeOut() {
        if (fadeOutTimer == null) {
            fadeOutTimer = new AnimationTimer() {
                private static final double FADE_DURATION = 1000000000.0; // Duration in nanoseconds
                private long startTime = -1;

                @Override
                public void handle(long now) {
                    if (startTime < 0) {
                        startTime = now;
                    }
                    double elapsed = now - startTime;
                    if (elapsed >= FADE_DURATION) {
                        // Fade-out complete, stop the timer
                        stop();
                        // Clear the block by painting it as empty
                        paintEmpty();
                        // Reset start time for future use
                        startTime = -1;
                        logger.info("Block faded out: " + x + ", " + y);
                    } else {
                        // Calculate the opacity based on elapsed time
                        double opacity = 1.0 - (elapsed / FADE_DURATION);
                        // Fill the block with a semi-transparent color
                        paintColor(Color.rgb(100, 100, 100, opacity));
                    }
                }
            };
            fadeOutTimer.start();
        }
    }

    @Override
    public String toString() {
        return "GameBlock{" +
                "x=" + x +
                ", y=" + y +
                ", value=" + value.get() +
                '}';
    }
}
