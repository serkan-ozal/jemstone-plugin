# Jemstone-Plugin
Plug-in repository for Jemstone

1. What is Jemstone-Plugin?
==============
**Jemstone-Plugin** repository contains all additional plugins (not built-in plugins) for [Jemstone](https://github.com/serkan-ozal/jemstone).

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

2.3. Diassembler Plugin
--------------
Prints the **JIT**ted native code of any given method dynamically at runtime in the target JVM.

Here is the [plugin](https://github.com/serkan-ozal/jemstone-plugin/blob/master/src/main/java/tr/com/serkanozal/jemstone/sa/plugin/disassembler/HotSpotSADisassemblerPlugin.java) and the [implementation](https://github.com/serkan-ozal/jemstone-plugin/blob/master/src/main/java/tr/com/serkanozal/jemstone/sa/plugin/disassembler/HotSpotSADisassemblerWorker.java)
The usage from commandline is:
```
tr.com.serkanozal.jemstone.Jemstone 
        (-i "HotSpot_Disassembler" <process_id> <method_name>) 
	| 
	(-p tr.com.serkanozal.jemstone.sa.plugin.disassembler.HotSpotSADisassemblerPlugin <process_id> <method_name>) 
```

This is the sample output the plugin:
```
HotSpotSAKeyValueResult [{tr.com.serkanozal.jemstone.PluginDemo.findSum(long[])=
Information:
	NMethod for tr/com/serkanozal/jemstone/PluginDemo.findSum([J)J content: [0x0000000002ca1f80, 0x0000000002ca20b8),  code: [0x0000000002ca1f80, 0x0000000002ca20b8),  data: [0x0000000002ca20b8, 0x0000000002ca2268),  oops: [0x0000000002ca20b8, 0x0000000002ca20c0),  frame size: 32
Disassembled code:
	[Constants]
	  0x0000000002ca1f80: mov    %eax,-0x6000(%rsp)
	  0x0000000002ca1f87: push   %rbp
	  0x0000000002ca1f88: sub    $0x10,%rsp
	  0x0000000002ca1f8c: mov    0xc(%rdx),%r10d
	  0x0000000002ca1f90: test   %r10d,%r10d
	  0x0000000002ca1f93: jle    0x0000000002ca2061
	  0x0000000002ca1f99: test   %r10d,%r10d
	  0x0000000002ca1f9c: jbe    0x0000000002ca2065
	  0x0000000002ca1fa2: mov    %r10d,%r8d
	  0x0000000002ca1fa5: dec    %r8d
	  0x0000000002ca1fa8: cmp    %r10d,%r8d
	  0x0000000002ca1fab: jae    0x0000000002ca2065
	  0x0000000002ca1fb1: mov    0x10(%rdx),%rax
	  0x0000000002ca1fb5: mov    %r10d,%r11d
	  0x0000000002ca1fb8: add    $0xfffffff1,%r11d
	  0x0000000002ca1fbc: mov    $0x1,%r9d
	  0x0000000002ca1fc2: mov    $0x80000000,%ebx
	  0x0000000002ca1fc7: cmp    %r11d,%r8d
	  0x0000000002ca1fca: cmovl  %ebx,%r11d
	  0x0000000002ca1fce: cmp    $0x1,%r11d
	  0x0000000002ca1fd2: jle    0x0000000002ca2042
	  0x0000000002ca1fd4: nopl   0x0(%rax,%rax,1)
	  0x0000000002ca1fdc: data16 data16 xchg %ax,%ax
	  0x0000000002ca1fe0: add    0x10(%rdx,%r9,8),%rax
	  0x0000000002ca1fe5: movslq %r9d,%r8
	  0x0000000002ca1fe8: add    0x18(%rdx,%r8,8),%rax
	  0x0000000002ca1fed: add    0x20(%rdx,%r8,8),%rax
	  0x0000000002ca1ff2: add    0x28(%rdx,%r8,8),%rax
	  0x0000000002ca1ff7: add    0x30(%rdx,%r8,8),%rax
	  0x0000000002ca1ffc: add    0x38(%rdx,%r8,8),%rax
	  0x0000000002ca2001: add    0x40(%rdx,%r8,8),%rax
	  0x0000000002ca2006: add    0x48(%rdx,%r8,8),%rax
	  0x0000000002ca200b: add    0x50(%rdx,%r8,8),%rax
	  0x0000000002ca2010: add    0x58(%rdx,%r8,8),%rax
	  0x0000000002ca2015: add    0x60(%rdx,%r8,8),%rax
	  0x0000000002ca201a: add    0x68(%rdx,%r8,8),%rax
	  0x0000000002ca201f: add    0x70(%rdx,%r8,8),%rax
	  0x0000000002ca2024: add    0x78(%rdx,%r8,8),%rax
	  0x0000000002ca2029: add    0x80(%rdx,%r8,8),%rax
	  0x0000000002ca2031: add    0x88(%rdx,%r8,8),%rax
	  0x0000000002ca2039: add    $0x10,%r9d
	  0x0000000002ca203d: cmp    %r11d,%r9d
	  0x0000000002ca2040: jl     0x0000000002ca1fe0
	  0x0000000002ca2042: cmp    %r10d,%r9d
	  0x0000000002ca2045: jge    0x0000000002ca2055
	  0x0000000002ca2047: nop
	  0x0000000002ca2048: add    0x10(%rdx,%r9,8),%rax
	  0x0000000002ca204d: inc    %r9d
	  0x0000000002ca2050: cmp    %r10d,%r9d
	  0x0000000002ca2053: jl     0x0000000002ca2048
	  0x0000000002ca2055: add    $0x10,%rsp
	  0x0000000002ca2059: pop    %rbp
	  0x0000000002ca205a: test   %eax,-0x2882060(%rip)        # 0x0000000000420000
	  0x0000000002ca2060: retq   
	  0x0000000002ca2061: xor    %eax,%eax
	  0x0000000002ca2063: jmp    0x0000000002ca2055
	  0x0000000002ca2065: mov    %rdx,%rbp
	  0x0000000002ca2068: mov    $0xffffff86,%edx
	  0x0000000002ca206d: xchg   %ax,%ax
	  0x0000000002ca206f: callq  0x00000000027f57a0
	  0x0000000002ca2074: int3   
	  0x0000000002ca2075: mov    $0xfffffff6,%edx
	  0x0000000002ca207a: nop
	  0x0000000002ca207b: callq  0x00000000027f57a0
	  0x0000000002ca2080: int3   
	  0x0000000002ca2081: hlt    
	  0x0000000002ca2082: hlt    
	  0x0000000002ca2083: hlt    
	  0x0000000002ca2084: hlt    
	  0x0000000002ca2085: hlt    
	  0x0000000002ca2086: hlt    
	  0x0000000002ca2087: hlt    
	  0x0000000002ca2088: hlt    
	  0x0000000002ca2089: hlt    
	  0x0000000002ca208a: hlt    
	  0x0000000002ca208b: hlt    
	  0x0000000002ca208c: hlt    
	  0x0000000002ca208d: hlt    
	  0x0000000002ca208e: hlt    
	  0x0000000002ca208f: hlt    
	  0x0000000002ca2090: hlt    
	  0x0000000002ca2091: hlt    
	  0x0000000002ca2092: hlt    
	  0x0000000002ca2093: hlt    
	  0x0000000002ca2094: hlt    
	  0x0000000002ca2095: hlt    
	  0x0000000002ca2096: hlt    
	  0x0000000002ca2097: hlt    
	  0x0000000002ca2098: hlt    
	  0x0000000002ca2099: hlt    
	  0x0000000002ca209a: hlt    
	  0x0000000002ca209b: hlt    
	  0x0000000002ca209c: hlt    
	  0x0000000002ca209d: hlt    
	  0x0000000002ca209e: hlt    
	  0x0000000002ca209f: hlt    
	[Stub Code]
	  0x0000000002ca20a0: jmpq   0x000000000281c720
	[Deopt Handler Code]
	  0x0000000002ca20a5: callq  0x0000000002ca20aa
	  0x0000000002ca20aa: subq   $0x5,(%rsp)
	  0x0000000002ca20af: jmpq   0x00000000027f7200
	  0x0000000002ca20b4: hlt    
	  0x0000000002ca20b5: hlt    
	  0x0000000002ca20b6: hlt    
	  0x0000000002ca20b7: hlt    
	}]
```
