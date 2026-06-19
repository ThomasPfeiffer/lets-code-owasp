package de.lise.lab.todo;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodoService {

    private final TodoRepository todos;

    public TodoService(TodoRepository todos) {
        this.todos = todos;
    }

    @Transactional(readOnly = true)
    public List<Todo> listOwn(String owner) {
        return todos.findByOwner(owner);
    }

        @Transactional(readOnly = true)
    public List<Todo> listAll() {
        return todos.findAll();
    }

    @Transactional(readOnly = true)
    public Todo getOwned(String owner, Long id) {
        return loadOwned(owner, id);
    }

    @Transactional
    public Todo create(String owner, TodoRequest request) {
        Todo todo = new Todo(owner, request.title(), request.doneOrDefault());
        return todos.save(todo);
    }

    @Transactional
    public Todo update(String owner, Long id, TodoRequest request) {
        Todo todo = loadOwned(owner, id);
        if (request.title() != null) {
            todo.setTitle(request.title());
        }
        if (request.done() != null) {
            todo.setDone(request.done());
        }
        return todos.save(todo);
    }

    @Transactional
    public void delete(String owner, Long id) {
        Todo todo = loadOwned(owner, id);
        todos.delete(todo);
    }

    @Transactional
    public void deleteAny(Long id) {
        todos.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        todos.deleteById(id);
    }

    private Todo loadOwned(String owner, Long id) {
        return todos.findById(id)
                .filter(todo -> todo.getOwner().equals(owner))
                .orElseThrow(() -> new TodoNotFoundException(id));
    }
}
