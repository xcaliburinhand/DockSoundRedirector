package net.muteheadlight.docksoundredir.tasker;

import net.muteheadlight.dockredir.R;
import net.muteheadlight.docksoundredir.dockRedirCentral;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TaskerChooser extends Activity {
	private int redirectDirection = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(dockRedirCentral.imSupported(this,true)){
        	setContentView(R.layout.activity_tasker_chooser);
        	
        	Button btnTasker = (Button)findViewById(R.id.taskerRedirectButton);
	        btnTasker.setOnClickListener(onBtnClick(1));
	        
	        btnTasker = (Button)findViewById(R.id.taskerNormalButton);
	        btnTasker.setOnClickListener(onBtnClick(0));
        } else {
        	Log.i(dockRedirCentral.getTag(),"I am not supported, exiting!");
    		setContentView(R.layout.unsupported);
        }
    }
    
    @Override
    public void finish()
    {
        if (redirectDirection == -1)
        {
            setResult(RESULT_CANCELED);
        }
        else
        {
            /*
             * This is the result Intent to Locale
             */
            final Intent resultIntent = new Intent();


            final Bundle resultBundle = new Bundle();
            resultBundle.putInt(PluginBundleManager.BUNDLE_EXTRA_NAME, redirectDirection);

            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

            if (redirectDirection==1)
            {
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, "Redirect to Dock");
            }
            else
            {
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, "Redirect Normal");
            }

            setResult(RESULT_OK, resultIntent);
        }

        super.finish();
    }
    
    private OnClickListener onBtnClick(final int redirect){
    	return new OnClickListener() {
    		public void onClick(View v) {
    			redirectDirection = redirect;
    			finish();
    		}
    	};
    }
}
