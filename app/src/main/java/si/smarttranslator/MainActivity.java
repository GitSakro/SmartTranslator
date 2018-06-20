package si.smarttranslator;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ImageButton micButton;
    private Recorder recorder;
    private Recognizer recognizer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Start the recording and recognition threads.
        requestMicrophonePermission();
        recorder = new Recorder(LOG_TAG);

        // Load the TensorFlow model.
        TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface(getAssets(), TranslatorValues.MODEL_FILENAME);
        String actualFilename = TranslatorValues.LABEL_FILENAME.split("file:///android_asset/")[1];
        try {
            recognizer = new Recognizer(LOG_TAG,new BufferedReader(new InputStreamReader(getAssets().open(actualFilename))),inferenceInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }

        handleVoiceButton();
    }



    @SuppressLint("ClickableViewAccessibility")
    private void handleVoiceButton() {
        micButton = this.findViewById(R.id.microphoneSwitcher);
        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    handleButtonClicked();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    handleButtonRealesed();
                }
                return true;
            }
        });
    }

    private void handleButtonRealesed() {
        micButton.setImageResource(R.drawable.mic_off);
        recorder.stopRecording();
        recognizer.stopRecognition();
    }

    private void handleButtonClicked() {
        micButton.setImageResource(R.drawable.mic_recording);
        recorder.startRecording();
        recognizer.startRecognition();
    }

    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, TranslatorValues.REQUEST_RECORD_AUDIO);
        }
    }





}
