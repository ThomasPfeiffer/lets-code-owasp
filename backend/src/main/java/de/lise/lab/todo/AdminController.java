package de.lise.lab.todo;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TodoService service;

    public AdminController(TodoService service) {
        this.service = service;
    }

    @GetMapping("/todos")
    @PreAuthorize("!hasRole('User')")
    public List<TodoResponse> listAll() {
        return service.listAll().stream()
                .map(TodoResponse::from)
                .toList();
    }

    @DeleteMapping("/todos/{id}")
    @PreAuthorize("!hasRole('User')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteAny(id);
        return ResponseEntity.noContent().build();
    }
}
