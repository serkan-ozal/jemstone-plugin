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

package tr.com.serkanozal.jemstone;

import tr.com.serkanozal.jemstone.Jemstone;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentManager;
import tr.com.serkanozal.jemstone.sa.plugin.classloaderstats.HotSpotSAClassLoaderStatsPlugin;
import tr.com.serkanozal.jemstone.sa.plugin.disassembler.HotSpotSADisassemblerPlugin;
import tr.com.serkanozal.jemstone.sa.plugin.heapsummary.HotSpotSAHeapSummarizerPlugin;

/**
 * Demo application for Jemstone framework's plugin usage.
 * 
 * @author Serkan Ozal
 */
public class PluginDemo {

    private static final HotSpotServiceabilityAgentManager hotSpotSAManager = 
            Jemstone.getHotSpotServiceabilityAgentManager();
    
    public static void main(String[] args) throws Exception {
        System.out.println(
                hotSpotSAManager.runPlugin(
                        HotSpotSAHeapSummarizerPlugin.PLUGIN_ID).toString());
		
        // ///////////////////////////////////////////////////////////////////////////////
	
        // *** NOTE ***
        // This may take a few minutes
        System.out.println(
                hotSpotSAManager.runPlugin(
                        HotSpotSAClassLoaderStatsPlugin.PLUGIN_ID).toString());
		
        // ///////////////////////////////////////////////////////////////////////////////
		
        long[] array = new long[1024];
        for (int i = 0; i < 10000; i++) {
            findSum(array);
        }
        
        Thread.sleep(3000);
        
        System.out.println(
                hotSpotSAManager.runPlugin(
                        HotSpotSADisassemblerPlugin.PLUGIN_ID,
                        new String[] {PluginDemo.class.getName() + ".findSum"} ).toString());
	}
	
    private static long findSum(long[] array) {
        long sum = 0L;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

}
