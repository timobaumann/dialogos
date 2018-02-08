/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.dialogos.sphinx;

import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.dialogos.sphinx.gui.PronDictDialog;
import com.clt.diamant.IdMap;
import com.clt.diamant.graph.Graph;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.speech.Language;
import com.clt.speech.recognition.*;
import com.clt.speech.recognition.G2PEntry;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Stores settings relevant to the recognition plugin:
 * - language-specific settings (path to AM, path to background LM?)
 * - exception dictionary to add/fix pronunciations
 * -
 */
public class Settings extends PluginSettings {

    private DefaultEnumProperty<LanguageName> defaultLanguage;
//    private Map<String, List<com.clt.speech.recognition.G2PEntry>> g2pLists;

    public Settings() {
        List<LanguageName> languages = Plugin.getAvailableLanguages();
        this.defaultLanguage = new DefaultEnumProperty<LanguageName>("language",
                Resources.getString("DefaultLanguage"), null,
                languages.toArray(new LanguageName[languages.size()])) {
            @Override public String getName() {
                return Resources.getString("DefaultLanguage");
            }
            @Override public void setValueFromString(String value) {
                for (LanguageName n : this.getPossibleValues()) {
                    if (n.toString().equals(value) || n.getName().equals(value)) {
                        this.setValue(n);
                        break;
                    }
                }
            }
        };
        if (!languages.isEmpty()) {
            this.defaultLanguage.setValue(languages.iterator().next());
        }
    }

    public LanguageName getDefaultLanguage() {
        return this.defaultLanguage.getValue();
    }

    @Override
    public void writeAttributes(XMLWriter out, IdMap uidMap) {
        if (this.getDefaultLanguage() != null) {
            if (this.getDefaultLanguage().getName().length() > 0) {
                Graph.printAtt(out, "language", this.getDefaultLanguage().getLanguage().getLocale().toString());
            }
        }
        for (LanguageName ln : getLanguages()) {
            SphinxLanguageSettings sls = Plugin.getRecognizer().getLanguageSettings(ln.getLanguage());
            if (!sls.getG2PList().isEmpty()) {
                out.openElement("g2p", new String[] {"language"}, new Locale[] {ln.getLanguage().getLocale()});
                for (G2PEntry entry : sls.getG2PList()) {
                    out.printElement("entry", new String[] {"g", "p"}, new String[] {entry.getGraphemes(), entry.getPhonemes()});
                }
                out.closeElement("g2p");
            }
        }
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
        if (name.equals("language")) {
            LanguageName ln = lnForLocale(value);
            assert ln != null;
            this.defaultLanguage.setValue(ln);
        }
        // more stuff is loaded via readOtherXML!
    }

    LanguageName lnForLocale(String locale) {
        Language l = new Language(Language.findLocale(locale));
        return new LanguageName(l.getName(), l);
    }

    @Override
    protected void readOtherXML(XMLReader r, String name, Attributes atts, IdMap uid_map) throws SAXException {
        if (name.equals("g2p")) {
            String langName = atts.getValue("language");
            LanguageName ln = lnForLocale(langName);
            SphinxLanguageSettings sls = Plugin.getRecognizer().getLanguageSettings(ln.getLanguage());
            assert sls != null;
            r.setHandler(new AbstractHandler("g2p") {
                @Override protected void start(String name, Attributes atts) throws SAXException {
                    sls.getG2PList().add(new G2PEntry(atts.getValue("g"), atts.getValue("p")));
                }
            });
        } else {
            super.readOtherXML(r, name, atts, uid_map);
        }
    }

    @Override
    protected PluginRuntime createRuntime(Component parent) throws Exception {
        return null; // we don't need a PluginRuntime (nor know what it is)
    }

    protected List<LanguageName> getLanguages() {
        return Arrays.asList(defaultLanguage.getPossibleValues());
    }

    @Override
    public JComponent createEditor() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new PropertySet<Property<?>>(defaultLanguage).createPropertyPanel(false),
                BorderLayout.NORTH);
        p.add(new JButton(new AbstractAction("Aussprachewörterbuch bearbeiten") { // TODO: l10n!
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                PronDictDialog.showDialog(p, Plugin.getRecognizer().getLanguageSettings(defaultLanguage.getValue().getLanguage()).getG2PList(), "description");
            }
        }), BorderLayout.CENTER);
        p.add(new JLabel(""), BorderLayout.SOUTH);
        return p;
    }

}
