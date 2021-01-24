package dev.morling.demos.quarkus;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class TodoForm {

    public @FormParam("title") @PartType(MediaType.TEXT_PLAIN) String title;
    public @FormParam("completed") @PartType(MediaType.TEXT_PLAIN) String completed;
    public @FormParam("priority") @PartType(MediaType.TEXT_PLAIN) int priority;

    public Todo convertIntoTodo() {
        Todo todo = new Todo();
        todo.title = title;
        todo.completed = "on".equals(completed);
        todo.priority = priority;
        return todo;
    }

    public Todo updateTodo(Todo toUpdate) {
        toUpdate.title = title;
        toUpdate.completed = "on".equals(completed);
        toUpdate.priority = priority;
        return toUpdate;
    }
}
