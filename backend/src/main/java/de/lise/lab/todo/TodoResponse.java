package de.lise.lab.todo;

public record TodoResponse(Long id, String owner, String title, boolean done) {

    public static TodoResponse from(Todo todo) {
        return new TodoResponse(todo.getId(), todo.getOwner(), todo.getTitle(), todo.isDone());
    }
}
