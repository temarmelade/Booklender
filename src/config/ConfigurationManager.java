package config;

import java.io.*;

import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public interface ConfigurationManager {
    static Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

            cfg.setDirectoryForTemplateLoading(new File("src/data"));

            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    default void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel, Configuration cfg) {
        try {
            Template temp = cfg.getTemplate(templateFile);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
                temp.process(dataModel, writer);
                writer.flush();

                byte[] data = out.toByteArray();
                try (OutputStream os = exchange.getResponseBody()) {
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                    exchange.sendResponseHeaders(200, 0);
                    os.write(data);
                    os.flush();
                }
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

    }
}
