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
package fr.jayblanc.mbyte.manager.process;

import fr.jayblanc.mbyte.manager.auth.AuthenticationService;
import fr.jayblanc.mbyte.manager.core.AccessDeniedException;
import fr.jayblanc.mbyte.manager.notification.NotificationService;
import fr.jayblanc.mbyte.manager.notification.NotificationServiceException;
import fr.jayblanc.mbyte.manager.notification.entity.Event;
import fr.jayblanc.mbyte.manager.process.entity.Process;
import fr.jayblanc.mbyte.manager.process.entity.ProcessContext;
import fr.jayblanc.mbyte.manager.process.entity.ProcessStatus;
import fr.jayblanc.mbyte.manager.process.task.TaskRequest;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jerome Blanchard
 */
@ApplicationScoped
public class ProcessEngineBean implements ProcessEngine, ProcessEngineAdmin {

    private static final Logger LOGGER = Logger.getLogger(ProcessEngineBean.class.getName());

    @Inject AuthenticationService auth;
    @Inject NotificationService notification;
    @Inject jakarta.enterprise.event.Event<TaskEvent> taskEvent;
    @Inject EntityManager em;

    @PostConstruct
    public void init() {
        LOGGER.info("ProcessEngineBean initialized");
        //TODO reload all process that are not finished and schedule their next task (or maybe rollback if a task was interrupted (no way to know at the moment))
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public String startProcess(ProcessDefinition processDef) throws ProcessAlreadyRunningException, NotificationServiceException {
        LOGGER.log(Level.INFO,"Starting new process: {0}", processDef.getName());
        if (this.findRunningProcessesForApp(processDef.getAppId()).stream().anyMatch(p -> p.getName().equals(processDef.getName()))) {
            throw new ProcessAlreadyRunningException("A process with name: " + processDef.getName() + " is already running for application with id: " + processDef.getAppId());
        }
        Process process = new Process(processDef);
        process.setOwner(auth.getConnectedIdentifier());
        process.setNextTaskId(process.findFirstTask());
        process.setStartDate(System.currentTimeMillis());
        em.persist(process);
        this.scheduleNextTask(process);
        notification.notify(process.getOwner(), Event.TYPE_PROCESS_STARTED, auth.getConnectedIdentifier(),
                "Process " + process.getName() + " started for application " + process.getAppId(),
                Map.of("processId", process.getId(), "processName", process.getName(), "appId", process.getAppId()));
        return process.getId();
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Process getProcess(String id) throws ProcessNotFoundException, AccessDeniedException {
        LOGGER.log(Level.INFO, "Getting process: {0}", id);
        Process process = this.findById(id);
        if (!auth.getConnectedIdentifier().equals(process.getOwner())) {
            throw new AccessDeniedException("The process with id: " + id + " is not owned by the connected user");
        }
        return process;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public List<Process> findRunningProcessesForApp(String appId) {
        LOGGER.log(Level.INFO,"Searching running processes for application: {0}", appId);
        return em.createNamedQuery("Process.findByAppAndStatus", Process.class)
                .setParameter("appId", appId)
                .setParameter("status", ProcessStatus.getRunningProcessStatuses()).getResultList();
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public List<Process> findAllProcessesForApp(String appId) {
        LOGGER.log(Level.INFO, "Searching all processes for application: {0}", appId);
        return em.createNamedQuery("Process.findByApp", Process.class).setParameter("appId", appId).getResultList();
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void assignTask(String processId, String taskId, String jobId) throws ProcessNotFoundException {
        Process instance = this.findById(processId);
        LOGGER.log(Level.INFO,  "Process.{0}[{1}].task[{2}] assigned", new Object[]{instance.getName(), instance.getId(), taskId});
        instance.setRunningTaskJobId(jobId);
        instance.setStatus(ProcessStatus.TASK_ASSIGNED);
        instance.appendLog("Process." + instance.getName()).appendLog(".task[").appendLog(taskId).appendLog("] assigned to job: ").appendLog(jobId).appendLog("\n");
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void startTask(String processId, String taskId) throws ProcessNotFoundException {
        Process instance = this.findById(processId);
        LOGGER.log(Level.INFO,  "Process.{0}[{1}].task[{2}] running", new Object[]{instance.getName(), instance.getId(), taskId});
        instance.setStatus(ProcessStatus.TASK_RUNNING);
        instance.appendLog("Process." + instance.getName()).appendLog(".task[").appendLog(taskId).appendLog("] starting").appendLog("\n");
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void completeTask(String processId, String taskId, String taskLog, ProcessContext ctx)
            throws ProcessNotFoundException, NotificationServiceException {
        Process instance = this.findById(processId);
        LOGGER.log(Level.INFO,  "Process.{0}[{1}].task[{2}] completed", new Object[]{instance.getName(), instance.getId(), taskId});
        instance.appendLog(taskLog);
        instance.setContext(ctx);
        instance.setStatus(ProcessStatus.PENDING);
        instance.appendLog("Process." + instance.getName()).appendLog(".task[").appendLog(taskId).appendLog("] completed").appendLog("\n");
        instance.setNextTaskId(instance.findNextTask(taskId));
        this.scheduleNextTask(instance);
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void failTask(String processId, String taskId, String taskLog, Exception e, ProcessContext ctx) {
        Process instance = em.find(Process.class, processId);
        if (instance == null) {
            LOGGER.log(Level.SEVERE, "Unable to find process instance for id: {0} while failing task", processId);
            return;
        }
        LOGGER.log(Level.INFO,  "Process.{0}[{1}].task[{2}] failed", new Object[]{instance.getName(), instance.getId(), taskId});
        instance.appendLog(taskLog);
        if (ctx != null) {
            instance.setContext(ctx);
        }
        instance.setStatus(ProcessStatus.FAILED);
        instance.appendLog("Process." + instance.getName()).appendLog(".task[").appendLog(taskId).appendLog("] failed: ").appendLog(e.getMessage()).appendLog("\n");
        instance.setEndDate(System.currentTimeMillis());
        try {
            notification.notify(instance.getOwner(), Event.TYPE_PROCESS_FAILED, instance.getOwner(),
                    "Process " + instance.getName() + " failed for application " + instance.getAppId() + ": " + e.getMessage(),
                    Map.of("processId", instance.getId(), "processName", instance.getName(), "appId", instance.getAppId()));
        } catch (NotificationServiceException nse) {
            LOGGER.log(Level.SEVERE, "Unable to send process failed notification for process id: " + instance.getId(), nse);
        }
        //TODO If the process is configured to rollback on failure, schedule rollback tasks
    }

    private void scheduleNextTask(Process instance) throws NotificationServiceException {
        if (instance.hasNextTask()) {
            LOGGER.log(Level.INFO,  "Process.{0}[{1}].task[{2}] scheduled", new Object[]{instance.getName(), instance.getId(), instance.getNextTaskId()});
            TaskRequest taskRequest = new TaskRequest(instance.getId(), instance.getName(), instance.getNextTaskId(), instance.getContext());
            taskEvent.fire(new TaskEvent(taskRequest));
            LOGGER.log(Level.INFO,  "Process.{0}[{1}].task[{2}] event fired, job will be queued after transaction commit", new Object[]{instance.getName(), instance.getId(), instance.getNextTaskId()});
            instance.setStatus(ProcessStatus.PENDING);
        } else {
            LOGGER.log(Level.INFO,  "Process.{0}[{1}] completed", new Object[]{instance.getName(), instance.getId()});
            instance.setEndDate(System.currentTimeMillis());
            instance.setStatus(ProcessStatus.COMPLETED);
            notification.notify(instance.getOwner(), Event.TYPE_PROCESS_COMPLETED, instance.getOwner(),
                    "Process " + instance.getName() + " completed for application " + instance.getAppId(),
                    Map.of("processId", instance.getId(), "processName", instance.getName(), "appId", instance.getAppId()));
        }
    }

    private Process findById(String id) throws ProcessNotFoundException {
        Process instance = em.find(Process.class, id);
        if ( instance == null ) {
            throw new ProcessNotFoundException("Unable to find a process instance for id: " + id);
        }
        return instance;
    }

}
