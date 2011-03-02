package net.muteheadlight.docksoundredir;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class dockSoundRedirect extends BroadcastReceiver{
	private static final String TAG = "dockSoundRedirector";
	public static final String PREFS_NAME = "prefsDockRedir";
	
    // Display an alert that we've received a message.    
    @Override 
    public void onReceive(Context context, Intent intent){

    	String intentAction = intent.getAction();
    	Log.i(TAG, "Recieved a message: " .concat(intentAction));
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        boolean carRedir = settings.getBoolean("carRedir", true);
        boolean deskRedir = settings.getBoolean("deskRedir", true);
    	
    	 if (intent.getAction().compareTo(Intent.ACTION_BOOT_COMPLETED) == 0){   
    		   Log.v(TAG, "DemoReceiver.onReceive(ACTION_BOOT_COMPLETED)");   
    		   context.startService(new Intent(context, dockRedirRegisterer.class));
    	 } else {    		   
	    	int dockstate = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);

	    	Intent intent1 = new Intent();
	        Intent intentA = intent1.setAction("com.sec.android.intent.action.INTERNAL_SPEAKER");
	        CharSequence text = "Dock Audio Redirection Disabled";
	        
	    	if (dockstate == 2){
	    		if (carRedir){
	    			Intent intentB = intent1.putExtra("state", 1);    
	        		text = "Audio Routed to Dock!";
	        		context.sendBroadcast(intent1);
	    		}
	    	} else if (dockstate == 1) {
	    		if (deskRedir){
	    			Intent intentB = intent1.putExtra("state", 1);    
		        	text = "Audio Routed to Dock!";
		        	context.sendBroadcast(intent1);
	    		}
	    	} else {
	            Intent intentB = intent1.putExtra("state", 0);
	            context.sendBroadcast(intent1);
	            text = "Audio Routed Normal.";
	    	}
	
	        int duration = Toast.LENGTH_SHORT;
	        Toast toast = Toast.makeText(context, text, duration);
	        toast.show();
	        Log.i(TAG, (String)text);	
    	 }
   }
}