package dev.morling.demos.quarkus;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.CheckedTemplate;

@Path("/todo")
public class TodoResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance error(String message);
        public static native TemplateInstance todo(Todo todo, List<Integer> priorities, boolean update);
        public static native TemplateInstance todos(List<Todo> todos, long totalCount, List<Integer> priorities, String filter, boolean filtered);
    }

    final List<Integer> priorities = IntStream.range(1, 6).boxed().collect(Collectors.toList());

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listTodos(@QueryParam("filter") String filter) {
        return Templates.todos(find(filter), Todo.count(), priorities, filter, filter != null && !filter.isEmpty());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<Todo> listTodosJson(@QueryParam("filter") String filter) {
        return find(filter);
    }

    private List<Todo> find(String filter) {
        Sort sort = Sort.ascending("completed")
            .and("priority", Direction.Descending)
            .and("title", Direction.Ascending);

        if (filter != null && !filter.isEmpty()) {
            return Todo.find("LOWER(title) LIKE LOWER(?1)", sort, "%" + filter + "%").list();
        }
        else {
            return Todo.findAll(sort).list();
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Path("/new")
    public Response addTodo(@MultipartForm TodoForm todoForm) {
        Todo todo = todoForm.convertIntoTodo();
        todo.persist();

        return Response.status(Status.SEE_OTHER)
            .location(URI.create("/todo"))
            .build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id}/edit")
    public TemplateInstance updateForm(@PathParam("id") long id) {
        Todo loaded = Todo.findById(id);

        if (loaded == null) {
            return Templates.error("Todo with id " + id + " does not exist.");
        }

        return Templates.todo(loaded, priorities, true);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Path("/{id}/edit")
    public Object updateTodo(
        @PathParam("id") long id,
        @MultipartForm TodoForm todoForm) {

        Todo loaded = Todo.findById(id);

        if (loaded == null) {
            return Templates.error("Todo with id " + id + " has been deleted after loading this form.");
        }

        loaded = todoForm.updateTodo(loaded);

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
