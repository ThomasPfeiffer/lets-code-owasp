package de.lise.lab.todo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class TodoSeeder {

    @Bean
    ApplicationRunner seedTodos(TodoRepository todos) {
        return args -> {
            if (todos.count() > 0) {
                return;
            }
            todos.save(new Todo("alice", "OWASP Top 10 lesen", false));
            todos.save(new Todo("alice", "Lab-Umgebung starten", true));

            todos.save(new Todo("bob", "Geheimes Projekt vorbereiten", false));
            todos.save(new Todo("bob", "Kaffee kaufen", true));

            todos.save(new Todo("admin", "Ventilator kaufen", false));
        };
    }
}
