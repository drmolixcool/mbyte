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

public class IndexStoreJob {

    private String id;
    private String type;
    private String node;
    private long startDate;
    private long stopDate;
    private int failures;
    private Status status;
    private String output;

    public IndexStoreJob() {
        this.failures = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getStartDate() {
        return startDate;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getStopDate() {
        return stopDate;
    }

    public void setStopDate(long stopDate) {
        this.stopDate = stopDate;
    }

    public int getFailures() {
        return failures;
    }

    public void setFailures(int failures) {
        this.failures = failures;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "IndexStoreJob{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", node='" + node + '\'' +
                ", startDate=" + startDate +
                ", stopDate=" + stopDate +
                ", failures=" + failures +
                ", status=" + status +
                ", output='" + output + '\'' +
                '}';
    }

    public enum Status {
        PENDING,
        RUNNING,
        DONE,
        FAILED
    }


}
