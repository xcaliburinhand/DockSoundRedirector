package net.muteheadlight.docksoundredir;

import net.muteheadlight.dockredir.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.ToggleButton;

public class dockTrigger extends Activity{
	dockSoundRedirect receiver;

    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	if(dockRedirCentral.imSupported(this)){
	        setContentView(R.layout.main);
	        startService(new Intent(this, dockRedirRegisterer.class));
    	} else {
    		Log.i(dockRedirCentral.TAG,"I'm am not supported, exiting!");
    		setContentView(R.layout.unsupported);
    		return;
    	}

        TextView ver=new TextView(this);
        ver=(TextView)findViewById(R.id.textVersion); 
        try {
        	PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        	ver.setText("Version: "+ packageInfo.versionName);
        } catch (NameNotFoundException e) {
        	ver.setText("Version: unknown");
        }
        
        SharedPreferences settings = getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
        boolean carRedir = settings.getBoolean("carRedir", true);
        boolean deskRedir = settings.getBoolean("deskRedir", true);
        
        ToggleButton redir = new ToggleButton(this);
        redir = (ToggleButton)findViewById(R.id.toggleCar);
        redir.setChecked(carRedir);
        redir = (ToggleButton)findViewById(R.id.toggleDesk);
        redir.setChecked(deskRedir);
        
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("useKernel", dockRedirCentral.useKernel());
    }

	protected void onStop() {
    	super.onStop();
    	
    	if(dockRedirCentral.imSupported(this)){
	        ToggleButton redir = new ToggleButton(this);
	        redir = (ToggleButton)findViewById(R.id.toggleCar);
	        boolean carRedir  = redir.isChecked();
	        redir = (ToggleButton)findViewById(R.id.toggleDesk);
	        boolean deskRedir = redir.isChecked();
	    	
	        SharedPreferences settings = getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putBoolean("deskRedir", deskRedir);
	        editor.putBoolean("carRedir", carRedir);
	
	        // Commit the edits!
	        editor.commit();
    	}
    }
}
