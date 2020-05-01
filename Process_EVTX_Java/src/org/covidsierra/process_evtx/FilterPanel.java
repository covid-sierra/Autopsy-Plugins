/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.covidsierra.process_evtx;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import org.covidsierra.process_evtx.EvtxIngestJobSettings.Filter;
import org.covidsierra.process_evtx.EvtxIngestJobSettings.FilterField;
import org.covidsierra.process_evtx.EvtxIngestJobSettings.FilterOperator;

class FilterPanel extends JPanel {
    
    private final EvtxFilterSettingsDialog parentPanel;
    private final Filter filter;
    
    private JComboBox<FilterField> comboBoxFieldName;
    private JComboBox<FilterOperator> comboBoxOperator;
    private JTextField textFieldParameter;

    FilterPanel(EvtxFilterSettingsDialog parentPanel, Filter filter) {
        this.parentPanel = parentPanel;
        this.filter = filter;
        initComponents();
        updateComponents();
        attachListeners();
    }

    private void initComponents() {
        comboBoxFieldName = new JComboBox(FilterField   .values());
        comboBoxOperator =  new JComboBox(FilterOperator.values());
        
        textFieldParameter = new JTextField();
        textFieldParameter.setPreferredSize(new Dimension(200, textFieldParameter.getPreferredSize().height));
        
        setLayout(new GridBagLayout());
        add(comboBoxFieldName,  new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(comboBoxOperator,   new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
        add(textFieldParameter, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
    }

    private void updateComponents() {
        comboBoxFieldName.setSelectedItem(filter.getField());
        comboBoxOperator.setSelectedItem(filter.getOperator());
        textFieldParameter.setText(filter.getParameter());
    }
    
    private void attachListeners() {
        comboBoxFieldName.addActionListener(this::comboBoxChanged);
        comboBoxOperator .addActionListener(this::comboBoxChanged);
        textFieldParameter.getDocument().addDocumentListener(new DocumentListenerAdapter(this::textFieldChanged));
    }

    private void comboBoxChanged(ActionEvent e) {
        if (e.getSource() == comboBoxFieldName) filter.setField   ((FilterField)    comboBoxFieldName.getSelectedItem());
        if (e.getSource() == comboBoxOperator)  filter.setOperator((FilterOperator) comboBoxOperator.getSelectedItem());
        parentPanel.filterChanged(this);
    }

    private void textFieldChanged(DocumentEvent e) {
        filter.setParameter(textFieldParameter.getText());
        parentPanel.filterChanged(this);
    }

    public Filter getFilter() {
        return filter;
    }
    
    void clear() {
        filter.clear();
        updateComponents();
    }
    
}
