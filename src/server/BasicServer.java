package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class BasicServer {
    private final HttpServer server;
    private final String dataDirectory = "data";
    private final Map<String, RouteHandler> routes = new HashMap<>();

    protected BasicServer(String host, int port) throws IOException {
        server = createServer(host, port);
        handleGeneral();
    }
    private String makeKey(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        int index = path.lastIndexOf('.');
        String fileName = index != -1 ? path.substring(index) : path;
        return String.format("%s %s", method, fileName);
    }
    protected HttpServer createServer(String host, int port) throws IOException {
        System.out.printf("Creating HTTP server on http://%s:/%d\n", host, port);
        InetSocketAddress address = new InetSocketAddress(host, port);
        return HttpServer.create(address, 50);
    }
    protected void handleGeneral() {
        server.createContext("/", this::handleIncomingRequest);

        handleDefault("/", exchange -> sendFile(exchange, ContentType.TEXT_HTML, makePath("registration.html")));

        handleFile(".css", ContentType.TEXT_CSS);
        handleFile(".js", ContentType.TEXT_JAVASCRIPT);
        handleFile(".html", ContentType.TEXT_HTML);
        handleFile(".jpg", ContentType.IMAGE_JPEG);
        handleFile(".png", ContentType.IMAGE_PNG);
    }
    protected void handleFile(String route, ContentType contentType) {
        handleDefault(route, exchange -> sendFile(exchange, contentType, makePath(route)));
    }
    private void handleIncomingRequest(HttpExchange exchange) {
        RouteHandler handler = getRouteHandler().getOrDefault(makeKey(exchange), this::respond404);
        handler.handle(exchange);
    }
    protected void handleDefault(String route, RouteHandler handler) {
        getRouteHandler().put("GET " + route, handler);
    }
    protected final Map<String, RouteHandler> getRouteHandler() {
        return routes;
    }
    private void sendBytesData(HttpExchange exchange, ResponseCodes responseCode, ContentType contentType, byte[] data) throws IOException {
        try (OutputStream out = exchange.getResponseBody()) {
            exchange.getResponseHeaders().add("Content-Type", contentType.getDescription());
            exchange.sendResponseHeaders(responseCode.getCode(), 0);
            out.write(data);
            out.flush();
        }
    }
    private void respond404(HttpExchange exchange) {
        try {
            byte[] data = "404 Not Found".getBytes();
            sendBytesData(exchange, ResponseCodes.NOT_FOUND, ContentType.TEXT_PLAIN, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void sendFile(HttpExchange exchange, ContentType contentType, Path pathToFile) {
        try {
            if (Files.notExists(pathToFile)) {
                respond404(exchange);
                return;
            }
            byte[] data = Files.readAllBytes(pathToFile);
            sendBytesData(exchange, ResponseCodes.OK, contentType, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Path makePath(String... path) {
        return Path.of(dataDirectory, path);
    }
    public void start() {
        server.start();
    }
}
