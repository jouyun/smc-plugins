package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Scale_Ramp_Zstack implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames();
		
		GenericDialog gd=new GenericDialog("Config Ramp");
		gd.addNumericField("Background:  ", 700, 1);
		gd.addNumericField("Final slice's intensity :  ", 0.1, 2);
		gd.showDialog();
		
		double background=gd.getNextNumber();
		double last_slice=gd.getNextNumber();
		double increment=(1-last_slice)/(slices-1);
		for (int f=0; f<frames; f++)
		{
			for (int c=0; c<channels; c++)
			{
				for (int s=0; s<slices; s++)
				{
					float [] frame=(float []) img.getStack().getProcessor(1+c+s*channels+f*channels*slices).getPixels();
					for (int i=0; i<frame.length; i++)
					{
						frame[i]=(float) (((frame[i]-background)/(1-s*increment))+background);
					}
				}
			}
		}
		img.updateAndDraw();
	}
}
