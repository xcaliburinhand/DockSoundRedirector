//Licensed under GPLv3

package net.muteheadlight.docksoundredir;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

public class dockRedirCentral {
	public static final String TAG = "dockSoundRedirector";
	public static final String PREFS_NAME = "prefsDockRedir";
	protected static PowerManager.WakeLock mWakeLock;
	private static boolean debuggable = false;
	
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
		} 
        
        return supported;
	}
	
	// Check if this app will work
	public static final boolean imSupported(Context context){
		boolean supported = false;
		
		//Am I on a Samsung device?
		if (!Build.MANUFACTURER.equalsIgnoreCase("samsung"))
			return false;
		
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
        }
          
        return supported;
	}
}
