package net.muteheadlight.docksoundredir.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class TaskerReceiver extends BroadcastReceiver {
    public TaskerReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    	final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);

        /*
         * Final verification of the plug-in Bundle before firing the setting.
         */
        if (PluginBundleManager.isBundleValid(bundle))
        {
   		 	Intent intent1 = new Intent();
            intent1.setAction("net.muteheadlight.docksoundredir.intent.action.REDIRECT");
            intent1.putExtra("android.intent.extra.DOCK_STATE", bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_NAME));
            context.sendBroadcast(intent1);
        }
    }
}
