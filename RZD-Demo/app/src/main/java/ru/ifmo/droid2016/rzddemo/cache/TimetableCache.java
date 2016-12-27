package ru.ifmo.droid2016.rzddemo.cache;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ru.ifmo.droid2016.rzddemo.model.TimetableEntry;
import ru.ifmo.droid2016.rzddemo.utils.TimeUtils;

import static ru.ifmo.droid2016.rzddemo.Constants.LOG_DATE_FORMAT;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.Timetable.TABLE;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.ARRIVAL_STATION_ID;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.ARRIVAL_STATION_NAME;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.ARRIVAL_TIME;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.DEPARTURE_DATE;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.DEPARTURE_STATION_ID;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.DEPARTURE_STATION_NAME;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.DEPARTURE_TIME;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.ROUTE_END_STATION_NAME;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.ROUTE_START_STATION_NAME;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.TRAIN_NAME;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.TRAIN_ROUTE_ID;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.V1_FIELDS;
import static ru.ifmo.droid2016.rzddemo.cache.TimetableContract.TimetableColumns.V2_FIELDS;

/**
 * Кэш расписания поездов.
 * <p>
 * Ключом является комбинация трех значений:
 * ID станции отправления, ID станции прибытия, дата в москомском часовом поясе
 * <p>
 * Единицей хранения является список поездов - {@link TimetableEntry}.
 */


public class TimetableCache {

    @NonNull
    private final Context context;

    /**
     * Версия модели данных, с которой работает кэш.
     */


    @DataSchemeVersion
    private final int version;

    /**
     * Создает экземпляр кэша с указанной версией модели данных.
     * <p>
     * Может вызываться на любом (в том числе UI потоке). Может быть создано несколько инстансов
     * {@link TimetableCache} -- все они должны потокобезопасно работать с одним физическим кэшом.
     */


    @AnyThread
    public TimetableCache(@NonNull Context context,
                          @DataSchemeVersion int version) {
        this.context = context.getApplicationContext();
        this.version = version;
    }

    /**
     * Берет из кэша расписание - список всех поездов, следующих по указанному маршруту с
     * отправлением в указанную дату.
     *
     * @param fromStationId ID станции отправления
     * @param toStationId   ID станции прибытия
     * @param dateMsk       дата в московском часовом поясе
     * @return - список {@link TimetableEntry}
     * @throws FileNotFoundException - если в кэше отсуствуют запрашиваемые данные.
     */


    @WorkerThread
    @NonNull
    public List<TimetableEntry> get(@NonNull String fromStationId,
                                    @NonNull String toStationId,
                                    @NonNull Calendar dateMsk)
            throws FileNotFoundException {
        SQLiteDatabase db = TimetableDBHelper.getInstance(context, version).getReadableDatabase();
        String[] projection = (version == DataSchemeVersion.V1
                ? V1_FIELDS
                : V2_FIELDS);

        List<TimetableEntry> timetable = new ArrayList<>();

        String selection = DEPARTURE_STATION_ID + "=? AND " +
                ARRIVAL_STATION_ID + "=? AND " +
                DEPARTURE_DATE + "=?";

        Cursor c = null;
        try {
            c = db.query(TABLE, projection, selection,
                    new String[]{fromStationId, toStationId, Objects.toString(getDate(dateMsk))},
                    null, null, null);

            if (c != null && c.moveToNext()) {
                for (; !c.isAfterLast(); c.moveToNext()) {
                    int i = 0;
                    String departureStationId = c.getString(i++);
                    String departureStationName = c.getString(i++);
                    Calendar departureTime = getTime(c.getLong(i++));
                    String arrivalStationId = c.getString(i++);
                    String arrivalStationName = c.getString(i++);
                    Calendar arrivalTime = getTime(c.getLong(i++));
                    String trainRouteId = c.getString(i++);
                    String routeStartStationName = c.getString(i++);
                    String routeEndStationName = c.getString(i++);
                    String trainName = (version == DataSchemeVersion.V1
                            ? null
                            : c.getString(i));

                    timetable.add(new TimetableEntry(departureStationId, departureStationName
                            , departureTime, arrivalStationId, arrivalStationName, arrivalTime
                            , trainRouteId, trainName, routeStartStationName, routeEndStationName));
                }
            } else {
                throw new FileNotFoundException("No data in timetable cache for: fromStationId="
                        + fromStationId + ", toStationId=" + toStationId
                        + ", dateMsk=" + LOG_DATE_FORMAT.format(dateMsk.getTime()));
            }
        } catch (SQLiteException e) {
            Log.wtf(TAG, "Query error: ", e);
            throw new FileNotFoundException("No data in timetable cache for: fromStationId="
                    + fromStationId + ", toStationId=" + toStationId
                    + ", dateMsk=" + LOG_DATE_FORMAT.format(dateMsk.getTime()));
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }

        return timetable;
    }

    private Calendar getTime(long time) {
        Calendar calendar = Calendar.getInstance(TimeUtils.getMskTimeZone());
        calendar.setTime(new Date(time));
        return calendar;
    }

    private long getDate(Calendar dateMsk) {
        return dateMsk.get(Calendar.DAY_OF_YEAR) + dateMsk.get(Calendar.YEAR) * 500;
    }

    @WorkerThread
    public void put(@NonNull String fromStationId,
                    @NonNull String toStationId,
                    @NonNull Calendar dateMsk,
                    @NonNull List<TimetableEntry> timetable) {
        SQLiteDatabase db = TimetableDBHelper.getInstance(context, version).getWritableDatabase();

        db.beginTransaction();
        /*StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ")
                .append(TABLE).append(" (")
                .append(DEPARTURE_DATE).append(", ")
                .append(DEPARTURE_STATION_ID).append(", ")
                .append(DEPARTURE_STATION_NAME).append(", ")
                .append(DEPARTURE_TIME).append(", ")
                .append(ARRIVAL_STATION_ID).append(", ")
                .append(ARRIVAL_STATION_NAME).append(", ")
                .append(ARRIVAL_TIME).append(", ")
                .append(TRAIN_ROUTE_ID).append(", ")
                .append(ROUTE_START_STATION_NAME).append(", ")
                .append(ROUTE_END_STATION_NAME);
        if (version == DataSchemeVersion.V2) {
            sb.append(", ").append(TRAIN_NAME)
                    .append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        } else {
            sb.append(") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }*/


        String insertion = "INSERT INTO " + TABLE + " ("
                + DEPARTURE_DATE + ", "
                + DEPARTURE_STATION_ID + ", "
                + DEPARTURE_STATION_NAME + ", "
                + DEPARTURE_TIME + ", "
                + ARRIVAL_STATION_ID + ", "
                + ARRIVAL_STATION_NAME + ", "
                + ARRIVAL_TIME + ", "
                + TRAIN_ROUTE_ID + ", "
                + ROUTE_START_STATION_NAME + ", "
                + ROUTE_END_STATION_NAME;
        if (version == DataSchemeVersion.V2) {
            insertion += ", " + TRAIN_NAME + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            insertion += ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        SQLiteStatement insert = null;
        try {
            insert = db.compileStatement(insertion/*sb.toString()*/);
            for (TimetableEntry entry : timetable) {
                getEntry(insert, entry, dateMsk);
                insert.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (insert != null) {
                try {
                    insert.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void getEntry(SQLiteStatement insert, TimetableEntry entry, Calendar dateMsk) {
        int i = 0;
        insert.bindLong(++i, getDate(dateMsk));
        insert.bindString(++i, entry.departureStationId);
        insert.bindString(++i, entry.departureStationName);
        insert.bindLong(++i, entry.departureTime.getTimeInMillis());
        insert.bindString(++i, entry.arrivalStationId);
        insert.bindString(++i, entry.arrivalStationName);
        insert.bindLong(++i, entry.arrivalTime.getTimeInMillis());
        insert.bindString(++i, entry.trainRouteId);
        insert.bindString(++i, entry.routeStartStationName);
        insert.bindString(++i, entry.routeEndStationName);
        if (version == DataSchemeVersion.V1) {
            insert.bindNull(++i);
        } else {
            if (entry.trainName != null) {
                insert.bindString(++i, entry.trainName);
            }
        }
    }

    private static final String TAG = "TimetableCache";
}
