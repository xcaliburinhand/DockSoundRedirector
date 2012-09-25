//Licensed under GPLv3

package net.muteheadlight.docksoundredir;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public final class dockRedirCentral {
	static private final String TAG = "dockSoundRedirector";
	static final String PREFS_NAME = "prefsDockRedir";
	protected static PowerManager.WakeLock mWakeLock;
	private static boolean debuggable = false;
	
	public static String getTag() {
		return TAG;
	}

	public static final void logD(String message) {
		if(debuggable)
			Log.d(TAG, message);
	}
	
	public static final boolean useKernel(){
		boolean supported = false;
		
		try {
    		BufferedReader reader = new BufferedReader(new FileReader("/sys/devices/virtual/misc/dockredir/dockredir_support"), 256);
            try {
                if (reader.readLine().contains("1"))                	
                    supported= true;
            } finally {
            	reader.close();
            }
        } catch (IOException e) {
        	supported = false;
        	logD("No kernel sysfs found.");
		} 
        
        return supported;
	}
	
	// Check if this app will work
	public static final boolean imSupported(Context context){
		boolean supported = false;
		
		//Check phone manufacturer
		if (!imSupported(context, true)) {
			return false;
		}
		
		//Do I have kernel support
		if (useKernel())
			return true;
		
		//Do I have Samsung framework changes
		//Can't directly check the framwork so look for related app 
        try{
        	final PackageManager packageManager = context.getPackageManager();
            if(packageManager.getApplicationInfo("com.sec.android.providers.downloads",0).enabled)
            	supported = true;
        } catch (PackageManager.NameNotFoundException e) {
            	supported = false;
            	logD("Dock framework not found.");
        }
          
        return supported;
	}	
	
	public static final boolean imSupported(Context context, boolean limited){		
		if (limited) {
			//Am I on a Samsung device?
			if (!Build.MANUFACTURER.equalsIgnoreCase("samsung") && !debuggable) {
				logD("Phone not made by Samsung.");
				return false;
			} else {
				return true;
			}
		} else {	//revert to full check	
			return imSupported(context);
		}
	}

    public static final OnClickListener onSupClick(){
    	return new OnClickListener() {
			public void onClick(View v) {
				Context context = v.getContext();
		    	String mailText ="Device information:\n";
				
				//Device information
				mailText = mailText.concat("Device: "+ Build.DEVICE+ " - "+ Build.MODEL+ "\n");
				mailText = mailText.concat("OS: "+ Build.VERSION.RELEASE+ " - "+ Build.DISPLAY+ "\n");
				
				//Last intent
				SharedPreferences settings = context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
				mailText = mailText.concat("Last intent: "+ settings.getString("_lastIntent", "None")+ "\n");
				
				//Audio device number
				mailText = mailText.concat("Audio Device: "+ String.valueOf(settings.getInt("_deviceNum", -1))+ "\n");

				//App version
		        try {
		        	PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
		        	mailText = mailText.concat("App version: "+packageInfo.versionName+ "\n");
		        } catch (NameNotFoundException e) {
		        	mailText = mailText.concat("App version: unknown\n");
		        } 
				
				Intent email = new Intent(Intent.ACTION_SEND);
				email.putExtra(Intent.EXTRA_EMAIL, new String[]{"android@muteheadlight.net"});		  
				email.putExtra(Intent.EXTRA_SUBJECT, "DockSoundRedirect: Device support request");
				email.putExtra(Intent.EXTRA_TEXT, mailText);
				email.setType("message/rfc822");
				context.startActivity(Intent.createChooser(email, "Choose an Email client:"));
		    }
    	};
    }
    
    public static final void warmUp(Context context) {
    	logD("warming up application");
    	
    	SharedPreferences settings = context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
    	if (settings.getInt("_mediaVolume", -999) == -999) { //set some defaults
    		SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("carRedir", true);
			editor.putBoolean("deskRedir", true);
			editor.putBoolean("showToast", true);
			editor.remove("mediaVolume").putBoolean("mediaVolume", false); //cleanup, type change from previous versions
			editor.putInt("_mediaVolume", -1);
			editor.commit();
			logD("default settings applied");
		 }
		 
		 final PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		 mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, dockRedirCentral.getTag());
    }
    
    public static final int getDockDeviceNumber(Context context) {
    	int _deviceNum = 0x0000;
    	String dev = Build.DEVICE.toLowerCase();
    			
    	if (Build.DISPLAY.toLowerCase().contains("cm") || (dev.contains("i9000") && (!Build.DISPLAY.contains(".") || Build.DISPLAY.matches("\\d\\.\\d"))) || (dev.contains("t959") && (!Build.DISPLAY.contains(".") || Build.DISPLAY.matches("\\d\\.\\d")))) {
			_deviceNum = 0x800; //2048 handle legacy devices running CM
		} else if (dev.contains("d700") || dev.contains("g70") || dev.contains("gi1") || dev.contains("i400") || dev.contains("i500") || dev.contains("i510") || dev.contains("i777") || dev.contains("i896") || dev.contains("i897") || dev.contains("i9000") || dev.contains("i9003") || dev.contains("i9070") || dev.contains("i997") || dev.contains("t959") || dev.contains("t989")) { //Galaxy S I
    		_deviceNum = 0x1000; //4096
    	} else if (dev.contains("i9001") || dev.contains("s5360") || dev.contains("s5830")) {
    		_deviceNum = 0x4000; //16384
    	} else { //default
    		_deviceNum = 0x800; //2048
    	}
    	
    	//store dock device number for future lookup
    	SharedPreferences settings = context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
        editor.putInt("_deviceNum", _deviceNum);
        editor.commit();
        
        //send back the number
        return _deviceNum;
    }
}