# Jemstone-Plugin
Plug-in repository for Jemstone

1. What is Jemstone-Plugin?
==============
**Jemstone-Plugin** repository is for all additional plugins (not built-in plugins) for [Jemstone](https://github.com/serkan-ozal/jemstone).

2. Plugins
==============

2.1. Heap Summary Plugin
--------------
Summarizes heap informations such as memory limits and capacity as global and as per generation (Eden space, Survivor spaces, Tenured space, G1 regions if G1 is used, ...) of target JVM.

Here is the [plugin](https://github.com/serkan-ozal/jemstone-plugin/blob/master/src/main/java/tr/com/serkanozal/jemstone/sa/plugin/heapsummary/HotSpotSAHeapSummarizerPlugin.java) and the [implementation](https://github.com/serkan-ozal/jemstone-plugin/blob/master/src/main/java/tr/com/serkanozal/jemstone/sa/plugin/heapsummary/HotSpotSAHeapSummarizerWorker.java)

The usage from commandline is:
```
tr.com.serkanozal.jemstone.Jemstone 
        (-i "HotSpot_Heap_Summarizer" <process_id>) 
	| 
	(-p tr.com.serkanozal.jemstone.sa.plugin.heapsummary.HotSpotSAHeapSummarizerPlugin <process_id>) 
```

This is the sample output the plugin:
```
HotSpotSAKeyValueResult [{Heap Summary=
   using thread-local object allocation.
   Parallel GC with 8 thread(s)
   
   Heap Limits:
      Start address = 0x0000000081200000
      Capacity      = 198705152 (189) MB
      End address   = 0x000000008cf80000
   
   Heap Configuration:
      MinHeapFreeRatio         = 40
      MaxHeapFreeRatio         = 70
      MaxHeapSize              = 2128609280 (2030.0MB)
      NewSize                  = 1572864 (1.5MB)
      MaxNewSize               = 709361664 (676.5MB)
      OldSize                  = 132644864 (126.5MB)
      NewRatio                 = 2
      SurvivorRatio            = 8
      MetaspaceSize            = 21807104 (20.796875MB)
      CompressedClassSpaceSize = 1073741824 (1024.0MB)
      MaxMetaspaceSize         = 17592186044415 MB
      G1HeapRegionSize         = 0 (0.0MB)
   
   Heap Usage:
   PS Young Generation
   Eden Space:
      start    = 0x0000000002154720
      capacity = 61341696 (58.5MB)
      end      = 0x0000000005bd4720
      top      = 0x00000000d6af7378
      bottom   = 0x00000000d5b80000
      used     = 16216952 (15.465690612792969MB)
      free     = 45124744 (43.03430938720703MB)
      26.437077970586273% used
   From Space:
      start    = 0x00000000021547c0
      capacity = 4718592 (4.5MB)
      end      = 0x00000000025d47c0
      top      = 0x00000000d9a7d720
      bottom   = 0x00000000d9600000
      used     = 4708128 (4.490020751953125MB)
      free     = 10464 (0.009979248046875MB)
      99.77823893229167% used
   To Space:
      start    = 0x0000000002154770
      capacity = 6815744 (6.5MB)
      end      = 0x00000000027d4770
      top      = 0x00000000dae80000
      bottom   = 0x00000000dae80000
      used     = 0 (0.0MB)
      free     = 6815744 (6.5MB)
      0.0% used
   PS Old Generation
      start    = 0x0000000002155c40
      capacity = 132644864 (126.5MB)
      end      = 0x0000000009fd5c40
      used     = 8666952 (8.265449523925781MB)
      free     = 123977912 (118.23455047607422MB)
      6.533952192826704% used
}]
```

2.2. ClassLoader Stats Plugin
--------------
Prints the classloader stats such as loaded classes, bytes, parent classloader, etc ... for defined classloaders in the target JVM.

Here is the [plugin](https://github.com/serkan-ozal/jemstone-plugin/blob/master/src/main/java/tr/com/serkanozal/jemstone/sa/plugin/classloaderstats/HotSpotSAClassLoaderStatsPlugin.java) and the [implementation](https://github.com/serkan-ozal/jemstone-plugin/blob/master/src/main/java/tr/com/serkanozal/jemstone/sa/plugin/classloaderstats/HotSpotSAClassLoaderStatsWorker.java)

The usage from commandline is:
```
tr.com.serkanozal.jemstone.Jemstone 
        (-i "HotSpot_ClassLoader_Stats_Finder" <process_id>) 
	| 
	(-p tr.com.serkanozal.jemstone.sa.plugin.classloaderstats.HotSpotSAClassLoaderStatsPlugin <process_id>) 
```

This is the sample output the plugin:
```
HotSpotSAKeyValueResult [{ClassLoader Stats=
	Finding class loader instances ... Done.
	Computing per loader stat ... Done.
	Please wait ... Computing liveness .... Liveness analysis may be inaccurate ...
	class_loader         classes    bytes      parent_loader        alive?               type                
	=================================================================================================
	<bootstrap>          1075       2026518    null                 live                 <internal>          
	0x0000000081307a30   0          0          0x0000000081264868   dead                 java/util/ResourceBundle$RBClassLoader@0x0000000017266838
	0x00000000d6a0c8a0   1          1471       0x0000000081264868   dead                 sun/reflect/DelegatingClassLoader@0x00000000171d9870
	0x00000000d6a1a380   1          1471       0x0000000081264868   dead                 sun/reflect/DelegatingClassLoader@0x00000000171d9870
	0x00000000d6aabe38   1          1471       0x0000000081264868   dead                 sun/reflect/DelegatingClassLoader@0x00000000171d9870
	0x00000000d69ccf98   1          1471       0x0000000081264868   dead                 sun/reflect/DelegatingClassLoader@0x00000000171d9870
	0x00000000812648c0   0          0          null                 dead                 sun/misc/Launcher$ExtClassLoader@0x00000000171fbdb8
	0x00000000d6a234a8   1          1471       null                 dead                 sun/reflect/DelegatingClassLoader@0x00000000171d9870
	0x00000000813b67c0   1          1471       null                 dead                 sun/reflect/DelegatingClassLoader@0x00000000171d9870
	0x00000000812d1b10   1          1471       null                 dead                 sun/reflect/DelegatingClassLoader@0x00000000171d9870
	0x00000000812d1bd0   1          1471       0x0000000081264868   dead                 sun/reflect/DelegatingClassLoader@0x00000000171d9870
	0x0000000081264868   392        724033     0x00000000812648c0   dead                 sun/misc/Launcher$AppClassLoader@0x0000000017207788
	
	total=12             1475       N/A        2762319              alive=1, dead=11     N/A
}]
```

**P.S:** Execution of this plugin may take a few minutes.
