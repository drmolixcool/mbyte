/*
 * Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jerome Blanchard
 */

@Authenticated
@WebSocket(path = "/notifications")
public class EventSocket {

    private static final Logger LOGGER = Logger.getLogger(EventSocket.class.getName());

    @Inject WebSocketConnection connection;
    @Inject AuthenticationService auth;
    @Inject ObjectMapper objectMapper;

    @OnOpen
    public void onOpen() {
        LOGGER.log(Level.INFO, "Opening event socket {0}", this);
        Event event = Event.build(auth.getConnectedIdentifier(), "WS_CONNECTED", auth.getConnectedIdentifier(), "Welcome onboard " + auth.getConnectedProfile().getFullname() + "!");
        try {
            connection.sendTextAndAwait(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize event to JSON", e);
        }
    }

    @ConsumeEvent(value = NotificationService.NOTIFICATION_TOPIC, blocking = true)
    public void onMessage(Event event) {
        LOGGER.log(Level.INFO, "Received event, checking connection owner {0} against event owner {1}", new Object[]{auth.getConnectedIdentifier(), event.getOwner()});
        if (event.getOwner().equals(auth.getConnectedIdentifier())) {
            LOGGER.log(Level.INFO, "Sending event to websocket connection {0}", connection);
            try {
                connection.sendTextAndAwait(objectMapper.writeValueAsString(event));
            } catch (JsonProcessingException e) {
                LOGGER.log(Level.SEVERE, "Failed to serialize event to JSON", e);
            }
        }
    }
}
