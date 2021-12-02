package net.celestialdata.plexbot.apis;

import net.celestialdata.plexbot.db.daos.UserDao;
import net.celestialdata.plexbot.db.entities.User;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "Users", description = "Manage the users of this bot thought this endpoint")
@RolesAllowed("admin")
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserDao userDao;

    @GET
    public List<User> get() {
        return userDao.listALl();
    }

    @GET
    @Path("/{id}")
    public User get(@PathParam("id") int id) {
        return userDao.get(id);
    }

    @POST
    public int create(User user) {
        return userDao.create(user.username, user.password, user.role).id;
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") int id) {
        userDao.delete(id);
    }
}