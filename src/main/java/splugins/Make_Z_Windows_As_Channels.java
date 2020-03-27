package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Make_Z_Windows_As_Channels implements PlugIn {

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
		// TODO Auto-generated method stub
		img=WindowManager.getCurrentImage();
		width=img.getWidth();
		height=img.getHeight();
		slices=img.getNSlices();
		frames=img.getNFrames();
		channels=img.getNChannels();
		GenericDialog gd=new GenericDialog("Choose options");
		gd.addNumericField("Slices up", 2, 0);
		gd.addNumericField("Slices down", 2, 0);
		gd.showDialog();
		
		int up=(int)(gd.getNextNumber());
		int down=(int)(gd.getNextNumber());
		int new_channels=up+down+1;
		
		new_img=NewImage.createFloatImage("NewImg", width, height, slices*new_channels*frames, NewImage.FILL_BLACK);
		
		for (int f=0; f<frames; f++)
		{
			for (int z=0; z<slices; z++)
			{
				for (int c=0; c<new_channels; c++)
				{
					int offset=c-down+z;
					if (offset<0||offset>=slices) continue;
					float [] pix = (float []) img.getStack().getProcessor(1+offset+f*slices).getPixels();
					float [] npix = (float []) new_img.getStack().getProcessor(1+c+z*new_channels+f*slices*new_channels).getPixels();
					System.arraycopy(pix, 0,  npix,  0, pix.length);					
				}
			}
		}
		new_img.setDimensions(new_channels,  slices,  frames);
		new_img.setOpenAsHyperStack(true);
		new_img.updateAndDraw();
		new_img.show();
		

	}

}
