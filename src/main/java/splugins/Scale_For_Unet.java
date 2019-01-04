package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Scale_For_Unet implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int channels=img.getNChannels(), frames=img.getNFrames(), slices=img.getNSlices();
		int channel=img.getChannel();
		
		GenericDialog gd=new GenericDialog("Parameters");
		gd.addNumericField("Mean", 1, 2);
		gd.addNumericField("Standard Deviation", 1, 2);
		gd.showDialog();
		
		float mean=(float)gd.getNextNumber();
		float std=(float)gd.getNextNumber();
		
		for (int f=0; f<frames; f++)
		{
			for (int s=0; s<slices; s++)
			{
				float [] pix=(float [])img.getStack().getPixels(channel+s*channels+f*slices*channels);
				for (int p=0; p<pix.length; p++)
				{
					pix[p]=(float) ((pix[p]-mean)/std+0.5);
				}
			}
		}

	}

}
