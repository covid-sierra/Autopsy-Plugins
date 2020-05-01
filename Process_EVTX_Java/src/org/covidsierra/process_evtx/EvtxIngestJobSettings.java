/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.covidsierra.process_evtx;

import java.util.ArrayList;
import java.util.Arrays;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

public class EvtxIngestJobSettings implements IngestModuleIngestJobSettings {
    
    private static final long serialVersionUID = 1L;
    
    private boolean analyzeAll = true;
    private boolean analyzeApplication = false;
    private boolean analyzeSecurity = false;
    private boolean analyzeSystem = false;
    private boolean analyzeOther = false;
    private String other;

    EvtxIngestJobSettings() {}

    @Override
    public long getVersionNumber() {
        return serialVersionUID;
    }    

    public boolean isAnalyzeAll() { return analyzeAll; }
    public void setAnalyzeAll(boolean analyzeAll) { this.analyzeAll = analyzeAll; }

    public boolean isAnalyzeApplication() { return analyzeApplication; }
    public void setAnalyzeApplication(boolean analyzeApplication) { this.analyzeApplication = analyzeApplication; }

    public boolean isAnalyzeSecurity() { return analyzeSecurity; }
    public void setAnalyzeSecurity(boolean analyzeSecurity) { this.analyzeSecurity = analyzeSecurity; }

    public boolean isAnalyzeSystem() { return analyzeSystem; }
    public void setAnalyzeSystem(boolean analyzeSystem) { this.analyzeSystem = analyzeSystem; }
    
    public boolean isAnalyzeOther() { return analyzeOther; }
    public void setAnalyzeOther(boolean analyzeOther) { this.analyzeOther = analyzeOther; }

    public String getOther() { return other; }
    public void setOther(String other) { this.other = other; }
    
    String[] getFilesToUse() {
        ArrayList<String> l = new ArrayList<>();
        if (analyzeAll)         l.add("%.evtx");
        if (analyzeApplication) l.add("Application.evtx");
        if (analyzeSecurity)    l.add("Security.evtx");
        if (analyzeSystem)      l.add("System.evtx");
        if (analyzeOther)       Arrays.stream(other.split("\n")).map(String::trim).filter(s -> !s.isEmpty()).forEach(l::add);
        return (String[]) l.stream().toArray(String[]::new);
    }
    
}