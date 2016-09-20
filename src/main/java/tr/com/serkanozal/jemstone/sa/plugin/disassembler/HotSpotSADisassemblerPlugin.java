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

package tr.com.serkanozal.jemstone.sa.plugin.disassembler;

import tr.com.serkanozal.jemstone.Jemstone;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentConfig;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentPlugin;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResultProcessor;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotSAKeyValueResult;

public class HotSpotSADisassemblerPlugin
        implements HotSpotServiceabilityAgentPlugin<HotSpotSADisassemblerParam, 
                                                    HotSpotSAKeyValueResult, 
                                                    HotSpotSADisassemblerWorker> {

    public static final String PLUGIN_ID = "HotSpot_Disassembler";
    
    private static final JavaVersion[] SUPPORTED_JAVA_VERSION = 
            new JavaVersion[] { 
                JavaVersion.JAVA_8
            };
    private static final String USAGE = 
            Jemstone.class.getName() + " " + 
                "(-i " + "\"" + PLUGIN_ID + "\"" + " <process_id> <method_name>)" + 
                " | " + 
                "(-p " + HotSpotSADisassemblerPlugin.class.getName() + " <process_id> <method_name>)";
    
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
    public HotSpotSADisassemblerWorker getWorker() {
        return new HotSpotSADisassemblerWorker();
    }

    @Override
    public HotSpotSADisassemblerParam getParamater(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "Invalid parameter count! Method name is required. See usage.\n" + 
                    "Usage: " + getUsage());
        }
        return new HotSpotSADisassemblerParam(args[0]);
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
