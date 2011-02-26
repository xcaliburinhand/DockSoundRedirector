package net.muteheadlight.docksoundredir;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class dockSoundRedirect extends BroadcastReceiver{
	private static final String TAG = "dockSoundRedirector";
	
    // Display an alert that we've received a message.    
    @Override 
    public void onReceive(Context context, Intent intent){

    	String intentAction = intent.getAction();
    	Log.i(TAG, "Recieved a message: " .concat(intentAction));
    	
    	 if (intent.getAction().compareTo(Intent.ACTION_BOOT_COMPLETED) == 0){   
    		   Log.v(TAG, "DemoReceiver.onReceive(ACTION_BOOT_COMPLETED)");   
    		   context.startService(new Intent(context, dockRedirRegisterer.class));
    	 } else {    		   
	    	int dockstate = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);

	    	Intent intent4 = new Intent();
	        Intent intent5 = intent4.setAction("com.sec.android.intent.action.INTERNAL_SPEAKER");
	        CharSequence text = "dockredir";
	        
	    	if (dockstate > 0){
	    		Intent intent6 = intent4.putExtra("state", 1);    
	        	text = "Audio Routed to Dock!";
	    	} else {
	            Intent intent6 = intent4.putExtra("state", 0);    
	            text = "Audio Routed Normal.";
	    	}
	
	        int duration = Toast.LENGTH_SHORT;
	    	context.sendBroadcast(intent4);
	        Toast toast = Toast.makeText(context, text, duration);
	        toast.show();
	        Log.i(TAG, (String)text);	
    	 }
   }
}