package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class MultiThreadMockup implements PlugIn {

	class MyThread extends Thread
	{
		public void run()
		{
			int height=ip.getHeight(), width=ip.getWidth();
			byte [] pix=(byte [])ip.getPixels();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					pix[x+y*width]=(byte) (pix[x+y*width]*pix[x+y*width]);
					
				}
			}
			IJ.log("Thread done: "+mycount);
		}
		public void init(ImageProcessor i, int c)
		{
			ip=i;
			mycount=c;
		}
		ImageProcessor ip;
		int mycount;
	}
	
	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		for (int i=0; i<img.getStackSize(); i++)
		{
			MyThread t=new MyThread();
			t.init(img.getStack().getProcessor(i+1), i);
			t.start();
		}
		

	}

}
