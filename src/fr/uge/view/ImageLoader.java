package fr.uge.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;

/**
 * Utility class to load and cache images from a specified directory.
 * <p>
 * Images are loaded recursively from the directory, and can be retrieved using
 * their filename (without extension) in a case-insensitive manner.
 * </p>
 */
public class ImageLoader {
    private final Map<String, BufferedImage> images = new HashMap<>();
    private final Path rootPath;

    /**
     * Creates a new ImageLoader and immediately loads all images from the specified directory.
     *
     * @param dirPath The string path to the root directory containing the images.
     * @throws NullPointerException if dirPath is null.
     * @throws RuntimeException if an error occurs while traversing the directory.
     */
    public ImageLoader(String dirPath) {
        Objects.requireNonNull(dirPath);
        this.rootPath = Path.of(dirPath);
        loadAll(); 
    }

    private void loadAll() {
        try (var stream = Files.walk(rootPath)) {
            stream
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String s = p.toString().toLowerCase();
                    return s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".gif");
                }) 
                .forEach(this::safeLoad);
        } catch (IOException e) {
            throw new RuntimeException("Error while walking through image directory: " + rootPath, e);
        }
    }

    private void safeLoad(Path path) {
        try {
            var file = path.toFile();
            var image = ImageIO.read(file);

            if (image == null) {
                return;
            }
            
            String fileName = path.getFileName().toString();
            // La clé est le nom du fichier en MAJUSCULE sans l'extension
            // ex: "ratwolf.gif" -> "RATWOLF"
            String key = fileName.substring(0, fileName.lastIndexOf('.')).toUpperCase();
            
            images.put(key, image);
            
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Retrieves a loaded image by its name.
     * <p>
     * The lookup is case-insensitive and should not include the file extension.
     * Example: {@code get("player")} will match "player.png", "Player.jpg", etc.
     * </p>
     *
     * @param name The name of the image to retrieve.
     * @return The corresponding BufferedImage, or null if the image was not found.
     * @throws NullPointerException if name is null.
     */
    public BufferedImage get(String name) {
        Objects.requireNonNull(name);
        return images.get(name.toUpperCase());
    }
}