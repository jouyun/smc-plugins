package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Mask_Single_Pixel_Value implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		GenericDialog gd=new GenericDialog("Mask single pixel");
		gd.addNumericField("Pixel value", 1, 0);
		gd.showDialog();
		short val=(short)gd.getNextNumber();
		
		for (int s=0; s<img.getStackSize(); s++)
		{
			short [] pix=(short []) img.getStack().getProcessor(s+1).getPixels();
			for (int xy=0; xy<img.getWidth()*img.getHeight(); xy++)
			{
				if (pix[xy]!=val) pix[xy]=0;
				else pix[xy]=1;
			}
		}

	}

}
