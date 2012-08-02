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
import android.os.Build;
import android.widget.Toast;

public class dockSoundRedirect extends BroadcastReceiver{
	
	private static final int DEVICE_OUT_WIRED_HEADPHONE = 0x8;
	private Context _context;
	
    @Override 
    public void onReceive(Context context, Intent intent){

    	_context = context;
    	String intentAction = intent.getAction();
    	SharedPreferences settings = _context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
    	
    	dockRedirCentral.logD("Recieved a message: " .concat(intentAction));
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
        boolean mediaVolume = settings.getBoolean("fallback", false);
        int _mediaVolume = settings.getInt("_mediaVolume", -1);
        int _deviceNum = settings.getInt("_deviceNum", 0x0000);
    	
    	 if (intent.getAction().compareTo(Intent.ACTION_BOOT_COMPLETED) == 0){   
    		   dockRedirCentral.logD("Received ACTION_BOOT_COMPLETED");   
    		   if(dockRedirCentral.imSupported(_context))
    			   _context.startService(new Intent(_context, dockRedirRegisterer.class));
    	 } else {
	    	int dockstate = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
	    	dockRedirCentral.logD("_docked " .concat(String.valueOf(_docked)));
	    	
	    	//Logic to prevent being triggered by rebroadcast
	    	if((dockstate == 1 || dockstate == 2) && _docked && (intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") != 0))
	    		return;
	    	
	    	if(dockstate == 0 && !_docked)
	    		return;

	        CharSequence text = "Dock Audio Redirection Disabled";
	        
	    	if (dockstate > 0){
	    		if ((dockstate == 2 & carRedir) || (dockstate == 1 && deskRedir) || (intent.getAction().compareTo("net.muteheadlight.docksoundredir.intent.action.REDIRECT") == 0)){
	    			if (!fallback) {
	    				redirectConnectionState(_deviceNum,1);
	    			} else {
		    			if (useKernel)
		    				redirectKernel(1);
		    			else
		    				redirectSamsung(1);
	    			}
	    			
	    			if (screenOn)
	    				dockRedirCentral.mWakeLock.acquire();
	    			
	    			if (mediaVolume) {
	    				AudioManager audioManager = (AudioManager)_context.getSystemService(Context.AUDIO_SERVICE);
	    				editor.putInt("_mediaVolume",audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
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
        	        _context.sendBroadcast(intent1);
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
	    			if(dockRedirCentral.mWakeLock.isHeld())
	    				dockRedirCentral.mWakeLock.release();
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
        Intent intentRet = intent1.setAction("com.sec.android.intent.action.INTERNAL_SPEAKER");
    	intentRet = intent1.putExtra("state", enable);
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
    		_deviceNum = getDockDeviceNumber();
    	}
    	
    	setDeviceConnectionState(_deviceNum, enable, "");
    	dockRedirCentral.logD("redirecting via connection state");
    }
    
    private int getDockDeviceNumber() {
    	int _deviceNum = 0x0000;
    			
    	if (Build.DEVICE.contains("I896") || Build.DEVICE.contains("T959")) { //Galaxy S I
    		_deviceNum = Integer.valueOf(4096);
    	} else { //default
    		_deviceNum = 0x800;
    	}
    	
    	//store dock device number for future lookup
    	SharedPreferences settings = _context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
        editor.putInt("_deviceNum", _deviceNum);
        editor.commit();
        
        //send back the number
        return _deviceNum;
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