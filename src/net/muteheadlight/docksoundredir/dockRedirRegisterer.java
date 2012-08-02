//Licensed under GPLv3

package net.muteheadlight.docksoundredir;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;

public class dockRedirRegisterer extends Service{
	  
    @Override
    public IBinder onBind(Intent intent) {
     return null;
    }
    
    @Override
    public void onStart(Intent intent, int startId){
     super.onStart(intent, startId);
     dockRedirCentral.logD("Dock Sound Redirector Started");
    }
    
    @Override
    public void onCreate(){
     super.onCreate();
     dockRedirCentral.logD("Dock Sound Redirector Created");
     
     SharedPreferences settings = getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
     SharedPreferences.Editor editor = settings.edit();
     editor.putBoolean("_useKernel", dockRedirCentral.useKernel());
     if (settings.getInt("_mediaVolume", -999) == -999) { //set some defaults
 		editor.putBoolean("carRedir", true);
 		editor.putBoolean("deskRedir", true);
 		editor.putBoolean("showToast", true);
 		editor.putInt("_mediaVolume", -1);
     }
     editor.commit();
     
     IntentFilter filter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
     dockSoundRedirect receiver = new dockSoundRedirect();
     registerReceiver(receiver, filter);
     
     final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
     dockRedirCentral.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, dockRedirCentral.TAG);
   }
}
