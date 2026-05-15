package termchat.persistence;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonStorageService {
    private final Gson gson;
    private final Path dataDirectory;

    public JsonStorageService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataDirectory = Path.of("data");
        createDataDirectory();
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            System.out.println("Could not create data directory: " + e.getMessage());
        }
    }

    public void save(String fileName, Object data) {
        Path filePath = dataDirectory.resolve(fileName);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.out.println("Could not save " + fileName + ": " + e.getMessage());
        }
    }

    public <T> T load(String fileName, Type type) {
        Path filePath = dataDirectory.resolve(fileName);
        if (!Files.exists(filePath)) return null;

        try (FileReader reader = new FileReader(filePath.toFile())) {
            return gson.fromJson(reader, type);
        } catch (IOException | JsonSyntaxException e) {
            System.out.println("Could not load " + fileName + ": " + e.getMessage());
            return null;
        }
    }
}
