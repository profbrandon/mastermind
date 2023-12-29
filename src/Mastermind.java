import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class Mastermind extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        final Group root = new Group();
        final Scene scene = new Scene(root, Paint.valueOf(Color.rgb(40, 40, 40).toString()));

        final GameCanvas canvas = new GameCanvas(new GameState(new short[] {1, 1, 1, 1}));
        canvas.addToGroup(root);

        stage.setTitle("MASTERMIND");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}