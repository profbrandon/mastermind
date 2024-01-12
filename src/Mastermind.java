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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Mastermind extends Application {

    public static final Color BACKGROUND_COLOR = Color.rgb(40, 40, 40);

    private Optional<MediaPlayer> musicPlayer = Optional.empty();

    private Optional<Stage> stage = Optional.empty();

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = Optional.of(stage);

        MediaLoader.getInstance().getImage(MediaLoader.ImageType.ICON).ifPresent(image -> stage.getIcons().add(image));

        loadMainMenuScene();

        this.musicPlayer = MediaLoader.getInstance().getSoundtrack().map(media -> {
            final MediaPlayer player = new MediaPlayer(media);
            player.setVolume(0.5);
            player.setAutoPlay(true);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            return player;
        });

        stage.setTitle("MASTERMIND");
        stage.setResizable(false);
        stage.show();
    }

    public void loadGameScene(final GameState gameState) {
        this.stage.ifPresent(s -> s.setScene(new GameScene(gameState).asScene()));
    }

    public void loadMainMenuScene() {
        this.stage.ifPresent(s -> s.setScene(new MainMenuScene().asScene()));
    }

    public void loadSettingsScene() {
        this.stage.ifPresent(s -> s.setScene(new SettingsScene().asScene()));
    }

    public void loadCustomGameScene() {
        this.stage.ifPresent(s -> s.setScene(new CustomGameScene().asScene()));
    }

    private class GameScene {
        private final Scene scene;

        public GameScene(final GameState gameState) {
            final Group root = new Group();
            scene = new Scene(root, BACKGROUND_COLOR);
            
            MediaLoader.getInstance().getGlobalCssUrl().ifPresent(url -> this.scene.getStylesheets().add(url.toExternalForm()));

            final GameCanvas canvas     = new GameCanvas(gameState);
            final BorderPane borderPane = new BorderPane();
            final HBox       buttonBox  = new HBox(5);

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
                borderPane.setCenter(canvas.asNode());
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

            borderPane.setCenter(canvas.asNode());
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

            MediaLoader.getInstance().getGlobalCssUrl().ifPresent(url -> this.scene.getStylesheets().add(url.toExternalForm()));

            final Button playButton = new Button("Play");
            playButton.setOnAction(event -> loadGameScene(new GameState()));

            final Button settingsButton = new Button("Settings");
            settingsButton.setOnAction(event -> loadSettingsScene());

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
            vBox.getChildren().addAll(playButton, settingsButton, loadGameButton);

            final StackPane stackPane = new StackPane();
            MediaLoader.getInstance().getImage(MediaLoader.ImageType.MAIN_MENU).ifPresent(
                image -> stackPane.getChildren().add(new ImageView(image)));
            stackPane.getChildren().add(vBox);

            root.getChildren().add(stackPane);
        }

        public Scene asScene() {
            return this.scene;
        }
    }

    private class SettingsScene {

        private final Scene scene;

        public SettingsScene() {
            final Group root = new Group();
            this.scene = new Scene(root, BACKGROUND_COLOR);

            MediaLoader.getInstance().getGlobalCssUrl().ifPresent(url -> this.scene.getStylesheets().add(url.toExternalForm()));

            final Label volumeLabel = new Label("MUSIC");
            volumeLabel.setAlignment(Pos.BASELINE_RIGHT);

            final Slider volumeSlider = new Slider(0, 1, 0.5);
            volumeSlider.setOnMouseDragged(event -> {
                musicPlayer.ifPresent(mediaPlayer -> mediaPlayer.setVolume(volumeSlider.getValue()));
            });

            final HBox volumeBox = new HBox(10);
            volumeBox.setAlignment(Pos.BASELINE_CENTER);
            volumeBox.getChildren().addAll(volumeLabel, volumeSlider);

            final Label soundEffectsLabel = new Label("SFX");
            soundEffectsLabel.setAlignment(Pos.BASELINE_RIGHT);

            final CheckBox soundEffectsCheckBox = new CheckBox();

            final HBox soundEffectsBox = new HBox(10);
            soundEffectsBox.setAlignment(Pos.BASELINE_CENTER);
            soundEffectsBox.getChildren().addAll(soundEffectsLabel, soundEffectsCheckBox);

            final Button backButton = new Button("Back");
            backButton.setOnAction(event -> loadMainMenuScene());

            final VBox settingsBox = new VBox(10);
            settingsBox.setAlignment(Pos.CENTER);
            settingsBox.getChildren().addAll(volumeBox, soundEffectsBox, backButton);

            final StackPane stackPane = new StackPane();
            MediaLoader.getInstance().getImage(MediaLoader.ImageType.SETTINGS_MENU).ifPresent(
                image -> stackPane.getChildren().add(new ImageView(image)));
            stackPane.getChildren().add(settingsBox);

            root.getChildren().add(stackPane);
        }

        public Scene asScene() {
            return this.scene;
        }
    }

    private class CustomGameScene {
        
        private final Scene scene;

        public CustomGameScene() {
            final Group root = new Group();
            this.scene = new Scene(root, BACKGROUND_COLOR);
        }

        public Scene asScene() {
            return this.scene;
        }
    }
}