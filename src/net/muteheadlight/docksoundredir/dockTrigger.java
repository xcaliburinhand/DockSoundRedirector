//Licensed under GPLv3

package net.muteheadlight.docksoundredir;

import net.muteheadlight.dockredir.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ToggleButton;

public class dockTrigger extends Activity{
	dockSoundRedirect receiver;

    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	if(dockRedirCentral.imSupported(this)){
	        setContentView(R.layout.main);
    	} else {
    		Log.i(dockRedirCentral.TAG,"I am not supported, exiting!");
    		setContentView(R.layout.unsupported);
    		return;
    	}

        TextView ver=new TextView(this);
        ver=(TextView)findViewById(R.id.widget33); 
        try {
        	PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        	ver.setText("Version: "+ packageInfo.versionName);
        } catch (NameNotFoundException e) {
        	ver.setText("Version: unknown");
        }        
        
        startService(new Intent(this, dockRedirRegisterer.class));
        
        final ToggleButton redir1 = (ToggleButton)findViewById(R.id.toggleRedir);
        redir1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks
            	Intent intent1 = new Intent();
                Intent intentRet = intent1.setAction("net.muteheadlight.docksoundredir.intent.action.REDIRECT");
                if (redir1.isChecked()) {
                	intentRet = intent1.putExtra("android.intent.extra.DOCK_STATE", 1);
                } else {
                	intentRet = intent1.putExtra("android.intent.extra.DOCK_STATE", 0);
                }
                sendBroadcast(intent1);
            }
        });
    }

	protected void onStop() {
    	super.onStop();
    	
    	if(dockRedirCentral.imSupported(this)){
	        CheckBox redir = new CheckBox(this);
	        redir = (CheckBox)findViewById(R.id.widget31);
	        boolean carRedir  = redir.isChecked();
	        redir = (CheckBox)findViewById(R.id.widget32);
	        boolean deskRedir = redir.isChecked();
	        redir = (CheckBox)findViewById(R.id.widgetNote);
	        boolean showToast = redir.isChecked();
	        redir = (CheckBox)findViewById(R.id.widgetScreen);
	        boolean screenOn = redir.isChecked();
	        redir = (CheckBox)findViewById(R.id.widgetFallback);
	        boolean fallback = redir.isChecked();
	    	
	        SharedPreferences settings = getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putBoolean("deskRedir", deskRedir);
	        editor.putBoolean("carRedir", carRedir);
	        editor.putBoolean("showToast", showToast);
	        editor.putBoolean("screenOn", screenOn);
	        editor.putBoolean("screenOn", fallback);
	        redir = (CheckBox)findViewById(R.id.widgetMaxVol);
	        if(redir.isChecked())
	        	editor.putInt("mediaVolume", 0);
	        else
	        	editor.putInt("mediaVolume", -1);
	        
	        // Commit the edits!
	        editor.commit();
    	}
    }
	
	protected void onResume() {
		super.onStart();
		
		if(dockRedirCentral.imSupported(this)){
	        SharedPreferences settings = getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
	        boolean carRedir = settings.getBoolean("carRedir", true);
	        boolean deskRedir = settings.getBoolean("deskRedir", true);
	        boolean showToast = settings.getBoolean("showToast", true);
	        boolean screenOn = settings.getBoolean("screenOn", false);
	        boolean _docked = settings.getBoolean("_docked", false);
	        boolean _redirected = settings.getBoolean("_redirected", false);
	        boolean fallback = settings.getBoolean("fallback", false);
	        int mediaVolume = settings.getInt("mediaVolume", -1);
	        
	        CheckBox redir = new CheckBox(this);
	        redir = (CheckBox)findViewById(R.id.widget31);
	        redir.setChecked(carRedir);
	        redir = (CheckBox)findViewById(R.id.widget32);
	        redir.setChecked(deskRedir);
	        redir = (CheckBox)findViewById(R.id.widgetNote);
	        redir.setChecked(showToast);
	        redir = (CheckBox)findViewById(R.id.widgetScreen);
	        redir.setChecked(screenOn);
	        redir = (CheckBox)findViewById(R.id.widgetFallback);
	        redir.setChecked(fallback);
	        redir = (CheckBox)findViewById(R.id.widgetMaxVol);
	        if (mediaVolume == -1)
	        	redir.setChecked(false);
	        else
	        	redir.setChecked(true);
	        final ToggleButton redir1 = (ToggleButton)findViewById(R.id.toggleRedir);
	        redir1.setEnabled(_docked);
	        redir1.setChecked(_redirected);
		}
	}
}
