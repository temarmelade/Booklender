package server;

import com.sun.net.httpserver.HttpExchange;

@FunctionalInterface
public interface RouteHandler {
    void handle(HttpExchange exchange);
}
