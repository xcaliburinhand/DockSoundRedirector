//Licensed under GPLv3

package net.muteheadlight.docksoundredir;

import java.util.ArrayList;
import java.util.List;

import net.muteheadlight.dockredir.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class dsrListView extends ListActivity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		if(dockRedirCentral.imSupported(this)){
			startService(new Intent(this, dockRedirRegisterer.class));
	        
			setContentView(R.layout.footer);
	        
			TextView ver=(TextView) findViewById(R.id.app_info);
	        try {
	        	PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
	        	ver.append(packageInfo.versionName);
	        } catch (NameNotFoundException e) {
	        	ver.append("unknown");
	        } 
	        
	        //Attach redirect button onClick
	        final ToggleButton redir1 = (ToggleButton)findViewById(R.id.toggleRedir);
	        redir1.setOnClickListener(new OnClickListener() {
		        public void onClick(View v) {
		   		 final ToggleButton redir1 = (ToggleButton)findViewById(R.id.toggleRedir);
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
	        SharedPreferences settings = getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
	    	redir1.setEnabled(settings.getBoolean("_docked", false));
	        redir1.setChecked(settings.getBoolean("_redirected", false));
	        
	        //Attach support button onClick
	        final Button supReq = (Button)findViewById(R.id.toggleRedir);
	        supReq.setOnClickListener(dockRedirCentral.onSupClick());
	        
	        // Create setting list
			ArrayAdapter<Setting> adapter = new InteractiveArrayAdapter(this,listSettings());
			setListAdapter(adapter);
    	} else {
    		Log.i(dockRedirCentral.TAG,"I am not supported, exiting!");
    		setContentView(R.layout.unsupported);
    		if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
    			TextView errTxt=(TextView) findViewById(R.id.errorText);
        		errTxt.setText(R.string.unsupported_manu);
        		final Button reqSup=(Button) findViewById(R.id.unsupReq);
        		reqSup.setVisibility(reqSup.INVISIBLE);
    		} else {
    			//Attach support button onClick
    	        final Button supReq = (Button)findViewById(R.id.unsupReq);
    	        supReq.setOnClickListener(dockRedirCentral.onSupClick());
    		}
    	}
	}

	private List<Setting> listSettings() {
		List<Setting> list = new ArrayList<Setting>();
		
		String[] settingKeys = getResources().getStringArray(R.array.checks_keys);
		String[] settingTexts = getResources().getStringArray(R.array.checks);
		int x = 0;
		
		for(String settingText : settingTexts){
			list.add(new Setting(settingText,settingKeys[x],getApplicationContext()));
			x++;
		}
		
		return list;
	}
	

}


