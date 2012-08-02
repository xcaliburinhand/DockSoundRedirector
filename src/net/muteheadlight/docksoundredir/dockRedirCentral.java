//Licensed under GPLv3

package net.muteheadlight.docksoundredir;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public final class dockRedirCentral {
	static final String TAG = "dockSoundRedirector";
	static final String PREFS_NAME = "prefsDockRedir";
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
        	//supported = false;
        	supported = true; //always return true, depreciate kernel sysfs usage
        	logD("No kernel sysfs found.");
		} 
        
        return supported;
	}
	
	// Check if this app will work
	public static final boolean imSupported(Context context){
		boolean supported = false;
		
		//Am I on a Samsung device?
		if (!Build.MANUFACTURER.equalsIgnoreCase("samsung") && !debuggable) {
			logD("Phone not made by Samsung.");
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
	
    public static final OnClickListener onSupClick(){
    	return new OnClickListener() {
			public void onClick(View v) {
				Context context = v.getContext();
		    	String mailText ="Device information:\n";
				
				//Device information
				mailText.concat("Device: "+ Build.DEVICE+ " - "+ Build.MODEL+ "\n");
				mailText.concat("OS: "+ Build.VERSION.RELEASE+ "\n");
				
				//Last intent
				SharedPreferences settings = context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
				mailText.concat("Last intent: "+ settings.getString("_lastIntent", "None"));
				
				//Unsupported details
				if (dockRedirCentral.imSupported(context)) {
					mailText.concat("Reason for block: N/A\n");
				} else {
					mailText.concat("Reason for block: N/A\n");
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
}