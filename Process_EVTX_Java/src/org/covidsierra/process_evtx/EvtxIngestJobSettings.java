package org.covidsierra.process_evtx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

public class EvtxIngestJobSettings implements IngestModuleIngestJobSettings {
    
    private static final long serialVersionUID = 1L;
    
    private boolean analyzeAll = true;
    private boolean analyzeApplication = false;
    private boolean analyzeSecurity = false;
    private boolean analyzeSystem = false;
    private boolean analyzeOther = false;
    private String other;
    private List<Filter> filters = new ArrayList<>();

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

    public List<Filter> getFilters() { if (filters == null) filters = new ArrayList<>(); return filters; }
    public void addFilter(Filter filter) { filters.add(filter); }
    public void removeFilter(Filter filter) { filters.remove(filter); }
    
    public static class Filter implements Serializable {
        
        private FilterField field;
        private FilterOperator operator;
        private String parameter;

        FilterField getField() { return field; }
        void setField(FilterField field) { this.field = field; }
        
        FilterOperator getOperator() { return operator; }
        void setOperator(FilterOperator operator) { this.operator = operator; }
        
        String getParameter() { return parameter; }
        void setParameter(String parameter) { this.parameter = parameter; }
        
        boolean isEmpty() { return field == null && operator == null && (parameter == null || parameter.trim().isEmpty()); }
        boolean isComplete() { return field != null && operator != null && parameter != null && !parameter.trim().isEmpty(); }
        
        public String getFilterString() {
            String res = String.format(operator.getPattern(), field.getFieldName());
            if (operator == FilterOperator.IN) {
                String repl = "(?";
                for (int i = 1; i < getFilterParameters().length; i++) {
                    repl += ", ?";
                }
                repl += ")";
                res = res.replace("(?)", repl);
            }
            return res;
        }
        
        public String[] getFilterParameters() {
            Stream<String> stream = operator == FilterOperator.IN ? Arrays.stream(parameter.split(",")) : Stream.of(parameter);
            return stream.map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        }
        
        void clear() {
            field = null;
            operator = null;
            parameter = null;
        }
        
    }
    
    public enum FilterField {
        COMPUTER_NAME   ("Computer_name",     "Computer Name"),
        EVENT_IDENTIFIER("Event_Identifier",  "Event Identifier"),
        EVENT_LEVEL     ("Event_Level",       "Event Level"),
        SOURCE_NAME     ("Event_source_Name", "Source Name"),
        EVENT_DETAIL    ("Event_Detail_Text", "Event Detail");
        
        private final String fieldName;
        private final String displayName;

        private FilterField(String fieldName, String displayName) {
            this.fieldName = fieldName;
            this.displayName = displayName;
        }

        public String getFieldName  () { return fieldName; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum FilterOperator {
        EQUALS("%s = ?"),
        NOT_EQUALS("%s != ?"),
        CONTAINS("%s LIKE ?"),
        STARTS_WITH("%s LIKE ?"),
        ENDS_WITH("%s LIKE ?"),
        IN("%s IN (?)");
        
        private final String pattern;
        
        private FilterOperator(String pattern) {
            this.pattern = pattern;
        }
        
        String getPattern() {
            return pattern;
        }
    }
    
}