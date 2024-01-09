import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Optional;

import javafx.scene.image.Image;
import javafx.scene.media.Media;

/**
 * Singleton instance class for loading and retrieving media for the {@link Mastermind} game.
 */
public class MediaLoader {
    private static final MediaLoader INSTANCE = new MediaLoader();

    private Optional<Media> soundtrack = Optional.empty();
    private Optional<URL>   cssUrl     = Optional.empty();

    /**
     * Private constructor for building the singleton instance.
     */
    private MediaLoader() {
        ImageType.ICON.image          = loadFile("images/icon.png").map(Image::new);
        ImageType.MAIN_MENU.image     = loadFile("images/main_menu.png").map(Image::new);
        ImageType.SETTINGS_MENU.image = loadFile("images/settings_menu.png").map(Image::new);

        this.soundtrack = loadResource("audio/mastermind.wav").map(audioURL -> new Media(audioURL.toExternalForm()));
        this.cssUrl     = loadResource("styling/textures.css");
    }

    /**
     * Private method for obtaining a {@link URL} to the requested resource.
     * 
     * @param location the relative path string within the resources folder
     * @return the {@link Optional}<{@link URL}> to the requested resource
     */
    private Optional<URL> loadResource(final String location) {
        try {
            return Optional.of(getClass().getResource("resources/" + location));
        } catch (final Exception e) {
            System.out.println("Exception while loading resource (at " + location + "): " + e.toString());
            return Optional.empty();
        }
    }

    /**
     * Private method for loading a {@link FileInputStream} for the requested file.
     * 
     * @param location the relative path string within the resources folder
     * @return the {@link Optional}<{@link FileInputStream}> for the requested file
     */
    private Optional<FileInputStream> loadFile(final String location) {
        try {
            return Optional.ofNullable(new FileInputStream(new File("resources/" + location)));
        } catch (final Exception e) {
            System.out.println("Exception while loading file (at " + location + "): " + e.toString());
            return Optional.empty();
        }
    }
    
    /**
     * Method for retrieving one of the {@link Image} objects defined in {@link ImageType}.
     * 
     * @param imageType which image is being requested
     * @return the {@link Optional}<{@link Image}>
     */
    public Optional<Image> getImage(final ImageType imageType) {
        return imageType.image;
    }

    /**
     * @return the {@link Optional}<{@link Media}> object containing the soundtrack 
     */
    public Optional<Media> getSoundtrack() {
        return this.soundtrack;
    }

    /**
     * @return the {@link Optional}<{@link URL}> to the global cascading style sheets file
     */
    public Optional<URL> getGlobalCssUrl() {
        return this.cssUrl;
    }

    /**
     * @return the static singleton instance of the {@link MediaLoader} class
     */
    public static MediaLoader getInstance() {
        return INSTANCE;
    }

    /**
     * Inner enumeration for the different kinds of images loaded by the {@link MediaLoader} class.
     */
    public enum ImageType{
        ICON,
        MAIN_MENU,
        SETTINGS_MENU;

        private Optional<Image> image = Optional.empty();
    }

}
