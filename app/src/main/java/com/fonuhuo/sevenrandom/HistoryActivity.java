package com.fonuhuo.sevenrandom;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class HistoryActivity extends Activity {
    private HistoryDbHelper db;
    private List<HistoryRecord> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new HistoryDbHelper(this);
        buildUi();
    }

    @Override
    protected void onDestroy() {
        if (db != null) db.close();
        super.onDestroy();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(16));
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("历史记录");
        title.setTextSize(25);
        title.setTextColor(Color.BLACK);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText("★ = 主动命名保存；其余记录按时间自动保存");
        note.setTextSize(13);
        note.setTextColor(Color.GRAY);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteLp.topMargin = dp(6);
        root.addView(note, noteLp);

        ListView list = new ListView(this);
        records = db.getAll();
        ArrayAdapter<HistoryRecord> adapter = new ArrayAdapter<HistoryRecord>(this, android.R.layout.simple_list_item_2, android.R.id.text1, records) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                HistoryRecord r = getItem(position);
                TextView t1 = v.findViewById(android.R.id.text1);
                TextView t2 = v.findViewById(android.R.id.text2);
                t1.setText((r.saved ? "★ " : "") + r.name);
                t1.setTextColor(Color.BLACK);
                t1.setTypeface(Typeface.DEFAULT, r.saved ? Typeface.BOLD : Typeface.NORMAL);
                t2.setText(r.n1 + " · " + r.n2 + " · " + r.n3 + "    " + r.p1 + " → " + r.p2 + " → " + r.p3);
                t2.setTextColor(Color.DKGRAY);
                return v;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> showDetail(records.get(position)));

        root.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
    }

    private TextView emptyView() {
        TextView empty = new TextView(this);
        empty.setText("还没有历史记录");
        empty.setGravity(Gravity.CENTER);
        empty.setTextColor(Color.GRAY);
        return empty;
    }

    private void showDetail(HistoryRecord r) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(r.createdAt));
        String message = "数字：" + r.n1 + " / " + r.n2 + " / " + r.n3
                + "\n宫位：" + r.p1 + " → " + r.p2 + " → " + r.p3
                + "\n时间：" + time
                + "\n\n" + DivinationEngine.interpret(r.palaces());
        new AlertDialog.Builder(this)
                .setTitle((r.saved ? "★ " : "") + r.name)
                .setMessage(message)
                .setPositiveButton("关闭", null)
                .show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
