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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import sun.jvm.hotspot.gc_implementation.g1.G1CollectedHeap;
import sun.jvm.hotspot.gc_implementation.g1.G1MonitoringSupport;
import sun.jvm.hotspot.gc_implementation.g1.HeapRegion;
import sun.jvm.hotspot.gc_implementation.g1.HeapRegionSetBase;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSOldGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.ParallelScavengeHeap;
import sun.jvm.hotspot.gc_implementation.shared.MutableSpace;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.ContiguousSpace;
import sun.jvm.hotspot.memory.DefNewGeneration;
import sun.jvm.hotspot.memory.GenCollectedHeap;
import sun.jvm.hotspot.memory.Generation;
import sun.jvm.hotspot.memory.SharedHeap;
import sun.jvm.hotspot.runtime.VM;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter.NoHotSpotServiceabilityAgentParameter;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotSAKeyValueResult;

@SuppressWarnings("serial")
public class HotSpotSAHeapSummarizerWorker
        implements HotSpotServiceabilityAgentWorker<NoHotSpotServiceabilityAgentParameter, 
                                                    HotSpotSAKeyValueResult> {

    private static final String ALIGNMENT = "   ";
    private static final double FACTOR = 1024 * 1024;

    @Override
    public HotSpotSAKeyValueResult run(HotSpotServiceabilityAgentContext context,
                                       NoHotSpotServiceabilityAgentParameter param) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(os);

        VM vm = context.getVM();
        CollectedHeap heap = vm.getUniverse().heap();
        VM.Flag[] flags = VM.getVM().getCommandLineFlags();
        Map<String, Object> flagMap = new HashMap<String, Object>();
        if (flags == null) {
            out.println("WARNING: command line flags are not available");
        } else {
            for (int f = 0; f < flags.length; f++) {
                flagMap.put(flags[f].getName(), flags[f]);
            }
        }

        printGCAlgorithm(flagMap, out);
        out.println();
        
        long capacity = heap.capacity();
        out.println("Heap Limits:");
        out.println(ALIGNMENT + "Start address = " + heap.start());
        out.println(ALIGNMENT + "Capacity      = " + capacity + " (" + (capacity >>> 20) + ") MB");
        out.println(ALIGNMENT + "End address   = " + heap.start().addOffsetTo(capacity));
        out.println();
        
        out.println("Heap Configuration:");
        printValue("MinHeapFreeRatio         = ", getFlagValue("MinHeapFreeRatio", flagMap), out);
        printValue("MaxHeapFreeRatio         = ", getFlagValue("MaxHeapFreeRatio", flagMap), out);
        printValMB("MaxHeapSize              = ", getFlagValue("MaxHeapSize", flagMap), out);
        printValMB("NewSize                  = ", getFlagValue("NewSize", flagMap), out);
        printValMB("MaxNewSize               = ", getFlagValue("MaxNewSize", flagMap), out);
        printValMB("OldSize                  = ", getFlagValue("OldSize", flagMap), out);
        printValue("NewRatio                 = ", getFlagValue("NewRatio", flagMap), out);
        printValue("SurvivorRatio            = ", getFlagValue("SurvivorRatio", flagMap), out);
        printValMB("MetaspaceSize            = ", getFlagValue("MetaspaceSize", flagMap), out);
        printValMB("CompressedClassSpaceSize = ", getFlagValue("CompressedClassSpaceSize", flagMap), out);
        printValMB("MaxMetaspaceSize         = ", getFlagValue("MaxMetaspaceSize", flagMap), out);
        printValMB("G1HeapRegionSize         = ", HeapRegion.grainBytes(), out);

        out.println();
        out.println("Heap Usage:");

        if (heap instanceof SharedHeap) {
            SharedHeap sharedHeap = (SharedHeap) heap;
            if (sharedHeap instanceof GenCollectedHeap) {
                GenCollectedHeap genHeap = (GenCollectedHeap) sharedHeap;
                for (int n = 0; n < genHeap.nGens(); n++) {
                    Generation gen = genHeap.getGen(n);
                    if (gen instanceof DefNewGeneration) {
                        out.println("New Generation (Eden + 1 Survivor Space):");
                        printGen(gen, out);

                        ContiguousSpace eden = ((DefNewGeneration) gen).eden();
                        out.println("Eden Space:");
                        printSpace(eden, out);

                        ContiguousSpace from = ((DefNewGeneration) gen).from();
                        out.println("From Space:");
                        printSpace(from, out);

                        ContiguousSpace to = ((DefNewGeneration) gen).to();
                        out.println("To Space:");
                        printSpace(to, out);
                    } else {
                        out.println(gen.name() + ":");
                        printGen(gen, out);
                    }
                }
            } else if (sharedHeap instanceof G1CollectedHeap) {
                G1CollectedHeap g1h = (G1CollectedHeap) sharedHeap;
                G1MonitoringSupport g1mm = g1h.g1mm();
                long edenRegionNum = g1mm.edenRegionNum();
                long survivorRegionNum = g1mm.survivorRegionNum();
                HeapRegionSetBase oldSet = g1h.oldSet();
                HeapRegionSetBase humongousSet = g1h.humongousSet();
                long oldRegionNum = oldSet.regionNum() + humongousSet.regionNum();
                printG1Space("G1 Heap:", 
                             g1h.n_regions(), g1h.used(), g1h.capacity(), out);
                out.println("G1 Young Generation:");
                printG1Space("Eden Space:", 
                             edenRegionNum, g1mm.edenUsed(), g1mm.edenCommitted(), out);
                printG1Space("Survivor Space:", 
                             survivorRegionNum, g1mm.survivorUsed(), g1mm.survivorCommitted(), out);
                printG1Space("G1 Old Generation:", 
                             oldRegionNum, g1mm.oldUsed(), g1mm.oldCommitted(), out);
            } else {
                throw new RuntimeException("unknown SharedHeap type : " + heap.getClass());
            }
        } else if (heap instanceof ParallelScavengeHeap) {
            ParallelScavengeHeap psh = (ParallelScavengeHeap) heap;
            PSYoungGen youngGen = psh.youngGen();
            printPSYoungGen(youngGen, out);

            PSOldGen oldGen = psh.oldGen();
            long oldFree = oldGen.capacity() - oldGen.used();
            out.println("PS Old Generation");
            out.println(ALIGNMENT + "start    = " + oldGen.getAddress());
            printValMB("capacity = ", oldGen.capacity(), out);
            out.println(ALIGNMENT + "end      = " + oldGen.getAddress().addOffsetTo(oldGen.capacity()));
            printValMB("used     = ", oldGen.used(), out);
            printValMB("free     = ", oldFree, out);
            out.println(ALIGNMENT + (double) oldGen.used() * 100.0 / oldGen.capacity() + "% used");
        } else {
            throw new RuntimeException("unknown CollectedHeap type : " + heap.getClass());
        }

        out.flush();

        return new HotSpotSAKeyValueResult().
                    addResult("Heap Summary", 
                              "\n" + ALIGNMENT + 
                                  os.toString().replace("\n", "\n" + ALIGNMENT).trim() + 
                              "\n");
    }

    private void printGCAlgorithm(Map<String, Object> flagMap, PrintStream out) {
        long l = getFlagValue("UseParNewGC", flagMap);
        if (l == 1L) {
            out.println("using parallel threads in the new generation.");
        }

        l = getFlagValue("UseTLAB", flagMap);
        if (l == 1L) {
            out.println("using thread-local object allocation.");
        }

        l = getFlagValue("UseConcMarkSweepGC", flagMap);
        if (l == 1L) {
            out.println("Concurrent Mark-Sweep GC");
            return;
        }

        l = getFlagValue("UseParallelGC", flagMap);
        if (l == 1L) {
            out.print("Parallel GC ");
            l = getFlagValue("ParallelGCThreads", flagMap);
            out.println("with " + l + " thread(s)");
            return;
        }

        l = getFlagValue("UseG1GC", flagMap);
        if (l == 1L) {
            out.print("Garbage-First (G1) GC ");
            l = getFlagValue("ParallelGCThreads", flagMap);
            out.println("with " + l + " thread(s)");
            return;
        }

        out.println("Mark Sweep Compact GC");
    }

    private void printPSYoungGen(PSYoungGen youngGen, PrintStream out) {
        out.println("PS Young Generation");
        MutableSpace eden = youngGen.edenSpace();
        out.println("Eden Space:");
        printMutableSpace(eden, out);
        MutableSpace from = youngGen.fromSpace();
        out.println("From Space:");
        printMutableSpace(from, out);
        MutableSpace to = youngGen.toSpace();
        out.println("To Space:");
        printMutableSpace(to, out);
    }

    private void printMutableSpace(MutableSpace space, PrintStream out) {
        out.println(ALIGNMENT + "start    = " + space.getAddress());
        printValMB("capacity = ", space.capacity(), out);
        out.println(ALIGNMENT + "end      = " + space.getAddress().addOffsetTo(space.capacity()));
        out.println(ALIGNMENT + "top      = " + space.top());
        out.println(ALIGNMENT + "bottom   = " + space.bottom());
        printValMB("used     = ", space.used(), out);
        long free = space.capacity() - space.used();
        printValMB("free     = ", free, out);
        out.println(ALIGNMENT + (double) space.used() * 100.0 / space.capacity() + "% used");
    }

    private void printGen(Generation gen, PrintStream out) {
        out.println(ALIGNMENT + "start    = " + gen.getAddress());
        printValMB("capacity = ", gen.capacity(), out);
        out.println(ALIGNMENT + "end      = " + gen.getAddress().addOffsetTo(gen.capacity()));
        printValMB("used     = ", gen.used(), out);
        printValMB("free     = ", gen.free(), out);
        out.println(ALIGNMENT + (double) gen.used() * 100.0 / gen.capacity() + "% used");
    }

    private void printSpace(ContiguousSpace space, PrintStream out) {
        out.println(ALIGNMENT + "start    = " + space.getAddress());
        printValMB("capacity = ", space.capacity(), out);
        out.println(ALIGNMENT + "end      = " + space.getAddress().addOffsetTo(space.capacity()));
        out.println(ALIGNMENT + "top      = " + space.top());
        out.println(ALIGNMENT + "bottom   = " + space.bottom());
        printValMB("used     = ", space.used(), out);
        printValMB("free     = ", space.free(), out);
        out.println(ALIGNMENT + (double) space.used() * 100.0 / space.capacity() + "% used");
    }

    private void printG1Space(String spaceName, long regionNum, long used,
                              long capacity, PrintStream out) {
        long free = capacity - used;
        out.println(spaceName);
        printValue("regions  = ", regionNum, out);
        printValMB("capacity = ", capacity, out);
        printValMB("used     = ", used, out);
        printValMB("free     = ", free, out);
        double occPerc = (capacity > 0) ? (double) used * 100.0 / capacity : 0.0;
        out.println(ALIGNMENT + occPerc + "% used");
    }

    private void printValMB(String title, long value, PrintStream out) {
        if (value < 0) {
            out.println(ALIGNMENT + title + (value >>> 20) + " MB");
        } else {
            double mb = value / FACTOR;
            out.println(ALIGNMENT + title + value + " (" + mb + "MB)");
        }
    }

    private void printValue(String title, long value, PrintStream out) {
        out.println(ALIGNMENT + title + value);
    }

    private long getFlagValue(String name, Map<String, Object> flagMap) {
        VM.Flag f = (VM.Flag) flagMap.get(name);
        if (f != null) {
            if (f.isBool()) {
                return f.getBool() ? 1L : 0L;
            } else {
                return Long.parseLong(f.getValue());
            }
        } else {
            return -1;
        }
    }

}
