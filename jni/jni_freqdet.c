//-----------------------------------------------------------------------
//
//	Frequency detection algorithm for Alcohoot
//	(c) 2012 by RadonSoft GmbH
//	    All Rights Reserved
//
//	Version: 1.0, initial release ported from java
//
//-----------------------------------------------------------------------


#include <string.h>
#include <jni.h>
#include <math.h>
#include <stdlib.h>

int iLen = 0;
float *pfCosTab = NULL;
float *pfSinTab = NULL;
float *pfWinTab = NULL;
float *pfFFTBuf = NULL;
short *sbuf = NULL;


void  InitDetect( int size);
void  ExitDetect( void);
float DoDetect( short *buf, int size);

JNIEXPORT void JNICALL
Java_radonsoft_net_freqdet_FreqDetActivity_InitDetect( JNIEnv*  env, jobject  thiz, jint size)
{
	InitDetect(size);
}

JNIEXPORT void JNICALL
Java_radonsoft_net_freqdet_FreqDetActivity_ExitDetect( JNIEnv*  env, jobject  thiz )
{
	ExitDetect();
}

JNIEXPORT float JNICALL
Java_radonsoft_net_freqdet_FreqDetActivity_DoDetect( JNIEnv*  env, jobject  thiz, jobject jsbuf , jint size)
{
	short *buf = (*env)->GetDirectBufferAddress(env, jsbuf);
//	short *buf = (*env)->GetShortArrayElements(env, jsbuf, 0);
	return DoDetect( buf, size);
//	(*env)->ReleaseShortArrayElements(env, jsbuf, buf, 0);
}


void CreateSinCos( void)
{
	int i;
    float f, df, pi;
	int anz = iLen / 2;

	pi = (float)(4.0f*atan(1.0f));

	f = 0;
	df = pi / ((float)anz * 2.0f);
	pfCosTab[0] = 1.0;
	for(i=0;i<anz;i++)
	  pfCosTab[i+1] = cos(f), f += df;

	f = 0;
	df = pi / (float)anz;
	for(i=0;i<anz;i++)
	  pfSinTab[i] = sin(f), f += df;

	for(i=0;i<anz/2;i++)
	  pfSinTab[i+anz] = pfSinTab[i];
}

void CreateWinBuffer( float *wint)
{
		int i;
		int len = iLen;
 		float df=(float)(4.0f*atan(1.0f)/(float)len);
		float f=df/2.0f;
		
		float fak = 1.4142f / (32768.0f * sqrt((float)len));
		for(i=0;i<len;i++)
		{
		    wint[i] = fak * (0.5f*(1.0f-(float)cos(f)));
		    f+=df;
		}
}

void InitDetect(int size)
{
	iLen  = size/2;
	int n = size*4;
	pfCosTab = malloc(n/4+4);
	pfSinTab = malloc(n/4+n/8);
	pfWinTab = malloc(n/2);
	pfFFTBuf = malloc(n);
	sbuf     = malloc(size*2);
	memset(sbuf,0,size*2);
	CreateSinCos();
	CreateWinBuffer(pfWinTab);
}

void ExitDetect( void)
{
	if(pfCosTab) free (pfCosTab);
	if(pfSinTab) free (pfSinTab);
	if(pfWinTab) free (pfWinTab);
	if(pfFFTBuf) free (pfFFTBuf);
	if(sbuf)	 free (sbuf);
}

void DoWindow(short *s_in, float *fout)
{
	int top = iLen*2-1;
	int i;
    for(i=0;i<iLen;i++)
    {
		fout[i] = pfWinTab[i] * (float) s_in[i];
		fout[top - i] = pfWinTab[i] * (float) s_in[top - i];                	 
    }
}

void DoAmp(float *fbuf)
{
	int i;
	float sum = 0;
	float sq;
	for(i=1;i<iLen;i++)
	{
	  sq = fbuf[i*2]*fbuf[i*2] + fbuf[i*2+1]*fbuf[i*2+1];
	  sum += sq;
	  fbuf[i] = sqrt(sq);
	}
//	fbuf[0] = sqrt(sq/(float)(iLen*2));
	fbuf[0] = 0;
}

void DoRealFFTfw(float *yf)
{
	long			nn = iLen;
	long 			ii,n,m,mmax,j=1,istep, i;
	long			si, ci, sca, k, l;
	float 			wr,wi,tempr,tempi;
	float			h1r, h1i, h2r, h2i, c1, c2;

	n=2*nn;
	for (ii=1;ii<=nn;ii++)
	{
		i=2*ii-1;
		if (j>i)
		{
			tempr=yf[j-1];
			tempi=yf[j];
			yf[j-1]=yf[i-1];
			yf[j]=yf[i];
			yf[i-1]=tempr;
			yf[i]=tempi;
		}
		m=n/2;
		while (m>=2 && j>m)
		{
			j-=m;
			m=m/2;
		}
		j+=m;
	}

	i=1;
	j=3;
	while(i<=n)
	{
		tempr=yf[j-1];
		tempi=yf[j];
		yf[j-1]  = yf[i-1] - yf[j-1];
		yf[j]    = yf[i]   - yf[j];
		yf[i-1] += tempr;
		yf[i]   += tempi;
		i+=4;
		j+=4;
	}
	i=1;
	j=5;
	while(i<=n)
	{
		tempr=yf[j-1];
		tempi=yf[j];
		yf[j-1]  = yf[i-1] - yf[j-1];
		yf[j]    = yf[i]   - yf[j];
		yf[i-1] += tempr;
		yf[i]   += tempi;
		i+=8;
		j+=8;
	}
	i=3;
	j=7;
	while(i<=n)
	{
		tempr    = yf[j-1];
		yf[j-1]  = yf[i-1] + yf[j];
		yf[i-1] -= yf[j];
		yf[j]    = yf[i]   - tempr;
		yf[i]   += tempr;
		i+=8;
		j+=8;
	}

	mmax=8;

	while (n>mmax)
	{
		istep=2*mmax;
				
		sca = nn/mmax;
		si  = sca;
		ci  = sca+nn/4;

		i=1;
		j=mmax+1;
		while(i<=n)
		{
		  tempr=yf[j-1];
		  tempi=yf[j];
		  yf[j-1]  = yf[i-1] - yf[j-1];
		  yf[j]    = yf[i]   - yf[j];
		  yf[i-1] += tempr;
		  yf[i]   += tempi;
		  i+=istep;
		  j+=istep;
		}
			
		for (ii=2;ii<=(mmax/4);ii++)
		{
			wr=pfSinTab[ci];
			wi=pfSinTab[si];
			ci += sca;
			si += sca;
			i=2*ii-1;
			j=i+mmax;
			while(i<=n)
			{
				tempr=wr*yf[j-1]-wi*yf[j];
				tempi=wr*yf[j]+wi*yf[j-1];
				yf[j-1]=yf[i-1]-tempr;
				yf[j]=yf[i]-tempi;
				yf[i-1]+=tempr;
				yf[i]+=tempi;
				i+=istep;
				j+=istep;
			}
		}
		
		i=mmax/2+1;
		j=i+mmax;
		while(i<=n)
		{
			tempr    = yf[j-1];
			yf[j-1]  = yf[i-1] + yf[j];
			yf[i-1] -= yf[j];
			yf[j]    = yf[i]   - tempr;
			yf[i]   += tempr;
			i+=istep;
			j+=istep;
		}

		si  += sca;
		ci  += sca;
		for (ii=2;ii<=(mmax/4);ii++)
		{
			wr=-pfSinTab[ci];
			wi=pfSinTab[si];
			ci += sca;
			si += sca;
			i=mmax/2 + 2*ii-1;
			j=i+mmax;
			while(i<=n)
			{
				tempr=wr*yf[j-1]-wi*yf[j];
				tempi=wr*yf[j]+wi*yf[j-1];
				yf[j-1]=yf[i-1]-tempr;
				yf[j]=yf[i]-tempi;
				yf[i-1]+=tempr;
				yf[i]+=tempi;
				i+=istep;
				j+=istep;
			}
		}
		
		mmax=istep;
	}

	c1=0.5;
	c2=-0.5;
	
	j = n-2;
	k = nn/2;
	l = 2;

	for (i=2;i<nn;i+=2)
	{
		wr=pfCosTab[l];
		wi=pfCosTab[k];
		l += 1;
		k -= 1;
		h1r    = c1*(yf[i]+yf[j]);
		h1i    = c1*(yf[i+1]-yf[j+1]);
		h2r    = -c2*(yf[i+1]+yf[j+1]);
		h2i    = c2*(yf[i]-yf[j]);
		yf[i]  = h1r+wr*h2r-wi*h2i;
		yf[i+1]= h1i+wr*h2i+wi*h2r;
		yf[j]  = h1r-wr*h2r+wi*h2i;
		yf[j+1]= -h1i+wr*h2i+wi*h2r;
		j-=2;
	}

	h1r=yf[0];
	yf[0]=h1r+yf[1];
	yf[1]=h1r-yf[1];
}

float DoDetect( short *buf, int size)
{
	int i;
	if(size > iLen*2) return iLen/2;
	memmove(sbuf, sbuf+size, (iLen*2-size)*2);
	memcpy(sbuf+(iLen*2-size), buf, size*2);
	DoWindow(sbuf, pfFFTBuf);
	DoRealFFTfw(pfFFTBuf);
	
	float maxamp = 0;
    int maxidx = 4;
        
    for(i=iLen/4;i<iLen-3;i++)	// change this to limit bandwidth
    {
        float amp = pfFFTBuf[i*2] * pfFFTBuf[i*2] + pfFFTBuf[i*2+1]*pfFFTBuf[i*2+1];
        if(amp > maxamp)
        {
			maxamp = amp;
			maxidx = i;
        }
    }

    if(maxidx < iLen/4) return iLen/4;

    float y1  = pfFFTBuf[(maxidx-1)*2] * pfFFTBuf[(maxidx-1)*2] + pfFFTBuf[(maxidx-1)*2+1]*pfFFTBuf[(maxidx-1)*2+1];
    float y2  = pfFFTBuf[(maxidx)*2] * pfFFTBuf[(maxidx)*2] + pfFFTBuf[(maxidx)*2+1]*pfFFTBuf[(maxidx)*2+1];
    float y3  = pfFFTBuf[(maxidx+1)*2] * pfFFTBuf[(maxidx+1)*2] + pfFFTBuf[(maxidx+1)*2+1]*pfFFTBuf[(maxidx+1)*2+1];
    float d1 = (y3 - y1) / (y1 + y2 + y3);
    float d2 = (y3 - y1) / (2 * (2 * y2 - y1 - y3));
    float k3 = (float)maxidx + (d1 + d2)*0.5f;
	return k3;
}
