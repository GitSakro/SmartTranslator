package si.smarttranslator;

import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;

public class SharedDataHandler {
    private static final ReentrantLock recordingBufferLock = new ReentrantLock();
    private static short[] recordingBuffer = new short[TranslatorValues.RECORDING_LENGTH];
    private static boolean isRecordingOn = false;
    private static int recordingOffset = 0;

    public static void writeData(short[] audioBuffer, SampleData data){
        recordingBufferLock.lock();
        System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, data.firstCopyLength);
        System.arraycopy(audioBuffer, data.firstCopyLength, recordingBuffer, 0, data.secondCopyLength);
        recordingOffset = data.newRecordingOffset % data.maxLength;
        recordingBufferLock.unlock();
        isRecordingOn = true;
    }
    public static int getRecordingBufferLenght(){
        recordingBufferLock.lock();
        int length = recordingBuffer.length;
        recordingBufferLock.unlock();
        return length;
    }
    public static short [] readData(){
        while(!isRecordingOn){}
        short[] inputBuffer = new short[TranslatorValues.RECORDING_LENGTH];
        recordingBufferLock.lock();
        System.arraycopy(recordingBuffer, recordingOffset, inputBuffer, 0, TranslatorValues.RECORDING_LENGTH - recordingOffset);
        System.arraycopy(recordingBuffer, 0, inputBuffer, TranslatorValues.RECORDING_LENGTH - recordingOffset, recordingOffset);
        recordingBufferLock.unlock();
        return inputBuffer;
    }
    public static int getRecordingOffset(){
        return recordingOffset;
    }
    public static void clearBuffer(){
        recordingBufferLock.lock();
        recordingBuffer = new short[TranslatorValues.RECORDING_LENGTH];
        recordingOffset = 0;
        recordingBufferLock.unlock();
    }
}
