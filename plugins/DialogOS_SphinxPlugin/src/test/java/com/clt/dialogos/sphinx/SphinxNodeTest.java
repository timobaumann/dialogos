package com.clt.dialogos.sphinx;

import com.clt.dialogos.plugin.*;
import com.clt.dialogos.plugin.Plugin;
import com.clt.diamant.Device;
import com.clt.diamant.Grammar;
import com.clt.diamant.graph.Edge;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.diamant.graph.nodes.CatchAllEdge;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.script.Environment;
import com.clt.speech.recognition.LanguageName;
import com.clt.speech.recognition.RecognizerException;
import com.clt.speech.recognition.SphinxTest;
import com.stanfy.enroscar.net.DataStreamHandler;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.jsgf.*;
import edu.cmu.sphinx.jsgf.parser.JSGFParser;
import edu.cmu.sphinx.linguist.dictionary.Dictionary;
import edu.cmu.sphinx.linguist.dictionary.TextDictionary;
import edu.cmu.sphinx.linguist.dictionary.Word;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.recognizer.StateListener;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by timo on 09.10.17.
 */
public class SphinxNodeTest {

    @Test public void testAvailableLanguages() {
        SphinxNode node = createNode();
        List<LanguageName> langs = node.getAvailableLanguages();
        assertNotNull(langs);
        assertFalse(langs.isEmpty());
    }
    @Test public void testDefaultLanguage() {
        SphinxNode node = createNode();
        assertNotNull(node.getDefaultLanguage());
        List<LanguageName> langs = node.getAvailableLanguages(); // this fails but should of course work!
        assertTrue(langs.contains(node.getDefaultLanguage()));
    }

    /** test recognition node's recognition capability (not yet the pattern matching) */
    @Test public void recognize() {
        SphinxNode node = createNode();
        node.setProperty("grammar", new Grammar("zahl", "language \"English\";\n" +
                "root $zahl;\n"
                +"$zahl"
                +" = zero  { $ = 0; }"
                +" | one   { $ = 1; }"
                +" | two   { $ = 2; }"
                +" | three { $ = 3; }"
                +" | four  { $ = 4; }"
                +" | five  { $ = 5; }"
                +" | six   { $ = 6; }"
                +" | seven { $ = 7; }"
                +" | eight { $ = 8; }"
                +" | nine  { $ = 9; };"));
        try {
            node.execute(null, null, null);
        } catch (NodeExecutionException nee) {
            nee.printStackTrace();
            System.err.println(nee.getMessage());
            assertTrue(nee.getMessage().startsWith("RecognizerError.:\ncom.clt.speech.recognition.RecognizerException: No match for recognition result"));
        }
    }

    @Test public void recognizeAndMatch() {
        SphinxNode node = createNode();
        node.setProperty("grammar", new Grammar("zahl", "language \"English\";\n" +
                "root $zahl;\n"
                +"$zahl"
                +" = zero  { $ = 0; }"
                +" | one   { $ = 1; }"
                +" | two   { $ = 2; }"
                +" | three { $ = 3; }"
                +" | four  { $ = 4; }"
                +" | five  { $ = 5; }"
                +" | six   { $ = 6; }"
                +" | seven { $ = 7; }"
                +" | eight { $ = 8; }"
                +" | nine  { $ = 9; };"));
        Edge edge = new Edge(node, null);
        edge.setCondition("_");
        node.addEdge(edge);
        node.execute(null, null, null);
    }

    private SphinxNode createNode() {
        SphinxNode node = new SphinxNode();
        node.setGraph(new Graph(new TrivialGraphOwner()));
        return node;
    }

    private class TrivialGraphOwner implements GraphOwner {

        com.clt.dialogos.sphinx.Plugin sphinxPlugin = new com.clt.dialogos.sphinx.Plugin();
        PluginSettings sphinxSettings = sphinxPlugin.createDefaultSettings();

        @Override
        public PluginSettings getPluginSettings(Class<? extends Plugin> pluginClass) {
            return sphinxSettings;
        }

        @Override
        public Graph getSuperGraph() {
            return null;
        }

        @Override
        public Graph getOwnedGraph() {
            return null;
        }

        @Override
        public Collection<Device> getDevices() {
            return null;
        }

        @Override
        public List<Grammar> getGrammars() {
            return null;
        }

        @Override
        public Environment getEnvironment(boolean local) {
            return null;
        }

        @Override
        public void setDirty(boolean dirty) {

        }

        @Override
        public void export(Graph g, File f) throws IOException {

        }

        @Override
        public String getGraphName() {
            return null;
        }

        @Override
        public void setGraphName(String name) {

        }
    }

}
