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
public class StopDockerContainerTask extends Task {

    public static final String TASK_NAME = "StopDockerContainer";
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
                this.fail(String.format("Cannot stop container with name: '%s' and id: '%s' because it is in an incompatible state: '%s'", containerName, dbContainer.getId(), dbContainer.getState()));
                throw new TaskException("Unable to stop container for name: " + containerName + ", container is in incompatible state: " + dbContainer.getState());
            }
            if (dbContainer.getState().equalsIgnoreCase("running")
                    || dbContainer.getState().equalsIgnoreCase("paused")
                    || dbContainer.getState().equalsIgnoreCase("restarting")) {
                client.stopContainerCmd(dbContainer.getId()).exec();
            }
            boolean running = true;
            long until = System.currentTimeMillis() + timeout;
            while (running && System.currentTimeMillis() < until) {
                Optional<Container> ct = client.listContainersCmd().withNameFilter(List.of(dbContainer.getId())).exec().stream().findFirst();
                if (ct.isEmpty() || ct.get().getState().equalsIgnoreCase("exited") || ct.get().getState().equalsIgnoreCase("created")) {
                    running = false;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException nothing) { //nothing
                }
            }
            if (running) {
                this.fail(String.format("Timeout reached while stopping container with name: '%s' and id: '%s'", containerName, dbContainer.getId()));
                throw new TaskException("Unable to stop container for name: " + containerName + " timeout reached");
            } else {
                this.complete(String.format("Container stopped with name: '%s' and id: '%s'", containerName, dbContainer.getId()));
            }
        } else {
            this.fail(String.format("Did not found existing container with name: '%s'", containerName));
            throw new TaskException("Unable to stop container, no container found for name: " + containerName);
        }
    }
}
