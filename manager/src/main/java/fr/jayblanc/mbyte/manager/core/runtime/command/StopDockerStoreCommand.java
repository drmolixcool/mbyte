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
package fr.jayblanc.mbyte.manager.core.runtime.command;

import fr.jayblanc.mbyte.manager.core.ApplicationCommand;
import fr.jayblanc.mbyte.manager.core.ApplicationStatus;
import fr.jayblanc.mbyte.manager.core.descriptor.DockerStoreDescriptor;
import fr.jayblanc.mbyte.manager.core.entity.Environment;
import fr.jayblanc.mbyte.manager.core.runtime.task.application.UpdateAppStatusTask;
import fr.jayblanc.mbyte.manager.core.runtime.task.container.StopDockerContainerTask;
import fr.jayblanc.mbyte.manager.process.ProcessDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;

/**
 * @author Jerome Blanchard
 */
@ApplicationScoped
public class StopDockerStoreCommand implements ApplicationCommand {

    public static final String NAME = "STOP";
    public static final int VERSION = 1;

    @Override
    public String forAppType() {
        return DockerStoreDescriptor.TYPE;
    }

    @Override
    public Set<ApplicationStatus> forAppStatus() {
        return Set.of(ApplicationStatus.STARTED, ApplicationStatus.AVAILABLE);
    }

    @Override
    public String getDescription() {
        return "Stop a Docker Store application. Database will also be stopped. No container are removed.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Integer getVersion() {
        return VERSION;
    }

    @Override
    public ProcessDefinition buildProcessDefinition(Environment env) {
        return ProcessDefinition.builder()
                .withAppId(env.getApp())
                .withName(forAppType() + "_" + getName() + "_V" + getVersion())
                .withEnvironment(env)
                .addTask(StopDockerContainerTask.TASK_NAME, Map.of(
                        StopDockerContainerTask.CONTAINER_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_CONTAINER_NAME
                ))
                .addTask(StopDockerContainerTask.TASK_NAME, Map.of(
                        StopDockerContainerTask.CONTAINER_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_DB_CONTAINER_NAME
                ))
                .addTask(UpdateAppStatusTask.TASK_NAME, Map.of(UpdateAppStatusTask.APP_STATUS, ApplicationStatus.STOPPED.name()))
                .build();
    }
}
