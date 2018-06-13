package si.smarttranslator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    ImageButton micButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        micButton = this.findViewById(R.id.microphoneSwitcher);

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    micButton.setImageResource(R.drawable.mic_recording);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    micButton.setImageResource(R.drawable.mic_off);
                }
                return true;
            }
        });
    }
}
