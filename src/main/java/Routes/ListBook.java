package Routes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import users.Book;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ListBook {
    private List<Book> books;

    public ListBook() {
        this.books = loadBooks();
    }

    public List<Book> getBooks() {
        return books;
    }

    private List<Book> loadBooks() {
        try {
            Path p = Path.of("data/books.json");
            ObjectMapper mapper = new ObjectMapper();

            if (!Files.exists(p)) {
                return new ArrayList<>();
            }

            return mapper.readValue(p.toFile(), new TypeReference<List<Book>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
