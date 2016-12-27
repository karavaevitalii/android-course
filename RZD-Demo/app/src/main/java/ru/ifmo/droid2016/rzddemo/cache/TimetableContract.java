package ru.ifmo.droid2016.rzddemo.cache;

import android.provider.BaseColumns;

public final class TimetableContract {
    public interface TimetableColumns extends BaseColumns {

        String DEPARTURE_DATE = "departure date";

        String DEPARTURE_STATION_ID = "departure station id";

        String DEPARTURE_STATION_NAME = "departure station name";

        String DEPARTURE_TIME = "departure time";

        String ARRIVAL_STATION_ID = "arrival station id";

        String ARRIVAL_STATION_NAME = "arrival station name";

        String ARRIVAL_TIME = "arrival time";

        String TRAIN_ROUTE_ID = "train route id";

        String TRAIN_NAME = "train name";

        String ROUTE_START_STATION_NAME = "route start station name";

        String ROUTE_END_STATION_NAME = "route end station name";

        String[] V1_FIELDS = {DEPARTURE_STATION_ID
                , DEPARTURE_STATION_NAME
                , DEPARTURE_TIME
                , ARRIVAL_STATION_ID
                , ARRIVAL_STATION_NAME
                , ARRIVAL_TIME
                , TRAIN_ROUTE_ID
                , ROUTE_START_STATION_NAME
                , ROUTE_END_STATION_NAME};

        String[] V2_FIELDS = {DEPARTURE_STATION_ID
                , DEPARTURE_STATION_NAME
                , DEPARTURE_TIME
                , ARRIVAL_STATION_ID
                , ARRIVAL_STATION_NAME
                , ARRIVAL_TIME
                , TRAIN_ROUTE_ID
                , ROUTE_START_STATION_NAME
                , ROUTE_END_STATION_NAME
                , TRAIN_NAME
        };
    }

    public static final class Timetable implements TimetableColumns {

        public static final String TABLE = "timetable";

        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE
                + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + DEPARTURE_DATE + " INTEGER, "
                + DEPARTURE_STATION_ID + " TEXT, "
                + DEPARTURE_STATION_NAME + " TEXT, "
                + DEPARTURE_TIME + " INTEGER, "
                + ARRIVAL_STATION_ID + " TEXT, "
                + ARRIVAL_STATION_NAME + " TEXT, "
                + ARRIVAL_TIME + " INTEGER, "
                + TRAIN_ROUTE_ID + " TEXT, "
                + ROUTE_START_STATION_NAME + " TEXT, "
                + ROUTE_END_STATION_NAME + " TEXT";

        public static final String CREATE_TABLE_V1 = CREATE_TABLE + ")";

        public static final String CREATE_TABLE_V2 = CREATE_TABLE + ", " + TRAIN_NAME + " TEXT)";

    }

    public TimetableContract() {
    }
}
