package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Rotate_Slices implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames();
		GenericDialog gd=new GenericDialog("Choose slices");
		gd.addNumericField("First slice: ", 0, 0);
		gd.addNumericField("Last slice: ", 0, 0);
		gd.addNumericField("Angle: ", 180, 0);
		gd.showDialog();
		
		int first=(int)gd.getNextNumber();
		int last=(int)gd.getNextNumber();
		double angle=gd.getNextNumber();
		
		for (int s=first; s<=last; s++)
		{
			for (int c=0; c<channels; c++)
			{
				img.getStack().getProcessor(1+c+(s-1)*channels).rotate(angle);
			}
		}
		img.updateAndDraw();
	}

}
