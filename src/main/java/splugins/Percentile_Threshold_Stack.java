package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;

public class Percentile_Threshold_Stack implements PlugIn {

	@Override
	public void run(String arg0) {
		double prctile=0.995;
		ImagePlus imp=WindowManager.getCurrentImage();
		int width=imp.getWidth(), height=imp.getHeight(), slices=imp.getNSlices(), frames=imp.getNFrames(), channels=imp.getNChannels();
		
		
		GenericDialog gd = new GenericDialog("In situ process");
		gd.addNumericField("Percentile", 10.0, 1);
		gd.addNumericField("SNR", 8, 1);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		prctile=(float)gd.getNextNumber();
		float SNR=(float)gd.getNextNumber();
		
		ImagePlus new_img=NewImage.createByteImage("Img", width, height, frames*channels*slices, NewImage.FILL_BLACK);
		new_img.setDimensions(channels,  slices,  frames);
		new_img.setOpenAsHyperStack(true);
		for (int f=0; f<frames; f++)
		{
			for (int s=0; s<slices; s++)
			{
				for (int c=0; c<channels; c++)
				{
					byte [] new_array=Percentile_Threshold.get_mask((float []) imp.getStack().getProcessor(imp.getStackIndex(c+1, s+1, f+1)).getPixels(), width, height, (float)prctile, SNR);
					byte [] new_pix=(byte []) new_img.getStack().getProcessor(imp.getStackIndex(c+1, s+1, f+1)).getPixels();
					for (int i=0; i<width*height; i++)
					{
						new_pix[i]=new_array[i];
					}
				}
			}
		}
		
		new_img.show();
		new_img.updateAndDraw();

	}

}
