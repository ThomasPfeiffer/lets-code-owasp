package de.lise.lab.todo;

public record TodoRequest(String title, Boolean done) {

    public boolean doneOrDefault() {
        return Boolean.TRUE.equals(done);
    }
}
