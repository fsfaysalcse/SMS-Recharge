package com.faysal.smsautomation.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PhoneSms.class}, version = 1)
public abstract class SmsDatabase extends RoomDatabase {
    private static SmsDatabase instance;
    public abstract PhoneSmsDao phoneSmsDao();
    public static synchronized SmsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    SmsDatabase.class, "sms_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
