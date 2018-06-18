package si.smarttranslator;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/** Reads in results from an instantaneous audio recognition model and smoothes them over time. */
public class RecognizeCommands {
    // Configuration settings.
    private List<String> labels;
    // Working variables.
    private Deque<Pair<Long, float[]>> previousResults = new ArrayDeque<>();
    private String previousTopLabel;
    private int labelsCount;
    private long previousTopLabelTime;
    private float previousTopLabelScore;

    public RecognizeCommands(List<String> inLabels) 
    {
        labels = inLabels;
        labelsCount = inLabels.size();
        previousTopLabel = VoiceRecognizionSettings.SILENCE_LABEL;
        previousTopLabelTime = Long.MIN_VALUE;
        previousTopLabelScore = 0.0f;
    }

    /** Holds information about what's been recognized. */
    public static class RecognitionResult {
        public final String foundCommand;
        public final float score;
        public final boolean isNewCommand;

        public RecognitionResult(String inFoundCommand, float inScore, boolean inIsNewCommand) {
            foundCommand = inFoundCommand;
            score = inScore;
            isNewCommand = inIsNewCommand;
        }
    }

    private static class ScoreForSorting implements Comparable<ScoreForSorting> {
        public final float score;
        public final int index;

        public ScoreForSorting(float inScore, int inIndex) {
            score = inScore;
            index = inIndex;
        }

        @Override
        public int compareTo(@NonNull ScoreForSorting other) {
            return Float.compare(other.score, this.score);
        }
    }

    public RecognitionResult processLatestResults(float[] currentResults, long currentTimeMS) {
        checkRequirements(currentResults, currentTimeMS);

        final int howManyResults = previousResults.size();

        if (ignoreTooFrequentResuts(currentTimeMS, howManyResults))
            return new RecognitionResult(previousTopLabel, previousTopLabelScore, false);

        previousResults.addLast(new Pair<>(currentTimeMS, currentResults));

        removeOldResults(currentTimeMS);

        if (isResultUnreliable(currentTimeMS, howManyResults))
            return new RecognitionResult(previousTopLabel, 0.0f, false);

        float[] averageScores = calculateAvarageScore(howManyResults);

        ScoreForSorting[] sortedAverageScores = sortResultDescending(averageScores);

        final int currentTopIndex = sortedAverageScores[0].index;
        final String currentTopLabel = labels.get(currentTopIndex);
        final float currentTopScore = sortedAverageScores[0].score;

        long timeSinceLastTop = checkRecentLabel(currentTimeMS);

        boolean isNewCommand = isNewCommand(currentTimeMS, currentTopLabel, currentTopScore, timeSinceLastTop);
        return new RecognitionResult(currentTopLabel, currentTopScore, isNewCommand);
    }

    private boolean isNewCommand(long currentTimeMS, String currentTopLabel, float currentTopScore, long timeSinceLastTop) {
        boolean isNewCommand;
        if ((currentTopScore > VoiceRecognizionSettings.DETECTION_THRESHOLD) && (timeSinceLastTop > VoiceRecognizionSettings.SUPPRESSION_MS)) {
            previousTopLabel = currentTopLabel;
            previousTopLabelTime = currentTimeMS;
            previousTopLabelScore = currentTopScore;
            isNewCommand = true;
        } else {
            isNewCommand = false;
        }
        return isNewCommand;
    }

    private long checkRecentLabel(long currentTimeMS) {
        long timeSinceLastTop;
        if (previousTopLabel.equals(VoiceRecognizionSettings.SILENCE_LABEL) || (previousTopLabelTime == Long.MIN_VALUE)) {
            timeSinceLastTop = Long.MAX_VALUE;
        } else {
            timeSinceLastTop = currentTimeMS - previousTopLabelTime;
        }
        return timeSinceLastTop;
    }

    @NonNull
    private ScoreForSorting[] sortResultDescending(float[] averageScores) {
        ScoreForSorting[] sortedAverageScores = new ScoreForSorting[labelsCount];
        for (int i = 0; i < labelsCount; ++i) {
            sortedAverageScores[i] = new ScoreForSorting(averageScores[i], i);
        }
        Arrays.sort(sortedAverageScores);
        return sortedAverageScores;
    }

    private float[] calculateAvarageScore(int howManyResults) {
        float[] averageScores = new float[labelsCount];
        for (Pair<Long, float[]> previousResult : previousResults) {
            final float[] scoresTensor = previousResult.second;
            int i = 0;
            while (i < scoresTensor.length) {
                averageScores[i] += scoresTensor[i] / howManyResults;
                ++i;
            }
        }
        return averageScores;
    }

    private boolean ignoreTooFrequentResuts(long currentTimeMS, int howManyResults) {
        if (howManyResults > 1) {
            final long timeSinceMostRecent = currentTimeMS - previousResults.getLast().first;
            if (timeSinceMostRecent < VoiceRecognizionSettings.MINIMUM_TIME_BETWEEN_SAMPLES_MS) {
                return true;
            }
        }
        return false;
    }

    private boolean isResultUnreliable(long currentTimeMS, int howManyResults) {
        final long earliestTime = previousResults.getFirst().first;
        final long samplesDuration = currentTimeMS - earliestTime;
        if ((howManyResults < VoiceRecognizionSettings.MINIMUM_COUNT)
                || (samplesDuration < (VoiceRecognizionSettings.AVERAGE_WINDOW_DURATION_MS / VoiceRecognizionSettings.MINIMUM_TIME_FRACTION))) {
            Log.v("RecognizeResult", "Too few results");
            return true;
        }
        return false;
    }

    private void removeOldResults(long currentTimeMS) {
        final long timeLimit = currentTimeMS - VoiceRecognizionSettings.AVERAGE_WINDOW_DURATION_MS;
        while (previousResults.getFirst().first < timeLimit) {
            previousResults.removeFirst();
        }
    }

    private void checkRequirements(float[] currentResults, long currentTimeMS) {
        if (currentResults.length != labelsCount) {
            throw new RuntimeException(
                    "The results for recognition should contain "
                            + labelsCount
                            + " elements, but there are "
                            + currentResults.length);
        }

        if ((!previousResults.isEmpty()) && (currentTimeMS < previousResults.getFirst().first)) {
            throw new RuntimeException(
                    "You must feed results in increasing time order, but received a timestamp of "
                            + currentTimeMS
                            + " that was earlier than the previous one of "
                            + previousResults.getFirst().first);
        }
    }
}
