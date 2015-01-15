package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
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
		GenericDialog gd=new GenericDialog("Process what");
		gd.addCheckbox("Process stack?", false);
		gd.showDialog();
		boolean process_stack=gd.getNextBoolean();
		
		if (!process_stack)
		{
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
		else
		{
			ImagePlus new_imp=NewImage.createFloatImage("Img", slices, frames, 1, NewImage.FILL_BLACK);
			float [] new_pix=(float []) new_imp.getProcessor().getPixels();
			for (int f=0; f<frames; f++)
			{
				for (int z=0; z<slices; z++)
				{
					float [] pix=(float [])(img.getStack().getProcessor(channel+z*channels+f*channels*slices+1).convertToFloat().getPixels());
					new_pix[z+f*slices]=(float) (calculate_auto_corr(pix, pix, width, height, 1)-calculate_auto_corr(pix, pix, width, height, 2));
					labels[z]=z+1;
					IJ.log("Slice "+z+": "+data[z]);
				}
			}
			new_imp.show();
			new_imp.updateAndDraw();
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
