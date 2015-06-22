package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Project_In_Y implements PlugIn {

	@Override
	public void run(String arg0) {

		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames();
		
		ImagePlus new_img=NewImage.createFloatImage("projection", width, slices, 1, NewImage.FILL_BLACK);
		float [] new_pix=(float []) new_img.getProcessor().getPixels();
		for (int s=0; s<slices; s++)
		{
			float [] cur_slice=(float[]) img.getStack().getProcessor(s+1).getPixels();
			
			for (int i=0; i<height; i++)
			{
				for (int j=0; j<width; j++)
				{
					new_pix[j+width*s]=new_pix[j+width*s]+cur_slice[j+i*width];
				}
			}
		}
		new_img.show();
		new_img.updateAndDraw();

	}

}
