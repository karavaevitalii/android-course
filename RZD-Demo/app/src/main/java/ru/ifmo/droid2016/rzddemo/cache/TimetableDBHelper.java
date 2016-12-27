package ru.ifmo.droid2016.rzddemo.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

import ru.ifmo.droid2016.rzddemo.utils.DatabaseCorruptionHandler;

public class TimetableDBHelper extends SQLiteOpenHelper {

    private static final String DB_FILE_NAME = "timetable.db";

    @DataSchemeVersion
    private final int VERSION;

    private static volatile TimetableDBHelper instance;

    public static TimetableDBHelper getInstance(Context c, @DataSchemeVersion int v) {
        if (instance == null) {
            synchronized (TimetableDBHelper.class) {
                if (instance == null) {
                    instance = new TimetableDBHelper(c, v);
                }
            }
        }

        return instance;
    }

    private final Context c;

    public TimetableDBHelper(Context c, @DataSchemeVersion int v) {
        super(c, DB_FILE_NAME, null, v, new DatabaseCorruptionHandler(c, DB_FILE_NAME));
        this.c = c.getApplicationContext();
        this.VERSION = v;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(VERSION == DataSchemeVersion.V1
                ? TimetableContract.Timetable.CREATE_TABLE_V1
                : TimetableContract.Timetable.CREATE_TABLE_V2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("ALTER TABLE " + TimetableContract.Timetable.TABLE
                + " ADD COLUMN " + TimetableContract.Timetable.TRAIN_NAME);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String tmpTable = TimetableContract.Timetable.TABLE + "_TMP";

        db.execSQL("ALTER TABLE " + TimetableContract.Timetable.TABLE + " RENAME TO " + tmpTable);
        db.execSQL(TimetableContract.Timetable.CREATE_TABLE_V1);

        /*StringBuilder sb = new StringBuilder();
        sb.append(DEPARTURE_DATE).append(", ");
        for (int i = 0; i < V1_FIELDS.length; i++) {
            sb.append(V1_FIELDS[i]);
            if (i != V1_FIELDS.length - 1) {
                sb.append(", ");
            }
        }

        db.execSQL("INSERT INTO " + TABLE + " (" + sb.toString() +
                ") SELECT " + sb.toString() + " FROM " + tmpTable);
        db.execSQL("DROP TABLE " + tmpTable);*/

        String allColumns = TimetableContract.Timetable.DEPARTURE_DATE + ", ";
        for (int i = 0; i < TimetableContract.Timetable.V1_FIELDS.length; i++) {
            allColumns += TimetableContract.Timetable.V1_FIELDS[i];
            if (i != TimetableContract.Timetable.V1_FIELDS.length - 1) {
                allColumns += ", ";
            }
        }
        db.execSQL("INSERT INTO " + TimetableContract.Timetable.TABLE
                + " (" + allColumns + ") SELECT " + allColumns + " FROM " + tmpTable);
        db.execSQL("DROP TABLE " + tmpTable);
    }

    public void dropDB() {
        SQLiteDatabase db = getWritableDatabase();

        if (db.isOpen()) {
            try {
                db.close();
            } catch (Exception ignored) {
            }
        }

        final File f = c.getDatabasePath(DB_FILE_NAME);
        try {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        } catch (Exception ignored) {
        }
    }
}
