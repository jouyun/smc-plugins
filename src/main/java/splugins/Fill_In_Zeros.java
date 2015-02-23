package splugins;

import java.util.ArrayList;
import java.util.ListIterator;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class Fill_In_Zeros implements PlugIn {

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
		
		float [] pix=(float [])imp.getProcessor().getPixels();
		
		//Find all of the non-zero points
		ArrayList <double []> measured_list=new ArrayList <double []>();
		for (int x=0; x<width; x++)
		{
			for (int y=0; y<height; y++)
			{
				if (pix[x+y*width]!=0)
				{
					double [] temp={x, y, pix[x+y*width]};
					measured_list.add(temp);
				}
			}
		}
		
		for (int x=0; x<width; x++)
		{
			for (int y=0; y<height; y++)
			{
				if (pix[x+y*width]!=0) continue;
				//double [][]my_list=new double[measured_list.size()][2];
				double min=1E100;
				double z=0;
				for (ListIterator jF=measured_list.listIterator();jF.hasNext();)
				{
					double [] temppt=(double [])jF.next();
					double dist=(temppt[0]-x)*(temppt[0]-x)+(temppt[1]-y)*(temppt[1]-y);
					if (dist<min)
					{
						min=dist;
						z=temppt[2];
					}
				}
				pix[x+y*width]=(float) z;				
			}
		}
		imp.updateAndDraw();

	}

}
