package si.smarttranslator;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class Recorder {
    private Thread recordingThread;
    private boolean shouldContinue = true;
    private String LOG_TAG;
    // Working variables.
    private int recordingOffset = 0;

    public Recorder(String log_tag) {
        this.LOG_TAG = log_tag;
    }
    public synchronized void startRecording() {
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        recordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                record();
                            }
                        });
        recordingThread.start();
    }
    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        int bufferSize = estimateBufforSize();

        short[] audioBuffer = new short[bufferSize / 2];

        AudioRecord record = createAudioRecord(bufferSize);
        if (record == null) return;

        record.startRecording();

        Log.v(LOG_TAG, "Start recording");

        // Loop, gathering audio data and copying it to a round-robin buffer.
        while (shouldContinue) {
            SampleData data = gatherData(audioBuffer, record);
            SharedDataHandler.writeData(audioBuffer,recordingOffset, data);
        }
        record.stop();
        record.release();
        Log.v(LOG_TAG, "stop");

    }

    @NonNull
    private SampleData gatherData(short[] audioBuffer, AudioRecord record) {
        int numberRead = record.read(audioBuffer, 0, audioBuffer.length);
        int maxLength = SharedDataHandler.getRecordingBufferLenght();
        int newRecordingOffset = recordingOffset + numberRead;
        int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
        int firstCopyLength = numberRead - secondCopyLength;
        return new SampleData(numberRead,maxLength,newRecordingOffset,secondCopyLength,firstCopyLength);
    }

    private int estimateBufforSize() {
        // Estimate the buffer size we'll need for this device.
        int bufferSize = AudioRecord.getMinBufferSize(TranslatorValues.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = TranslatorValues.SAMPLE_RATE * 2;
        }
        return bufferSize;
    }

    @Nullable
    private AudioRecord createAudioRecord(int bufferSize) {
        AudioRecord record =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        TranslatorValues.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return null;
        }
        return record;
    }
    public synchronized void stopRecording() {
        if (recordingThread == null) {
            return;
        }
        shouldContinue = false;
        recordingThread = null;
    }
}
