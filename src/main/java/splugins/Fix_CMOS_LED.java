package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class Fix_CMOS_LED implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus imp;
		int width, height, slices, frames, channels, cur_slice, cur_frame, cur_channel;
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		
		double [][]average_arrays=new double[channels][height];
		
		for (int c=0; c<channels; c++)
		{
			for (int s=0; s<slices; s++)
			{
				float [] pix=(float[]) imp.getStack().getProcessor(1+c+s*channels).getPixels();
				for (int x=0; x<width; x++)
				{
					for (int y=0; y<height; y++)
					{
						average_arrays[c][y]+=pix[x+y*width]/width/slices;
					}
				}
			}
		}
		
		for (int c=0; c<channels; c++)
		{
			for (int s=0; s<slices; s++)
			{
				float [] pix=(float[]) imp.getStack().getProcessor(1+c+s*channels).getPixels();
				for (int y=0; y<height; y++)
				{
					double tmp=0;
					for (int x=0; x<width; x++)
					{
						tmp+=pix[x+y*width]/width;
					}
					
					for (int x=0; x<width; x++)
					{
						pix[x+y*width]=(float) (pix[x+y*width]*(average_arrays[c][y]/tmp));
					}
				}
			}
		}
		imp.updateAndDraw();

	}

}
