package net.muteheadlight.docksoundredir;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.widget.Toast;

public class dockSoundRedirect extends BroadcastReceiver{
	
		    
    @Override 
    public void onReceive(Context context, Intent intent){

    	String intentAction = intent.getAction();
    	dockRedirCentral.logD("Recieved a message: " .concat(intentAction));
    	SharedPreferences settings = context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
        boolean carRedir = settings.getBoolean("carRedir", true);
        boolean deskRedir = settings.getBoolean("deskRedir", true);
        boolean useKernel = settings.getBoolean("useKernel", false);
        boolean showToast = settings.getBoolean("showToast", true);
        boolean _docked = settings.getBoolean("_docked", false);
        boolean screenOn = settings.getBoolean("screenOn", false);
        int mediaVolume = settings.getInt("mediaVolume", -1);
    	
    	 if (intent.getAction().compareTo(Intent.ACTION_BOOT_COMPLETED) == 0){   
    		   dockRedirCentral.logD("Received ACTION_BOOT_COMPLETED");   
    		   if(dockRedirCentral.imSupported(context))
    			   context.startService(new Intent(context, dockRedirRegisterer.class));
    	 } else {    		   
	    	int dockstate = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
	    	
	    	//Logic to prevent being triggered by rebroadcast
	    	if((dockstate == 1 || dockstate == 2) && _docked)
	    		return;
	    	
	    	if(dockstate == 0 && !_docked)
	    		return;

	        CharSequence text = "Dock Audio Redirection Disabled";
	        SharedPreferences.Editor editor = settings.edit();
	        
	    	if (dockstate > 0){
	    		if ((dockstate == 2 & carRedir) || (dockstate == 1 && deskRedir)){
	    			if (useKernel)
	    				redirectKernel(1,context);
	    			else
	    				redirectSamsung(1, context);
	    			if (screenOn)
	    				dockRedirCentral.mWakeLock.acquire();
	    			if (!(mediaVolume == -1)) {
	    				AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	    				editor.putInt("mediaVolume",audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
	    				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FLAG_SHOW_UI);
	    			}
	    				
	        		text = "Audio Routed to Dock!";
	        		editor.putBoolean("_docked", true);
	    		}
	    	} else {
	    		if (useKernel)
	    			redirectKernel(0, context);
	    		else
	    			redirectSamsung(0, context);
	    		if (screenOn)
    				dockRedirCentral.mWakeLock.release();
    			if (!(mediaVolume == -1)) {
    				AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mediaVolume,AudioManager.FLAG_SHOW_UI);
    			}
	            text = "Audio Routed Normal.";
	            editor.putBoolean("_docked", false);
	    	}
	
	        int duration = Toast.LENGTH_SHORT;
	        if(showToast){
	        	Toast toast = Toast.makeText(context, text, duration);
	        	toast.show();
	        }
	        editor.commit();
	        dockRedirCentral.logD((String)text);	
    	 }
   }
    
    private void redirectSamsung(int enable, Context context){
    	Intent intent1 = new Intent();
        Intent intentRet = intent1.setAction("com.sec.android.intent.action.INTERNAL_SPEAKER");
    	intentRet = intent1.putExtra("state", enable);
        context.sendBroadcast(intent1);
        dockRedirCentral.logD("redirecting via ROM");
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
			dockRedirCentral.logD(e.toString());
		} 
        context.sendStickyBroadcast(intent1); //say a headset has been connected for proper eq
        dockRedirCentral.logD("redirecting via kernel");
    }
}