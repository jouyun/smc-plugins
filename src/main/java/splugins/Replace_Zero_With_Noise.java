package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Replace_Zero_With_Noise implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus myimg=WindowManager.getCurrentImage();
		GenericDialog gd=new GenericDialog("Replace zero with noise");
		gd.addNumericField("Baseline:", 0, 2);
		gd.addNumericField("Spread:", 1, 2);
		gd.showDialog();
		float base=(float)gd.getNextNumber();
		float spread=(float)gd.getNextNumber();
		
		switch (myimg.getType()) 
		{
			case ImagePlus.GRAY32: 
			{
				for (int i=0; i<myimg.getStackSize(); i++)
				{
					do_replace(myimg.getStack().getProcessor(i+1), 0.0f, base, spread);
				}	
			}
			case ImagePlus.GRAY8:
			{
				for (int i=0; i<myimg.getStackSize(); i++)
				{
					do_replace(myimg.getStack().getProcessor(i+1), (byte)0, (byte)base, (byte)spread);
				}
			}
			case ImagePlus.GRAY16:
			{
				for (int i=0; i<myimg.getStackSize(); i++)
				{
					do_replace(myimg.getStack().getProcessor(i+1), (short)0, (short)base, (short)spread);
				}
			}
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
	public static void do_replace(ImageProcessor ip, byte to_replace, byte init, byte last)
	{
		byte [] pix=(byte []) ip.getPixels();
		int width=ip.getWidth(), height=ip.getHeight();
		for (int i=0; i<width*height; i++)
		{
			if (pix[i]==to_replace)
			{
				pix[i]=(byte)(Math.random()*(last-init)+init);
			}
		}
	}
	public static void do_replace(ImageProcessor ip, short to_replace, short init, short last)
	{
		short [] pix=(short []) ip.getPixels();
		int width=ip.getWidth(), height=ip.getHeight();
		for (int i=0; i<width*height; i++)
		{
			if (pix[i]==to_replace)
			{
				pix[i]=(short)(Math.random()*(last-init)+init);
			}
		}
	}

}
