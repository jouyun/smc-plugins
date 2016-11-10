package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Paste_Projection_To_Front implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		
		
		
		
		GenericDialog gd=new GenericDialog("Which channel's z projection do you want pasted to front?");
		gd.addNumericField("Channel to paste?", 1, 0);
		gd.showDialog();
		int z_channel=(int) (gd.getNextNumber()-1);
		
		ImagePlus new_img=paste_project(img, z_channel);
		new_img.show();
		new_img.updateAndDraw();
	}
	static public ImagePlus paste_project(ImagePlus img, int z_channel)
	{
		int width=img.getWidth(), height=img.getHeight(), channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames();
		ImagePlus new_img=NewImage.createShortImage("Projection", width, height, (channels*slices+1)*frames, NewImage.FILL_BLACK);
		for (int f=0; f<frames; f++)
		{
			short [] tmp_max=new short[width*height];
			for (int s=0; s<slices; s++)
			{
				short [] pix=(short [])img.getStack().getProcessor(f*channels*slices+s*channels+z_channel+1).getPixels();
				for (int p=0; p<width*height; p++)
				{
					if (pix[p]>tmp_max[p]) tmp_max[p]=pix[p];
				}
			}
			
			//Create first channel (the projection)
			short [] new_pix=(short [])new_img.getStack().getProcessor(f*(channels*slices+1)+1).getPixels();
			for (int p=0; p<new_pix.length; p++) new_pix[p]=tmp_max[p];
			
			//Fill in the others
			for (int s=0; s<slices; s++)
			{
				for (int c=0; c<channels; c++)
				{
					short [] pix=(short [])img.getStack().getProcessor(f*(channels*slices)+s*channels+c+1).getPixels();
					new_pix=(short [])new_img.getStack().getProcessor(f*(channels*slices+1)+s*channels+c+2).getPixels();
					for (int p=0; p<width*height; p++) new_pix[p]=pix[p];
				}
			}
			
		}

		new_img.setOpenAsHyperStack(true);
		new_img.setDimensions((channels*slices+1), 1, frames);
		new_img.setProperty("Info", img.getInfoProperty());
		new_img.setCalibration(img.getCalibration());
		return new_img;
	}

}
