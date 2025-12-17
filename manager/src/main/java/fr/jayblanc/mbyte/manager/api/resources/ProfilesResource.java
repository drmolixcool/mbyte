/*
 * Copyright (C) 2025 Jerome Blanchard <jayblanc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package fr.jayblanc.mbyte.manager.api.resources;

import fr.jayblanc.mbyte.manager.auth.AuthenticationService;
import fr.jayblanc.mbyte.manager.auth.entity.Profile;
import fr.jayblanc.mbyte.manager.core.CoreService;
import fr.jayblanc.mbyte.manager.core.CoreServiceException;
import fr.jayblanc.mbyte.manager.core.StoreNotFoundException;
import fr.jayblanc.mbyte.manager.core.entity.Store;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("profiles")
public class ProfilesResource {

    private static final Logger LOGGER = Logger.getLogger(ProfilesResource.class.getName());

    @Inject AuthenticationService auth;
    @Inject CoreService core;
    @Inject Template profile;

    @GET
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public Response profiles(@Context UriInfo uriInfo) {
        LOGGER.log(Level.INFO, "GET /api/profiles");
        String connectedId = auth.getConnectedIdentifier();
        URI root = uriInfo.getRequestUriBuilder().path(connectedId).build();
        return Response.seeOther(root).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response profile(@PathParam("id") String id, @Context UriInfo uriInfo) {
        LOGGER.log(Level.INFO, "GET /api/profiles/" + id);
        Profile profile = auth.getConnectedProfile();
        return Response.ok(profile).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance profileView(@PathParam("id") String id, @Context UriInfo uriInfo) {
        LOGGER.log(Level.INFO, "GET /api/profiles/" + id + " (html)");
        TemplateInstance view = profile.data("profile", auth.getConnectedProfile());
        try {
            view = view.data("store", core.getConnectedUserStore());
        } catch (StoreNotFoundException | CoreServiceException e ) {
            //
        }
        return view;
    }

    @GET
    @Path("{id}/store")
    @Produces(MediaType.APPLICATION_JSON)
    public Store getProfileStore(@PathParam("id") String id) throws StoreNotFoundException, CoreServiceException {
        LOGGER.log(Level.INFO, "GET /api/profiles/" + id + "/store");
        return core.getConnectedUserStore();
    }

    @GET
    @Path("{id}/store/log")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProfileStoreLog(@PathParam("id") String id) throws StoreNotFoundException, CoreServiceException {
        LOGGER.log(Level.INFO, "GET /api/profiles/" + id + "/store/log");
        return core.getConnectedUserStore().getLog();
    }

    @POST
    @Path("{id}/store")
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createProfileStore(@PathParam("id") String id, MultivaluedMap<String, String> form, @Context UriInfo uriInfo) {
        LOGGER.log(Level.INFO, "POST /api/profiles/" + id + "/store");
        Store store = core.createStore(form.getFirst("name"));
        LOGGER.log(Level.INFO, "Store created with id: " + store.getId());
        String connectedId = auth.getConnectedIdentifier();
        URI root = uriInfo.getBaseUriBuilder().path(ProfilesResource.class).path(connectedId).build();
        return Response.seeOther(root).build();
    }


}
