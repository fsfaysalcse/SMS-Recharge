package com.faysal.smsautomation.msg_app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class GetAllSMS {



    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


}//end class
