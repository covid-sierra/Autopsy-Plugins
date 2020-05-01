package org.covidsierra.process_evtx;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.IngestModuleFactory;
import org.sleuthkit.autopsy.ingest.IngestModuleFactoryAdapter;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;

@ServiceProvider(service = IngestModuleFactory.class)
public class EvtxIngestModuleFactory extends IngestModuleFactoryAdapter {
    
    static String getModuleName() {
        return NbBundle.getMessage(EvtxIngestModuleFactory.class, "OpenIDE-Module-Name");
    }

    @Override
    public String getModuleDisplayName() {
        return getModuleName();
    }

    @Override
    public String getModuleDescription() {
        return "Parses EVTX Files and creates artifacts for the found events";
    }

    @Override
    public String getModuleVersionNumber() {
        return "0.1";
    }

    @Override
    public IngestModuleIngestJobSettings getDefaultIngestJobSettings() {
        return new EvtxIngestJobSettings();
    }

    @Override
    public boolean hasIngestJobSettingsPanel() {
        return true;
    }

    @Override
    public IngestModuleIngestJobSettingsPanel getIngestJobSettingsPanel(IngestModuleIngestJobSettings settings) {
        if (!(settings instanceof EvtxIngestJobSettings)) settings = getDefaultIngestJobSettings();
        return new EvtxIngestJobSettingsPanel((EvtxIngestJobSettings) settings);
    }

    @Override
    public boolean isDataSourceIngestModuleFactory() {
        return true;
    }

    @Override
    public DataSourceIngestModule createDataSourceIngestModule(IngestModuleIngestJobSettings settings) {
        if (!(settings instanceof EvtxIngestJobSettings)) {
            throw new IllegalArgumentException("Expected settings argument to be instanceof EvtxIngestJobSettings");
        }
        return new EvtxIngestModule((EvtxIngestJobSettings) settings);
    }
    
}
