package si.smarttranslator;

import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;

public class SharedDataHandler {
    private static final ReentrantLock recordingBufferLock = new ReentrantLock();
    private static short[] recordingBuffer = new short[TranslatorValues.RECORDING_LENGTH];
    private static SampleData lastSampleData;
    public static void writeData(short[] audioBuffer,int recordingOffset, SampleData data){
        recordingBufferLock.lock();
        lastSampleData = data;
        Log.v("ezjasar","hererererer");
        System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, data.firstCopyLength);
        System.arraycopy(audioBuffer, data.firstCopyLength, recordingBuffer, 0, data.secondCopyLength);
        recordingBufferLock.unlock();
    }
    public static int getRecordingBufferLenght(){
        recordingBufferLock.lock();
        int length = recordingBuffer.length;
        recordingBufferLock.unlock();
        return length;
    }
    public static short [] readData(){
        short[] inputBuffer = new short[TranslatorValues.RECORDING_LENGTH];
        recordingBufferLock.lock();
        System.arraycopy(recordingBuffer, lastSampleData.newRecordingOffset, inputBuffer, 0, lastSampleData.firstCopyLength);
        System.arraycopy(recordingBuffer, 0, inputBuffer, lastSampleData.firstCopyLength, lastSampleData.secondCopyLength);
        recordingBufferLock.unlock();
        return inputBuffer;
    }

}
