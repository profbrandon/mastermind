import java.util.Optional;

import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class GameCanvas {
    private static final double SLOT_WIDTH      = 50.0;
    private static final double RESPONSE_WIDTH  = 100.0;

    private static final double BORDER_WIDTH    = 2.0;
    private static final double BORDER_WIDTH_2  = BORDER_WIDTH * 2;
    
    private static final double PEG_HOLE_RADIUS = SLOT_WIDTH / 6;
    private static final double PEG_HOLE_DIAM   = 2 * PEG_HOLE_RADIUS;

    private static final double PEG_RADIUS      = SLOT_WIDTH / 4;
    private static final double PEG_DIAM        = 2 * PEG_RADIUS;

    private static final Color  INLAY_COLOR     = Color.rgb(30, 30, 30);

    private Canvas canvas;
    private GameState gameState;

    private double width  = 0;
    private double height = 0;

    private Optional<Pair<Integer, Integer>> selected = Optional.empty();
    
    public GameCanvas(final GameState gameState) {
        this.setGameState(gameState);
    }

    public void setGameState(final GameState gameState) {
        this.gameState = gameState;
        this.canvas = new Canvas(this.getWidth(this.gameState.slots), this.getHeight(this.gameState.maxRows));

        this.render();
    }

    public void addToGroup(final Group group) {
        group.getChildren().add(this.canvas);
    }

    private void render() {
        final GraphicsContext context = this.canvas.getGraphicsContext2D();

        context.setFill(INLAY_COLOR);
        context.fillRect(BORDER_WIDTH, BORDER_WIDTH, RESPONSE_WIDTH - BORDER_WIDTH_2, this.height - BORDER_WIDTH_2);

        for (int i = 0; i < this.gameState.maxRows; ++i) {
            final Pair<Integer, Integer> test = this.gameState.testRow(i);

            if (test.getKey().intValue() != 0) {
                context.setStroke(Color.RED);
                context.strokeText(test.getKey().toString(), BORDER_WIDTH + SLOT_WIDTH / 2 + 50, BORDER_WIDTH + SLOT_WIDTH / 2 + SLOT_WIDTH * i);
            }

            if (test.getValue().intValue() != 0) {
                context.setStroke(Color.WHITE);
                context.strokeText(test.getValue().toString(), BORDER_WIDTH + SLOT_WIDTH / 2 + 50, BORDER_WIDTH + SLOT_WIDTH / 2 + SLOT_WIDTH * i);
            }

            for (int j = 0; j < this.gameState.slots; ++j) {
                final Optional<Peg> peg = this.gameState.pegAt(i, j);

                final double leftX  = RESPONSE_WIDTH + SLOT_WIDTH * j;
                final double upperY = SLOT_WIDTH * i;

                final double centerX = leftX + SLOT_WIDTH / 2;
                final double centerY = upperY + SLOT_WIDTH / 2;

                context.setFill(INLAY_COLOR);
                context.fillRect(BORDER_WIDTH + leftX, BORDER_WIDTH + upperY, SLOT_WIDTH - BORDER_WIDTH_2, SLOT_WIDTH - BORDER_WIDTH_2);

                if (peg.isPresent()) {
                    context.setFill(peg.get().getColor().color);
                    context.fillOval(centerX - PEG_RADIUS, centerY - PEG_RADIUS, PEG_DIAM, PEG_DIAM);
                    context.fill();
                } else {
                    context.setFill(Color.BLACK);
                    context.fillOval(centerX - PEG_HOLE_RADIUS, centerY - PEG_HOLE_RADIUS, PEG_HOLE_DIAM, PEG_HOLE_DIAM);
                    context.fill();
                }
            }
        }
    }

    private double getWidth(final int slots) {
        this.width = SLOT_WIDTH * slots + RESPONSE_WIDTH;
        return this.width;
    }

    private double getHeight(final int maxRows) {
        this.height = SLOT_WIDTH * maxRows;
        return this.height;
    }
}
