package radonsoft.net.freqdet;


import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioIn implements Runnable 
{
    private int frequency;
    private int channelConfiguration;
    private volatile boolean isPaused;
    private volatile boolean isRecording;
    private final Object mutex = new Object();
    private FreqDetActivity myFDA;
    private int frameSize = 512;
    ByteBuffer smpBuffer = null;
    int IsOK = 0;


    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    public AudioIn(FreqDetActivity parent) 
    {
         super();
         this.setFrequency(44100);
         this.setChannelConfiguration(AudioFormat.CHANNEL_CONFIGURATION_MONO);
         this.setPaused(false);
    }

    public int ChkOK()
    {
   	 return IsOK;
    }
    
    public void run() 
    {
         synchronized (mutex) 
         {
              while (!this.isRecording) 
              {
                   try 
                   {
                        mutex.wait();
                   } 
                   catch (InterruptedException e) 
                   {
                        throw new IllegalStateException("Wait() interrupted!", e);
                   }
              }
         }

         android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
         int bufferRead = 0;
         
         int recframeSize = 2 * AudioRecord.getMinBufferSize(this.getFrequency(),
                 this.getChannelConfiguration(), this.getAudioEncoding());
         if(recframeSize <= 0)
         {
             this.IsOK = -1;
             return;
         }
         int bufferSize = recframeSize * 2;
         while(bufferSize < 8192) 			 bufferSize += recframeSize;
         while(bufferSize%(frameSize*2) != 0) bufferSize += recframeSize;
         
         AudioRecord recordInstance = new AudioRecord( MediaRecorder.AudioSource.MIC, this.getFrequency(), 
       		                       this.getChannelConfiguration(), this.getAudioEncoding(), bufferSize);

         if(recordInstance.getState() == AudioRecord.STATE_UNINITIALIZED)
         {
             this.IsOK = -1;
             return;
         }
       	  
         try
         {
             recordInstance.startRecording();       	  
         }
         catch (IllegalStateException e)
         {
           this.IsOK = -1;
       	return;
         }
         
         this.IsOK = 1;

         while (this.isRecording) 
         {
              synchronized (mutex) 
              {
                   if (this.isPaused) 
                   {
                        try 
                        {
                             mutex.wait(250);
                        } 
                        catch (InterruptedException e) 
                        {
                             throw new IllegalStateException("Wait() interrupted!", e);
                        }
                        continue;
                   }
              }
              if(smpBuffer != null)
              {
                bufferRead = recordInstance.read(smpBuffer, frameSize*2);

                if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
                  throw new IllegalStateException(
                            "read() returned AudioRecord.ERROR_INVALID_OPERATION");
                } else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
                  throw new IllegalStateException(
                            "read() returned AudioRecord.ERROR_BAD_VALUE");
                } else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
                  throw new IllegalStateException(
                            "read() returned AudioRecord.ERROR_INVALID_OPERATION");
                }
              else myFDA.SetData(smpBuffer);
              }
         }

         recordInstance.stop();
         recordInstance.release();
    }
    
    public void setRA( FreqDetActivity setFDA)
    {
   	 this.myFDA = setFDA;
    }
    
    public void setframeSize( int frsize)
    {
   	 this.frameSize = frsize;
	 this.smpBuffer = ByteBuffer.allocateDirect(frameSize*2);   	 
//  	 this.smpBuffer = new short[frameSize];
    }

    public void setRecording(boolean isRecording) {
         synchronized (mutex) {
              this.isRecording = isRecording;
              if (this.isRecording) {
                   mutex.notify();
              }
         }
    }

    public boolean isRecording() {
         synchronized (mutex) {
              return isRecording;
         }
    }

    public void setFrequency(int frequency) {
         this.frequency = frequency;
    }

    public int getFrequency() {
         return frequency;
    }

    public void setChannelConfiguration(int channelConfiguration) {
         this.channelConfiguration = channelConfiguration;
    }

    public int getChannelConfiguration() {
         return channelConfiguration;
    }

    public int getAudioEncoding() {
         return audioEncoding;
    }

    public void setPaused(boolean isPaused) {
         synchronized (mutex) {
              this.isPaused = isPaused;
         }
    }

    public boolean isPaused() {
         synchronized (mutex) {
              return isPaused;
         }
    }
}
