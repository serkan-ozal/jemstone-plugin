package tr.com.serkanozal.jemstone.sa.plugin.disassembler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import sun.jvm.hotspot.asm.Disassembler;
import sun.jvm.hotspot.asm.InstructionVisitor;
import sun.jvm.hotspot.code.CodeBlob;
import sun.jvm.hotspot.code.CodeCacheVisitor;
import sun.jvm.hotspot.code.NMethod;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.Method;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotSAKeyValueResult;

@SuppressWarnings("serial")
public class HotSpotSADisassemblerWorker 
        implements HotSpotServiceabilityAgentWorker<HotSpotSADisassemblerParam, HotSpotSAKeyValueResult> {

    private static class LibInfo {
        
        private final String libExportPath;
        private final String libName;
        
        private LibInfo(String libExportPath, String libName) {
            this.libExportPath = libExportPath;
            this.libName = libName;
        }
        
    }
    
    private LibInfo getLibInfo() throws IOException {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("hotspot-sa-disassembler", "tmp");
            String libExportPath = tempFile.getParent() + File.separator;
            String libName = "hsdis";
            String os = System.getProperty("os.name");
            String arch = System.getProperty("os.arch");
            
            if (arch.equals("x86") || arch.equals("i386")) {
                libName += "-i386";
            } else if (arch.equals("amd64") || arch.equals("x86_64")) {
                libName +=  "-amd64";
            } else {
                throw new IllegalStateException("Unsupported CPU architeture: " + arch);
            }
            
            if (os.lastIndexOf("Windows", 0) != -1) {
                libName += "-windows.dll";
            } else if (os.lastIndexOf("SunOS", 0) != -1) {
                libName += "-solaris.so";
            } else if (os.lastIndexOf("Linux", 0) != -1) {
                libName += "-linux.so";
            } else if (os.lastIndexOf("Mac OS X", 0) != -1) {
                libName += "-bsd.dylib";
            } else {
                throw new IllegalStateException("Unsupported operation system: " + os);
            }
            
            return new LibInfo(libExportPath.toString() + libName, libName);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }    
    }
    
    private void init() {
        try {
            LibInfo libInfo = getLibInfo();
            File exportLibPathFile = new File(libInfo.libExportPath);
            if (!exportLibPathFile.exists()) {
                ClassLoader cl = getClass().getClassLoader();
                InputStream libSrcStream = null;
                OutputStream libDestStream = null;
                try {
                    libSrcStream = cl.getResourceAsStream(libInfo.libName);
                    exportLibPathFile.createNewFile();
                    libDestStream = new FileOutputStream(exportLibPathFile);
                    byte[] buf = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = libSrcStream.read(buf)) > 0) {
                        libDestStream.write(buf, 0, bytesRead);
                    }
                    libDestStream.flush();
                } finally {
                    if (libSrcStream != null) {
                        libSrcStream.close();
                    }
                    if (libDestStream != null) {
                        libDestStream.close();
                    }
                }
            }

            Class<?> disassemblerClass = Class.forName("sun.jvm.hotspot.asm.Disassembler");
            Field decodeFunctionField = disassemblerClass.getDeclaredField("decode_function");
            java.lang.reflect.Method loadLibraryMethod = 
                    disassemblerClass.getDeclaredMethod("load_library", String.class, String.class);
            decodeFunctionField.setAccessible(true);
            loadLibraryMethod.setAccessible(true);
            long decodeFunctionValue = 
                    (Long) loadLibraryMethod.invoke(null, exportLibPathFile.getParent() + File.separator, libInfo.libName);
            decodeFunctionField.set(null, decodeFunctionValue);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to initialize " + getClass().getSimpleName(), t);
        }
    }
    
    @Override
    public HotSpotSAKeyValueResult run(HotSpotServiceabilityAgentContext context,
                                       final HotSpotSADisassemblerParam param) {
        init();

        final HotSpotSAKeyValueResult result = new HotSpotSAKeyValueResult();
        context.getVM().getCodeCache().iterate(new CodeCacheVisitor() {
            private String nMethodToStr(NMethod nMethod) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                nMethod.printOn(ps);
                ps.flush();
                return baos.toString();
            }

            @Override
            public void visit(CodeBlob codeBlob) {
                NMethod nMethod = codeBlob.asNMethodOrNull();
                if (nMethod != null) {
                    Method method = nMethod.getMethod();
                    if (method != null && !method.isObsolete() && !nMethod.isZombie()) {
                        String methodName = method.externalNameAndSignature();
                        String normalizedMethodName = methodName.replace("/", ".");
                        if (normalizedMethodName.startsWith(param.getMethodName())) {
                            final StringBuilder disassembledCode = new StringBuilder();
                            final long constBegin = Long.parseLong(nMethod.constantsBegin().toString().substring(2), 16);
                            final long exceptionHandlerBegin = Long.parseLong(nMethod.handlerTableBegin().toString().substring(2), 16);
                            final long stubBegin = Long.parseLong(nMethod.stubBegin().toString().substring(2), 16);
                            final long deoptBegin = Long.parseLong(nMethod.deoptHandlerBegin().toString().substring(2), 16);
                            Disassembler.decode(new InstructionVisitor() {
                                @Override
                                public void prologue() {
                                }
                                
                                @Override
                                public void beginInstruction(long currentPc) {
                                    if (currentPc == constBegin) {
                                        disassembledCode.append("[Constants]\n");
                                    } else if (currentPc == exceptionHandlerBegin) {
                                        disassembledCode.append("[Exception Handler]\n");
                                    } else if (currentPc == stubBegin) {
                                        disassembledCode.append("[Stub Code]\n");
                                    } else if (currentPc == deoptBegin) {
                                        disassembledCode.append("[Deopt Handler Code]\n");
                                    }
                                    disassembledCode.append(String.format("  0x%016x: ", currentPc));
                                }

                                @Override
                                public void printAddress(long address) {
                                    disassembledCode.append(String.format("0x%016x", address));
                                }
                                
                                @Override
                                public void print(String format) {
                                    disassembledCode.append(format);
                                }
                                
                                @Override
                                public void endInstruction(long endPc) {
                                    disassembledCode.append("\n");
                                }

                                @Override
                                public void epilogue() {
                                }
                            }, nMethod);
                            String methodResultInfo = 
                                    "\nInformation:\n" + 
                                            "\t" + nMethodToStr(nMethod) +
                                    "Disassembled code:\n" +
                                            "\t" + disassembledCode.toString().replace("\n", "\n\t");
                            result.addResult(normalizedMethodName, methodResultInfo);
                        }    
                    }    
                }    
            }
            
            @Override
            public void prologue(Address start, Address end) {
            }
            
            @Override
            public void epilogue() {
            }
        });
        return result;
    }

}

