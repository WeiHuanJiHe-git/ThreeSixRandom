package com.fonuhuo.sevenrandom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class HistoryDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "three_six_random.db";
    private static final int DB_VERSION = 1;

    public HistoryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "n1 INTEGER NOT NULL," +
                "n2 INTEGER NOT NULL," +
                "n3 INTEGER NOT NULL," +
                "p1 TEXT NOT NULL," +
                "p2 TEXT NOT NULL," +
                "p3 TEXT NOT NULL," +
                "saved INTEGER NOT NULL DEFAULT 0," +
                "created_at INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // v1 only
    }

    public long insertAuto(RandomEngine.RollResult result) {
        int[] n = result.getNumbers();
        String[] p = result.getPalaces();
        long now = System.currentTimeMillis();
        String defaultName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(now));

        ContentValues v = new ContentValues();
        v.put("name", defaultName);
        v.put("n1", n[0]);
        v.put("n2", n[1]);
        v.put("n3", n[2]);
        v.put("p1", p[0]);
        v.put("p2", p[1]);
        v.put("p3", p[2]);
        v.put("saved", 0);
        v.put("created_at", now);
        return getWritableDatabase().insertOrThrow("history", null, v);
    }

    public void markSaved(long id, String name) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("saved", 1);
        getWritableDatabase().update("history", v, "id=?", new String[]{String.valueOf(id)});
    }

    public List<HistoryRecord> getAll() {
        List<HistoryRecord> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                "history",
                new String[]{"id","name","n1","n2","n3","p1","p2","p3","saved","created_at"},
                null, null, null, null,
                "saved DESC, created_at DESC")) {
            while (c.moveToNext()) {
                HistoryRecord r = new HistoryRecord();
                r.id = c.getLong(0);
                r.name = c.getString(1);
                r.n1 = c.getInt(2);
                r.n2 = c.getInt(3);
                r.n3 = c.getInt(4);
                r.p1 = c.getString(5);
                r.p2 = c.getString(6);
                r.p3 = c.getString(7);
                r.saved = c.getInt(8) == 1;
                r.createdAt = c.getLong(9);
                out.add(r);
            }
        }
        return out;
    }
}
