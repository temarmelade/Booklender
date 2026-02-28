package server;
import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;

import java.io.IOException;

import config.ConfigurationManager;

public class BooklenderServer extends BasicServer implements ConfigurationManager {
    private static final Configuration loginConfig = ConfigurationManager.initFreeMarker();
    public BooklenderServer(String host, int port) throws IOException {
        super(host, port);
        handleDefault("/login", this::homeHandler);
    }
    private void homeHandler(HttpExchange exchange) {
        renderTemplate(exchange, "login.html", null, loginConfig);
    }
}
