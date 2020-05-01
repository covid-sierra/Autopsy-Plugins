package org.covidsierra.process_evtx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.openide.util.Exceptions;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.casemodule.services.Services;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestModule;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.datamodel.Blackboard.BlackboardException;
import org.sleuthkit.datamodel.TskException;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;

class EvtxIngestModule implements DataSourceIngestModule {

    private final EvtxIngestJobSettings settings;
    private IngestJobContext context = null;
    private File tempDir = null;
    private File tempBinaryPath = null;

    EvtxIngestModule(EvtxIngestJobSettings settings) {
        this.settings = settings;
    }

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
        this.context = context;
        
        Case autopsyCase = Case.getCurrentCase();
        
        // Create temp dir
        String tempDirString = autopsyCase.getTempDirectory();
        tempDir = new File(tempDirString + "/evtx/");
        tempDir.mkdir();
        
        // Choose correct binary
        String binaryName = PlatformUtil.isWindowsOS() ? "export_evtx.exe" : "Export_EVTX";
        String resourcePath = "/resources/" + binaryName;
        tempBinaryPath = new File(tempDir, binaryName);
        
        if (!tempBinaryPath.exists()) {
            try (BufferedInputStream stream = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream(resourcePath));
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(tempBinaryPath))) {
                int data = stream.read();
                while(data != -1) {
                    os.write(data);
                    data = stream.read();
                }
            } catch (IOException ex) {
                throw new IngestModuleException("Can not write binary to temp folder", ex);
            }
        }
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        if (context.dataSourceIngestIsCancelled()) return IngestModule.ProcessResult.OK;

        if (!settings.isAnalyzeAll() && !settings.isAnalyzeApplication() && !settings.isAnalyzeSecurity() && !settings.isAnalyzeSystem() && !settings.isAnalyzeOther()) {
            log(IngestMessage.MessageType.ERROR, "No Event Logs selected to parse");
            return IngestModule.ProcessResult.ERROR;
        }
        
        progressBar.switchToIndeterminate();

        Case autopsyCase = Case.getCurrentCase();
        SleuthkitCase sleuthkitCase = autopsyCase.getSleuthkitCase();
        Services services = new Services(sleuthkitCase);
        FileManager fileManager = services.getFileManager();
        
        int artID_evtx;
        int artID_evtx_long;
        BlackboardArtifact.Type artID_evtx_evt;
        BlackboardArtifact.Type artID_evtx_long_evt;
        
        BlackboardAttribute.Type attID_ev_fn;
        BlackboardAttribute.Type attID_ev_rc;
        BlackboardAttribute.Type attID_ev_cn;
        BlackboardAttribute.Type attID_ev_ei;
        BlackboardAttribute.Type attID_ev_eiq;
        BlackboardAttribute.Type attID_ev_el;
        BlackboardAttribute.Type attID_ev_oif;
        BlackboardAttribute.Type attID_ev_id;
        BlackboardAttribute.Type attID_ev_sn;
        BlackboardAttribute.Type attID_ev_usi;
        BlackboardAttribute.Type attID_ev_et;
        BlackboardAttribute.Type attID_ev_ete;
        BlackboardAttribute.Type attID_ev_dt;
        BlackboardAttribute.Type attID_ev_cnt;
        
        try {
            artID_evtx      = addArtifactType(sleuthkitCase, "TSK_EVTX_LOGS",      "Windows Event Logs");
            artID_evtx_long = addArtifactType(sleuthkitCase, "TSK_EVTX_LOGS_LONG", "Windows Event Logs Long Tail Analysis");
            artID_evtx_evt      = sleuthkitCase.getArtifactType("TSK_EVTX_LOGS");
            artID_evtx_long_evt = sleuthkitCase.getArtifactType("TSK_EVTX_LOGS_LONG");
            
            attID_ev_fn  = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_FILE_NAME",                  BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Event Log File Name");
            attID_ev_rc  = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_RECOVERED_RECORD",           BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Recovered Record");
            attID_ev_cn  = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_COMPUTER_NAME",              BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Computer Name");
            attID_ev_ei  = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_EVENT_IDENTIFIER",           BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.LONG,   "Event Identiifier");
            attID_ev_eiq = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_EVENT_IDENTIFIER_QUALIFERS", BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Event Identifier Qualifiers");
            attID_ev_el  = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_EVENT_LEVEL",                BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Event Level");
            attID_ev_oif = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_OFFSET_IN_FILE",             BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Event Offset In File");
            attID_ev_id  = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_IDENTIFIER",                 BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Identifier");
            attID_ev_sn  = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_SOURCE_NAME",                BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Source Name");
            attID_ev_usi = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_USER_SECURITY_ID",           BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "User Security ID");
            attID_ev_et  = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_EVENT_TIME",                 BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Event Time");
            attID_ev_ete = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_EVENT_TIME_EPOCH",           BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Event Time Epoch");
            attID_ev_dt  = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_EVENT_DETAIL_TEXT",          BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Event Detail");
            attID_ev_cnt = addArtifactAttributeType(sleuthkitCase, "TSK_EVTX_EVENT_ID_COUNT",             BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.LONG,   "Event Id Count");
        } catch (IngestModuleException | TskException e) {
            return IngestModule.ProcessResult.ERROR;
        }
        
        // Find the Windows Event Log Files
        List<AbstractFile> files = new ArrayList<>();
        for (String fileName : settings.getFilesToUse()) {
            try {
                files.addAll(fileManager.findFiles(dataSource, fileName));
            } catch (TskCoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        progressBar.switchToDeterminate(files.size());
        
        for (AbstractFile file : files) {
            if (context.dataSourceIngestIsCancelled()) return IngestModule.ProcessResult.OK;
            File current = new File(tempDir, file.getName());
            try {
                ContentUtils.writeToFile(file, current, context::dataSourceIngestIsCancelled);
            } catch (IOException ex) {
                log(IngestMessage.MessageType.ERROR, "Cant write temp file '" + current + "'");
            }
        }
        
        File dbPath = new File(tempDir, "EventLogs.db3");
        try {
            Process process = new ProcessBuilder(tempBinaryPath.getAbsolutePath(), tempDir.getAbsolutePath(), dbPath.getAbsolutePath()).inheritIO().start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            log(IngestMessage.MessageType.ERROR, "Cant run binary: " + e.getLocalizedMessage());
            return IngestModule.ProcessResult.ERROR;
        }
        
        // DB Connection
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Logger.getLogger("org.covidsierra.process_evtx").log(Level.SEVERE, "SQLITE ERROR", e);
            log(IngestMessage.MessageType.ERROR, "Cant load db driver: " + e.getLocalizedMessage());
            return IngestModule.ProcessResult.ERROR;
        }
        
        try (Connection dbConn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.getAbsolutePath())) {
            
            for (AbstractFile file : files) {
                String fileName = file.getName();
                
                String sql = "SELECT File_Name, Recovered_Record, Computer_name, Event_Identifier, " + 
                        " Event_Identifier_Qualifiers, Event_Level, Event_offset, Identifier, " + 
                        " Event_source_Name, Event_User_Security_Identifier, Event_Time, " + 
                        " Event_Time_Epoch, Event_Detail_Text FROM Event_Logs where upper(File_Name) = ?";
                
                // TODO filtering
                
                PreparedStatement pstmt = dbConn.prepareStatement(sql);
                pstmt.setString(1, fileName.toUpperCase());
                
                // TODO filtering

                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    try {
                        String computerName                = resultSet.getString("Computer_Name");
                        long eventIdentifier               = resultSet.getLong  ("Event_Identifier");
                        String eventLevel                  = resultSet.getString("Event_Level");
                        String eventSourceName             = resultSet.getString("Event_Source_Name");
                        String eventUserSecurityIdentifier = resultSet.getString("Event_User_Security_Identifier");
                        String eventTime                   = resultSet.getString("Event_Time");
                        String eventDetailText             = resultSet.getString("Event_Detail_Text");
                        
                        BlackboardArtifact art = file.newArtifact(artID_evtx);
                        art.addAttribute(new BlackboardAttribute(attID_ev_cn,  EvtxIngestModuleFactory.getModuleName(), computerName));
                        art.addAttribute(new BlackboardAttribute(attID_ev_ei,  EvtxIngestModuleFactory.getModuleName(), eventIdentifier));
                        art.addAttribute(new BlackboardAttribute(attID_ev_el,  EvtxIngestModuleFactory.getModuleName(), eventLevel));
                        art.addAttribute(new BlackboardAttribute(attID_ev_sn,  EvtxIngestModuleFactory.getModuleName(), eventSourceName));
                        art.addAttribute(new BlackboardAttribute(attID_ev_usi, EvtxIngestModuleFactory.getModuleName(), eventUserSecurityIdentifier));
                        art.addAttribute(new BlackboardAttribute(attID_ev_et,  EvtxIngestModuleFactory.getModuleName(), eventTime));
                        art.addAttribute(new BlackboardAttribute(attID_ev_dt,  EvtxIngestModuleFactory.getModuleName(), eventDetailText));
                        
                        sleuthkitCase.getBlackboard().postArtifact(art, EvtxIngestModuleFactory.getModuleName());
                    } catch (SQLException | TskCoreException | BlackboardException e) {
                        log(IngestMessage.MessageType.WARNING, "Error getting values from DB: " + e.getLocalizedMessage());
                    }
                }
                resultSet.close();
                pstmt.close();
                
                sql = "select event_identifier, file_name, count(*) 'Number_Of_Events'  " + 
                        " FROM Event_Logs where upper(File_Name) = ?";
                
                // TODO filtering
                
                sql += " GROUP BY event_identifier, file_name ORDER BY 3";
                
                // TODO filtering
                
                pstmt = dbConn.prepareStatement(sql);
                pstmt.setString(1, fileName.toUpperCase());
                
                // TODO filtering
                
                resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    try {
                        long eventIdentifier = resultSet.getLong("Event_Identifier");
                        long eventIdCount    = resultSet.getLong("Number_Of_Events");
                        
                        BlackboardArtifact art = file.newArtifact(artID_evtx_long);
                        art.addAttribute(new BlackboardAttribute(attID_ev_ei,  EvtxIngestModuleFactory.getModuleName(), eventIdentifier));
                        art.addAttribute(new BlackboardAttribute(attID_ev_cnt, EvtxIngestModuleFactory.getModuleName(), eventIdCount));
                        
                        sleuthkitCase.getBlackboard().postArtifact(art, EvtxIngestModuleFactory.getModuleName());
                    } catch (SQLException | TskCoreException | BlackboardException e) {
                        log(IngestMessage.MessageType.WARNING, "Error getting values from DB: " + e.getLocalizedMessage());
                    }
                }
                resultSet.close();
                pstmt.close();
                progressBar.progress(1);
            }
            
        } catch (SQLException e) {
            log(IngestMessage.MessageType.ERROR, "Cant connect to DB: " + e.getLocalizedMessage());
            return IngestModule.ProcessResult.ERROR;
        }
        
        // Delete recursively
        deleteDirectory(tempDir);
        
        log(IngestMessage.MessageType.INFO, "Event Logs successfully parsed");
        
        return IngestModule.ProcessResult.OK;
    }
    
    private int addArtifactType(SleuthkitCase skCase, String artifactTypeName, String displayName) throws IngestModuleException {
        try {
            skCase.addBlackboardArtifactType(artifactTypeName, displayName);
        } catch (TskException e) {}
        try {
            return skCase.getArtifactTypeID(artifactTypeName);
        } catch (TskException e) {
            throw new IngestModuleException("Cant get artifact type", e);
        }
    }
    
    private BlackboardAttribute.Type addArtifactAttributeType(SleuthkitCase skCase, String attrTypeString, BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE valuetype, String displayName) throws IngestModuleException {
        try {
            skCase.addArtifactAttributeType(attrTypeString, valuetype, displayName);
        } catch (TskException e) {}
        try {
            return skCase.getAttributeType(attrTypeString);
        } catch (TskException e) {
            throw new IngestModuleException("Cant get artifact type", e);
        }
    }
    
    private void log(IngestMessage.MessageType type, String msg) {
        IngestMessage message = IngestMessage.createMessage(type, EvtxIngestModuleFactory.getModuleName(), msg);
        IngestServices.getInstance().postMessage(message);
    }
    
    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}