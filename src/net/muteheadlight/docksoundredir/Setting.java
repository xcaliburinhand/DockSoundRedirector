//Licensed under GPLv3

package net.muteheadlight.docksoundredir;

import android.content.Context;
import android.content.SharedPreferences;

public class Setting {

	private String text;
	private String key;
	private static Context context;
	private boolean disabled;

	public Setting(String text, String key, Context context) {
		this.text = text;
		this.key = key;
		Setting.context = context;
		disabled = false;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean getChecked() {
		SharedPreferences settings = context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
		return settings.getBoolean(key, false);
	}

	public void setChecked(boolean selected) {
		SharedPreferences settings = context.getSharedPreferences(dockRedirCentral.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(key, selected);
	    editor.commit();
	    dockRedirCentral.logD(key.concat(" has been set to ").concat(String.valueOf(selected)));
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
} 
