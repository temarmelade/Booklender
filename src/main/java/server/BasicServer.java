package server;

import Routes.ListBook;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import users.Book;
import users.Rent;
import users.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BasicServer {
    private final HttpServer server;
    private final String dataDirectory = "src/resources";
    private final Map<String, RouteHandler> routes = new HashMap<>();

    protected BasicServer(String host, int port) throws IOException {
        server = createServer(host, port);
        handleGeneral();
    }
    private String makeKey(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/") && (path.length() > 1)) {
            path = path.substring(0, path.length() - 1);
        }
        int index = path.lastIndexOf('.');
        String fileName = index != -1 ? path.substring(index) : path;
        return String.format("%s %s", method, fileName);
    }
    protected HttpServer createServer(String host, int port) throws IOException {
        System.out.printf("Creating HTTP server on http://%s:%d/\n", host, port);
        InetSocketAddress address = new InetSocketAddress(host, port);
        return HttpServer.create(address, 50);
    }
    protected void handleGeneral() {
        server.createContext("/", this::handleIncomingRequest);

        handleDefaultGet("/", exchange -> sendFile(exchange, ContentType.TEXT_HTML, makePath("registration.html")));
        handleFile(".css", ContentType.TEXT_CSS);
        handleFile(".js", ContentType.TEXT_JAVASCRIPT);
        handleFile(".html", ContentType.TEXT_HTML);
        handleFile(".jpg", ContentType.IMAGE_JPEG);
        handleFile(".png", ContentType.IMAGE_PNG);
    }
    protected void handleFile(String route, ContentType contentType) {
        handleDefaultGet(route, exchange -> sendFile(exchange, contentType, makePath(exchange)));
    }
    private void handleIncomingRequest(HttpExchange exchange) {
        RouteHandler handler = getRouteHandler().getOrDefault(makeKey(exchange), this::respond404);
        handler.handle(exchange);
    }
    protected void handleDefaultGet(String route, RouteHandler handler) {
        getRouteHandler().put("GET " + route, handler);
    }
    protected void handleDefaultPost(String route, RouteHandler handler) {
        getRouteHandler().put("POST " + route, handler);
    }
    protected final Map<String, RouteHandler> getRouteHandler() {
        return routes;
    }
    protected void redirect303(HttpExchange exchange, String path) {
        try {
            exchange.getResponseHeaders().add("Location", path);
            exchange.sendResponseHeaders(ResponseCodes.REDIRECT.getCode(), 0);
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void sendBytesData(HttpExchange exchange, ResponseCodes responseCode, ContentType contentType, byte[] data) throws IOException {
        try (OutputStream out = exchange.getResponseBody()) {
            exchange.getResponseHeaders().add("Content-Type", contentType.getDescription());
            exchange.sendResponseHeaders(responseCode.getCode(), 0);
            out.write(data);
            out.flush();
        }
    }
    protected void respond404(HttpExchange exchange) {
        System.out.println("404 for: " + exchange.getRequestURI());
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
    private Path makePath(HttpExchange exchange) {
        return makePath(exchange.getRequestURI().getPath());
    }
    private Path makePath(String... path) {
        return Path.of(dataDirectory, path);
    }
    public void start() {
        server.start();
    }
    protected String getBody(HttpExchange exchange) {
        InputStream inputStream = exchange.getRequestBody();
        Charset utf8 = StandardCharsets.UTF_8;
        InputStreamReader isr = new InputStreamReader(inputStream, utf8);
        try (BufferedReader reader = new BufferedReader(isr)) {
            return reader.lines()
                    .collect(Collectors.joining(""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    protected void saveUser(User newUser) throws IOException {
        Path path = Path.of("data/users.json");
        ObjectMapper mapper = new ObjectMapper();

        List<User> users;

        if (Files.exists(path)) {
            users = mapper.readValue(path.toFile(), new TypeReference<List<User>>() {});
        } else {
            users = new ArrayList<>();
        }
        users.add(newUser);
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), users);
    }
    protected boolean isNewUser(String email) throws IOException {
        Path path = Path.of("data/users.json");
        ObjectMapper mapper = new ObjectMapper();

        if (!Files.exists(path)) return false;
        List<User> users = mapper.readValue(path.toFile(), new TypeReference<>() {});
        for (User user : users) {
            if (email.equals(user.getEmail())) return false;
        }
        return true;
    }
    protected boolean isRight(User currentUser) {
        if (currentUser.getEmail() == null || currentUser.getPassword() == null) return false;

        try {
            Path path = Path.of("data/users.json");
            ObjectMapper mapper = new ObjectMapper();

            if (!Files.exists(path)) return false;
            List<User> users = mapper.readValue(path.toFile(), new TypeReference<List<User>>() {
            });
            for (User user : users) {
                if (currentUser.getEmail().equals(user.getEmail()) && (currentUser.getPassword().equals(user.getPassword()))) return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    protected void saveRent(Rent newRent) throws IOException {
        Path path = Path.of("data/rents.json");
        ObjectMapper mapper = new ObjectMapper();
        List<Rent> rents;
        if (Files.exists(path)) {
            rents = mapper.readValue(path.toFile(), new TypeReference<List<Rent>>() {});
        } else {
            rents = new ArrayList<>();
        }
        rents.add(newRent);
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), rents);
    }
    protected User findUserByEmail(String email) {
        if (email == null) return null;

        try {
            Path p = Path.of("data/users.json");
            ObjectMapper mapper = new ObjectMapper();
            List<User> users = mapper.readValue(p.toFile(), new TypeReference<>() {});
            for (User user : users) {
                if (email.equals(user.getEmail())) return user;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    protected String getCookieValue(HttpExchange exchange, String cookieName) {
        String cookieString = exchange.getRequestHeaders()
                .getOrDefault("Cookie", List.of("")).getFirst();
        Map<String, String> cookie = Cookie.parse(cookieString);
        return cookie.get(cookieName);
    }
    protected void addCookie(User user, HttpExchange exchange) {
        Cookie sessionCookie = Cookie.make("email", user.getEmail());
        sessionCookie.setMaxAge(3600);
        sessionCookie.setHttpOnly(true);
        exchange.getResponseHeaders().add("Set-Cookie", sessionCookie.toString());
    }
    protected Map<String, List<Rent>> groupByUser(List<User> users, List<Rent> rents) {
        Map<String, List<Rent>> userRents = new LinkedHashMap<>();
        for (User user : users) {
            userRents.put(user.getEmail(), new ArrayList<>());
        }
        for (Rent rent : rents) {
            String renterEmail = rent.getRenterName();
            if (userRents.containsKey(renterEmail)) {
                userRents.get(renterEmail).add(rent);
            }
        }
        return userRents;
    }
    protected boolean isBookFree(String bookName) {
        List<Rent> currentRents = currentRents();
        for (Rent rent : currentRents) {
            if (bookName.equals(rent.getRenterName())) {
                return false;
            }
        }
        return true;
    }
    protected void borrowBook(String bookName, String renterName) {
        try {
            Path p = Path.of("data/books.json");
            if (!Files.exists(p)) return;

            ObjectMapper mapper = new ObjectMapper();
            List<Book> books = mapper.readValue(p.toFile(), new TypeReference<List<Book>>() {});

            Book targetBook = null;
            for (Book book : books) {
                if (bookName.equals(book.getTitle())) {
                    targetBook = book;
                    break;
                }
            }

            if (targetBook == null) return;
            if (!targetBook.getIsAvailable()) return;

            String startDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Rent newRent = new Rent(bookName, renterName, startDate, null);

            saveRent(newRent);

            targetBook.setAvailable(false);

            mapper.writerWithDefaultPrettyPrinter().writeValue(p.toFile(), books);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void returnBook(String bookName, String renterName) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            Path booksPath = Path.of("data/books.json");
            List<Book> books = mapper.readValue(booksPath.toFile(), new TypeReference<List<Book>>() {});

            Book targetBook = null;
            for (Book book : books) {
                if (book.getTitle().equals(bookName)) {
                    targetBook = book;
                    break;
                }
            }

            if (targetBook == null) return;

            targetBook.setAvailable(true);

            mapper.writerWithDefaultPrettyPrinter().writeValue(booksPath.toFile(), books);


            Path rentsPath = Path.of("data/rents.json");
            List<Rent> rents = mapper.readValue(rentsPath.toFile(), new TypeReference<List<Rent>>() {});

            Rent targetRent = null;
            for (Rent rent : rents) {
                if (rent.getBookName().equals(bookName) &&
                        rent.getRenterName().equals(renterName) &&
                        rent.getEndDate() == null) {

                    targetRent = rent;
                    break;
                }
            }

            if (targetRent == null) return;

            String endDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            targetRent.setEndDate(endDate);

            mapper.writerWithDefaultPrettyPrinter().writeValue(rentsPath.toFile(), rents);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected List<Rent> currentRents() {
        try {
            Path p = Path.of("data/rents.json");
            if (!Files.exists(p)) return null;
            ObjectMapper mapper = new ObjectMapper();
            List<Rent> currentRents = mapper.readValue(p.toFile(), new TypeReference<List<Rent>>() {})
                    .stream()
                    .filter(BasicServer::isCurrentRent)
                    .collect(Collectors.toList());
            return currentRents;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    private static boolean isCurrentRent(Rent rent) {
        if (rent == null) return false;
        return rent.getEndDate() == null;
    }
    protected String getQueryParams(HttpExchange exchange) {
        String queryParams = exchange.getRequestURI().getQuery();
        return Objects.nonNull(queryParams) ? queryParams.trim() : "";
    }
}
