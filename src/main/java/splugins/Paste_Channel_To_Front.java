package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Paste_Channel_To_Front implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames();
		
		GenericDialog gd=new GenericDialog("Paste a channel to front");
		gd.addNumericField("Channel to paste?", 1, 0);
		gd.addNumericField("Slice to paste?", 1, 0);
		gd.showDialog();
		int front_channel=(int)gd.getNextNumber();
		int front_slice=(int)gd.getNextNumber();
		if (front_slice!=1) front_channel=(front_slice-1)*channels+front_channel;
		channels=channels*slices;
		IJ.log("Channels: "+channels+","+frames);
		ImagePlus new_img=NewImage.createShortImage("Projection", width, height, (channels+1)*frames, NewImage.FILL_BLACK);
		//Insert the first
		for (int i=0; i<frames; i++)
		{
			short [] pix=(short [])img.getStack().getProcessor(i*channels+front_channel).getPixels();
			short [] new_pix=(short [])new_img.getStack().getProcessor(i*(channels+1)+1).getPixels();
			for (int p=0; p<pix.length; p++) new_pix[p]=pix[p];
		}
		//Do the rest
		for (int f=0; f<frames; f++)
		{
			for (int c=0; c<channels; c++)
			{
				short [] pix=(short [])img.getStack().getProcessor(f*channels+c+1).getPixels();
				short [] new_pix=(short [])new_img.getStack().getProcessor(f*(channels+1)+c+2).getPixels();
				for (int p=0; p<pix.length; p++) new_pix[p]=pix[p];
			}
		}
		new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(channels+1, 1, frames);
		new_img.setProperty("Info", img.getInfoProperty());
		new_img.setCalibration(img.getCalibration());
		
		new_img.show();
		new_img.updateAndDraw();
	}

}
