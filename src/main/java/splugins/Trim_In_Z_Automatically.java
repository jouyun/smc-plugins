package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.gui.Plot;
import ij.plugin.PlugIn;

public class Trim_In_Z_Automatically implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames(), channel=img.getChannel()-1, frame=img.getFrame()-1;
		int width=img.getWidth();
		int height=img.getHeight();
		float [] data=new float[slices];

		//Get the z profile array
		for (int z=0; z<slices; z++)
		{
			float [] pix=(float [])(img.getStack().getProcessor(channel+z*channels+frame*channels*slices+1).convertToFloat().getPixels());
			data[z]=(float) (Profile_Z_Information.calculate_auto_corr(pix, pix, width, height, 1)-Profile_Z_Information.calculate_auto_corr(pix, pix, width, height, 2));
		}
		
		//Find the max autocorr and min autocorr
		double max=0, min=1E10;
		int max_idx=-1, min_idx=-1;
		for (int z=0; z<slices; z++)
		{
			if (data[z]>max)
			{
				max=data[z];
				max_idx=z;
			}
			if (data[z]<min)
			{
				min=data[z];
				min_idx=z;
			}
		}
		
		//Find the first slice that has an autocorr that is .05*(max-min) above the min
		int start_slice=0;
		for (int z=0; z<slices; z++)
		{
			if (data[z]>(max-min)*0.05+min)
			{
				start_slice=z;
				break;
			}
		}
		
		//Find the last slice that has an autocorr that is .05*(max-min) above the min
		int last_slice=slices-1;
		for (int z=slices-1; z>0; z--)
		{
			if (data[z]>(max-min)*0.05+min)
			{
				last_slice=z;
				break;
			}
		}
		
		int num_new_slices=(last_slice-start_slice+1);
		ImagePlus new_imp=NewImage.createFloatImage("Img", width, height, channels*num_new_slices, NewImage.FILL_BLACK);
		for (int c=0; c<channels; c++)
		{
			for (int z=0; z<num_new_slices; z++)
			{
				float [] new_pix=(float []) new_imp.getStack().getPixels(c+z*channels+1);
				float [] pix=(float []) img.getStack().getProcessor(c+(z+start_slice)*channels+1).convertToFloat().getPixels();
				for (int p=0; p<width*height; p++)
				{
					new_pix[p]=pix[p];
				}
			}
		}
		new_imp.setOpenAsHyperStack(true);
		new_imp.setDimensions(channels, num_new_slices, 1);
		new_imp.show();
		new_imp.updateAndDraw();
					

	}

}
