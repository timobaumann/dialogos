package edu.cmu.lti.dialogos.sphinx.client;

import com.stanfy.enroscar.net.DataStreamHandler;
import edu.cmu.sphinx.linguist.dictionary.TextDictionary;
import edu.cmu.sphinx.linguist.dictionary.Word;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by timo on 18.11.17.
 */
public class ExtensibleDictionary extends TextDictionary {

    static {
        try {
            URL.setURLStreamHandlerFactory(protocol -> "data".equals(protocol) ? new DataStreamHandler() : null);
        } catch (Error e) {
            if (!"factory already defined".equals(e.getMessage())) {
                System.err.println("ExtensibleDictionary");
                throw e;
            }
        }
    }

    @Override
    public Word getWord(String text) {
        Word w = super.getWord(text);
        if (w == null)
            w = super.getWord(text.toLowerCase());
        if (w == null)
            w = super.getWord(text.toUpperCase());
        return w;
    }

    public void loadExceptions(List<G2PEntry> g2pList) throws IOException {
        if (!g2pList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (G2PEntry e : g2pList) {
                sb.append(e.getGraphemes());
                sb.append(" ");
                sb.append(e.getPhonemes());
                sb.append("\n");
            }
            addendaUrlList.clear();
            //System.err.println("after encoding, data is: " + DataURLHelper.encodeData(sb.toString()));
            addendaUrlList.add(new URL(DataURLHelper.encodeData(sb.toString())));
        }
    }

}
