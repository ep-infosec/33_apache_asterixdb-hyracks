/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.hyracks.control.nc.work;

import org.apache.hyracks.control.common.job.profiling.om.TaskProfile;
import org.apache.hyracks.control.common.work.AbstractWork;
import org.apache.hyracks.control.nc.NodeControllerService;
import org.apache.hyracks.control.nc.Task;

public class NotifyTaskCompleteWork extends AbstractWork {
    private final NodeControllerService ncs;
    private final Task task;

    public NotifyTaskCompleteWork(NodeControllerService ncs, Task task) {
        this.ncs = ncs;
        this.task = task;
    }

    @Override
    public void run() {
        TaskProfile taskProfile = new TaskProfile(task.getTaskAttemptId(), task.getPartitionSendProfile());
        task.dumpProfile(taskProfile);
        try {
            ncs.getClusterController().notifyTaskComplete(task.getJobletContext().getJobId(), task.getTaskAttemptId(),
                    ncs.getId(), taskProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        task.getJoblet().removeTask(task);
    }
}
