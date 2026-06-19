package de.lise.lab.todo;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TodoResponse> listOwn(@AuthenticationPrincipal Jwt jwt) {
        return service.listOwn(owner(jwt)).stream()
                .map(TodoResponse::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TodoResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody TodoRequest request) {
        Todo created = service.create(owner(jwt), request);
        return ResponseEntity
                .created(URI.create("/api/todos/" + created.getId()))
                .body(TodoResponse.from(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public TodoResponse getOne(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return TodoResponse.from(service.getOwned(owner(jwt), id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public TodoResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody TodoRequest request) {
        return TodoResponse.from(service.update(owner(jwt), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.delete(owner(jwt), id);
        return ResponseEntity.noContent().build();
    }

    private static String owner(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        return jwt.getClaimAsString("preferred_username");
    }
}
