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
import fr.jayblanc.mbyte.manager.core.runtime.task.container.StartDockerContainerTask;
import fr.jayblanc.mbyte.manager.core.runtime.task.database.CreateDockerPgSQLTask;
import fr.jayblanc.mbyte.manager.core.runtime.task.network.CreateDockerNetworkTask;
import fr.jayblanc.mbyte.manager.core.runtime.task.store.CreateDockerStoreTask;
import fr.jayblanc.mbyte.manager.core.runtime.task.volume.CreateDockerVolumeTask;
import fr.jayblanc.mbyte.manager.process.ProcessDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;

/**
 * @author Jerome Blanchard
 */
@ApplicationScoped
public class StartDockerStoreCommand implements ApplicationCommand {

    public static final String NAME = "START";
    public static final int VERSION = 1;

    @Override
    public String forAppType() {
        return DockerStoreDescriptor.TYPE;
    }

    @Override
    public Set<ApplicationStatus> forAppStatus() {
        return Set.of(ApplicationStatus.CREATED, ApplicationStatus.STOPPED);
    }

    @Override
    public String getDescription() {
        return "Start a Docker Store application along with its PostgreSQL database. Dedicated volumes and network will be created if not already present.";
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
                .addTask(CreateDockerNetworkTask.TASK_NAME, Map.of(CreateDockerNetworkTask.NETWORK_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_NETWORK_NAME ))
                .addTask(CreateDockerVolumeTask.TASK_NAME, Map.of(CreateDockerVolumeTask.VOLUME_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_DB_VOLUME_NAME ))
                .addTask(CreateDockerVolumeTask.TASK_NAME, Map.of(CreateDockerVolumeTask.VOLUME_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_VOLUME_NAME ))
                .addTask(CreateDockerPgSQLTask.TASK_NAME, Map.of(
                        CreateDockerPgSQLTask.NETWORK_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_NETWORK_NAME,
                        CreateDockerPgSQLTask.DB_VOLUME_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_DB_VOLUME_NAME,
                        CreateDockerPgSQLTask.DB_CONTAINER_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_DB_CONTAINER_NAME,
                        CreateDockerPgSQLTask.DB_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_DB_NAME,
                        CreateDockerPgSQLTask.DB_USER, "$" + DockerStoreDescriptor.EnvKey.STORE_DB_USER,
                        CreateDockerPgSQLTask.DB_PASSWORD, "$" + DockerStoreDescriptor.EnvKey.STORE_DB_PASSWORD
                ))
                .addTask(CreateDockerStoreTask.TASK_NAME)
                .addTask(StartDockerContainerTask.TASK_NAME, Map.of(
                        StartDockerContainerTask.CONTAINER_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_DB_CONTAINER_NAME
                ))
                .addTask(StartDockerContainerTask.TASK_NAME, Map.of(
                        StartDockerContainerTask.CONTAINER_NAME, "$" + DockerStoreDescriptor.EnvKey.STORE_CONTAINER_NAME
                ))
                .addTask(UpdateAppStatusTask.TASK_NAME, Map.of(UpdateAppStatusTask.APP_STATUS, ApplicationStatus.STARTED.name()))
                .build();
    }
}
