package org.covidsierra.process_evtx;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.covidsierra.process_evtx.EvtxIngestJobSettings.Filter;
import org.openide.util.NbBundle;

public class EvtxFilterSettingsDialog extends JDialog {
    
    private final EvtxIngestJobSettings settings;
    private final List<FilterPanel> panels = new ArrayList<>();
    private final Map<FilterPanel, JButton> removeButtons = new HashMap<>();
    private JPanel componentPanel;
    private JButton buttonOK;

    public EvtxFilterSettingsDialog(Window parent, EvtxIngestJobSettings settings) {
        super(parent, NbBundle.getMessage(EvtxIngestJobSettingsPanel.class, "EvtxFilterSettingsDialog.title"), Dialog.ModalityType.DOCUMENT_MODAL);
        setLocationRelativeTo(parent);
        this.settings = settings;
        initComponents();
    }
    
    private void initComponents(){
        setLayout(new GridBagLayout());
        
        componentPanel = new JPanel(new GridBagLayout());
        JScrollPane pane = new JScrollPane(componentPanel);
        pane.setBorder(BorderFactory.createEmptyBorder());
        
        buttonOK = new JButton(NbBundle.getMessage(EvtxIngestJobSettingsPanel.class, "EvtxFilterSettingsDialog.OkButton.text"));
        buttonOK.addActionListener(this::okButtonPressed);
        
        add(pane,     new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
        add(buttonOK, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.EAST,   GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
        
        for (Filter filter : settings.getFilters()) {
            addFilterPanel(filter);
        }
        if (panels.isEmpty() || !panels.get(panels.size() - 1).getFilter().isEmpty()) createNewFilter();
        pack();
    }

    private void addFilterPanel(Filter filter) {
        FilterPanel panel = new FilterPanel(this, filter);
        panels.add(panel);
        componentPanel.add(panel, new GridBagConstraints(0, -1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 0), 0, 0));
        
        JButton removeBtn = new JButton("X");
        removeButtons.put(panel, removeBtn);
        removeBtn.addActionListener(e -> removeFilterPanel(panel));
        componentPanel.add(removeBtn, new GridBagConstraints(1, -1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
        
        repaint();
        pack();
    }
    
    private void removeFilterPanel(FilterPanel panel) {
        int panelIndex = panels.indexOf(panel);
        if (panelIndex == 0 && !panels.stream().filter(p -> p != panel).map(FilterPanel::getFilter).anyMatch(f -> !f.isEmpty())) {
            panel.clear();
            for (int i = panelIndex + 1; i < panels.size(); i++) {
                FilterPanel p = panels.get(i);
                if (p.getFilter().isEmpty()) removeFilterPanel(p);
            }
        } else if (panelIndex != panels.size() - 1 || (panelIndex == 1 && panels.get(0).getFilter().isEmpty())) {
            componentPanel.remove(panel);
            componentPanel.remove(removeButtons.get(panel));
            panels.remove(panel);
            removeButtons.remove(panel);
            settings.removeFilter(panel.getFilter());
        }
        
        repaint();
        pack();
    }
    
    private void createNewFilter() {
        Filter filter = new Filter();
        settings.addFilter(filter);
        addFilterPanel(filter);
    }
    
    void filterChanged(FilterPanel panel) {
        if (panels.indexOf(panel) == panels.size() - 1 && !panel.getFilter().isEmpty()) {
            createNewFilter();
        }
    }
    
    private void okButtonPressed(ActionEvent e) {
        setVisible(false);
    }
    
}
