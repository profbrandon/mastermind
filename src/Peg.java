import java.util.Optional;

import javafx.scene.paint.Color;

public class Peg {
    public static enum PegColor {
        RED(Color.RED, 'r'),
        AQUA(Color.AQUA, 'a'),
        GREEN(Color.rgb(30, 240, 0), 'g'),
        WHITE(Color.WHITE, 'w'),
        BROWN(Color.rgb(90, 21, 8), 'b'),
        YELLOW(Color.YELLOW, 'y'),
        PURPLE(Color.PURPLE, 'p'),
        ORANGE(Color.rgb(240, 100, 0), 'o');

        public final Color color;
        public final char key;

        private PegColor(final Color color, final char key) {
            this.color = color;
            this.key = key;
        }

        public short toShort() {
            return (short) (this.ordinal() + 1);
        }

        public static Optional<PegColor> fromCharacter(final char c, final int available) {
            for (int i = 0; i < available; ++i) {
                final PegColor color = PegColor.values()[i];
                if (color.key == c) return Optional.of(color);
            }
            return Optional.empty();
        }
    
        public static PegColor randomPegColor(final int available) {
            return PegColor.values()[(int) (Math.random() * Math.min(available, 8))];
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

    @Override
    public String toString() {
        return this.color.key + "";
    }
}
