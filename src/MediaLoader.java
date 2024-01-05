import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

import javafx.scene.image.Image;
import javafx.scene.media.Media;

public class MediaLoader {

    private static final MediaLoader INSTANCE = new MediaLoader();

    private Optional<Image> icon          = Optional.empty();
    private Optional<Image> mainMenuImage = Optional.empty();
    private Optional<Media> soundtrack    = Optional.empty();

    private MediaLoader() {
        try {
            icon = Optional.of(new Image(new FileInputStream(new File("resources/images/icon.png"))));
        } catch (final Exception e) {
            System.out.println("Exception while loading icon image: " + e.toString());
        }

        try {
            mainMenuImage = Optional.of(new Image(new FileInputStream(new File("resources/images/main_menu.png"))));
        } catch (final Exception e) {
            System.out.println("Exception while loading main menu image: " + e.toString());
            return;
        }

        try {
            soundtrack = Optional.of(new Media(getClass().getResource("resources/audio/mastermind.wav").toExternalForm()));
        } catch (final Exception e) {
            System.out.println("Exception while loading soundtrack: " + e.toString());
        }
    }
    
    public Optional<Image> getIcon() {
        return this.icon;
    }

    public Optional<Image> getMainMenuImage() {
        return this.mainMenuImage;
    }

    public Optional<Media> getSoundtrack() {
        return this.soundtrack;
    }

    public static MediaLoader getInstance() {
        return INSTANCE;
    }
}
