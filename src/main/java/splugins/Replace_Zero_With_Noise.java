package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Replace_Zero_With_Noise implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus myimg=WindowManager.getCurrentImage();
		for (int i=0; i<myimg.getStackSize(); i++)
		{
			do_replace(myimg.getStack().getProcessor(i+1), 0.0f, 0f, 255f);
		}

	}
	public static void do_replace(ImageProcessor ip, float to_replace, float init, float last)
	{
		float [] pix=(float []) ip.getPixels();
		int width=ip.getWidth(), height=ip.getHeight();
		for (int i=0; i<width*height; i++)
		{
			if (pix[i]==to_replace)
			{
				pix[i]=(float)Math.random()*(last-init)+init;
			}
		}
	}

}
