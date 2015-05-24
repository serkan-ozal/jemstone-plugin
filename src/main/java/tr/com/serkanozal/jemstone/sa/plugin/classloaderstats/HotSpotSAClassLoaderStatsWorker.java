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

package tr.com.serkanozal.jemstone.sa.plugin.classloaderstats;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sun.jvm.hotspot.memory.SystemDictionary;
import sun.jvm.hotspot.oops.ConstantPool;
import sun.jvm.hotspot.oops.DefaultHeapVisitor;
import sun.jvm.hotspot.oops.DefaultOopVisitor;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.Method;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.OopField;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.utilities.GenericArray;
import sun.jvm.hotspot.utilities.HeapProgressThunk;
import sun.jvm.hotspot.utilities.MethodArray;
import sun.jvm.hotspot.utilities.ReversePtrs;
import sun.jvm.hotspot.utilities.ReversePtrsAnalysis;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter.NoHotSpotServiceabilityAgentParameter;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotSAKeyValueResult;

@SuppressWarnings("serial")
public class HotSpotSAClassLoaderStatsWorker
        implements HotSpotServiceabilityAgentWorker<NoHotSpotServiceabilityAgentParameter, 
                                                    HotSpotSAKeyValueResult> {

    private static class ClassData {
        
        @SuppressWarnings("unused")
        Klass klass;
        @SuppressWarnings("unused")
        long size;

        ClassData(Klass klass, long size) {
           this.klass = klass; this.size = size;
        }
        
    }
    
    private static class LoaderData {
        
        long numClasses;
        long classSize;
        List<ClassData> classDetail = new ArrayList<ClassData>();
        
    }

    @Override
    public HotSpotSAKeyValueResult run(HotSpotServiceabilityAgentContext context,
                                       NoHotSpotServiceabilityAgentParameter param) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(os);

        VM vm = context.getVM();
        
        out.print("Finding class loader instances ...");
        final Map<Oop, LoaderData> loaderMap = new HashMap<Oop, LoaderData>();
        // Loader data for bootstrap class loader
        final LoaderData bootstrapLoaderData = new LoaderData();

        ObjectHeap heap = vm.getObjectHeap();
        Klass classLoaderKlass = SystemDictionary.getClassLoaderKlass();
        try {
            heap.iterateObjectsOfKlass(
                   new DefaultHeapVisitor() {
                       public boolean doObj(Oop oop) {
                           loaderMap.put(oop, new LoaderData());
                           return false;
                       }
                   }, classLoaderKlass);
        } catch (Exception se) {
            se.printStackTrace(out);
        }

        out.println(" Done.");
        out.print("Computing per loader stat ...");

        SystemDictionary dict = vm.getSystemDictionary();
        dict.classesDo(new SystemDictionary.ClassAndLoaderVisitor() {
                            public void visit(Klass k, Oop loader) {
                                if (! (k instanceof InstanceKlass)) {
                                    return;
                                }
                                LoaderData ld = 
                                        (loader != null) 
                                            ? (LoaderData) loaderMap.get(loader)
                                            : bootstrapLoaderData;
                                if (ld != null) {
                                    ld.numClasses++;
                                    long size = computeSize((InstanceKlass)k);
                                    ld.classDetail.add(new ClassData(k, size));
                                    ld.classSize += size;
                                }
                            }
                       });

        out.println(" Done.");
        out.print("Please wait ... Computing liveness ...");

        // Compute reverse pointer analysis (takes long time for larger app)
        ReversePtrsAnalysis analysis = new ReversePtrsAnalysis();

        analysis.setHeapProgressThunk(new HeapProgressThunk() {
            public void heapIterationFractionUpdate(double fractionOfHeapVisited) {
                out.print('.');
            }
            // This will be called after the iteration is complete
            public void heapIterationComplete() {
                out.println(" Done.");
            }
        });
        
        try {
           analysis.run();
        } catch (Exception e) {
            out.println(" Liveness analysis may be inaccurate ...");
        }
        ReversePtrs liveness = VM.getVM().getRevPtrs();

        out.println(String.format(
                        "%-20s %-10s %-10s %-20s %-20s %-20s", 
                        "class_loader", "classes", "bytes", "parent_loader", "alive?", "type"));
        out.println("=================================================================================================");

        long numClassLoaders = 1L;
        long totalNumClasses = bootstrapLoaderData.numClasses;
        long totalClassSize  = bootstrapLoaderData.classSize;
        long numAliveLoaders = 1L;
        long numDeadLoaders  = 0L;

        // Print bootstrap loader details
        out.println(String.format(
                        "%-20s %-10s %-10s %-20s %-20s %-20s",
                        "<bootstrap>", 
                        bootstrapLoaderData.numClasses,
                        bootstrapLoaderData.classSize,
                        "null",
                        "live", // bootstrap loader is always alive
                        "<internal>"));

        for (Iterator<Oop> keyItr = (Iterator<Oop>) loaderMap.keySet().iterator(); keyItr.hasNext();) {
            Oop loader = keyItr.next();
            LoaderData data = (LoaderData) loaderMap.get(loader);
            numClassLoaders ++;
            totalNumClasses += data.numClasses;
            totalClassSize  += data.classSize;

            out.print(String.format(
                            "%-20s %-10s %-10s ",
                            loader.getHandle(),
                            data.numClasses,
                            data.classSize));
                        
//            out.print(loader.getHandle());
//            out.print('\t');
//            out.print(data.numClasses);
//            out.print('\t');
//            out.print(data.classSize);
//            out.print('\t');

            class ParentFinder extends DefaultOopVisitor {
                
                private Oop parent = null;
                
                public void doOop(OopField field, boolean isVMField) {
                    if (field.getID().getName().equals("parent")) {
                        parent = field.getValue(getObj());
                    }
                }
                
                public Oop getParent() { 
                    return parent; 
                }
                
           }

           ParentFinder parentFinder = new ParentFinder();
           loader.iterate(parentFinder, false);
           Oop parent = parentFinder.getParent();
           out.print(String.format("%-20s ",
                       (parent != null) 
                           ? parent.getHandle().toString() 
                           : "null"));

           boolean alive = (liveness != null) ? (liveness.get(loader) != null) : true;
           out.print(String.format("%-20s ", alive ? "live" : "dead"));
           if (alive) {
               numAliveLoaders++;
           } else {
               numDeadLoaders++;
           }

           Klass loaderKlass = loader.getKlass();
           if (loaderKlass != null) {
               out.print(String.format("%-20s", 
                           loaderKlass.getName().asString() + 
                           "@" + 
                           loader.getKlass().getAddress()));
           } else {
               out.print(String.format("%-20s", "null"));
           }
           out.println();
        }

        out.println();
        // Summary line
        out.println(String.format(
                        "%-20s %-10s %-10s %-20s %-20s %-20s",
                        "total=" + numClassLoaders,
                        totalNumClasses,
                        "N/A",
                        totalClassSize,
                        "alive=" + numAliveLoaders + ", dead=" + numDeadLoaders,
                        "N/A"));
        
        out.flush();

        return new HotSpotSAKeyValueResult().
                    addResult("Heap Summary", 
                              "\n\t" + 
                                  os.toString().replace("\n", "\n\t").trim() + 
                              "\n");
    }

    // Don't count the shared empty arrays
    private static long arraySize(GenericArray arr) {
        return arr.getLength() != 0L ? arr.getSize() : 0L;
    }

    private long computeSize(InstanceKlass k) {
        long size = 0L;
        // the InstanceKlass object itself
        size += k.getSize();

        // Constant pool
        ConstantPool cp = k.getConstants();
        size += cp.getSize();
        if (cp.getCache() != null) {
          size += cp.getCache().getSize();
        }
        size += arraySize(cp.getTags());

        // Interfaces
        size += arraySize(k.getLocalInterfaces());
        size += arraySize(k.getTransitiveInterfaces());

        // Inner classes
        size += arraySize(k.getInnerClasses());

        // Fields
        size += arraySize(k.getFields());

        // Methods
        MethodArray methods = k.getMethods();
        int nmethods = (int) methods.getLength();
        if (nmethods != 0L) {
           size += methods.getSize();
           for (int i = 0; i < nmethods; ++i) {
              Method m = methods.at(i);
              size += m.getSize();
              size += m.getConstMethod().getSize();
           }
        }

        return size;
    }

}
