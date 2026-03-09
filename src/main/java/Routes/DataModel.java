package Routes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import users.Rent;
import users.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DataModel {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<Rent> rents;
    private final List<User> users;


    private DataModel(List<Rent> rents, List<User> users) {
        this.rents = rents;
        this.users = users;
    }
    public List<Rent> getRents() {
        return rents;
    }

    public List<User> getUsers() {
        return users;
    }

    public static DataModel load() {
        try {
            Path usersPath = Path.of("data/users.json");
            Path rentsPath = Path.of("data/rents.json");

            requireExists(usersPath);
            requireExists(rentsPath);

            List<User> users = readList(usersPath, new TypeReference<List<User>>() {});
            List<Rent> rents = readList(rentsPath, new TypeReference<List<Rent>>() {});

            return new DataModel(rents, users);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return new DataModel(new ArrayList<>(), new ArrayList<>());
    }

    private static void requireExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + path.toAbsolutePath());
        }
    }

    private static <T> List<T> readList(Path path, TypeReference<List<T>> type) throws IOException {
        return MAPPER.readValue(path.toFile(), type);
    }
}