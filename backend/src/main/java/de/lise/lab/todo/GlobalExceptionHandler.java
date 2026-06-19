package de.lise.lab.todo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment env;

    public GlobalExceptionHandler(Environment env) {
        this.env = env;
    }

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(TodoNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(ex, request));
    }

    private Map<String, Object> errorBody(Exception ex, WebRequest request) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));

        Runtime rt = Runtime.getRuntime();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getClass().getName());
        body.put("message", ex.getMessage());
        body.put("cause", ex.getCause() != null ? ex.getCause().toString() : null);
        body.put("path", request.getDescription(false));
        body.put("trace", sw.toString());
        body.put("environment", System.getenv());
        body.put("systemProperties", System.getProperties());
        body.put("runtime", Map.of(
                "javaVersion", System.getProperty("java.version"),
                "javaHome", System.getProperty("java.home"),
                "userDir", System.getProperty("user.dir"),
                "freeMemory", rt.freeMemory(),
                "totalMemory", rt.totalMemory(),
                "availableProcessors", rt.availableProcessors()
        ));
        body.put("activeProfiles", Arrays.asList(env.getActiveProfiles()));
        return body;
    }
}
