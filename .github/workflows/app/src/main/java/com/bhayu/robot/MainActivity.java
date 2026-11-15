package com.bhayu.robot;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView status;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;

    private String language = "en-IN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 50, 35, 35);

        status = new TextView(this);
        status.setText("BHAYU AI is ready!");
        status.setTextSize(25);

        Button english = new Button(this);
        english.setText("English");

        Button hindi = new Button(this);
        hindi.setText("हिन्दी");

        Button listen = new Button(this);
        listen.setText("🎤 TALK TO BHAYU");

        layout.addView(status);
        layout.addView(english);
        layout.addView(hindi);
        layout.addView(listen);

        setContentView(layout);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                speak("Hello Ashutosh. BHAYU is ready.");
            }
        });

        english.setOnClickListener(v -> {
            language = "en-IN";
            status.setText("English selected");
            speak("English selected");
        });

        hindi.setOnClickListener(v -> {
            language = "hi-IN";
            status.setText("हिन्दी चुनी गई");
            speak("हिन्दी चुनी गई");
        });

        listen.setOnClickListener(v -> startListening());

        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }
    }

    private void startListening() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("Speech recognition unavailable");
            speak("Speech recognition is not available.");
            return;
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
            return;
        }

        if (recognizer != null) {
            recognizer.destroy();
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                status.setText("Listening...");
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                status.setText("Processing...");
            }

            @Override
            public void onError(int error) {
                status.setText("Try speaking again");
            }

            @Override
            public void onResults(Bundle results) {

                ArrayList<String> matches =
                        results.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                        );

                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0);
                    status.setText(command);
                    processCommand(command);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                language
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        recognizer.startListening(intent);
    }

    private void processCommand(String command) {

        String text = command.toLowerCase(Locale.ROOT);

        if (text.contains("who built you") ||
                text.contains("who created you") ||
                text.contains("how were you built") ||
                text.contains("तुम्हें किसने बनाया") ||
                text.contains("किसने बनाया")) {

            if (language.equals("hi-IN")) {
                speak("मुझे Ashutosh ने बनाया है।");
            } else {
                speak("I was built by Ashutosh.");
            }

        } else if (text.contains("hello") ||
                text.contains("hi") ||
                text.contains("नमस्ते") ||
                text.contains("हेलो")) {

            speak(language.equals("hi-IN")
                    ? "जी Ashutosh!"
                    : "Yes, Ashutosh?");

        } else if (text.contains("how are you") ||
                text.contains("कैसे हो")) {

            speak(language.equals("hi-IN")
                    ? "मैं बिल्कुल ठीक हूँ।"
                    : "I am doing great, Ashutosh.");

        } else if (text.contains("your name") ||
                text.contains("तुम्हारा नाम")) {

            speak("My name is BHAYU.");

        } else {

            speak(language.equals("hi-IN")
                    ? "माफ कीजिए, मैं समझ नहीं पाया।"
                    : "Sorry, I didn't understand that command.");
        }
    }

    private void speak(String message) {

        if (tts != null) {

            Locale locale = language.equals("hi-IN")
                    ? new Locale("hi", "IN")
                    : new Locale("en", "IN");

            tts.setLanguage(locale);

            tts.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "BHAYU_RESPONSE"
            );
        }
    }

    @Override
    protected void onDestroy() {

        if (recognizer != null) {
            recognizer.destroy();
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}
