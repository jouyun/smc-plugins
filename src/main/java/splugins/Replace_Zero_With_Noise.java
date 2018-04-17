package splugins;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Replace_Zero_With_Noise implements PlugIn {
	
	class RunnableWrapper implements Runnable
	{
		public RunnableWrapper(ImageProcessor i, int c, float r, float s, float e, int t)
		{
			ip=i;
			mycount=c;
			replaceme=r;
			startval=s;
			endval=e;
			type=t;
		}
		public void run()
		{
			//IJ.log("Starting: "+mycount);
			switch (type) 
			{
				case ImagePlus.GRAY32: 
				{
					do_replace32(ip, replaceme, startval, endval);
					break;
				}
				case ImagePlus.GRAY8:
				{
					do_replace8(ip, (byte)replaceme, (byte)startval, (byte)endval);
					break;
				}
				case ImagePlus.GRAY16:
				{
					do_replace16(ip, (short)replaceme, (short)startval, (short)endval);
					break;
				}
				default:
				{}
			}
			//IJ.log("Thread done: "+mycount);
		}
		ImageProcessor ip;
		int mycount;
		float replaceme;
		float startval;
		float endval;
		int type;
		public void do_replace32(ImageProcessor ip, float to_replace, float init, float last)
		{
			float [] pix=(float []) ip.getPixels();
			int width=ip.getWidth(), height=ip.getHeight();
			for (int i=0; i<width*height; i++)
			{
				if (pix[i]==to_replace)
				{
					pix[i]=(float)ThreadLocalRandom.current().nextFloat()*(last-init)+init;
				}
			}
		}
		public void do_replace8(ImageProcessor ip, byte to_replace, byte init, byte last)
		{
			byte [] pix=(byte []) ip.getPixels();
			int width=ip.getWidth(), height=ip.getHeight();
			for (int i=0; i<width*height; i++)
			{
				if (pix[i]==to_replace)
				{
					pix[i]=(byte)(ThreadLocalRandom.current().nextFloat()*(last-init)+init);
				}
			}
		}
		public void do_replace16(ImageProcessor ip, short to_replace, short init, short last)
		{
			short [] pix=(short []) ip.getPixels();
			int width=ip.getWidth(), height=ip.getHeight();
			for (int i=0; i<width*height; i++)
			{
				if (pix[i]==to_replace)
				{
					pix[i]=(short)(ThreadLocalRandom.current().nextFloat()*(last-init)+init);
				}
			}
		}
	}
	@Override
	public void run(String arg0) {
		ImagePlus myimg=WindowManager.getCurrentImage();
		GenericDialog gd=new GenericDialog("Replace zero with noise");
		gd.addNumericField("Baseline:", 0, 2);
		gd.addNumericField("Spread:", 1, 2);
		gd.showDialog();
		float base=(float)gd.getNextNumber();
		float spread=(float)gd.getNextNumber();
		
		ExecutorService executor=Executors.newFixedThreadPool(32);
		for (int i=0; i<myimg.getStackSize(); i++)
		{
			Runnable worker=new RunnableWrapper(myimg.getStack().getProcessor(i+1), i, 0, base, spread, myimg.getType());
			executor.execute(worker);
			//MyThread obj=new MyThread();
			//obj.init(myimg.getStack().getProcessor(i+1), i, 0, base, spread, myimg.getType());
			//obj.start();
			
		}
		executor.shutdown();
		while (!executor.isTerminated()){}
		IJ.log("All done");
		
	}


}
