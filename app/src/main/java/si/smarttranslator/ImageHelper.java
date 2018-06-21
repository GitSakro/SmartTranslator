package si.smarttranslator;

import java.util.HashMap;
import java.util.Map;

public class ImageHelper {
    Map<String, Integer> dictionary = new HashMap<>();

    ImageHelper() {
        dictionary.put(TranslatorValues.COMMAND_DOWN, R.drawable.down);
        dictionary.put(TranslatorValues.COMMAND_UP, R.drawable.up);
        dictionary.put(TranslatorValues.COMMAND_GO, R.drawable.go);
        dictionary.put(TranslatorValues.COMMAND_LEFT, R.drawable.left);
        dictionary.put(TranslatorValues.COMMAND_NO, R.drawable.no);
        dictionary.put(TranslatorValues.COMMAND_OFF, R.drawable.off);
        dictionary.put(TranslatorValues.COMMAND_ON, R.drawable.on);
        dictionary.put(TranslatorValues.COMMAND_RIGHT, R.drawable.right);
        dictionary.put(TranslatorValues.COMMAND_STOP, R.drawable.stop);
        dictionary.put(TranslatorValues.COMMAND_YES, R.drawable.yes);
    }

    public Integer getImageName(String command){
        return dictionary.get(command);
    }
}
