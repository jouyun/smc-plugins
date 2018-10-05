package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class Inflate_Dome_Mask implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		
		int height=img.getHeight(), width=img.getWidth(), slices=img.getNSlices();
		
		byte [] master_mask=new byte[height*width];
		
		for (int f=0; f<slices; f++)
		{
			byte [] pix=(byte [])img.getStack().getProcessor(f+1).getPixels();
			for (int p=0; p<width*height; p++)
			{
				if (pix[p]!=0) 
				{
					master_mask[p]=(byte) 255;
					continue;
				}
				if (master_mask[p]!=0)
				{
					pix[p]=(byte)255;
				}
				
			}
		}
		img.updateAndDraw();

	}

}
