package splugins;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

public class Pad_To_Certain_Z_Slices implements PlugIn {

	int height;
	int width;
	int slices;
	int frames;
	int channels;
	ImagePlus img;
	ImagePlus new_img;
	@Override
	public void run(String arg0) 
	{
		img=WindowManager.getCurrentImage();
		width=img.getWidth();
		height=img.getHeight();
		slices=img.getNSlices();
		frames=img.getNFrames();
		channels=img.getNChannels();
		
		GenericDialog gd=new GenericDialog("Slices");
		gd.addNumericField("Number slices", 4, 0);
		gd.showDialog();
		int new_slices=(int)gd.getNextNumber();
		
		ImageStack new_stack=new ImageStack(width, height);
		for (int f=0; f<frames; f++)
		{
			for (int z=0; z<slices; z++)
			{
				for (int c=0; c<channels; c++)
				{
					new_stack.addSlice(img.getStack().getProcessor(1+c+z*channels+f*channels*slices));
				}
			}
			for (int z=slices; z<new_slices; z++)
			{
				for (int c=0; c<channels; c++)
				{
					new_stack.addSlice(new FloatProcessor(width, height));
				}
			}
		}
		new_img=new ImagePlus("NewImg", new_stack);
		new_img.setDimensions(channels, new_slices, frames);
		new_img.setOpenAsHyperStack(true);
		img.changes=false;
		img.close();
		new_img.show();
		new_img.updateAndDraw();

	}

}
