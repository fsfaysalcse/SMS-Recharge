package com.faysal.smsautomation.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import me.everything.providers.android.contacts.Contact;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "recharge_app_db";
    private static final String TABLE_APP = "sms_recharge";

    private static final String KEY_ID = "id";
    private static final String KEY_SENDER_PHONE  = "key_sender_phone";
    private static final String KEY_RECEIVER_PHONE = "key_receiver_phone";
    private static final String KEY_BODY = "key_body";
    private static final String KEY_TIMESTAMP = "key_timestamp";
    private static final String KEY_PROCESS = "key_process";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_APP + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_SENDER_PHONE + " TEXT,"
                + KEY_RECEIVER_PHONE + " TEXT,"
                + KEY_BODY + " TEXT,"
                + KEY_TIMESTAMP + " TEXT,"
                + KEY_SENDER_PHONE + " TEXT,"
                + KEY_PROCESS + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP);

        // Create tables again
        onCreate(db);
    }

    // code to add the new contact

    void addSMS(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SENDER_PHONE, message.getSender());
        values.put(KEY_RECEIVER_PHONE, message.getReceiver());
        values.put(KEY_BODY, message.getBody());
        values.put(KEY_TIMESTAMP, message.getTimestamp());
        values.put(KEY_SENDER_PHONE, message.getSender());
        values.put(KEY_PROCESS, message.getProcess());
        db.insert(TABLE_APP, null, values);
        db.close();
    }

    // code to get the single contact
    Message getMessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_APP, new String[] { KEY_ID,
                        KEY_SENDER_PHONE, KEY_BODY }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Message contact = new Message(
                Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5)
        );
        // return contact
        return contact;
    }

    // code to get all contacts in a list view
    public List<Message> getAllMessage() {
        List<Message> messageList = new ArrayList<Message>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_APP;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Message contact = new Message();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setSender(cursor.getString(1));
                contact.setReceiver(cursor.getString(2));
                contact.setBody(cursor.getString(3));
                contact.setTimestamp(cursor.getString(4));
                contact.setProcess(cursor.getString(5));

                messageList.add(contact);
            } while (cursor.moveToNext());
        }

        return messageList;
    }

    // code to update the single contact
    public int updateContact(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SENDER_PHONE, message.getSender());
        values.put(KEY_RECEIVER_PHONE, message.getReceiver());
        values.put(KEY_BODY, message.getBody());
        values.put(KEY_TIMESTAMP, message.getTimestamp());
        values.put(KEY_SENDER_PHONE, message.getSender());
        values.put(KEY_PROCESS, message.getProcess());
        // updating row
        return db.update(TABLE_APP, values, KEY_ID + " = ?",
                new String[] { String.valueOf(message.getId()) });
    }

    // Deleting single message
    public void deleteContact(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APP, KEY_ID + " = ?",
                new String[] { String.valueOf(message.getId()) });
        db.close();
    }

    // Getting message Count
    public int getMessagesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_APP;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}
