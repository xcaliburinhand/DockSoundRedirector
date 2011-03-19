package net.muteheadlight.docksoundredir;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class dockRedirRegisterer extends Service{
	  
    @Override
    public IBinder onBind(Intent intent) {
     return null;
    }
    
    @Override
    public void onStart(Intent intent, int startId){
     super.onStart(intent, startId);
     Log.v(dockRedirCentral.TAG, "Dock Sound Redirector Started");
    }
    
    @Override
    public void onCreate(){
     super.onCreate();
     Log.v(dockRedirCentral.TAG, "Dock Sound Redirector Created");
     
     SharedPreferences settings = getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
     SharedPreferences.Editor editor = settings.edit();
     editor.putBoolean("useKernel", dockRedirCentral.useKernel());
     
     IntentFilter filter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
     dockSoundRedirect receiver = new dockSoundRedirect();
     registerReceiver(receiver, filter);
   }
}
