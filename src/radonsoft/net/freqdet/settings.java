package radonsoft.net.freqdet;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class settings extends Activity
{
	private TextView maxbac;
	private TextView coeffa;
	private TextView coeffb;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        setContentView(R.layout.settings);
        
        maxbac = (TextView)findViewById(R.id.editText1);
        coeffa = (TextView)findViewById(R.id.editText2);
        coeffb = (TextView)findViewById(R.id.editText3);
       ((Button) findViewById(R.id.button1)).setOnClickListener(onCancel);      
       ((Button) findViewById(R.id.button2)).setOnClickListener(onDef);      
       ((Button) findViewById(R.id.button3)).setOnClickListener(onOK);      


        SharedPreferences mPrefs = getSharedPreferences(radonsoft.net.freqdet.FreqDetActivity.PREFS_NAME, 0);
        maxbac.setText(String.valueOf(mPrefs.getFloat("baclim", 4.0f)));
        coeffa.setText(String.format(Locale.US, "%f", mPrefs.getFloat("coefa", 0.000651f)));
        coeffb.setText(String.valueOf(mPrefs.getFloat("coefb", 12.8465f)));
    }
	
	OnClickListener onCancel = new OnClickListener()
	{
		public void onClick(View v) 
		{
	        setResult(RESULT_CANCELED);
	        finish();
		}
	};

	OnClickListener onOK = new OnClickListener()
	{
		public void onClick(View v) 
		{
	        SharedPreferences mPrefs = getSharedPreferences(radonsoft.net.freqdet.FreqDetActivity.PREFS_NAME, 0);
			SharedPreferences.Editor editor = mPrefs.edit();
			
			CharSequence s = maxbac.getText();
			try{editor.putFloat("baclim", Float.valueOf(s.toString()));}
			catch(NumberFormatException e){}
			s = coeffa.getText();
			try{editor.putFloat("coefa", Float.valueOf(s.toString()));}
			catch(NumberFormatException e){}
			s = coeffb.getText();
			try{editor.putFloat("coefb", Float.valueOf(s.toString()));}
			catch(NumberFormatException e){}
			
			editor.commit();
	        setResult(RESULT_OK);
	        finish();
		}
	};
	
	OnClickListener onDef = new OnClickListener()
	{
		public void onClick(View v) 
		{
	        maxbac.setText("4.0");
	        coeffa.setText("0.000651");
	        coeffb.setText("12.8465");
		}
	};
};
