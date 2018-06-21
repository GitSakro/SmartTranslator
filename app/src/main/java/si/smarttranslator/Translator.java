package si.smarttranslator;

import java.util.HashMap;
import java.util.Map;

public class Translator {
    private Map<String, String> dictionary = new HashMap<>();

    Translator() {
        dictionary.put(TranslatorValues.COMMAND_YES, "tak");
        dictionary.put(TranslatorValues.COMMAND_NO, "nie");
        dictionary.put(TranslatorValues.COMMAND_LEFT, "lewo");
        dictionary.put(TranslatorValues.COMMAND_RIGHT, "prawo");
        dictionary.put(TranslatorValues.COMMAND_ON, "włączone");
        dictionary.put(TranslatorValues.COMMAND_OFF, "wyłączone");
        dictionary.put(TranslatorValues.COMMAND_STOP, "zatrzymaj się");
        dictionary.put(TranslatorValues.COMMAND_GO, "idź");
        dictionary.put(TranslatorValues.COMMAND_UP, "góra");
        dictionary.put(TranslatorValues.COMMAND_DOWN, "dół");
    }

    public String getTranslation(String word) {
        return dictionary.get(word);
    }
}
