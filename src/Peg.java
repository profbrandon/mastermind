import java.util.Optional;

import javafx.scene.paint.Color;

/**
 * Class to represent an colored "peg" for the {@link Mastermind} game. There are eight possible colors,
 * of which default play only uses the first six.
 */
public class Peg {
    private PegColor color;

    /**
     * Constructs a {@link Peg} using the given {@link PegColor}.
     * 
     * @param color the color of the desired {@link Peg}
     */
    public Peg(final PegColor color) {
        this.color = color;
    }

    /**
     * Method to change the {@link PegColor} of this {@link Peg}.
     * 
     * @param newColor the new {@link PegColor}
     */
    public void setColor(final PegColor newColor) {
        this.color = newColor;
    }

    /**
     * @return the current {@link PegColor}
     */
    public PegColor getColor() {
        return this.color;
    }

    /**
     * @return a single byte representing this {@link Peg}
     */
    public byte toByte() {
        return this.color.toByte();
    }

    /**
     * Overridden {@link Object#toString()} method. Creates a single-character {@link String} representing this {@link Peg}.
     * 
     * @return a single-character {@link String}
     */
    @Override
    public String toString() {
        return this.color.key + "";
    }

    /**
     * Static method to read an {@link Optional}<{@link Peg}> from the given byte. Returns an
     * empty {@link Optional} if reading fails.
     * 
     * @param pegByte the byte to read
     * @return the {@link Optional}<{@link Peg}>
     */
    public static Optional<Peg> fromByte(final byte pegByte) {
        if (pegByte >= 1 && pegByte < 9) {
            return Optional.of(new Peg(PegColor.values()[pegByte - 1]));
        } else {
            return Optional.empty();
        }
    }

    /**
     * An enumeration representing the possible colors for a {@link Peg} object.
     */
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

        /**
         * Private constructor for building a {@link PegColor} with the given {@link Color} and character value.
         * 
         * @param color the color of the peg
         * @param key the character representing this peg
         */
        private PegColor(final Color color, final char key) {
            this.color = color;
            this.key = key;
        }

        /**
         * @return a single byte containing the color information of this peg
         */
        public byte toByte() {
            return (byte) (this.ordinal() + 1);
        }

        /**
         * Static method that converts a single character into a {@link Optional}<{@link PegColor}>.
         * Creates an empty {@link Optional} if the color is unvailable.
         * 
         * @param c the character to convert into a {@link PegColor}
         * @param available how many colors are available
         * @return the {@link Optional}<{@link PegColor}> object version of the supplied character
         */
        public static Optional<PegColor> fromCharacter(final char c, final int available) {
            for (int i = 0; i < available; ++i) {
                final PegColor color = PegColor.values()[i];
                if (color.key == c) return Optional.of(color);
            }
            return Optional.empty();
        }
    
        /**
         * @param available how many colors are available
         * @return a random {@link PegColor} object
         */
        public static PegColor randomPegColor(final int available) {
            return PegColor.values()[(int) (Math.random() * Math.min(available, 8))];
        }
    }
}
