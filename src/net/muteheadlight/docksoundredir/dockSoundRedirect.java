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
import android.util.Log;
import android.widget.Toast;

public class dockSoundRedirect extends BroadcastReceiver{
	
	private static final int DEVICE_OUT_WIRED_HEADPHONE = 0x8;
	private Context _context;
	
    @Override 
    public void onReceive(Context context, Intent intent){

    	_context = context;
    	String intentAction = intent.getAction();
    	SharedPreferences settings = _context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
    	
    	dockRedirCentral.logD("Recieved a message: "+ intentAction);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("_lastIntent", intentAction);
    	editor.commit();
    	
        boolean carRedir = settings.getBoolean("carRedir", true);
        boolean deskRedir = settings.getBoolean("deskRedir", true);
        boolean useKernel = settings.getBoolean("_useKernel", false);
        boolean showToast = settings.getBoolean("showToast", true);
        boolean _docked = settings.getBoolean("_docked", false);
        boolean screenOn = settings.getBoolean("screenOn", false);
        boolean fallback = settings.getBoolean("fallback", false);
        boolean mediaVolume = false;
        try {
        	mediaVolume = settings.getBoolean("mediaVolume", false);
        } catch (ClassCastException e) {
        	editor.remove("mediaVolume").putBoolean("mediaVolume", true);
        	editor.commit();
        }
        int _mediaVolume = settings.getInt("_mediaVolume", -1);
        int _deviceNum = settings.getInt("_deviceNum", 0x0000);
    	
    	 if (intent.getAction().compareTo(Intent.ACTION_BOOT_COMPLETED) == 0 || intent.getAction().compareTo(Intent.ACTION_PACKAGE_REPLACED) == 0){
    		 dockRedirCentral.logD("Initial startup, received "+ intent.getAction());   
			 if(dockRedirCentral.imSupported(_context,true)) {
				 editor.putBoolean("_useKernel", dockRedirCentral.useKernel());
				 				     
				 dockRedirCentral.warmUp(_context);
				 _deviceNum = dockRedirCentral.getDockDeviceNumber(_context); //refresh dock device number on boot
			 }
    	 } else {
    		if (!dockRedirCentral.imSupported(_context,true)) return;
    		
    		int dockstate;
    		if (intent.getAction().contains("WIDGET")) {
    			dockRedirCentral.logD("redirect widget triggered");
    			if(!settings.getBoolean("_redirected", false) && _docked){
    				dockstate = 10;
    			} else {
    				dockstate = 0;
    			}
    		} else {
    			dockstate = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
    		}
	    	editor.putString("_lastIntent", settings.getString("_lastIntent", "None").concat(" - devID: "+ dockstate));
	    	
	    	//Logic to prevent being triggered by rebroadcast
	    	if((dockstate == 1 || dockstate == 2) && _docked && (intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") != 0) && !intent.getAction().contains("WIDGET"))
	    		return;
	    	
	    	if(dockstate == 0 && !_docked)
	    		return;

	        CharSequence text = "Dock Audio Redirection Disabled";
	        
	    	if (dockstate > 0){
	    		if ((dockstate == 2 & carRedir) || (dockstate == 1 && deskRedir) || (intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") == 0) || intent.getAction().contains("WIDGET")){
	    			if (!fallback) {
	    				redirectConnectionState(_deviceNum,1);
	    			} else {
		    			if (useKernel)
		    				redirectKernel(1);
		    			else
		    				redirectSamsung(1);
	    			}
	    			
	    			if (screenOn) {
	    				try {
	    					dockRedirCentral.mWakeLock.acquire();
	    				} catch (Exception e) {
		    				Log.d(dockRedirCentral.getTag(), "Unable to aquire wakelock");
		    			}
	    			}
	    			
	    			if (mediaVolume) {
	    				AudioManager audioManager = (AudioManager)_context.getSystemService(Context.AUDIO_SERVICE);
	    				editor.putInt("_mediaVolume",audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
	    				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FLAG_SHOW_UI);
	    			}
	    				
	        		text = "Audio Routed to Dock!";
	        		
	        		editor.putBoolean("_redirected", true);
	    		}
        		//If redirect was triggered manually, don't change dock status
        		if(intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") != 0 && !intent.getAction().contains("WIDGET")) 
        			editor.putBoolean("_docked", true);
	    	} else {
	    		//If redirect was triggered manually, don't change dock status
	            if(intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") != 0 && !intent.getAction().contains("WIDGET"))
        			editor.putBoolean("_docked", false); 
	            
	            //Pause any playing audio
        	    Intent intent1 = new Intent(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        	    try {
        	    	_context.sendBroadcast(intent1);
        	    } catch (Exception Ex) {
        	    	//do nothing
        	    }
	            
	            if (!fallback){
	            	redirectConnectionState(_deviceNum,0);
	            } else {
		    		if (useKernel)
		    			redirectKernel(0);
		    		else
		    			redirectSamsung(0);
	            }
	    		
	    		if (screenOn){
	    			try {
	    				if(dockRedirCentral.mWakeLock.isHeld())
	    					dockRedirCentral.mWakeLock.release();
	    			} catch (Exception e) {
	    				Log.d(dockRedirCentral.getTag(), "Unable to check/release wakelock");
	    			}
	    		}
	    		
    			if (mediaVolume) {
    				AudioManager audioManager = (AudioManager)_context.getSystemService(Context.AUDIO_SERVICE);
    				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,_mediaVolume,AudioManager.FLAG_SHOW_UI);
    			}
    			
	            text = "Audio Routed Normal.";

	            editor.putBoolean("_redirected", false);
	    	}
	
	        int duration = Toast.LENGTH_SHORT;
	        if(showToast){
	        	Toast toast = Toast.makeText(_context, text, duration);
	        	toast.show();
	        }
	        
	        editor.commit();
	        dockRedirCentral.logD((String)text);	
    	 }
   }
    
    private void redirectSamsung(int enable){
    	Intent intent1 = new Intent();
        intent1.setAction("com.sec.android.intent.action.INTERNAL_SPEAKER");
    	intent1.putExtra("state", enable);
        _context.sendBroadcast(intent1);
        dockRedirCentral.logD("redirecting via ROM");
    }
    
    private void redirectKernel(int enable){     
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/sys/devices/virtual/misc/dockredir/usedock"), 256);
			try {
				writer.write(Integer.toString(enable)); //enable kernel redirection
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
    
    private void redirectConnectionState(int _deviceNum, int enable) {
    	if(_deviceNum == 0x0000){
    		_deviceNum = dockRedirCentral.getDockDeviceNumber(_context);
    	}
    	
    	setDeviceConnectionState(_deviceNum, enable, "");
    	dockRedirCentral.logD("redirecting via connection state - dev: "+ String.valueOf(_deviceNum));
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