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
package org.apache.hyracks.control.cc.work;

import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.hyracks.control.cc.ClusterControllerService;
import org.apache.hyracks.control.cc.job.JobRun;
import org.apache.hyracks.control.common.work.SynchronizableWork;

public class GetJobSummariesJSONWork extends SynchronizableWork {
    private final ClusterControllerService ccs;
    private JSONArray summaries;

    public GetJobSummariesJSONWork(ClusterControllerService ccs) {
        this.ccs = ccs;
    }

    @Override
    protected void doRun() throws Exception {
        summaries = new JSONArray();
        populateJSON(ccs.getActiveRunMap().values());
        populateJSON(ccs.getRunMapArchive().values());
    }

    private void populateJSON(Collection<JobRun> jobRuns) throws JSONException {
        for (JobRun run : jobRuns) {
            JSONObject jo = new JSONObject();
            jo.put("type", "job-summary");
            jo.put("job-id", run.getJobId().toString());
            jo.put("create-time", run.getCreateTime());
            jo.put("start-time", run.getCreateTime());
            jo.put("end-time", run.getCreateTime());
            jo.put("status", run.getStatus().toString());
            summaries.put(jo);
        }
    }

    public JSONArray getSummaries() {
        return summaries;
    }
}
