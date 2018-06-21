package si.smarttranslator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ImageButton micButton;
    private Recorder recorder;
    private Recognizer recognizer;

    TextView textRecognizedField;
    TextView textTranslatedField;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textRecognizedField = findViewById(R.id.text_recognized);
        textTranslatedField = findViewById(R.id.text_translated);

        // Start the recording and recognition threads.
        requestMicrophonePermission();
        recorder = new Recorder(LOG_TAG);

        // Load the TensorFlow model.
        TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface(getAssets(), TranslatorValues.MODEL_FILENAME);
        String actualFilename = TranslatorValues.LABEL_FILENAME.split("file:///android_asset/")[1];
        try {
            recognizer = new Recognizer(LOG_TAG, new BufferedReader(new InputStreamReader(getAssets().open(actualFilename))), inferenceInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }

        handleVoiceButton();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void handleVoiceButton() {
        micButton = this.findViewById(R.id.microphoneSwitcher);
        micButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                handleButtonClicked();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                handleButtonRealised();
            }
            return true;
        });
    }

    private void handleButtonRealised() {
        micButton.setImageResource(R.drawable.vs_micbtn_off);
        recorder.stopRecording();
        try {
            Context context = getApplicationContext();
            synchronized (context) {
                context.wait(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        recognizer.stopRecognition();

        Pair<String, String> recognizedText = recognizer.getResults();
        displayResults(recognizedText);
        SharedDataHandler.clearBuffer();
    }

    private void displayResults(Pair<String, String> recognizedText) {
        if (!recognizedText.first.equals("_unknown_")) {
            if (recognizedText.second != null && !recognizedText.second.equals("_unknown_")) {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                CharSequence items[] = new CharSequence[]{recognizedText.first, recognizedText.second};
                adb.setItems(items, (dialog, which) -> {
                    setTranslation(items[which].toString());
                });
                adb.setTitle("What did you say?");
                adb.show();
            } else {
                setTranslation(recognizedText.first);
            }
        } else {

            resetTranslation();
        }
    }

    private void resetTranslation() {
        textTranslatedField.setText("");
        textRecognizedField.setText("");
        ImageView imageCommand = this.findViewById(R.id.image_command);
        imageCommand.setVisibility(View.INVISIBLE);
    }

    private void setTranslation(String item) {
        textRecognizedField.setText(item);
        Translator translator = new Translator();
        System.out.println(translator.getTranslation(item));
        textTranslatedField.setText(translator.getTranslation(item));

        ImageView imageCommand = this.findViewById(R.id.image_command);
        Integer id = new ImageHelper().getImageName(item);
        if(id != null) {
            imageCommand.setVisibility(View.VISIBLE);
            imageCommand.setImageResource(id);
        }
    }

    private void handleButtonClicked() {
        micButton.setImageResource(R.drawable.vs_micbtn_on);
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
