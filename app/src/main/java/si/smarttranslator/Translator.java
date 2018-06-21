package si.smarttranslator;

import java.util.HashMap;
import java.util.Map;

public class Translator {
    private Map<String, String> dictionary = new HashMap<>();

    Translator() {
        dictionary.put("yes", "tak");
        dictionary.put("no", "nie");
        dictionary.put("left", "lewo");
        dictionary.put("right", "prawo");
        dictionary.put("on", "włączone");
        dictionary.put("off", "wyłączone");
        dictionary.put("stop", "zatrzymaj się");
        dictionary.put("go", "idź");
        dictionary.put("up", "góra");
    }

    public String getTranslation(String word) {
        return dictionary.get(word);
    }
}
