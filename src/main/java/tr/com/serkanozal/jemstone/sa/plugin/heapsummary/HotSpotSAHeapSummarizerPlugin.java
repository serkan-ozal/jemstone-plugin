/*
 * Copyright (c) 1986-2015, Serkan OZAL, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tr.com.serkanozal.jemstone.sa.plugin.heapsummary;

import tr.com.serkanozal.jemstone.Jemstone;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentConfig;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentPlugin;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResultProcessor;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter.NoHotSpotServiceabilityAgentParameter;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotSAKeyValueResult;

public class HotSpotSAHeapSummarizerPlugin
        implements HotSpotServiceabilityAgentPlugin<NoHotSpotServiceabilityAgentParameter, 
                                                    HotSpotSAKeyValueResult, 
                                                    HotSpotSAHeapSummarizerWorker> {

    public static final String PLUGIN_ID = "HotSpot_Heap_Summarizer";
    
    private static final JavaVersion[] SUPPORTED_JAVA_VERSION = 
            new JavaVersion[] { 
                JavaVersion.JAVA_7,
                JavaVersion.JAVA_8
            };
    private static final String USAGE = 
            Jemstone.class.getName() + " " + 
                "(-i " + "\"" + PLUGIN_ID + "\"" + " <process_id>)" + 
                " | " + 
                "(-p " + HotSpotSAHeapSummarizerPlugin.class.getName() + " <process_id>)";
    
    private int processId = HotSpotServiceabilityAgentConfig.CONFIG_NOT_SET;
    
    @Override
    public String getId() {
        return PLUGIN_ID;
    }
    
    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public JavaVersion[] getSupportedJavaVersions() {
        return SUPPORTED_JAVA_VERSION;
    }

    @Override
    public HotSpotSAHeapSummarizerWorker getWorker() {
        return new HotSpotSAHeapSummarizerWorker();
    }

    @Override
    public NoHotSpotServiceabilityAgentParameter getParamater(String[] args) {
        // No need to parameter
        return null;
    }

    @Override
    public HotSpotServiceabilityAgentConfig getConfig() {
        if (processId != HotSpotServiceabilityAgentConfig.CONFIG_NOT_SET) {
            HotSpotServiceabilityAgentConfig config = new HotSpotServiceabilityAgentConfig();
            // Only set "process id" and don't touch others ("pipeline size", "timeout").
            // So default configurations will be used for them ("pipeline size", "timeout").
            config.setProcessId(processId);
            return config;
        } else {
            // Use default configuration, so just returns "null"
            return null;
        }
    }
    
    @Override
    public HotSpotServiceabilityAgentResultProcessor<HotSpotSAKeyValueResult> getResultProcessor() {
        // Use default result processor (print to console)
        return null;
    }
    
}
