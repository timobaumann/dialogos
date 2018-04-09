package edu.cmu.lti.dialogos.sphinx.client;

import com.clt.properties.Property;
import com.clt.speech.Language;
import com.clt.speech.SpeechException;
import com.clt.speech.recognition.*;
import com.clt.srgf.Grammar;

import java.net.URL;
import java.util.*;

import com.stanfy.enroscar.net.DataStreamHandler;
import edu.cmu.sphinx.api.*;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.endpoint.SpeechClassifiedData;

import javax.sound.sampled.AudioFormat;

/**
 * @author koller, timo
 *
 * List of TODOs:
 * handle multiple languages (configurable for more than DE/EN?),
 * exhibit a "default" language through Plugin
 *
 */
public class Sphinx extends SingleDomainRecognizer {
    static {
        try {
            URL.setURLStreamHandlerFactory(protocol -> "data".equals(protocol) ? new DataStreamHandler() : null);
        } catch (Error e) {
            if (!"factory already defined".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    public static final Language US_ENGLISH = new Language(new Locale("en", "US"), "US English");
    public static final Language GERMAN = new Language(new Locale("de", "DE"), "Deutsch");

    public static AudioFormat audioFormat = new AudioFormat(16000f, 16, 1, true, false);
    public static AudioFormat getAudioFormat() { return audioFormat; }

    private Map<Language, SphinxLanguageSettings> languageSettings;
    private SphinxContext context;

    ConfigurableSpeechRecognizer csr;

    private boolean vadInSpeech = false;

    public Sphinx() {
        languageSettings = SphinxLanguageSettings.createDefault();
    }

    public SphinxLanguageSettings getLanguageSettings(Language l) {
        return languageSettings.get(l);
    }

    @Override protected RecognitionResult startImpl() throws SpeechException {
        fireRecognizerEvent(RecognizerEvent.RECOGNIZER_LOADING);
        assert context != null : "cannot start recognition without a context";
        csr = context.getRecognizer();
        context.getVadListener().setRecognizer(this);
        vadInSpeech = false;
        SpeechResult speechResult;
        boolean isMatch;
        do {
            csr.startRecognition();
            fireRecognizerEvent(RecognizerEvent.RECOGNIZER_READY);
            speechResult = csr.getResult();
            isMatch = isMatch(speechResult);
            if (!isMatch)
                fireRecognizerEvent(new RecognizerEvent(this, RecognizerEvent.INVALID_RESULT, new SphinxResult(speechResult)));
        } while (!isMatch);
        if (speechResult != null) {
            SphinxResult sphinxResult = new SphinxResult(speechResult);
            fireRecognizerEvent(sphinxResult);
            csr.stopRecognition();
            return sphinxResult;
        }
        return null;
    }

    private boolean isMatch(SpeechResult speechResult) {
        Grammar gr = context.getGrammar();
        String result = speechResult.getHypothesis().replaceAll("<PHONE_.*?> ?", "");
        return gr.match(result, gr.getRoot()) != null;
    }

    @Override protected void stopImpl() throws SpeechException {
        if (csr != null)
            csr.stopRecognition();
        vadInSpeech = false;
    }

    /** Return an array of supported languages */
    @Override public Language[] getLanguages() throws SpeechException {
        Collection<Language> langs = languageSettings.keySet();
        return langs.toArray(new Language[langs.size()]);
    }

    @Override public void setContext(RecognitionContext context) throws SpeechException {
        assert context instanceof SphinxContext : "you're feeding a context that I do not understand";
        this.context = (SphinxContext) context;
    }

    @Override public RecognitionContext getContext() throws SpeechException {
        return this.context;
    }

    @Override public SphinxContext createTemporaryContext(Grammar g, Domain domain) throws SpeechException {
        //TODO: ponder name, ponder timestamp
        return createContext("temp", g, domain, System.currentTimeMillis());
    }

    Map<Language, SphinxContext> contextCache = new HashMap<>();

    @Override protected SphinxContext createContext(String name, Grammar g, Domain domain, long timestamp) throws SpeechException {
        //TODO: figure out what to do if the grammar does not have a language
        assert g.getLanguage() != null;
        Language l = new Language(Language.findLocale(g.getLanguage()));
        assert l != null;
        if (!contextCache.containsKey(l)) {
            contextCache.put(l, new SphinxContext(name, g, this.languageSettings.get(l)));
        } else {
        }
        SphinxContext sc = contextCache.get(l);
        sc.setGrammar(g);
        return sc;
    }

    /** called during startup, possibly used to configure things via the GUI */
    @Override public Property<?>[] getProperties() {
        return null;
    }

    /** only ever called from TranscriptionWindow (and nobody seems to use that */
    @Override public String[] transcribe(String word, Language language) throws SpeechException {
        return null;
    }

    void evesdropOnFrontend(Data d) {
        if (d instanceof SpeechClassifiedData) {
            SpeechClassifiedData scd = (SpeechClassifiedData) d;
            if (scd.isSpeech() != vadInSpeech) {
                vadInSpeech = scd.isSpeech();
                fireRecognizerEvent(vadInSpeech ? RecognizerEvent.START_OF_SPEECH : RecognizerEvent.END_OF_SPEECH);
            }
            informAudioListeners(scd.getValues());
        }
    }

}
