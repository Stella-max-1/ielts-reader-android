package com.stella.ieltsreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReaderActivity extends Activity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private Article article;
    private Button playButton;
    private float speed = 1.0f;
    private final List<String> chunks = new ArrayList<>();
    private int chunkIndex = 0;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_reader);
        article = ArticleRepository.find(this, getIntent().getStringExtra("article_id"));
        ((TextView) findViewById(R.id.articleKicker)).setText(article.category);
        ((TextView) findViewById(R.id.articleTitle)).setText(article.title);
        ((TextView) findViewById(R.id.articleMeta)).setText(article.minutes + " min read  ·  Tap highlighted words to learn");
        renderBody();

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        playButton = findViewById(R.id.playButton);
        playButton.setEnabled(false);
        playButton.setOnClickListener(v -> toggleSpeech());
        findViewById(R.id.speedButton).setOnClickListener(v -> cycleSpeed((TextView) v));
        findViewById(R.id.wordsButton).setOnClickListener(v -> showSavedWords());
        setupProgress();
        tts = new TextToSpeech(this, this);
    }

    private void renderBody() {
        TextView body = findViewById(R.id.articleBody);
        SpannableString styled = new SpannableString(article.body);
        for (String word : Vocabulary.WORDS.keySet()) {
            Matcher matcher = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE).matcher(article.body);
            while (matcher.find()) {
                final String key = word;
                styled.setSpan(new BackgroundColorSpan(Color.rgb(255, 229, 154)), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styled.setSpan(new ClickableSpan() {
                    @Override public void onClick(View widget) { showWord(key); }
                }, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        body.setText(styled);
        body.setMovementMethod(LinkMovementMethod.getInstance());
        body.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupProgress() {
        ScrollView scroll = findViewById(R.id.readerScroll);
        ProgressBar progress = findViewById(R.id.readingProgress);
        int saved = getSharedPreferences("ielts_reader", MODE_PRIVATE).getInt("progress_" + article.id, 0);
        progress.setProgress(saved);
        scroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View child = scroll.getChildAt(0);
            int range = Math.max(1, child.getHeight() - scroll.getHeight());
            int value = Math.min(100, Math.round(scroll.getScrollY() * 100f / range));
            progress.setProgress(value);
            getSharedPreferences("ielts_reader", MODE_PRIVATE).edit().putInt("progress_" + article.id, value).apply();
        });
    }

    private void showWord(String word) {
        SharedPreferences prefs = getSharedPreferences("ielts_reader", MODE_PRIVATE);
        Set<String> saved = new HashSet<>(prefs.getStringSet("saved_words", new HashSet<>()));
        boolean exists = saved.contains(word);
        new AlertDialog.Builder(this).setTitle(word)
                .setMessage(Vocabulary.WORDS.get(word))
                .setNegativeButton("Hear", (d, w) -> { if (tts != null) tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word"); })
                .setPositiveButton(exists ? "Remove star" : "★ Save word", (d, w) -> {
                    if (exists) saved.remove(word); else saved.add(word);
                    prefs.edit().putStringSet("saved_words", saved).apply();
                }).show();
    }

    private void showSavedWords() {
        Set<String> saved = getSharedPreferences("ielts_reader", MODE_PRIVATE).getStringSet("saved_words", new HashSet<>());
        if (saved.isEmpty()) { Toast.makeText(this, "Tap a highlighted word and save it first.", Toast.LENGTH_LONG).show(); return; }
        StringBuilder text = new StringBuilder();
        for (String word : saved) text.append("★ ").append(word).append("\n   ").append(Vocabulary.WORDS.get(word)).append("\n\n");
        new AlertDialog.Builder(this).setTitle("My IELTS words").setMessage(text.toString().trim()).setPositiveButton("Done", null).show();
    }

    @Override public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.US);
            }
            tts.setSpeechRate(speed);
            tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
                @Override public void onStart(String id) { runOnUiThread(() -> playButton.setText("■  Stop")); }
                @Override public void onDone(String id) { runOnUiThread(() -> speakNext()); }
                @Override public void onError(String id) { runOnUiThread(() -> stopSpeech()); }
            });
            playButton.setEnabled(true);
        } else Toast.makeText(this, "Text-to-speech is unavailable on this device.", Toast.LENGTH_LONG).show();
    }

    private void toggleSpeech() {
        if (tts != null && tts.isSpeaking()) stopSpeech();
        else {
            chunks.clear(); chunkIndex = 0;
            for (String sentence : article.body.split("(?<=[.!?])\\s+")) if (!sentence.trim().isEmpty()) chunks.add(sentence.trim());
            speakNext();
        }
    }

    private void speakNext() {
        if (tts == null || chunkIndex >= chunks.size()) { stopSpeech(); return; }
        tts.setSpeechRate(speed);
        tts.speak(chunks.get(chunkIndex), TextToSpeech.QUEUE_FLUSH, null, "sentence_" + chunkIndex++);
    }

    private void stopSpeech() {
        if (tts != null) tts.stop();
        chunks.clear(); chunkIndex = 0;
        playButton.setText("▶  Listen");
    }

    private void cycleSpeed(TextView view) {
        speed = speed == 0.8f ? 1.0f : speed == 1.0f ? 1.2f : speed == 1.2f ? 1.5f : 0.8f;
        view.setText(String.format(Locale.US, "%.1f×", speed));
        if (tts != null) tts.setSpeechRate(speed);
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
