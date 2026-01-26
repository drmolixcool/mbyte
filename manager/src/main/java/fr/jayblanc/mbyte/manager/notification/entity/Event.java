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
package fr.jayblanc.mbyte.manager.notification.entity;

import java.util.Map;
import java.util.UUID;

public class Event {

    public static final String TYPE_APP_CREATED = "app.created";
    public static final String TYPE_APP_DELETED = "app.deleted";
    public static final String TYPE_APP_STATUS_UPDATED = "app.status.updated";
    public static final String TYPE_APP_COMMAND_RUN = "app.command.run";
    public static final String TYPE_ENV_UPDATED = "env.updated";
    public static final String TYPE_PROCESS_STARTED = "process.started";
    public static final String TYPE_PROCESS_COMPLETED = "process.completed";
    public static final String TYPE_PROCESS_FAILED = "process.failed";

    private String id;
    private long timestamp;
    private String type;
    private String owner;
    private String source;
    private String message;
    private Map<String, String> params;

    public Event() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public static Event build(String owner, String type, String source) {
        return build(owner, type, source, null, null);
    }

    public static Event build(String owner, String type, String source, String message) {
        return build(owner, type, source, message, null);
    }

    public static Event build(String owner, String type, String source, String message, Map<String, String> params) {
        Event event = new Event();
        event.setId(UUID.randomUUID().toString());
        event.setTimestamp(System.currentTimeMillis());
        event.setOwner(owner);
        event.setType(type);
        event.setSource(source);
        event.setMessage(message);
        event.setParams(params);
        return event;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", owner='" + owner + '\'' +
                ", type='" + type + '\'' +
                ", source='" + source + '\'' +
                ", message='" + message + '\'' +
                ", params=" + params +
                '}';
    }

}
