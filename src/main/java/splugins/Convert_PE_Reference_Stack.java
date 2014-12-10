package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Convert_PE_Reference_Stack implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames();
		
		GenericDialog gd=new GenericDialog("Reference conversion");
		gd.addNumericField("Number of channels (including reference):", 4, 0);
		gd.showDialog();
		
		int number_channels=(int) gd.getNextNumber();
		int number_slices=((channels*slices*frames)-1)/(number_channels-1);
		ImagePlus new_img=NewImage.createShortImage(img.getTitle(), width, height, number_channels*number_slices, NewImage.FILL_BLACK);
		for (int c=0; c<number_channels-1; c++)
		{
			for (int s=0; s<number_slices; s++)
			{
				short [] pix=(short[]) img.getStack().getProcessor(c*number_slices+s+1).getPixels();
				short [] new_pix=(short [])new_img.getStack().getProcessor(c+s*number_channels+1).getPixels();
				for (int i=0; i<pix.length; i++) new_pix[i]=pix[i];
			}
		}
		for (int s=0; s<number_slices; s++)
		{
			short [] pix=(short[]) img.getStack().getProcessor((number_channels-1)*number_slices+1).getPixels();
			short [] new_pix=(short [])new_img.getStack().getProcessor((number_channels-1)+s*number_channels+1).getPixels();
			for (int i=0; i<pix.length; i++) new_pix[i]=pix[i];
		}
		img.changes=false;
		img.close();
		new_img.setDimensions(number_channels, number_slices, 1);
		new_img.setOpenAsHyperStack(true);
		new_img.show();
		new_img.updateAndDraw();
	}

}
