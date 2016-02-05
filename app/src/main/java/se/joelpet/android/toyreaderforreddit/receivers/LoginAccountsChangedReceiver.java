package se.joelpet.android.toyreaderforreddit.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import se.joelpet.android.toyreaderforreddit.services.LoginAccountsUpdatedIntentService;

public class LoginAccountsChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, LoginAccountsUpdatedIntentService.class);
        context.startService(intent);
    }
}
