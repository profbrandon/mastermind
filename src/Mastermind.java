import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Mastermind extends Application {

    private static final Color BACKGROUND_COLOR = Color.rgb(40, 40, 40);

    private final MainMenuScene mainMenu = new MainMenuScene();

    private Optional<Stage> stage = Optional.empty();

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = Optional.of(stage);

        loadGameScene(new GameState());

        stage.setTitle("MASTERMIND");
        stage.setResizable(false);
        stage.show();
    }

    public void loadGameScene(final GameState gameState) {
        this.stage.ifPresent(s -> s.setScene(new GameScene(gameState).asScene()));
    }

    public void loadMainMenuScene() {
        this.stage.ifPresent(s -> s.setScene(this.mainMenu.asScene()));
    }

    public void loadModeMenuScene() {
        //this.stage.ifPresent(s -> s.setScene(this.modeMenu));
    }

    private class GameScene {
    
        private final Scene scene;

        public GameScene(final GameState gameState) {
            final Group root = new Group();

            scene = new Scene(root, BACKGROUND_COLOR);
            final GameCanvas canvas = new GameCanvas(gameState);
            final BorderPane borderPane = new BorderPane();
            final Group buttonGroup = new Group();

            final Button saveButton = new Button("Save");
            saveButton.setOnAction(event -> {
                final List<Byte> temp = gameState.toByteList();

                final byte[] data = new byte[temp.size()];

                for (int i = 0; i < temp.size(); ++i) {
                    data[i] = temp.remove(0);
                }

                try {
                    final FileOutputStream fStream = new FileOutputStream(new File("../gamedata/mastermind_" + ((long) (Math.random() * Long.MAX_VALUE))));
                    fStream.write(data);
                    fStream.close();
                } catch (final Exception e) {
                    System.out.println("Failed to write save file: " + e.toString());
                }

                canvas.requestFocus();
            });

            final Button newGameButton = new Button("New Game");
            newGameButton.setOnAction(event -> {
                canvas.setGameState(new GameState(gameState.slots, gameState.colors, gameState.maxRows, GameState.randomSolution(gameState.slots, gameState.colors)));
                borderPane.setCenter(canvas.asCanvas());
                canvas.requestFocus();
            });

            saveButton.setFocusTraversable(false);
            newGameButton.setFocusTraversable(false);

            buttonGroup.getChildren().add(newGameButton);
            //buttonGroup.getChildren().add(saveButton);

            borderPane.setCenter(canvas.asCanvas());
            borderPane.setBottom(buttonGroup);

            root.getChildren().add(borderPane);
            canvas.requestFocus();
        }

        public Scene asScene() {
            return this.scene;
        }
    }

    private class MainMenuScene {

        private final Scene scene;
        
        public MainMenuScene() {
            final Group root = new Group();
            this.scene = new Scene(root, BACKGROUND_COLOR);
        }

        public Scene asScene() {
            return this.scene;
        }
    }
}