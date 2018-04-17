package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;

public class Separate_Out_3D_Objects implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames();
		int current_channel=img.getC()-1, current_slice=img.getSlice()-1, current_frame=img.getFrame()-1;
		
		int current_object=1;
		
		boolean keep_going=true;
		while (keep_going==true)
		{
			int max_x=-1, max_y=-1, max_z=-1;
			int min_x=10000000, min_y=10000000, min_z=10000000;
			for (int s=0; s<slices; s++)
			{
				float [] pix=(float []) img.getStack().getProcessor(1+current_channel+s*channels+current_frame*slices).getPixels();
				for (int x=0; x<width; x++)
				{
					for (int y=0; y<height; y++)
					{
						if (!(pix[x+y*width]==current_object)) continue;
						if (s>max_z) max_z=s;
						if (s<min_z) min_z=s;
						if (x>max_x) max_x=x;
						if (x<min_x) min_x=x;
						if (y>max_y) max_y=y;
						if (y<min_y) min_y=y;
					}
				}				
			}
			if (max_x==-1)
			{
				keep_going=false;
				continue;
			}
			//Only cases where object "current_object" found
			max_x=max_x-min_x;
			max_y=max_y-min_y;
			int[] xpts={min_x, max_x, max_x, min_x};
			int[] ypts={min_y, max_y, min_y, max_y};
			img.setRoi(new Roi(min_x+1, min_y+1, max_x, max_y));
			
			ImagePlus dup=new Duplicator().run(img, 1, channels, min_z+1, max_z+1, 1, frames);
			dup.setTitle("Object"+current_object);
			dup.show();
			dup.updateAndDraw();
			
			current_object++;
		}

	}

}
