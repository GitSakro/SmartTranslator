package si.smarttranslator;

import android.util.Log;
import android.util.Pair;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Recognizer {
    private String LOG_TAG;
    private Thread recognitionThread;
    private boolean shouldContinueRecognition = true;
    private List<String> displayedLabels = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private TensorFlowInferenceInterface inferenceInterface;
    private RecognizeCommands recognizeCommands;
    private List<String> results = new ArrayList<>();


    public Recognizer(String log_tag, BufferedReader bufferedReader, TensorFlowInferenceInterface inferenceInterface) {
        LOG_TAG = log_tag;
        readLabelsFromFile(bufferedReader);
        this.inferenceInterface = inferenceInterface;
        recognizeCommands = new RecognizeCommands(labels);
    }

    public synchronized void startRecognition() {
        if (recognitionThread != null) {
            return;
        }
        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                recognize();
                            }
                        });
        recognitionThread.start();
    }

    public synchronized void stopRecognition() {
        if (recognitionThread == null) {
            return;
        }
        shouldContinueRecognition = false;
        recognitionThread = null;
    }

    private void recognize() {
        Log.v(LOG_TAG, "Start recognition");

        short[] inputBuffer;
        float[] floatInputBuffer = new float[TranslatorValues.RECORDING_LENGTH];
        float[] outputScores = new float[labels.size()];
        String[] outputScoresNames = new String[]{TranslatorValues.OUTPUT_SCORES_NAME};
        int[] sampleRateList = new int[]{TranslatorValues.SAMPLE_RATE};

        // Loop, grabbing recorded data and running the recognition model on it.
        while (shouldContinueRecognition) {
            inputBuffer = SharedDataHandler.readData();

            for (int i = 0; i < TranslatorValues.RECORDING_LENGTH; ++i) {
                floatInputBuffer[i] = inputBuffer[i] / 32767.0f;
            }

            // Run the model.
            inferenceInterface.feed(TranslatorValues.SAMPLE_RATE_NAME, sampleRateList);
            inferenceInterface.feed(TranslatorValues.INPUT_DATA_NAME, floatInputBuffer, TranslatorValues.RECORDING_LENGTH, 1);
            inferenceInterface.run(outputScoresNames);
            inferenceInterface.fetch(TranslatorValues.OUTPUT_SCORES_NAME, outputScores);

            // Use the smoother to figure out if we've had a real recognition event.
            long currentTime = System.currentTimeMillis();
            final RecognizeCommands.RecognitionResult result =
                    recognizeCommands.processLatestResults(outputScores, currentTime);
            Log.v("ezmajka", result.foundCommand);
            results.add(result.foundCommand);
            try {
                // We don't need to run too frequently, so snooze for a bit.
                Thread.sleep(TranslatorValues.MINIMUM_TIME_BETWEEN_SAMPLES_MS);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        Log.v(LOG_TAG, "End recognition");
    }

    private void readLabelsFromFile(BufferedReader bufferedReader) {
        String actualFilename = TranslatorValues.LABEL_FILENAME.split("file:///android_asset/")[1];
        Log.i(LOG_TAG, "Reading labels from: " + actualFilename);
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                labels.add(line);
                if (line.charAt(0) != '_') {
                    displayedLabels.add(line.substring(0, 1).toUpperCase() + line.substring(1));
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }
    }

    public Pair<String, String> getResults() {
        Pair<String, String> finalResult = new Pair<>("", "");
        Pair<Integer, Integer> countResults = new Pair<>(0, 0);
        for (String result : results) {
            if (result.equals(TranslatorValues.SILENCE_LABEL)) continue;
            int current = Collections.frequency(results, result);
            if (current > countResults.first) {
                countResults = new Pair<>(current, countResults.second);
                finalResult = new Pair<>(result, finalResult.second);
            } else if (current > countResults.second) {
                countResults = new Pair<>(countResults.first, current);
                finalResult = new Pair<>(finalResult.first, result);
            }
        }

        if(countResults.first - countResults.second >= 3) {
            finalResult = new Pair<>(finalResult.first, null);
        }

        results.clear();
        return finalResult;
    }

}


