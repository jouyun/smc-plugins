package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class Crop_NonZero implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames(), channel=img.getChannel()-1, frame=img.getFrame()-1;
		int width=img.getWidth();
		int height=img.getHeight();
		int x_max=0, x_min=width, y_min=height, y_max=0, z_min=slices, z_max=0;
		for (int f=0; f<frames; f++)
		{
			for (int s=0; s<slices; s++)
			{
				float [] pix=(float []) img.getStack().getProcessor(channel+s*channels+f*channels*slices+1).convertToFloat().getPixels();
				
				for (int x=0; x<width; x++)
				{
					for (int y=0; y<height; y++)
					{
						if (pix[x+y*width]==0) continue;
						if (x<x_min) x_min=x;
						if (x>x_max) x_max=x;
						if (y<y_min) y_min=y;
						if (y>y_max) y_max=y;
						if (s>z_max) z_max=s;
						if (s<z_min) z_min=s;
					}
				}
			}
		}
		//IJ.log("Range: "+x_min+","+x_max+","+y_min+","+y_max+","+z_min+","+z_max);
		//img.getProcessor().setRoi(x_min,y_min,(x_max-x_min),(y_max-y_min));
		//img.setProcessor(null,img.getProcessor().crop());
		ImageStack new_stack=img.getStack().crop(x_min,y_min,z_min, (x_max-x_min), (y_max-y_min), (z_max-z_min));
		ImagePlus new_img=new ImagePlus("Cropped", new_stack);
		new_img.show();
		new_img.updateAndDraw();
		
	}

}
