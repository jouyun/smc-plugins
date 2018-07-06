package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Make_Windows implements PlugIn {

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
		height=img.getHeight();
		width=img.getWidth();
		channels=img.getNChannels();
		slices=img.getNSlices();
		
		GenericDialog gd=new GenericDialog("Choose");
		gd.addNumericField("Window size: ", 128, 0);
		gd.addNumericField("Z Window size: ", 1, 0);
		gd.addCheckbox("Staggered? ", true);
		gd.showDialog();
		window_size=(int)gd.getNextNumber();
		z_window_size=(int)gd.getNextNumber();
		staggered=gd.getNextBoolean();
		shift_size=window_size;
		z_shift_size=z_window_size;
		
		int x_slices=(int)Math.floor(width/window_size);
		int y_slices=(int)Math.floor(height/window_size);
		int z_slices=(int)Math.floor(slices/z_window_size);
		
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
		
		new_img=NewImage.createFloatImage("NewImg", window_size, window_size, channels*z_window_size*z_slices*x_slices*y_slices, NewImage.FILL_BLACK);
		
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
							float [] new_pix=(float [])new_img.getStack().getProcessor(1+c+zz*channels+z*channels*z_window_size+x*channels*z_window_size*z_slices+y*channels*z_window_size*z_slices*x_slices).getPixels();
							float [] pix=(float [])img.getStack().getProcessor(1+c+(zz+z*z_shift_size)*channels).getPixels();
							for (int xx=0; xx<window_size; xx++)
							{
								for (int yy=0; yy<window_size; yy++)
								{
									new_pix[xx+yy*window_size]=pix[xx+x*shift_size+(yy+y*shift_size)*width];
								}
							}
						}
					}
				}		
			}
		}
		new_img.setDimensions(channels, z_window_size, x_slices*y_slices*z_slices);
		new_img.setOpenAsHyperStack(true);
		new_img.show();
		new_img.updateAndDraw();
		
		

	}

}
