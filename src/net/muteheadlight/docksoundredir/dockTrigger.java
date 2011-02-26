package net.muteheadlight.docksoundredir;

import net.muteheadlight.dockredir.R;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class dockTrigger extends Activity{
	dockSoundRedirect receiver;

    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.main);
        IntentFilter filter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
        receiver = new dockSoundRedirect();
        registerReceiver(receiver, filter);
        Log.i("dockSoundRedirector", "Receiver Registered");
        
    }

}
