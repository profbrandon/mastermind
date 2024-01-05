import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Mastermind extends Application {

    private static final Color BACKGROUND_COLOR = Color.rgb(40, 40, 40);

    private final MainMenuScene mainMenu = new MainMenuScene();

    private final MediaPlayer musicPlayer = new MediaPlayer(new Media(getClass().getResource("resources/audio/mastermind.wav").toExternalForm()));

    private Optional<Stage> stage = Optional.empty();

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = Optional.of(stage);

        final Image image;

        try {
            image = new Image(new FileInputStream(new File("resources/images/icon.png")));
        } catch (final Exception e) {
            System.out.println("Exception while loading main menu image: " + e.toString());
            return;
        }

        stage.getIcons().add(image);

        loadMainMenuScene();

        musicPlayer.setVolume(0.5);
        musicPlayer.setAutoPlay(true);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);

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
            scene.getStylesheets().add(getClass().getResource("resources\\styling\\textures.css").toExternalForm());

            final GameCanvas canvas = new GameCanvas(gameState);
            final BorderPane borderPane = new BorderPane();
            final HBox buttonBox = new HBox(5);

            final Button saveButton = new Button("Save");
            saveButton.setOnAction(event -> {
                final List<Byte> temp = canvas.getGameState().toByteList();

                final byte[] data = new byte[temp.size()];

                for (int i = 0; i < temp.size(); ++i) {
                    data[i] = temp.get(i);
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
                final GameState gs = canvas.getGameState();
                canvas.setGameState(new GameState(gs.slots, gs.colors, gs.maxRows, GameState.randomSolution(gs.slots, gs.colors)));
                borderPane.setCenter(canvas.asCanvas());
                canvas.requestFocus();
            });

            final Button mainMenuButton = new Button("Main Menu");
            mainMenuButton.setOnAction(event -> {
                loadMainMenuScene();
            });

            saveButton.setFocusTraversable(false);
            newGameButton.setFocusTraversable(false);
            mainMenuButton.setFocusTraversable(false);

            buttonBox.getChildren().add(newGameButton);
            buttonBox.getChildren().add(saveButton);
            buttonBox.getChildren().add(mainMenuButton);

            borderPane.setCenter(canvas.asCanvas());
            borderPane.setBottom(buttonBox);

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
            this.scene.getStylesheets().add(getClass().getResource("resources\\styling\\textures.css").toExternalForm());

            final ImageView image;

            try {
                image = new ImageView(new Image(new FileInputStream(new File("resources/images/main_menu.png"))));
            } catch (final Exception e) {
                System.out.println("Exception while loading main menu image: " + e.toString());
                return;
            }

            final Button playButton = new Button("Play");
            playButton.setOnAction(event -> {
                loadGameScene(new GameState());
            });

            final Button customGameButton = new Button("Custom Game");
            customGameButton.setOnAction(event -> {
                loadModeMenuScene();
            });

            final Button loadGameButton = new Button("Load game");
            loadGameButton.setOnAction(event -> {
                try {
                    final FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Open Mastermind File");
                    fileChooser.setInitialDirectory(new File("..\\gamedata"));
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*"));
                    
                    final File file = fileChooser.showOpenDialog(stage.get().getOwner());

                    if (file == null) {
                        return;
                    }

                    final FileInputStream fStream = new FileInputStream(file);

                    final byte[] temp = fStream.readAllBytes();

                    fStream.close();

                    final List<Byte> data = new ArrayList<>();

                    for (final byte b : temp) {
                        data.add(b);
                    }

                    loadGameScene(GameState.fromByteList(data));

                } catch (final Exception e) {
                    System.out.println("Exception occurred while attempting to load game file: " + e.toString());
                }
            });

            final VBox vBox = new VBox(10);
            vBox.setAlignment(Pos.CENTER);
            vBox.getChildren().addAll(playButton, customGameButton, loadGameButton);

            final StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(image, vBox);

            root.getChildren().add(stackPane);
        }

        public Scene asScene() {
            return this.scene;
        }
    }
}