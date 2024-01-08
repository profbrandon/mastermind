import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Optional;

import javafx.scene.image.Image;
import javafx.scene.media.Media;

public class MediaLoader {

    public enum ImageType{
        ICON,
        MAIN_MENU,
        SETTINGS_MENU
    }

    private static final MediaLoader INSTANCE = new MediaLoader();

    private Optional<Image> icon              = Optional.empty();
    private Optional<Image> mainMenuImage     = Optional.empty();
    private Optional<Image> settingsMenuImage = Optional.empty();
    private Optional<Media> soundtrack        = Optional.empty();
    private Optional<URL>   cssUrl            = Optional.empty();

    private MediaLoader() {
        this.icon              = loadFile("images/icon.png").map(Image::new);
        this.mainMenuImage     = loadFile("images/main_menu.png").map(Image::new);
        this.settingsMenuImage = loadFile("images/settings_menu.png").map(Image::new);

        this.soundtrack = loadResource("audio/mastermind.wav").map(audioURL -> new Media(audioURL.toExternalForm()));
        this.cssUrl     = loadResource("styling/textures.css");
    }

    private Optional<URL> loadResource(final String location) {
        try {
            return Optional.of(getClass().getResource("resources/" + location));
        } catch (final Exception e) {
            System.out.println("Exception while loading resource (at " + location + "): " + e.toString());
            return Optional.empty();
        }
    }

    private Optional<FileInputStream> loadFile(final String location) {
        try {
            return Optional.ofNullable(new FileInputStream(new File("resources/" + location)));
        } catch (final Exception e) {
            System.out.println("Exception while loading file (at " + location + "): " + e.toString());
            return Optional.empty();
        }
    }
    
    public Optional<Image> getImage(final ImageType imageType) {
        switch(imageType) {
            case ICON:
                return this.icon;
            case MAIN_MENU:
                return this.mainMenuImage;
            case SETTINGS_MENU:
                return this.settingsMenuImage;
        }

        return Optional.empty();
    }

    public Optional<Media> getSoundtrack() {
        return this.soundtrack;
    }

    public Optional<URL> getGlobalCssUrl() {
        return this.cssUrl;
    }

    public static MediaLoader getInstance() {
        return INSTANCE;
    }
}
