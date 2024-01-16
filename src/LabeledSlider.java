import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

/**
 * Slider class that displays a text label as well as the current (integer) value of the slider.
 */
public class LabeledSlider {
    private final Node   node;
    private final Slider slider;

    /**
     * Creates a labeled slider with the label text, min, max, and initial value.
     * 
     * @param text the text for the label
     * @param min the minimum value
     * @param max the maximum value
     * @param value the initial value
     */
    public LabeledSlider(final String text, final int min, final int max, final int value) {
        final Label  label      = new Label(text);
        final Slider slider     = new Slider(min, max, value);
        final HBox   box        = new HBox(10);

        this.slider = slider;
        this.node   = box;

        final Label  valueLabel = new Label(this.getValueText());

        label.setAlignment(Pos.BASELINE_RIGHT);
        valueLabel.setAlignment(Pos.BASELINE_LEFT);
        valueLabel.setMinWidth(40);
        slider.setOnMouseDragged(event -> valueLabel.setText(this.getValueText()));
        slider.setOnKeyPressed(event -> valueLabel.setText(this.getValueText()));
        slider.setBlockIncrement(1.0);

        box.setAlignment(Pos.BASELINE_CENTER);
        box.getChildren().addAll(label, slider, valueLabel);
    }

    /**
     * @return the string representing the current value to be displayed on the value label
     */
    private String getValueText() {
        return "" + (int) (this.slider.getValue());
    }

    /**
     * @return the present (integer) value of the slider
     */
    public int getValue() {
        return (int) this.slider.getValue();
    }

    /**
     * @return the internal javafx {@link javafx.scene.Node} 
     */
    public Node asNode() {
        return this.node;
    }
}
