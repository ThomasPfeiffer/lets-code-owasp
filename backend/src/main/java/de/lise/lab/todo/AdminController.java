package de.lise.lab.todo;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TodoService service;

    public AdminController(TodoService service) {
        this.service = service;
    }

    /**
     * Liefert alle Todos aller Benutzer. Nur Rolle {@code admin}.
     */
    @GetMapping("/todos")
    @PreAuthorize("hasRole('admin')")
    public List<TodoResponse> listAll() {
        return service.listAll().stream()
                .map(TodoResponse::from)
                .toList();
    }
}
