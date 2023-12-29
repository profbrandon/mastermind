import java.util.Optional;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Peg {
    public static enum PegColor {
        RED(Color.RED),
        AQUA(Color.AQUA),
        GREEN(Color.GREEN),
        WHITE(Color.WHITE),
        BROWN(Color.BROWN),
        YELLOW(Color.YELLOW),
        PURPLE(Color.PURPLE),
        ORANGE(Color.ORANGE);

        public final Color color;

        private PegColor(final Color color) {
            this.color = color;
        }

        public short toShort() {
            return (short) (this.ordinal() + 1);
        }
    }

    public static double PEG_RADIUS = 10;
    public static double PEG_DIAM   = 2 * PEG_RADIUS;

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

    public void render(final GraphicsContext context, final double x, final double y) {
        context.setFill(this.color.color);
        context.fillOval(x - PEG_RADIUS, y - PEG_RADIUS, PEG_DIAM, PEG_DIAM);
        context.fill();
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
