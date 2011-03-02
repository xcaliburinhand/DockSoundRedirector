package net.muteheadlight.docksoundredir;

import net.muteheadlight.dockredir.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ToggleButton;

public class dockTrigger extends Activity{
	dockSoundRedirect receiver;
	public static final String PREFS_NAME = "prefsDockRedir";

    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.main);
        TextView ver=new TextView(this); 

        ver=(TextView)findViewById(R.id.textVersion); 
        try {
        	PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        	ver.setText("Version: "+ packageInfo.versionName);
        } catch (NameNotFoundException e) {
        	ver.setText("Version: unknown");
        }
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean carRedir = settings.getBoolean("carRedir", true);
        boolean deskRedir = settings.getBoolean("deskRedir", true);
        
        ToggleButton redir = new ToggleButton(this);
        redir = (ToggleButton)findViewById(R.id.toggleCar);
        redir.setChecked(carRedir);
        redir = (ToggleButton)findViewById(R.id.toggleDesk);
        redir.setChecked(deskRedir);        
    }
    
    protected void onStop() {
    	super.onStop();
    	
        ToggleButton redir = new ToggleButton(this);
        redir = (ToggleButton)findViewById(R.id.toggleCar);
        boolean carRedir  = redir.isChecked();
        redir = (ToggleButton)findViewById(R.id.toggleDesk);
        boolean deskRedir = redir.isChecked();
    	
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("deskRedir", deskRedir);
        editor.putBoolean("carRedir", carRedir);

        // Commit the edits!
        editor.commit();
    }

}
