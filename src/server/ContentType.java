package server;

public enum ContentType {
    TEXT_PLAIN("text/plain; charset=utf-8"),
    TEXT_HTML("text/html; charset=utf-8"),
    TEXT_XML("text/xml; charset=utf-8"),
    TEXT_CSS("text/css; charset=utf-8"),
    TEXT_JAVASCRIPT("text/javascript; charset=utf-8"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png");
    private final String description;
    ContentType(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
