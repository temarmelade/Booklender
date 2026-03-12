package users;

public class Book {
    private String title;
    private String author;
    private String description;
    private boolean isAvailable;
    public Book() {}
    public Book(String title, String author, String description) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.isAvailable = true;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }
    public boolean getIsAvailable() {
        return isAvailable;
    }
}
