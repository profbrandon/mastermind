import java.util.Optional;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
    private static final Color  SELECTED_COLOR  = Color.rgb(100, 100, 100);

    private Canvas canvas;
    private GameState gameState;

    private double width  = 0;
    private double height = 0;

    private Optional<Pair<Integer, Integer>> selected = Optional.empty();
    
    public GameCanvas(final GameState gameState) {
        this.setGameState(gameState);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(final GameState gameState) {
        this.gameState = gameState;
        this.canvas = new Canvas(this.getWidth(this.gameState.slots), this.getHeight(this.gameState.maxRows));
        this.canvas.getGraphicsContext2D().setFont(Font.font("Consolas", 18));

        final EventHandler<MouseEvent> mouseMovedHandler  = event -> {
            final double x = event.getX();
            final double y = event.getY();

            if (x > RESPONSE_WIDTH && x < width && y >= 0 && y < height) {
                final int slotColumn = (int) ((x - RESPONSE_WIDTH) / SLOT_WIDTH);
                final int slotRow = (int) (y / SLOT_WIDTH);

                final Pair<Integer, Integer> newSelection = new Pair<>(slotRow, slotColumn);

                if (selected.map(pair -> !pair.equals(newSelection)).orElse(true)) {
                    Platform.runLater(() -> render());   
                    selected = Optional.of(newSelection);
                }
            } else {
                selected = Optional.empty();
                Platform.runLater(() -> render());
            }
        };
        final EventHandler<MouseEvent> mouseExitedHandler = event -> {
            selected = Optional.empty();
            Platform.runLater(() -> render());
        };
        final EventHandler<KeyEvent>   keyTypedHandler    = event -> {
            if (selected.isPresent()) {
                final char key = event.getCharacter().charAt(0);
                final Pair<Integer, Integer> pair = selected.get();

                if (key == 'x') {
                    gameState.clearPeg(pair.getKey(), pair.getValue());
                    Platform.runLater(() -> render());
                    return;
                }

                final Optional<Peg.PegColor> pegColor = Peg.PegColor.fromCharacter(key, gameState.colors);

                if (pegColor.isPresent()) {
                    if (gameState.setPeg(pair.getKey(), pair.getValue(), new Peg(pegColor.get()))) {
                        gameState.nextRowIfPossible();
                        Platform.runLater(() -> render());
                    }
                }
            }
        };
        final EventHandler<KeyEvent>   keyPressedHandler  = event -> {
            if (event.getCode().isArrowKey()) {
                if (selected.isEmpty()) {
                    selected = Optional.of(new Pair<>(0, 0));
                } else {
                    final Pair<Integer, Integer> pair = selected.get();
                    final int i = pair.getKey();
                    final int j = pair.getValue();

                    switch (event.getCode()) {
                        case DOWN:
                            selected = Optional.of(new Pair<>(Math.min(i + 1, gameState.maxRows - 1), j));
                            break;
                        case UP:
                            selected = Optional.of(new Pair<>(Math.max(i - 1, 0), j));
                            break;
                        case LEFT:
                            selected = Optional.of(new Pair<>(i, (j + gameState.slots - 1) % gameState.slots));
                            break;
                        case RIGHT:
                            selected = Optional.of(new Pair<>(i, (j + 1) % gameState.slots));
                            break;
                        default:
                    }
                }

                Platform.runLater(() -> render());
            }
        };

        this.canvas.addEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedHandler);
        this.canvas.addEventFilter(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        this.canvas.addEventFilter(KeyEvent.KEY_TYPED, keyTypedHandler);
        this.canvas.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);

        this.render();
    }

    public Canvas asCanvas() {
        return this.canvas;
    }

    public void processKeyTypedEvent(final KeyEvent event) {
        this.canvas.onKeyTypedProperty().get().handle(event);
    }

    public void requestFocus() {
        this.canvas.requestFocus();
    }

    private void render() {
        final GraphicsContext context = this.canvas.getGraphicsContext2D();

        context.setFill(INLAY_COLOR);
        context.fillRect(BORDER_WIDTH, BORDER_WIDTH, RESPONSE_WIDTH - BORDER_WIDTH_2, this.height - BORDER_WIDTH_2);
        context.setFill(Color.BLACK);
        context.fillRect(this.width - SLOT_WIDTH + BORDER_WIDTH, BORDER_WIDTH, this.width - BORDER_WIDTH_2, this.height - BORDER_WIDTH_2);

        for (int i = 0; i < this.gameState.colors; ++i) {
            final Peg.PegColor color = Peg.PegColor.values()[i];
            context.setFill(color.color);
            context.fillText(color.key + "", this.width - SLOT_WIDTH / 2, SLOT_WIDTH * i + SLOT_WIDTH / 2 + 5);
        }

        for (int i = 0; i < this.gameState.maxRows; ++i) {
            final Pair<Integer, Integer> test = this.gameState.testRow(i);

            if (this.gameState.isRowFull(i)) {
                context.setFill(Color.RED);
                context.fillText(test.getKey().toString(), BORDER_WIDTH + SLOT_WIDTH / 2, BORDER_WIDTH + SLOT_WIDTH / 2 + SLOT_WIDTH * i + 5);

                context.setFill(Color.WHITE);
                context.fillText(test.getValue().toString(), BORDER_WIDTH + SLOT_WIDTH / 2 + 50, BORDER_WIDTH + SLOT_WIDTH / 2 + SLOT_WIDTH * i + 5);
            }

            for (int j = 0; j < this.gameState.slots; ++j) {
                final Optional<Peg> peg = this.gameState.pegAt(i, j);

                final double leftX  = RESPONSE_WIDTH + SLOT_WIDTH * j;
                final double upperY = SLOT_WIDTH * i;

                final double centerX = leftX + SLOT_WIDTH / 2;
                final double centerY = upperY + SLOT_WIDTH / 2;

                if (selected.isPresent() && selected.get().equals(new Pair<>(i, j))) {
                    context.setFill(SELECTED_COLOR);
                } else {
                    context.setFill(INLAY_COLOR);
                }
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
        this.width = SLOT_WIDTH * (slots + 1) + RESPONSE_WIDTH;
        return this.width;
    }

    private double getHeight(final int maxRows) {
        this.height = SLOT_WIDTH * maxRows;
        return this.height;
    }
}
