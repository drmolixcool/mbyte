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
package fr.jayblanc.mbyte.store.index;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Startup
@Singleton
public class IndexStoreServiceWorkerBean implements IndexStoreServiceWorker {

    private static final Logger LOGGER = Logger.getLogger(IndexStoreServiceWorkerBean.class.getName());
    private static final String[] EVENTS_HANDLE = {"folder.create", "folder.delete", "file.create", "file.delete"};

    @Inject ManagedExecutor executor;

    @Inject IndexStoreService indexStore;

    @Inject
    Instance<IndexableContentProvider> providers;

    @Override
    public void submit(String type, String node) {
        LOGGER.log(Level.INFO, "Submitting new job to worker");
        IndexStoreJob job = new IndexStoreJob();
        job.setId(UUID.randomUUID().toString());
        job.setStatus(IndexStoreJob.Status.PENDING);
        job.setType(type);
        job.setNode(node);
        executor.submit(new JobWorker(job));
    }

    class JobWorker implements Runnable {

        private static final Logger LOGGER = Logger.getLogger(JobWorker.class.getName());

        private final IndexStoreJob job;

        public JobWorker(IndexStoreJob job) {
            this.job = job;
        }

        @Override
        public void run() {
            LOGGER.log(Level.INFO, "Handle job: " + job.getId());
            StringBuilder report = new StringBuilder();
            try {
                if (job.getType().endsWith("create") || job.getType().endsWith("update")) {
                    Optional<IndexableContent> content = providers.stream()
                            .map(provider -> provider.getIndexableContent(job.getNode()))
                            .filter(Objects::nonNull).findFirst();

                    if (content.isPresent()) {
                        LOGGER.log(Level.INFO, "Submitting content to index store: " + content.get().getIdentifier());
                        indexStore.index(content.get());
                    } else {
                        LOGGER.log(Level.WARNING, "No content found for node: " + job.getNode());
                        report.append("No content found for node: ").append(job.getNode());
                    }
                }

                if (job.getType().endsWith("remove")) {
                    indexStore.remove(job.getNode());
                }

                report.append("Job done.");
                job.setOutput(report.toString());
            } catch (Exception e) {
                report.append("Error while processing job: ").append(e.getMessage());
                LOGGER.log(Level.WARNING, "Something wrong happened: " + e.getMessage(), e);
            }
            LOGGER.log(Level.INFO, "Job done.");
        }
    }


}
