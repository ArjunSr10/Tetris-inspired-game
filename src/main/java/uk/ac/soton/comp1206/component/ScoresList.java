package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.scene.control.ListCell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The ScoresList class represents a custom ListView to display scores in the Scores Scene.
 * It provides methods to set scores, reveal the list with fading animation, and customize individual score items.
 */
public class ScoresList extends ListView<Pair<String, Integer>>{

    /**
     * The List property containing pairs of player names and their corresponding scores.
     */
    private final ListProperty<Pair<String, Integer>> scores = new SimpleListProperty<>(FXCollections.observableArrayList());

    /**
     * Array of colours used to set each entry in the scores list a different colour, based on the order of the colours in this array
     */
    private static final String[] colors = {"pink", "red", "orange", "yellow", "lime", "lightgreen", "green", "darkgreen", "aqua", "lightblue"};

    /**
     * Constructs a ScoresList.
     */
    public ScoresList(){
        // Set the items of the ListView to the scores list property
        setItems(scores);
        setCellFactory(param -> new ScoreCell());
        // Set a custom cell factory to customize the appearance of each score item
        getStyleClass().add("scores-list"); // Add custom CSS class
    }

    /**
     * Sets the scores to be displayed in the list.
     * @param scores the list of scores to set
     */
    public void setScores(ObservableList<Pair<String, Integer>> scores) {
        // Sort the scores
        List<Pair<String, Integer>> sortedScores = new ArrayList<>(scores);
        Collections.sort(sortedScores, (s1, s2) -> s2.getValue().compareTo(s1.getValue()));

        // Take the top 10 scores
        ObservableList<Pair<String, Integer>> top10Scores = FXCollections.observableArrayList(sortedScores.subList(0, Math.min(sortedScores.size(), 10)));
        setItems(top10Scores);
    }

    /**
     * Gets the list of scores.
     * @return the list of scores
     */
    public ObservableList<Pair<String, Integer>> getScores() {
        return scores.get();
    }

    /**
     * Gets the property containing the list of scores.
     * @return the list property of scores
     */
    public ListProperty<Pair<String, Integer>> scoresProperty() {
        return scores;
    }

    /**
     * Reveals the list with a fading animation.
     */
    public void reveal(){
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), this);
        fadeTransition.setFromValue(0); // Scores not visible at first
        fadeTransition.setToValue(1); // Scores now visible as they fade in
        fadeTransition.play();
    }

    /**
     * Custom cell class to apply styles to individual score items
     */
    private static class ScoreCell extends ListCell<Pair<String, Integer>> {
        /**
         * Updates the appearance of the ListCell with the given item.
         *
         * @param item the item to be displayed in the ListCell
         * @param empty a boolean indicating whether the cell is empty or not
         */
        @Override
        protected void updateItem(Pair<String, Integer> item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                // If the cell is empty or the item is null, clear the cell content
                setText(null);
                setGraphic(null);
                setStyle(""); // Reset style
            } else {
                setText(item.getKey() + ":" + item.getValue()); // Customize text format
                // Calculate the index based on the position of the cell in the ListView
                int index = getIndex() % colors.length;
                setTextFill(javafx.scene.paint.Color.web(colors[index])); // Set text color dynamically
            }
        }
    }



}
