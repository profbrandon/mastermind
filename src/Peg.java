import java.util.Optional;

import javafx.scene.paint.Color;

public class Peg {
    public static enum PegColor {
        RED(Color.RED, 'r'),
        AQUA(Color.AQUA, 'a'),
        GREEN(Color.GREEN, 'g'),
        WHITE(Color.WHITE, 'w'),
        BROWN(Color.BROWN, 'b'),
        YELLOW(Color.YELLOW, 'y'),
        PURPLE(Color.PURPLE, 'p'),
        ORANGE(Color.ORANGE, 'o');

        public final Color color;
        public final char key;

        private PegColor(final Color color, final char key) {
            this.color = color;
            this.key = key;
        }

        public short toShort() {
            return (short) (this.ordinal() + 1);
        }
    }

    private PegColor color;

    public Peg(final PegColor color) {
        this.color = color;
    }

    public void setColor(final PegColor newColor) {
        this.color = newColor;
    }

    public PegColor getColor() {
        return this.color;
    }

    public short toShort() {
        return this.color.toShort();
    }

    public static Optional<Peg> fromShort(final short pegShort) {
        if (pegShort >= 1 && pegShort < 9) {
            return Optional.of(new Peg(PegColor.values()[pegShort - 1]));
        } else {
            return Optional.empty();
        }
    }
}
