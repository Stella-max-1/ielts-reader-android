package com.stella.ieltsreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout list;
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.articleList);
        findViewById(R.id.importButton).setOnClickListener(v -> showImportDialog());
        String today = new SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH).format(new Date());
        ((TextView) findViewById(R.id.streakText)).setText(today + "  ·  Keep your reading streak alive");
    }

    @Override protected void onResume() {
        super.onResume();
        renderArticles();
    }

    private void renderArticles() {
        list.removeAllViews();
        for (Article article : ArticleRepository.all(this)) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.card_background);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(0, 0, 0, dp(12));
            card.setLayoutParams(params);
            card.setElevation(dp(2));

            TextView category = text(article.category, 12, Color.rgb(40, 89, 197), true);
            TextView title = text(article.title, 21, Color.rgb(26, 35, 51), true);
            title.setPadding(0, dp(7), 0, dp(9));
            int progress = getSharedPreferences("ielts_reader", MODE_PRIVATE).getInt("progress_" + article.id, 0);
            TextView meta = text(article.minutes + " min read  ·  " + (progress > 0 ? progress + "% complete" : "New"), 13, Color.rgb(101, 112, 133), false);
            card.addView(category); card.addView(title); card.addView(meta);
            card.setOnClickListener(v -> startActivity(new Intent(this, ReaderActivity.class).putExtra("article_id", article.id)));
            list.addView(card);
        }
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private void showImportDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), 0, dp(20), 0);
        EditText title = new EditText(this);
        title.setHint("Article title");
        EditText body = new EditText(this);
        body.setHint("Paste the English article here");
        body.setMinLines(8);
        body.setGravity(android.view.Gravity.TOP);
        body.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        form.addView(title); form.addView(body);
        new AlertDialog.Builder(this).setTitle("Add reading material").setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    if (title.getText().toString().trim().isEmpty() || body.getText().toString().trim().length() < 80) {
                        Toast.makeText(this, "Please add a title and at least 80 characters.", Toast.LENGTH_LONG).show(); return;
                    }
                    try { ArticleRepository.saveCustom(this, title.getText().toString().trim(), body.getText().toString().trim()); renderArticles(); }
                    catch (Exception e) { Toast.makeText(this, "Could not save the article.", Toast.LENGTH_SHORT).show(); }
                }).show();
    }
}
