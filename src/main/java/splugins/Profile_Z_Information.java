package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Plot;
import ij.plugin.PlugIn;

public class Profile_Z_Information implements PlugIn {

	
	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames(), channel=img.getChannel()-1, frame=img.getFrame()-1;
		int width=img.getWidth();
		int height=img.getHeight();
		float [] data=new float[slices];
		float [] labels=new float[slices];
		for (int z=0; z<slices; z++)
		{
			float [] pix=(float [])(img.getStack().getProcessor(channel+z*channels+frame*channels*slices+1).convertToFloat().getPixels());
			data[z]=(float) (calculate_auto_corr(pix, pix, width, height, 1)-calculate_auto_corr(pix, pix, width, height, 2));
			labels[z]=z+1;
			IJ.log("Slice "+z+": "+data[z]);
		}
					
		Plot plot=new Plot("My plot", "X", "Y", labels, data);
		plot.show();


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
