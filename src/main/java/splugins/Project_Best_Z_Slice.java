package splugins;

import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Plot;
import ij.plugin.PlugIn;

public class Project_Best_Z_Slice implements PlugIn {

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		int channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames(), channel=img.getChannel()-1, frame=img.getFrame()-1;
		int width=img.getWidth();
		int height=img.getHeight();
		
		float [] data=new float[slices];
		float [] labels=new float[slices];
		
		ImagePlus new_img=NewImage.createFloatImage(img.getTitle()+"_BestProjected", width, height, frames*channels, NewImage.FILL_BLACK);
		new_img.setDimensions(channels, 1, frames);
		new_img.setOpenAsHyperStack(true);
		
		
		for (int f=0; f<frames; f++)
		{
			for (int z=0; z<slices; z++)
			{
				float [] pix = (float []) (img.getStack().getProcessor(channel+z*channels+f*channels*slices+1).convertToFloat().getPixels());
				data[z]=(float) (calculate_auto_corr(pix, pix, width, height, 1)-calculate_auto_corr(pix, pix, width, height, 2));
				labels[z]=z;
			}
			
			int best_slice=-1;
			float best_auto = -1000;
			for (int z=0; z<slices; z++)
			{
				if (data[z]>best_auto) 
				{
					best_auto=data[z];
					best_slice=z;
				}
			}
			
			for (int c=0; c<channels; c++)
			{
				float [] new_pix = (float []) (new_img.getStack().getProcessor(c+f*channels+1).convertToFloat().getPixels());
				IJ.log(""+c+","+best_slice+","+f);
				float [] pix = (float []) (img.getStack().getProcessor(c+best_slice*channels+f*slices*channels+1).convertToFloat().getPixels());
				for (int i=0; i<new_pix.length; i++)
				{
					new_pix[i] = pix[i];
				}
			}
			new_img.show();
			new_img.updateAndDraw();
		}
	}
	
	static public double calculate_auto_corr(float [] Apix, float [] Bpix, int width, int height, int shift)
	{
		double rtn=0;
		for (int y=0; y<height; y++)
		{
			for (int x=shift; x<width-shift; x++)
			{
				rtn=rtn+Apix[x+y*width]*Bpix[x+y*width+shift];
			}
		}
		return rtn;
	}
}
