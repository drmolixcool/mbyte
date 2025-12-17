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
package fr.jayblanc.mbyte.store.api.dto;

import fr.jayblanc.mbyte.store.metrics.MetricsService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jerome Blanchard
 */
public class Status {

    private String connectedId;
    private int nbCpus;
    private long totalMemory;
    private long availableMemory;
    private long maxMemory;
    private Map<String, Long> latestMetrics;
    private Map<String, Long> metrics;

    public Status() {
        latestMetrics = new HashMap<>();
        metrics = new HashMap<>();
    }

    public String getConnectedId() {
        return connectedId;
    }

    public Status withConnectedId(String connectedId) {
        this.connectedId = connectedId;
        return this;
    }

    public int getNbCpus() {
        return nbCpus;
    }

    public void setNbCpus(int nbCpus) {
        this.nbCpus = nbCpus;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public long getAvailableMemory() {
        return availableMemory;
    }

    public void setAvailableMemory(long availableMemory) {
        this.availableMemory = availableMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public Map<String, Long> getLatestMetrics() {
        return latestMetrics;
    }

    public void setLatestMetrics(Map<String, Long> latestMetrics) {
        this.latestMetrics = latestMetrics;
    }

    public Map<String, Long> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Long> metrics) {
        this.metrics = metrics;
    }

    public static Status fromRuntime() {
        Status status = new Status();
        status.setNbCpus(Runtime.getRuntime().availableProcessors());
        status.setTotalMemory(Runtime.getRuntime().totalMemory());
        status.setAvailableMemory(Runtime.getRuntime().freeMemory());
        status.setMaxMemory(Runtime.getRuntime().maxMemory());
        return status;
    }

    public Status withMetrics(MetricsService service) {
        this.metrics = service.listMetrics();
        this.latestMetrics = service.listLatestMetrics();
        return this;
    }
}
