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
package fr.jayblanc.mbyte.manager.core.runtime.task.database;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import fr.jayblanc.mbyte.manager.process.Task;
import fr.jayblanc.mbyte.manager.process.TaskException;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionScoped;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Jerome Blanchard
 */
@TransactionScoped
public class CreateDockerPgSQLTask extends Task {

    public static final String TASK_NAME = "CreateDockerPgSQL";
    public static final String NETWORK_NAME = "NETWORK_NAME";
    public static final String DB_IMAGE_NAME = "DB_IMAGE_NAME";
    public static final String DB_VOLUME_NAME = "DB_VOLUME_NAME";
    public static final String DB_CONTAINER_NAME = "DB_CONTAINER_NAME";
    public static final String DB_NAME = "DB_NAME";
    public static final String DB_USER = "DB_USER";
    public static final String DB_PASSWORD = "DB_PASSWORD";

    @Inject DockerClient client;

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public void execute() throws TaskException {
        String imageName = getContextValue(DB_IMAGE_NAME, "postgres:latest");
        String networkName = getMandatoryContextValue(NETWORK_NAME);
        String volumeName = getMandatoryContextValue(DB_VOLUME_NAME);
        String containerName = getMandatoryContextValue(DB_CONTAINER_NAME);
        String dbName = getContextValue(DB_NAME, "database");
        String dbUser = getMandatoryContextValue(DB_USER);
        String dbPass = getMandatoryContextValue(DB_PASSWORD);

        Optional<Container> container = client.listContainersCmd().withShowAll(true).withNameFilter(List.of(containerName)).exec().stream()
                .filter(c -> Arrays.asList(c.getNames()).contains("/" + containerName)).findFirst();
        if (container.isEmpty()) {
            CreateContainerResponse response = client.createContainerCmd(imageName)
                    .withName(containerName)
                    .withHostName(containerName)
                    .withEnv(
                            "POSTGRES_USER=" + dbUser,
                            "POSTGRES_PASSWORD=" + dbPass,
                            "POSTGRES_DB=" + dbName
                    )
                    .withHostConfig(HostConfig.newHostConfig()
                            .withNetworkMode(networkName)
                            .withBinds(new Bind(volumeName, new Volume("/var/lib/postgresql"))))
                    .exec();
            if (response.getId() == null) {
                this.fail(String.format("Failed to create database container with name: '%s'", containerName));
                throw new TaskException("Database container creation failed for name: " + containerName + ", warnings: " + String.join(", ",
                        response.getWarnings()));
            }
            this.complete(String.format("Database container created with id: '%s'", response.getId()));
        } else {
            this.log(String.format("Found existing database container with name: '%s', id: '%s'", container.get().getNames()[0], container.get().getId()));
            InspectContainerResponse inspect = client.inspectContainerCmd(container.get().getId()).exec();
            if (!imageName.equals(inspect.getConfig().getImage())) {
                this.fail(String.format("Existing container image '%s' does not match expected '%s'", inspect.getConfig().getImage(), imageName));
                throw new TaskException("Existing container image " + inspect.getConfig().getImage() + " does not match expected " + imageName);
            }
            if (!containerName.equals(inspect.getConfig().getHostName())) {
                this.fail(String.format("Existing container hostname '%s' does not match expected '%s'", inspect.getConfig().getHostName(), containerName));
                throw new TaskException("Existing container hostname " + inspect.getConfig().getHostName() + " does not match expected " + containerName);
            }
            List<String> envVars = inspect.getConfig().getEnv() != null ? Arrays.asList(inspect.getConfig().getEnv()) : List.of();
            String expectedUser = "POSTGRES_USER=" + dbUser;
            String expectedPass = "POSTGRES_PASSWORD=" + dbPass;
            String expectedDb = "POSTGRES_DB=" + dbName;
            if (!envVars.contains(expectedUser) || !envVars.contains(expectedPass) || !envVars.contains(expectedDb)) {
                this.fail(String.format("Existing container environment variables do not match expected: USER='%s', PASS='%s', DB='%s'", expectedUser, expectedPass, expectedDb));
                throw new TaskException("Existing container environment variables do not match expected: USER='" + expectedUser + "', PASS='" + expectedPass  + "', DB='" + expectedDb + "'");
            }
            if (!networkName.equals(inspect.getHostConfig().getNetworkMode())) {
                this.fail(String.format("Existing container network mode '%s' does not match expected '%s'", inspect.getHostConfig().getNetworkMode(), networkName));
                throw new TaskException("Existing container network mode " + inspect.getHostConfig().getNetworkMode() + " does not match expected " + networkName);
            }
            List<Bind> binds = inspect.getHostConfig().getBinds() != null ? Arrays.asList(inspect.getHostConfig().getBinds()) : null;
            boolean volumeMatch = binds != null && binds.stream().anyMatch(bind -> volumeName.equals(bind.getPath()) && "/var/lib/postgresql".equals(bind.getVolume().getPath()));
            if (!volumeMatch) {
                this.fail(String.format("Existing container volume bind does not match expected: volume='%s' to '/var/lib/postgresql'", volumeName));
                throw new TaskException("Existing container volume bind does not match expected: volume='" + volumeName + "' to '/var/lib/postgresql'");
            }
            if (!container.get().getState().equalsIgnoreCase("running")) {
                client.startContainerCmd(container.get().getId()).exec();
                this.complete(String.format("Database container started with id: '%s'", container.get().getId()));
            } else {
                this.complete(String.format("Database container already exists for name: '%s' with id: '%s'", containerName, container.get().getId()));
            }
        }

    }

}
