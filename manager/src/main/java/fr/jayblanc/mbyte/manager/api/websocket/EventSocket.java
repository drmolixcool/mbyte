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
package fr.jayblanc.mbyte.manager.api.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.jayblanc.mbyte.manager.auth.AuthenticationService;
import fr.jayblanc.mbyte.manager.notification.NotificationService;
import fr.jayblanc.mbyte.manager.notification.entity.Event;
import io.quarkus.security.Authenticated;
import io.quarkus.vertx.ConsumeEvent;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jerome Blanchard
 */

@Authenticated
@WebSocket(path = "/notifications")
@ApplicationScoped
public class EventSocket {

    private static final Logger LOGGER = Logger.getLogger(EventSocket.class.getName());
    private final Map<String, String> ownerConnections = new ConcurrentHashMap<>();

    @Inject OpenConnections connections;
    @Inject AuthenticationService auth;
    @Inject ObjectMapper objectMapper;

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        LOGGER.log(Level.INFO, "Opening event socket for owner {0}", auth.getConnectedIdentifier());
        this.ownerConnections.put(auth.getConnectedIdentifier(), connection.id());
        Event event = Event.build(auth.getConnectedIdentifier(), "WS_CONNECTED", auth.getConnectedIdentifier(), "Welcome onboard " + auth.getConnectedProfile().getFullname() + "!");
        try {
            connection.sendTextAndAwait(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize event to JSON", e);
        }
    }

    @ConsumeEvent(value = NotificationService.NOTIFICATION_TOPIC, blocking = true)
    public void onMessage(Event event) {
        LOGGER.log(Level.INFO, "Received event, searching connection for event owner {0}", event.getOwner());
        if (event.getOwner() == null || event.getOwner().isEmpty()) {
            for (WebSocketConnection connection : connections) {
                LOGGER.log(Level.INFO, "Broadcasting event to connection {0}", connection);
                try {
                    connection.sendTextAndAwait(objectMapper.writeValueAsString(event));
                } catch (JsonProcessingException e) {
                    LOGGER.log(Level.SEVERE, "Failed to serialize event to JSON", e);
                }
            }
        }
        connections.findByConnectionId(ownerConnections.get(event.getOwner()) ).ifPresent(connection -> {
            LOGGER.log(Level.INFO, "Found websocket connection {0} for owner {1}", new Object[]{connection, event.getOwner()});
            try {
                connection.sendTextAndAwait(objectMapper.writeValueAsString(event));
            } catch (JsonProcessingException e) {
                LOGGER.log(Level.SEVERE, "Failed to serialize event to JSON", e);
            }
        });
    }
}
