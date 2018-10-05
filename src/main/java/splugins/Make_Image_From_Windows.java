package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Make_Image_From_Windows implements PlugIn{
	int height;
	int width;
	int slices;
	int frames;
	int channels;
	int window_size;
	int z_window_size;
	int shift_size;
	int z_shift_size;
	ImagePlus img;
	ImagePlus new_img;
	boolean staggered;
	@Override
	public void run(String arg0) {
		img=WindowManager.getCurrentImage();
		window_size=img.getHeight();
		channels=img.getNChannels();
		z_window_size=img.getNSlices();
		
		GenericDialog gd=new GenericDialog("Choose");
		gd.addNumericField("width: ", 128, 0);
		gd.addNumericField("height: ", 128, 0);
		gd.addNumericField("slices: ", 128, 0);
		gd.addCheckbox("Staggered? ", true);
		gd.showDialog();
		
		width=(int)gd.getNextNumber();
		height=(int)gd.getNextNumber();
		slices=(int)gd.getNextNumber();
		staggered=gd.getNextBoolean();
		
		int x_slices=(int)Math.floor(width/window_size);
		int y_slices=(int)Math.floor(height/window_size);
		int z_slices=(int)Math.floor(slices/z_window_size);
		
		height=(y_slices)*window_size;
		width=(x_slices)*window_size;
		slices=(z_slices)*z_window_size;
		
		shift_size=window_size;
		z_shift_size=z_window_size;
		
		
		if (staggered) 
		{
			x_slices=x_slices*2-1;
			y_slices=y_slices*2-1;
			shift_size=window_size/2;
			if (z_window_size>2)
			{
				z_slices=z_slices*2-1;
				z_shift_size=z_window_size/2;
			}
		}
		
		new_img=NewImage.createFloatImage("NewImg", width, height, channels*slices, NewImage.FILL_BLACK);
		

		
		for (int z=0; z<z_slices; z++)
		{
			for (int c=0; c<channels; c++)
			{
				for (int x=0; x<x_slices; x++)
				{
					for (int y=0; y<y_slices; y++)
					{
						for (int zz=0; zz<z_window_size; zz++)
						{
							float [] pix=(float [])img.getStack().getProcessor(1+c+zz*channels+z*channels*z_window_size+x*channels*z_window_size*z_slices+y*channels*z_window_size*z_slices*x_slices).getPixels();
							float [] new_pix=(float [])new_img.getStack().getProcessor(1+c+(zz+z*z_shift_size)*channels).getPixels();
							for (int xx=0; xx<window_size; xx++)
							{
								for (int yy=0; yy<window_size; yy++)
								{
									if (new_pix[xx+x*shift_size+(yy+y*shift_size)*width]>0) new_pix[xx+x*shift_size+(yy+y*shift_size)*width]=(pix[xx+yy*window_size]+new_pix[xx+x*shift_size+(yy+y*shift_size)*width])/2;
									else new_pix[xx+x*shift_size+(yy+y*shift_size)*width]=pix[xx+yy*window_size];
								}
							}
						}
					}
				}		
			}
		}
		new_img.setDimensions(channels, slices,1);
		new_img.setOpenAsHyperStack(true);
		new_img.setDisplayMode(IJ.GRAYSCALE);
		new_img.show();
		new_img.updateAndDraw();
	}
}
