package Routes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.ConfigurationManager;
import server.BasicServer;
import server.Cookie;
import users.Rent;
import users.User;
import utils.Utils;

import javax.xml.crypto.Data;

import static utils.Utils.sha256;


public class BooklenderServer extends BasicServer implements ConfigurationManager {
    private static final Configuration freeMarketConfig = ConfigurationManager.initFreeMarker();
    public BooklenderServer(String host, int port) throws IOException {
        super(host, port);
        handleDefaultPost("/login", this::loginHandlerPost);
        handleDefaultGet("/login", this::loginHandlerGet);
        handleDefaultGet("/home", this::homeHandler);
        handleDefaultPost("/registration", this::registrationHandlerPost);
        handleDefaultGet("/home/books", this::booksHandler);
        handleDefaultGet("/home/staff", this::staffHandler);
        handleDefaultGet("/profile", this::profileHandler);
        handleDefaultGet("/registration", this::registrationHandlerGet);
        handleDefaultGet("/bookInfo", this::bookInfoHandler);
        handleDefaultGet("/welcome", this::welcomeHandler);
        handleDefaultGet("/cookies", this::cookieHandler);
    }
    private void cookieHandler(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        int times = 36;
        data.put("times", times);
        renderTemplate(exchange, "cookie.html", data, freeMarketConfig);
    }
    private void loginHandlerGet(HttpExchange exchange) {
        System.out.println("ya tolko tut");
        renderTemplate(exchange, "login.html", null, freeMarketConfig);
    }
    private void loginHandlerPost(HttpExchange exchange) {
        String raw = getBody(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(raw, "&");
        User user = new User(params.get("email"), sha256(params.get("password")));
        System.out.println("ya tut");
        if (!isRight(user)) {
            Map<String, Object> data = new HashMap<>();
            data.put("error", "Invalid email or password");
            renderTemplate(exchange, "login.html", data, freeMarketConfig);
            return;
        }
        addCookie(user, exchange);
        System.out.println("ya proveril");
        user.setConfirmed();
        redirect303(exchange, "/welcome");
    }
    private void homeHandler(HttpExchange exchange) {
        DataModel dataModel = DataModel.load();
//        String currentEmail = getCookieValue(exchange, "email");
//        User currentUser = findUserByEmail(currentEmail);
//        Map<String, Object> data = new HashMap<>();
//        data.put("currentUser", currentUser);
//        data.put("dataModel", dataModel);
        renderTemplate(exchange, "home.html", dataModel, freeMarketConfig);
    }
    private void booksHandler(HttpExchange exchange) {
        ListBook books = new ListBook();
        renderTemplate(exchange, "books.html", books, freeMarketConfig);
    }
    private void staffHandler(HttpExchange exchange) {
        DataModel dataModel = DataModel.load();
        Map<String, List<Rent>> userRents = groupByUser(dataModel.getUsers(), dataModel.getRents());
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("userRents", userRents);
        renderTemplate(exchange, "staff.html", dataMap, freeMarketConfig);
    }
    private void profileHandler(HttpExchange exchange) {
        DataModel dataModel = DataModel.load();
        String currentEmail = getCookieValue(exchange, "email");
        User currentUser = findUserByEmail(currentEmail);
        if (currentUser == null) {
            redirect303(exchange, "/login");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("user", currentUser);
        data.put("dataModel", dataModel);
        renderTemplate(exchange, "profile.html", data, freeMarketConfig);
    }
    private void welcomeHandler(HttpExchange exchange) {
        String cookieValue = getCookieValue(exchange, "email");
        if (cookieValue == null) {
            redirect303(exchange, "/registration");
            return;
        }

        User currentUser = findUserByEmail(cookieValue);
        if (currentUser == null) {
            redirect303(exchange, "/registration");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("user", currentUser);
        renderTemplate(exchange, "welcome.html", data, freeMarketConfig);
    }
    private void registrationHandlerGet(HttpExchange exchange) {
        renderTemplate(exchange, "registration.html", null, freeMarketConfig);
    }
    private void registrationHandlerPost(HttpExchange exchange) {
        String raw = getBody(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(raw, "&");
        User user = new User(params.get("email"), sha256(params.get("password")));
        addCookie(user, exchange);
        try {
            if (isNewUser(user.getEmail())) {
                saveUser(user);
                redirect303(exchange, "/welcome");
            }
            else {
                respond404(exchange);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void bookInfoHandler(HttpExchange exchange) {
        renderTemplate(exchange, "bookInfo.html", null, freeMarketConfig);
    }
}
