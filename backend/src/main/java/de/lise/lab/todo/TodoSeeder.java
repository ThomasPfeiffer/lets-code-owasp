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
            todos.save(new Todo("alice", "Alice: OWASP Top 10 lesen", false));
            todos.save(new Todo("alice", "Alice: Lab-Umgebung starten", true));
            todos.save(new Todo("alice", "Alice: Access-Token im localStorage anschauen", false));

            todos.save(new Todo("bob", "Bob: Geheimes Projekt vorbereiten", false));
            todos.save(new Todo("bob", "Bob: Kaffee kaufen", true));

            todos.save(new Todo("admin", "Admin: Realm-Export pruefen", false));
        };
    }
}
