package radonsoft.net.freqdet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Formatter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;


public class FreqDetActivity extends Activity 
{
	
	private Button mButton_start;
	boolean boIsRecording = false;
	private AudioIn GetAudioInstance = null;
	private Thread th = null;
//	short frameSize = 512;			// this will produce datapoints at a rate of 48000/(2*512) Hz
	short frameSize = 1024;			// this will produce datapoints at a rate of 48000/(2*1024) Hz
	int sfreq = 48000;				// 48 kHz sample frequency
	int cnt = 0;
	short FFTSIZE = (short) (frameSize*2);

	public static final String PREFS_NAME = "FDPrefs";
    graph gra;
    float kfak;
    float coefa = 0.000651f;
    float coefb = 12.8465f;
    float maxbac = 0;
    float baclim = 10.0f;
    public static final int MENU_SETTINGS = 1;
	public static final int SETTINGS = 1;
	SharedPreferences mPrefs;


	Formatter fmt;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);
       
        mButton_start = (Button) findViewById(R.id.button1);     
	    mButton_start.setOnClickListener(mStartListener);
	    
	    gra = (graph) findViewById(R.id.View01);	    
	    
	    kfak = (float)sfreq / (float)FFTSIZE;
        mPrefs = getSharedPreferences(PREFS_NAME, 0);
        LoadPref();
        InitDetect(FFTSIZE);
//	    CreateWinBuffer(fwinBuffer, FFTSIZE/2, 2);
    }
    
    protected void LoadPref()
    {
    	baclim = mPrefs.getFloat("baclim", 4.0f);
    	coefa = mPrefs.getFloat("coefa", 0.000651f);
    	coefb = mPrefs.getFloat("coefb", 12.8465f);
	    gra.setYmax(baclim);
    }

    protected void StartRecording()
    {
    	try 
    	{
			fmt = new Formatter(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FreqDet.txt");
		} 
    	catch (IOException e) 
        {
// TODO: add dialog        	mstat.setText("I/O error!");
        }

    	boIsRecording = true;
    	mButton_start.setText(getString(R.string.measuring));
    	gra.clr();
    	cnt = 0;
    	maxbac = 0;
    }

    protected void StopRecording()
    {
        boIsRecording = false;
    	mButton_start.setText(getString(R.string.measure));
        fmt.close();
    	cnt = 0;
    	maxbac = 0;
    }
    
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    menu.add(0, MENU_SETTINGS, 0, "Settings");
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		Intent intent; 
	    switch (item.getItemId()) 
	    {
	    case MENU_SETTINGS:
	    	intent = new Intent();
	        intent.setClass(FreqDetActivity.this, settings.class);
	    	startActivityForResult(intent, SETTINGS);
	        return true;
	    }
	    return false;
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == SETTINGS && resultCode == RESULT_OK) 
        {
        	LoadPref();
        }
    }	
    
    @Override
    protected void onStart()
    {
 	    super.onStart();
    }
    
    protected void WaitForAudio()
    {
        long lstart = System.currentTimeMillis();
        while(GetAudioInstance.IsOK == 0)				// wait for thread to start, cancel after 3s
        {
            long now = System.currentTimeMillis();        	 
            if(now - lstart > 3000) 
            {
            	finish();
            }
        }    	
    }
    
    @Override
    protected void onResume() 
    {
        super.onResume();
    
        GetAudioInstance = new AudioIn(this);
        GetAudioInstance.setRA(this);
        GetAudioInstance.setframeSize(frameSize);
        GetAudioInstance.setRecording(true);
        GetAudioInstance.setFrequency(sfreq);
        th = new Thread(GetAudioInstance);
        th.start();
 	
        WaitForAudio();
    }

    @Override
    protected void onPause() 
    {
        super.onPause();
  	   if(GetAudioInstance != null)
  	   {
 	 	   GetAudioInstance.setRecording(false);
 	       try {th.join();} 
 	       catch (InterruptedException e){ e.printStackTrace();}
 	       if(boIsRecording) StopRecording();
  	   }
    }

    OnClickListener mStartListener = new OnClickListener() 
    {
        public void onClick(View v) 
        {
        	
//        	mstat.setText("Detection running...");
     	   if(boIsRecording)
     	   {
           }
     	   else
     	   {
     		  StartRecording();
     	   }            
        }
    };
    
    public Handler handleUpd = new Handler() 
    {
 	   @Override
        public void handleMessage(Message msg) 
        {
 		   if(msg.what == 1)
 		   {
 			   StopRecording();
 		   }
        }
    };
        
    public void SetData(ByteBuffer sdata)
    {
    	float k3 = DoDetect(sdata, frameSize);
        if(boIsRecording)
        {
        	if(cnt++ > 1000) 
        	{
        		handleUpd.sendEmptyMessage(1);
        		return;
        	}
        	
        	float frq = k3 * kfak;
        	//if(frq<=10050)
        	//	frq = 10000;
        	float bac = (frq-10000);///2100;//(coefb - coefa*frq);
        	//if(bac<=50)
        	//	bac = 0;
        	//else
        		bac = bac/10000;
            fmt.format("%d -- %3.3f -- %3.0f\n", cnt, bac, frq);

            if(bac < 0) bac = 0.0f;
        	if(bac > baclim) bac = baclim;
        	
        	gra.setVal(bac);
        	
        	if(bac >= maxbac) 
        	{
        		maxbac = bac;
        		if(maxbac<0.005f)
        			maxbac = 0;
        		gra.setMaxVal(maxbac, frq);
        	}
        	
        }
    } 
    public native void  InitDetect(int size);
    public native void  ExitDetect();
    public native float DoDetect(ByteBuffer bbuf, int size);

    static 
    {
        System.loadLibrary("FreqDet-jni");
    }
}