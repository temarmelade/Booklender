package Routes;
import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import config.ConfigurationManager;
import server.BasicServer;
import users.User;
import utils.Utils;


public class BooklenderServer extends BasicServer implements ConfigurationManager {
    private static final Configuration loginConfig = ConfigurationManager.initFreeMarker();
    public BooklenderServer(String host, int port) throws IOException {
        super(host, port);
        handleDefaultGet("/login", this::loginHandler);
        handleDefaultGet("/home", this::homeHandler);
        handleDefaultPost("/registration", this::registrationHandlerPost);
        handleDefaultGet("/home/books", this::booksHandler);
        handleDefaultGet("/home/staff", this::staffHandler);
        handleDefaultGet("/profile", this::profileHandler);
        handleDefaultGet("/registration", this::registrationHandlerGet);
        handleDefaultGet("/bookInfo", this::bookInfoHandler);
        handleDefaultGet("/welcome", this::welcomeHandler);
    }
    private void loginHandler(HttpExchange exchange) {
        renderTemplate(exchange, "login.html", null, loginConfig);
    }
    private void homeHandler(HttpExchange exchange) {
        renderTemplate(exchange, "home.html", null, loginConfig);
    }
    private void booksHandler(HttpExchange exchange) {
        renderTemplate(exchange, "books.html", null, loginConfig);
    }
    private void staffHandler(HttpExchange exchange) {
        renderTemplate(exchange, "staff.html", null, loginConfig);
    }
    private void profileHandler(HttpExchange exchange) {
        renderTemplate(exchange, "profile.html", null, loginConfig);
    }
    private void welcomeHandler(HttpExchange exchange) {renderTemplate(exchange, "welcome.html", null, loginConfig);}
    private void registrationHandlerGet(HttpExchange exchange) {
        renderTemplate(exchange, "registration.html", null, loginConfig);
    }
    private void registrationHandlerPost(HttpExchange exchange) {
        redirect303(exchange, "/welcome");
        String raw = getBody(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(raw, "&");
        User user = new User(params.get("email"), params.get("password"));
        try {
            saveUser(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void bookInfoHandler(HttpExchange exchange) {
        renderTemplate(exchange, "bookInfo.html", null, loginConfig);
    }
}
