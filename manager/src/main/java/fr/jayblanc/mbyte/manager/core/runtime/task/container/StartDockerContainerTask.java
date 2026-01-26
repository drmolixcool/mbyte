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
package fr.jayblanc.mbyte.manager.core.runtime.task.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
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
public class StartDockerContainerTask extends Task {

    public static final String TASK_NAME = "StartDockerContainer";
    public static final String CONTAINER_NAME = "CONTAINER_NAME";
    public static final String TIMEOUT = "TIMEOUT";

    @Inject DockerClient client;

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public void execute() throws TaskException {
        String containerName = getMandatoryContextValue(CONTAINER_NAME);
        Long timeout = getContextValue(TIMEOUT, Long.class, 30000L);

        Optional<Container> container = client.listContainersCmd().withShowAll(true).withNameFilter(List.of(containerName)).exec().stream()
                .filter(c -> Arrays.asList(c.getNames()).contains("/" + containerName)).findFirst();
        if (container.isPresent()) {
            Container dbContainer = container.get();
            if (dbContainer.getState().equalsIgnoreCase("dead") || dbContainer.getState().equalsIgnoreCase("removing")) {
                this.fail(String.format("Cannot start container with name: '%s' and id: '%s' because it is in an incompatible state: '%s'", containerName, dbContainer.getId(), dbContainer.getState()));
                throw new TaskException("Unable to start container for name: " + containerName + ", container is in incompatible state: " + dbContainer.getState());
            }
            if (dbContainer.getState().equalsIgnoreCase("paused")) {
                client.unpauseContainerCmd(dbContainer.getId()).exec();
            }
            if (dbContainer.getState().equalsIgnoreCase("created") || dbContainer.getState().equalsIgnoreCase("exited")) {
                client.startContainerCmd(dbContainer.getId()).exec();
            }
            boolean running = false;
            long until = System.currentTimeMillis() + timeout;
            while (!running && System.currentTimeMillis() < until) {
                InspectContainerResponse ct = client.inspectContainerCmd(dbContainer.getId()).exec();
                running = Boolean.TRUE.equals(ct.getState().getRunning());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException nothing) { //nothing
                }
            }
            if (running) {
                this.complete(String.format("Container started with name: '%s' and id: '%s'", containerName, dbContainer.getId()));
            } else {
                this.fail(String.format("Timeout reached while starting container with name: '%s' and id: '%s'", containerName, dbContainer.getId()));
                throw new TaskException("Unable to start container for name: " + containerName + " timeout reached");
            }
        } else {
            this.fail(String.format("Did not found an existing container with name: '%s'", containerName));
            throw new TaskException("Unable to start container, no container found for name: " + containerName);
        }
    }
}
