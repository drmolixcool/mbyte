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
package fr.jayblanc.mbyte.manager.api.dto;

import fr.jayblanc.mbyte.manager.core.ApplicationCommand;

import java.util.Set;

/**
 * @author Jerome Blanchard
 */
public class CommandDescriptor {

    private String appType;
    private Set<String> appStatus;
    private String name;
    private String version;
    private String description;

    public CommandDescriptor() {
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public Set<String> getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(Set<String> appStatus) {
        this.appStatus = appStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static CommandDescriptor  fromApplicationCommand(ApplicationCommand command) {
        CommandDescriptor dto = new CommandDescriptor();
        dto.setAppType(command.forAppType());
        Set<String> statusSet = command.forAppStatus().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        dto.setAppStatus(statusSet);
        dto.setName(command.getName());
        dto.setVersion(command.getVersion() != null ? command.getVersion().toString() : null);
        dto.setDescription(command.getDescription());
        return dto;
    }
}
