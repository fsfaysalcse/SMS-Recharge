package com.faysal.smsautomation.msg_app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class HeadlessSmsSendService extends Service {
    public HeadlessSmsSendService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: HANDEL HEADLESS SMS
        throw new UnsupportedOperationException("Not yet implemented");
    }

}//end class
