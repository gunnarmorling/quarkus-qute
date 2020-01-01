package dev.morling.demos.quarkus;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@Path("/todo")
public class TodoResource {

    @Inject
    Template error;

    @Inject
    Template todo;

    @Inject
    Template todos;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listTodos(@QueryParam("filter") String filter) {
        Sort sort = Sort.ascending("completed")
            .and("priority", Direction.Descending)
            .and("title", Direction.Ascending);

        List<Todo> results;
        if (filter != null && !filter.isEmpty()) {
            results = Todo.find("LOWER(title) LIKE LOWER(?1)", sort, "%" + filter + "%").list();
        }
        else {
            results = Todo.findAll(sort).list();
        }

        return todos.data("todos", results)
            .data("filter", filter)
            .data("filtered", filter != null && !filter.isEmpty());
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Path("/new")
    public Response addTodo(
        @FormParam("title") String title,
        @FormParam("completed") String completed,
        @FormParam("priority") int priority) {

        Todo todo = new Todo();
        todo.title = title;
        todo.completed = "on".equals(completed);
        todo.priority = priority;

        todo.persist();
        
        return Response.status(301)
            .location(URI.create("/todo"))
            .build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id}/edit")
    public TemplateInstance updateForm(@PathParam("id") long id) {
        Todo loaded = Todo.findById(id);

        if (loaded == null) {
            return error.data("error", "Todo with id " + id + " does not exist.");
        }

        return todo.data("todo", loaded)
            .data("update", true);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Path("/{id}/edit")
    public Object updateTodo(
        @PathParam("id") long id,
        @FormParam("title") String title,
        @FormParam("completed") String completed,
        @FormParam("priority") int priority) {

        Todo loaded = Todo.findById(id);

        if (loaded == null) {
            return error.data("error", "Todo with id " + id + " has been deleted after loading this form.");
        }

        loaded.title = title;
        loaded.completed = "on".equals(completed);
        loaded.priority = priority;

        return Response.status(301)
            .location(URI.create("/todo"))
            .build();
    }

    @POST
    @Transactional
    @Path("/{id}/delete")
    public Response deleteTodo(@PathParam("id") long id) {
        Todo.delete("id", id);

        return Response.status(301)
            .location(URI.create("/todo"))
            .build();
    }
}
