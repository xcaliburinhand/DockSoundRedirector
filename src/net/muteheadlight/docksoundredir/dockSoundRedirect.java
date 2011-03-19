package net.muteheadlight.docksoundredir;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

public class dockSoundRedirect extends BroadcastReceiver{
	    
    @Override 
    public void onReceive(Context context, Intent intent){

    	String intentAction = intent.getAction();
    	Log.i(dockRedirCentral.TAG, "Recieved a message: " .concat(intentAction));
    	SharedPreferences settings = context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
        boolean carRedir = settings.getBoolean("carRedir", true);
        boolean deskRedir = settings.getBoolean("deskRedir", true);
        boolean useKernel = settings.getBoolean("useKernel", false);
    	
    	 if (intent.getAction().compareTo(Intent.ACTION_BOOT_COMPLETED) == 0){   
    		   Log.v(dockRedirCentral.TAG, "Received ACTION_BOOT_COMPLETED");   
    		   if(dockRedirCentral.imSupported(context))
    			   context.startService(new Intent(context, dockRedirRegisterer.class));
    	 } else {    		   
	    	int dockstate = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);

	        CharSequence text = "Dock Audio Redirection Disabled";
	        
	    	if (dockstate == 2){
	    		if (carRedir){
	    			if (useKernel)
	    				redirectKernel(1,context);
	    			else
	    				redirectSamsung(1, context);    
	        		text = "Audio Routed to Dock!";
	    		}
	    	} else if (dockstate == 1) {
	    		if (deskRedir){
	    			if (useKernel)
	    				redirectKernel(1, context);
	    			else
	    				redirectSamsung(1, context);    
		        	text = "Audio Routed to Dock!";
	    		}
	    	} else {
	    		if (useKernel)
	    			redirectKernel(0, context);
	    		else
	    			redirectSamsung(0, context);
	            text = "Audio Routed Normal.";
	    	}
	
	        int duration = Toast.LENGTH_SHORT;
	        Toast toast = Toast.makeText(context, text, duration);
	        toast.show();
	        Log.i(dockRedirCentral.TAG, (String)text);	
    	 }
   }
    
    private void redirectSamsung(int enable, Context context){
    	Intent intent1 = new Intent();
        Intent intentRet = intent1.setAction("com.sec.android.intent.action.INTERNAL_SPEAKER");
    	intentRet = intent1.putExtra("state", enable);
        context.sendBroadcast(intent1);
    }
    
    private void redirectKernel(int enable, Context context){
    	if (enable == 0) {            	
        	Intent intent = new Intent(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            context.sendBroadcast(intent);
        }
        
    	Intent intent1 = new Intent(Intent.ACTION_HEADSET_PLUG);
        intent1.putExtra("name", "h2w");
        intent1.putExtra("microphone", 0);
        
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/sys/devices/virtual/misc/dockredir/usedock"), 256);
			try {
				writer.write(Integer.toString(enable)); //enable kernel redirection
				intent1.putExtra("state", enable);
			}
	        finally {
	            writer.close();
	        }
        } catch (IOException e) {
			Log.v(dockRedirCentral.TAG, e.toString());
		} 
        context.sendStickyBroadcast(intent1); //say a headset has been connected for proper eq
    }
}