package radonsoft.net.freqdet;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class graph extends View {
	private Paint	mPaint1 = new Paint();
	private Paint   mPaint2 = new Paint();
	private Paint   mPaint3 = new Paint();
	private Paint   mPaint4 = new Paint();
	private Canvas  mCanvas;
    private Bitmap  mBitmap;
	
	int ctrlheight;
	int ctrlwidth;
	float xfak;
	int x1;
    int x2;
    int x3;
	int y;
	int offx1;
	int offx2;
	int offx3;
	int offy;
	int txtsize1;
	int txtsize2;
	int txtsize3;
	int txtsize4;
	boolean IsInit = false;
	float ymax=4.0f;
	float xpos;
	float dxpos;
	int lstyval;

	
    public graph(Context context) {
        super(context);
        setFocusable(true);
    	mPaint1.setColor(0xFFffff00);
    	mPaint2.setColor(0xff000000);
    	mPaint4.setColor(0xff00ffc0);
    	mPaint3.setAntiAlias(true);
        mPaint3.setTypeface(Typeface.SERIF);
        mPaint3.setStyle(Style.STROKE);
    }
    
    public graph(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
    	mPaint1.setColor(0xFFffff00);
    	mPaint2.setColor(0xff000000);
    	mPaint4.setColor(0xff00ffc0);
    	mPaint3.setAntiAlias(true);
        mPaint3.setTypeface(Typeface.SERIF);
        mPaint3.setStyle(Style.STROKE);
    }

    protected void DrawFrame( Canvas canvas)
    {
        canvas.drawRect(0, 0, ctrlwidth, ctrlheight, mPaint2);
    	canvas.drawLine(offx2, y+15, offx2, offy, mPaint1);
    	canvas.drawLine(offx2-15, y, offx2+x2, y, mPaint1);
    	float dx = x2/21;
    	float dy = (y)/11;
    	mPaint1.setTextSize(txtsize2);
    	
    	for (int i=0;i<21;i++)
    	{
    		canvas.drawLine(offx2+dx*i, y-5, offx2+dx*i, y+5, mPaint1);
//    		String s = String.valueOf(i/2);
//    		if(i%2==1)s=s.concat(".5");
//    		else s=s.concat(".0");
    		String s = String.valueOf(i);//.concat(".0");    	
    		canvas.drawText(s,offx2+dx*i,y+10+txtsize2, mPaint1);
    	}
    	canvas.drawText("[s]",offx2+dx*20+txtsize2*2,y+10+txtsize2, mPaint1);

    	float dy2=ymax/10;
    	float yt=0;
    	for (int i=0;i<11;i++)
    	{
    		canvas.drawLine(offx2-5, y-dy*i, offx2+5, y-dy*i, mPaint1);
    		int iy = (int)yt;
       		String s = String.valueOf(iy) + "." + String.valueOf((int)(yt*10.0f) - (iy*10));
       		yt += dy2;
       		canvas.drawText(s, offx2-10-2*txtsize2, y-dy*i, mPaint1);
    	}
    	canvas.drawText("[%]",offx2-10-2*txtsize2, y+(int)(txtsize2*1.5f), mPaint1);
    	
    	mPaint3.setColor(0xFFC0C0C0);
        canvas.drawRect(offx3, offy-1, offx3+x3, offy+y+1, mPaint3);			// frame for text
        canvas.drawRect(offx3, offy+y/2-2, offx3+x3, offy+y/2-1, mPaint3);	// frame for text
        mPaint3.setTextSize(txtsize3);
    	canvas.drawText("Freq", offx3+(10*xfak), offy+(25*xfak), mPaint3);
		canvas.drawText("BAC", offx3+(15*xfak), offy+y/2+(25*xfak), mPaint3);
		mPaint3.setTextSize(txtsize2);
	    canvas.drawText("[Hz]", offx3+(21.0f*xfak), offy+(50.0f*xfak), mPaint3);
		canvas.drawText("[%]", offx3+(22.0f*xfak), offy+y/2+(50.0f*xfak), mPaint3);
    }
    
    public void clr()
    {
    	if(!IsInit) return;
    	xpos = 0;
        lstyval = y;
    	mCanvas.drawRect(0, 0, ctrlwidth, ctrlheight, mPaint2);
    	DrawFrame(mCanvas);
    	postInvalidate();
    }
    
    public void setVal( float val)
    {
    	if(!IsInit) return;
    	int yval = (int)((float)y*0.88f / ymax * val);
    	if(yval == lstyval) yval += 1;
		mCanvas.drawRect(offx2+xpos, y-yval-2, offx2+xpos+dxpos+1, y-lstyval-2, mPaint4);
//    	postInvalidate((int)(offx2+xpos),  y-yval-2, (int)(offx2+xpos+dxpos+1), y-lstyval-2);
    	postInvalidate();
    	xpos+=dxpos;
    	lstyval = (int)yval;
    }
    
    public void setYmax(float setmax)
    {
    	ymax = setmax;
    	if(!IsInit) return;
    	DrawFrame(mCanvas);
    	postInvalidate();
    }

    public void setMaxVal(float setmax, float maxfrq)
    {
    	int imax = (int)setmax;
    	int imax10 = (int)(setmax*10.0f) - imax*10;
    	int imax100 = (int)(setmax*100.0f) - imax*100 - imax10*10;
    	int imax1000 = (int)(setmax*1000.0f) - imax*1000 - imax10*100 - imax100*10;
    	String s1 = String.valueOf(imax) + "." + String.valueOf(imax10) + String.valueOf(imax100) + String.valueOf(imax1000);
    	String s2 = String.valueOf((int)maxfrq);
       	int y1 = offy+(int)(85.0f*xfak);
       	int y2 = y1 + y/2;
       	int x1 = offx3+(int)(3*xfak);
       	int x2 = offx3+(int)(8*xfak);
       	int adj = txtsize4 / 3;
//      	if(ia < 10000) x1 += adj;
//       	if(ia < 1000) x1 += adj;
//       	if(ia < 100) x1 += adj;
//       	if(ib < 10) x2 += adj;
       	x2 += adj;
        mPaint3.setColor(0xFFffffff);
      	mPaint3.setTextSize(txtsize4);
      	mCanvas.drawRect(offx3+2, y1 - txtsize4, offx3+x3-2, y1+2, mPaint2);
   		mCanvas.drawRect(offx3+2, y2 - txtsize4, offx3+x3-2, y2+2, mPaint2);
      	mCanvas.drawText(s2, x1, y1, mPaint3);
      	mCanvas.drawText(s1, x1, y2, mPaint3);
    }

    
    @Override protected void onDraw(Canvas canvas) {

    	if(!IsInit) return;
    	canvas.drawBitmap(mBitmap, 0, 0, null);
    }
    
    protected void initBitmap(int ctrlw, int ctrlh)
    {
        mBitmap = Bitmap.createBitmap(ctrlw, ctrlh, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }
    
    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom)
    {
    	if(!changed) return;
    	
    	IsInit = false;
    	ctrlwidth = right - left;
        ctrlheight = bottom - top;

        xfak = (float)(ctrlheight / 256.0f);
        offx1 = (int)(8.0*xfak);
        offx2 = (int)(48.0*xfak);
        x1 = (int)(20.0*xfak);
        x2 = ctrlwidth - x1 - (int)(150.0*xfak);
        //x2 = x2/2;
        x3 = (int)(80.0*xfak);
        offx3 = offx2 + x2 + (int)(30.0*xfak);
        offy = 25;
        y  = ctrlheight - 30;
        dxpos = (float)x2*512.0f/(48000.0f * 5.0f)*10.0f/21.0f;
    
        txtsize1 = (int)(9.0*xfak);
        txtsize2 = (int)(9.0*xfak);
        txtsize3 = (int)(20.0*xfak);
        txtsize4 = (int)(26.0*xfak);
        
        initBitmap(ctrlwidth, ctrlheight);
        IsInit = true;
        clr();
    }
}
