//Licensed under GPLv3

package net.muteheadlight.docksoundredir;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.widget.Toast;

public class dockSoundRedirect extends BroadcastReceiver{
	
	private static final int DEVICE_OUT_WIRED_HEADPHONE = 0x8;    
	
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
	    	dockRedirCentral.logD("_docked " .concat(String.valueOf(_docked)));
	    	
	    	//Logic to prevent being triggered by rebroadcast
	    	if((dockstate == 1 || dockstate == 2) && _docked && (intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") != 0))
	    		return;
	    	
	    	if(dockstate == 0 && !_docked)
	    		return;

	        CharSequence text = "Dock Audio Redirection Disabled";
	        SharedPreferences.Editor editor = settings.edit();
	        
	    	if (dockstate > 0){
	    		if ((dockstate == 2 & carRedir) || (dockstate == 1 && deskRedir) || (intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") == 0)){
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
	        		if(intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") != 0)
	        			editor.putBoolean("_docked", true);
	        		editor.putBoolean("_redirected", true);
	    		}
	    	} else {
	            if(intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") != 0) {
        			editor.putBoolean("_docked", false);           	
        	        Intent intent1 = new Intent(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        	        context.sendBroadcast(intent1);
	            }
	            
	    		if (useKernel)
	    			redirectKernel(0, context);
	    		else
	    			redirectSamsung(0, context);
	    		if (screenOn){
	    			if(dockRedirCentral.mWakeLock.isHeld())
	    				dockRedirCentral.mWakeLock.release();
	    		}
    			if (!(mediaVolume == -1)) {
    				AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mediaVolume,AudioManager.FLAG_SHOW_UI);
    			}
	            text = "Audio Routed Normal.";

	            editor.putBoolean("_redirected", false);
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
        setDeviceConnectionState(DEVICE_OUT_WIRED_HEADPHONE, enable, "");
        dockRedirCentral.logD("redirecting via kernel");
    }
    
    // The following is a grab from Dan Walkes toggleheadset2 - http://code.google.com/p/toggleheadset2/
    /**
     * set device connection state through reflection for Android 2.1, 2.2, 2.3 - 4.0 - later?
     * Thanks Adam King!
     * @param device
     * @param state
     * @param address
     */
    private void setDeviceConnectionState(final int device, final int state, final String address) {
        try {
            Class<?> audioSystem = Class.forName("android.media.AudioSystem");
            Method setDeviceConnectionState = audioSystem.getMethod(
                    "setDeviceConnectionState", int.class, int.class, String.class);

            setDeviceConnectionState.invoke(audioSystem, device, state, address);
        } catch (Exception e) {
            dockRedirCentral.logD("setDeviceConnectionState failed: " + e);
        }
    }
}