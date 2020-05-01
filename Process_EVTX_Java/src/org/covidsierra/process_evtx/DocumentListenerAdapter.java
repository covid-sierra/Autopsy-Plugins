package org.covidsierra.process_evtx;

import java.util.function.Consumer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class DocumentListenerAdapter implements DocumentListener {
    
    private final Consumer<DocumentEvent> listener;

    public DocumentListenerAdapter(Consumer<DocumentEvent> listener) {
        this.listener = listener;
    }

    @Override public void insertUpdate(DocumentEvent e) { listener.accept(e); }
    @Override public void removeUpdate(DocumentEvent e) { listener.accept(e); }
    @Override public void changedUpdate(DocumentEvent e) { listener.accept(e); }
    
}
