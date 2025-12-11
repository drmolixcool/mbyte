package fr.jayblanc.mbyte.manager.api.resources;

import fr.jayblanc.mbyte.manager.auth.AuthenticationService;
import fr.jayblanc.mbyte.manager.store.StoreManager;
import fr.jayblanc.mbyte.manager.store.StoreProviderException;
import fr.jayblanc.mbyte.manager.store.StoreProviderNotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("status")
public class StatusResource {

    private static final Logger LOGGER = Logger.getLogger(StatusResource.class.getName());

    @Inject AuthenticationService auth;
    @Inject StoreManager manager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() throws StoreProviderNotFoundException, StoreProviderException {
        LOGGER.log(Level.INFO, "GET /api/status");
        Map<String, String> status = new HashMap<>();
        status.put("connected-profile", auth.getConnectedProfile().toString());
        status.put("status", "ok");
        status.put("apps", manager.getProvider().listAllStores().stream().collect(Collectors.joining(",")));
        return Response.ok(status).build();
    }

}
