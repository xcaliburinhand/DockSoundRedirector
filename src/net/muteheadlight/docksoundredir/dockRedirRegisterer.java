package net.muteheadlight.docksoundredir;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class dockRedirRegisterer extends Service{
	private static final String TAG = "dockSoundRedirector";
	  
    @Override
    public IBinder onBind(Intent intent) {
     return null;
    }
    
    @Override
    public void onStart(Intent intent, int startId){
     super.onStart(intent, startId);
     Log.v(TAG, "Dock Sound Redirector Started");
    }
    
    @Override
    public void onCreate(){
     super.onCreate();
     Log.v(TAG, "Dock Sound Redirector Created");
     
     IntentFilter filter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
     dockSoundRedirect receiver = new dockSoundRedirect();
     registerReceiver(receiver, filter);
   }
}
