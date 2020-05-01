/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.covidsierra.process_evtx;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;

public class EvtxIngestJobSettingsPanel extends IngestModuleIngestJobSettingsPanel {
    
    private JCheckBox checkBoxAll;
    private JCheckBox checkBoxApplication;
    private JCheckBox checkBoxSecurity;
    private JCheckBox checkBoxSystem;
    private JCheckBox checkBoxOther;
    private JTextArea textAreaOther;
    private JButton   buttonFilter;

    public EvtxIngestJobSettingsPanel(EvtxIngestJobSettings settings) {
        initComponents();
        customizeComponents(settings);
    }

    private void customizeComponents(EvtxIngestJobSettings settings) {
        checkBoxAll        .setSelected(settings.isAnalyzeAll());
        checkBoxApplication.setSelected(settings.isAnalyzeApplication());
        checkBoxSecurity   .setSelected(settings.isAnalyzeSecurity());
        checkBoxSystem     .setSelected(settings.isAnalyzeSystem());
        checkBoxOther      .setSelected(settings.isAnalyzeOther());
        textAreaOther.setText(settings.getOther());
    }

    @Override
    public IngestModuleIngestJobSettings getSettings() {
        EvtxIngestJobSettings settings = new EvtxIngestJobSettings();
        settings.setAnalyzeAll        (checkBoxAll        .isSelected());
        settings.setAnalyzeApplication(checkBoxApplication.isSelected());
        settings.setAnalyzeSecurity   (checkBoxSecurity   .isSelected());
        settings.setAnalyzeSystem     (checkBoxSystem     .isSelected());
        settings.setAnalyzeOther      (checkBoxOther      .isSelected());
        settings.setOther(textAreaOther.getText());
        return settings;
    }

    private void initComponents() {
        checkBoxAll         = new JCheckBox(NbBundle.getMessage(EvtxIngestJobSettingsPanel.class, "EvtxIngestJobSettingsPanel.AllCheckBox.text"));
        checkBoxApplication = new JCheckBox(NbBundle.getMessage(EvtxIngestJobSettingsPanel.class, "EvtxIngestJobSettingsPanel.ApplicationCheckBox.text"));
        checkBoxSecurity    = new JCheckBox(NbBundle.getMessage(EvtxIngestJobSettingsPanel.class, "EvtxIngestJobSettingsPanel.SecurityCheckBox.text"));
        checkBoxSystem      = new JCheckBox(NbBundle.getMessage(EvtxIngestJobSettingsPanel.class, "EvtxIngestJobSettingsPanel.SystemCheckBox.text"));
        checkBoxOther       = new JCheckBox(NbBundle.getMessage(EvtxIngestJobSettingsPanel.class, "EvtxIngestJobSettingsPanel.OtherCheckBox.text"));
        
        textAreaOther = new JTextArea(5,0);
        textAreaOther.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JScrollPane pane = new JScrollPane(textAreaOther);
        
        buttonFilter = new JButton(NbBundle.getMessage(EvtxIngestJobSettingsPanel.class, "EvtxIngestJobSettingsPanel.FilterButton.text"));
        buttonFilter.addActionListener(e -> filterButtonPressed());

        setLayout(new GridBagLayout());
        
        add(checkBoxAll,          new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(checkBoxApplication,  new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(checkBoxSecurity,     new GridBagConstraints(0, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(checkBoxSystem,       new GridBagConstraints(0, 3, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(checkBoxOther,        new GridBagConstraints(0, 4, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(pane,                 new GridBagConstraints(0, 5, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,       new Insets(0, 0, 0, 0), 0, 0));
        add(buttonFilter,         new GridBagConstraints(0, 6, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,       new Insets(5, 0, 0, 0), 0, 0));
    }
    
    private void filterButtonPressed() {
        
    }
    
}
