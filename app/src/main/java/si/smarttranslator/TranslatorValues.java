package si.smarttranslator;

public class TranslatorValues {
    // Constants that control the behavior of the recognition code and model
    // settings.
    public static final int SAMPLE_RATE = 16000;
    public static final int SAMPLE_DURATION_MS = 1000;
    public static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    public static final long AVERAGE_WINDOW_DURATION_MS = 500;
    public static final float DETECTION_THRESHOLD = 0.70f;
    public static final int SUPPRESSION_MS = 1500;
    public static final int MINIMUM_COUNT = 3;
    public static final long MINIMUM_TIME_BETWEEN_SAMPLES_MS = 30;
    public static final String LABEL_FILENAME = "file:///android_asset/labels.txt";
    public static final String MODEL_FILENAME = "file:///android_asset/my_frozen_graph.pb";
    public static final String INPUT_DATA_NAME = "decoded_sample_data:0";
    public static final String SAMPLE_RATE_NAME = "decoded_sample_data:1";
    public static final String OUTPUT_SCORES_NAME = "labels_softmax";
    // UI elements.
    public static final int REQUEST_RECORD_AUDIO = 13;
    static final long MINIMUM_TIME_FRACTION = 4;
    static final String SILENCE_LABEL = "_silence_";
}
