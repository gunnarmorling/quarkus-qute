package dev.morling.demos.quarkus;

import java.net.URI;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateExtension;
import io.quarkus.qute.TemplateInstance;

@Path("/todo")
public class TodoResource {

    @Inject
    Template todo;

    @Inject
    Template todos;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listTodos() {
        return todos.data("todos", Todo.findAll().list());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/new")
    public TemplateInstance hello() {
        return todo.instance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Path("/new")
    public Response addTodo(
        @FormParam("title") String title,
        @FormParam("completed") String completed,
		@FormParam("order") int order) {

        Todo todo = new Todo();
        todo.title = title;
        todo.completed = "on".equals(completed);
        todo.order = order;

        todo.persist();
        
        return Response.status(301)
            .location(URI.create("/todo"))
			//.entity("addUser is called, name : " + name + ", age : " + age)
			.build();

    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id}/edit")
    public TemplateInstance updateForm(@PathParam("id") long id) {
        Todo loaded = Todo.findById(id);
        return todo.data("todo", loaded)
            .data("update", true);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Path("/{id}/edit")
    public Response updateTodo(
        @PathParam("id") long id,
        @FormParam("title") String title,
        @FormParam("completed") String completed,
		@FormParam("order") int order) {

        Todo loaded = Todo.findById(id);
System.out.println(completed + "++++");
        loaded.title = title;
        loaded.completed = "on".equals(completed);
        loaded.order = order;

        return Response.status(301)
            .location(URI.create("/todo"))
			//.entity("addUser is called, name : " + name + ", age : " + age)
			.build();

    }

    @POST
    @Transactional
    @Path("/{id}/delete")
    public Response deleteTodo(@PathParam("id") long id) {
        Todo.delete("id", id);

        return Response.status(301)
            .location(URI.create("/todo"))
			//.entity("addUser is called, name : " + name + ", age : " + age)
			.build();

    }
}
