import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class GameCanvas {
    private static final double SLOT_WIDTH     = 50.0;
    private static final double RESPONSE_WIDTH = 100.0;
    private static final double BORDER_WIDTH   = 3.0;

    private Canvas canvas;
    private GameState gameState;
    
    public GameCanvas(final GameState gameState) {
        this.setGameState(gameState);
    }

    public void setGameState(final GameState gameState) {
        this.gameState = gameState;
        this.canvas = new Canvas(this.getWidth(this.gameState.slots), this.getHeight(this.gameState.maxRows));
        this.render();
    }

    private void render() {
        final GraphicsContext context = this.canvas.getGraphicsContext2D();

    }

    private double getWidth(final int slots) {
        return SLOT_WIDTH * slots + RESPONSE_WIDTH + BORDER_WIDTH;
    }

    private double getHeight(final int maxRows) {
        return SLOT_WIDTH * maxRows + BORDER_WIDTH;
    }
}
