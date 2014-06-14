package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Subtract_Leakage implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int current_channel=img.getChannel();
		GenericDialog gd=new GenericDialog("Leakage Subtraction");
		gd.addNumericField("Fraction:  ", .045, 4);
		gd.addNumericField("Reference channel :  ", 1, 0);
		gd.addNumericField("Subtracted channel :  ", 2, 0);
		gd.addNumericField("Background:  ", -1, 1);
		gd.showDialog();
		double fraction=gd.getNextNumber();
		double reference_channel=gd.getNextNumber();
		double subtracted_channel=gd.getNextNumber();
		double background=gd.getNextNumber();
		if (background==-1)	subtract_leakage_imageplus(img, (int)reference_channel, (int)subtracted_channel, (float)fraction);
		else subtract_leakage_imageplus(img, (int)reference_channel, (int)subtracted_channel, (float)fraction, (float)background);
		img.updateAndDraw();
	}
	
	public static void subtract_leakage_imageplus(ImagePlus img, int reference_channel, int adjusted_channel, float fraction)
	{
		float [] ptiles=Histogram_Normalize_Percentile.Find_Percentiles(img, 100, 90, 1, reference_channel, 1);
		IJ.log("Ptiles: " + ptiles[0]+ ","+ptiles[1]+","+ptiles[2]);
		subtract_leakage_imageplus(img, reference_channel, adjusted_channel, fraction, ptiles[0]);
	}
	
	public static void subtract_leakage_imageplus(ImagePlus img, int reference_channel, int adjusted_channel, float fraction, float background)
	{
		int width=img.getWidth(), height=img.getHeight(), slices=img.getNSlices(), frames=img.getNFrames(), channels=img.getNChannels();
		for (int f=0; f<frames; f++)
		{
			for (int s=0; s<slices; s++)
			{
				float [] ref_pix=(float [])img.getStack().getProcessor(reference_channel+s*channels+f*channels*slices).getPixels();
				float [] adj_pix=(float [])img.getStack().getProcessor(adjusted_channel+s*channels+f*channels*slices).getPixels();
				subtract_leakage(ref_pix, adj_pix, fraction, background);
			}
		}
	}
	
	public static void subtract_leakage(float [] reference_pix, float [] adjusted_pix, float fraction, float background)
	{
		for (int i=0; i<reference_pix.length; i++)
		{
			adjusted_pix[i]=adjusted_pix[i]-(reference_pix[i]-background)*fraction;
		}
	}

}
