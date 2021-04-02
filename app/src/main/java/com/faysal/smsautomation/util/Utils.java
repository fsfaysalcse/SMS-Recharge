package com.faysal.smsautomation.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class Utils {

    public static String getRealtiveDate(String str_date) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = formatter.parse(str_date);
            long now = System.currentTimeMillis();
            return String.valueOf(android.text.format.DateUtils.getRelativeTimeSpanString(date.getTime(), now, android.text.format.DateUtils.DAY_IN_MILLIS));
        } catch (ParseException e) {
            e.printStackTrace();
            return str_date;
        }
    }

}//end class
