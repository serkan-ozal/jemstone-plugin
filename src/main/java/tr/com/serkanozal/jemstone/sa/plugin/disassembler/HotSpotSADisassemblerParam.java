package tr.com.serkanozal.jemstone.sa.plugin.disassembler;

import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter;

@SuppressWarnings("serial")
public class HotSpotSADisassemblerParam implements HotSpotServiceabilityAgentParameter {
    
    private final String methodName;
    
    public HotSpotSADisassemblerParam(String methodName) {
        this.methodName = methodName;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
}
