package com.fonuhuo.sevenrandom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final RandomEngine engine = new RandomEngine();

    private HistoryDbHelper db;
    private RandomEngine.RollingSession session;
    private RandomEngine.RollResult currentResult;
    private long currentRecordId = -1L;
    private boolean rolling = true;

    private final TextView[] numberViews = new TextView[3];
    private final TextView[] palaceViews = new TextView[3];
    private TextView finalView;
    private TextView hintView;
    private TextView interpretationView;
    private Button saveButton;

    private final Runnable previewTask = new Runnable() {
        @Override public void run() {
            if (!rolling || session == null) return;
            int[] n = session.preview();
            for (int i = 0; i < 3; i++) numberViews[i].setText(String.valueOf(n[i]));
            handler.postDelayed(this, 60L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new HistoryDbHelper(this);
        buildUi();
        startRolling();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (db != null) db.close();
        super.onDestroy();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.WHITE);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(18), dp(18), dp(24));
        scroll.addView(page, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.END);

        Button historyButton = new Button(this);
        historyButton.setText("历史记录");
        historyButton.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        actions.addView(historyButton);

        saveButton = new Button(this);
        saveButton.setText("保存");
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(v -> promptSaveName());
        actions.addView(saveButton);
        page.addView(actions);

        LinearLayout touchArea = new LinearLayout(this);
        touchArea.setOrientation(LinearLayout.VERTICAL);
        touchArea.setGravity(Gravity.CENTER_HORIZONTAL);
        touchArea.setPadding(0, dp(18), 0, dp(28));
        touchArea.setMinimumHeight(getResources().getDisplayMetrics().heightPixels - dp(130));
        page.addView(touchArea, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = text("三数六宫", 28, true);
        touchArea.addView(title);

        TextView sub = text("三数连续起宫", 14, false);
        sub.setTextColor(Color.DKGRAY);
        touchArea.addView(sub);

        LinearLayout nums = horizontalRow();
        LinearLayout pals = horizontalRow();
        for (int i = 0; i < 3; i++) {
            numberViews[i] = text("---", 32, true);
            palaceViews[i] = text("—", 19, true);
            nums.addView(numberViews[i], weighted());
            pals.addView(palaceViews[i], weighted());
        }
        addTopMargin(touchArea, nums, 28);
        addTopMargin(touchArea, pals, 8);

        TextView label = text("最终落宫", 15, false);
        label.setTextColor(Color.GRAY);
        addTopMargin(touchArea, label, 30);

        finalView = text("—", 36, true);
        addTopMargin(touchArea, finalView, 6);

        hintView = text("数字滚动中 · 点击空白处停止", 15, false);
        hintView.setTextColor(Color.DKGRAY);
        addTopMargin(touchArea, hintView, 20);

        interpretationView = text("", 15, false);
        interpretationView.setGravity(Gravity.START);
        interpretationView.setLineSpacing(0f, 1.25f);
        addTopMargin(touchArea, interpretationView, 24);

        TextView disclaimer = text("传统文化参考，不作为医疗、法律、投资等决策依据。", 12, false);
        disclaimer.setTextColor(Color.GRAY);
        addTopMargin(touchArea, disclaimer, 24);

        touchArea.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (rolling) stopRolling(event);
                else startRolling();
                v.performClick();
                return true;
            }
            return true;
        });

        setContentView(scroll);
    }

    private void startRolling() {
        rolling = true;
        currentResult = null;
        currentRecordId = -1L;
        session = engine.newSession();
        saveButton.setText("保存");
        saveButton.setEnabled(false);
        hintView.setText("数字滚动中 · 点击空白处停止");
        finalView.setText("—");
        interpretationView.setText("");
        for (TextView p : palaceViews) p.setText("—");
        handler.removeCallbacks(previewTask);
        handler.post(previewTask);
    }

    private void stopRolling(MotionEvent event) {
        if (!rolling || session == null) return;
        rolling = false;
        handler.removeCallbacks(previewTask);

        currentResult = session.stop(event.getEventTime(), event.getX(), event.getY());
        int[] n = currentResult.getNumbers();
        String[] p = currentResult.getPalaces();
        for (int i = 0; i < 3; i++) {
            numberViews[i].setText(String.valueOf(n[i]));
            palaceViews[i].setText(p[i]);
        }
        finalView.setText(currentResult.getFinalPalace());
        interpretationView.setText(DivinationEngine.interpret(p));
        currentRecordId = db.insertAuto(currentResult);
        saveButton.setEnabled(true);
        hintView.setText("已定数 · 再点空白处重新摇");
    }

    private void promptSaveName() {
        if (rolling || currentResult == null || currentRecordId <= 0) return;

        EditText input = new EditText(this);
        input.setHint("例如：工作面试 / 某件事");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        int pad = dp(18);
        LinearLayout holder = new LinearLayout(this);
        holder.setPadding(pad, 0, pad, 0);
        holder.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(this)
                .setTitle("命名并保存")
                .setView(holder)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "名称不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    db.markSaved(currentRecordId, name);
                    saveButton.setText("已保存");
                    saveButton.setEnabled(false);
                    Toast.makeText(this, "已保存到历史记录", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private LinearLayout horizontalRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        return row;
    }

    private LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(Color.BLACK);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void addTopMargin(LinearLayout parent, View child, int marginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(marginDp);
        parent.addView(child, lp);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
