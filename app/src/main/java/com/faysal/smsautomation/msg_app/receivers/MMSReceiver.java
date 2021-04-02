package com.faysal.smsautomation.msg_app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Shantanu on 23-08-2016.
 * We need to implement MMS receiver in API 19+ to make our app as default SMS app
 * Only default SMS app can write to the SMS Provider defined by the android.provider.Telephony class
 */

public class MMSReceiver extends BroadcastReceiver {
    public MMSReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: Handel incomming MMS
    }
}//end class
