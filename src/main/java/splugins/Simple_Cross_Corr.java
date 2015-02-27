package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.gui.Plot;
import ij.plugin.PlugIn;

/******************************************************************
 * Simple_Cross_Corr
 * @author smc
 *
 *	Will simply calculate a normalized cross correlation over slices and frames, channel 1 to channel 2, generating
 *	an image with the 0,0 value for each slice frame pair, useful for seeing if things are more or less cross-correlated
 *  in time (e.g. par3 and nuclear label)
 */

public class Simple_Cross_Corr implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames(), channel=img.getChannel()-1, frame=img.getFrame()-1, slice=img.getSlice()-1;
		int width=img.getWidth();
		int height=img.getHeight();
		ImagePlus new_img=NewImage.createFloatImage("Tmp", slices, frames, 1, NewImage.FILL_BLACK);
		float [] labels=new float [slices];
		float [] data=new float [slices];
		float [] new_pix=(float []) new_img.getProcessor().getPixels();
		for (int f=0; f<frames; f++)
		{
			for (int s=0; s<slices; s++)
			{
				float [] A_pix=(float [])img.getStack().getProcessor(1+s*channels+f*slices*channels).convertToFloat().getPixels();
				float [] B_pix=(float [])img.getStack().getProcessor(2+s*channels+f*slices*channels).convertToFloat().getPixels();
				//	float [] A_pix=(float [])img.getStack().getProcessor(1+s*channels+frame*slices*channels).getPixels();
				//	float [] B_pix=(float [])img.getStack().getProcessor(2+s*channels+frame*slices*channels).getPixels();
			
				double A=0;
				double B=0;
				double A_std=0;
				double B_std=0;
				for (int x=0; x<width; x++)
				{
					for (int y=0; y<height; y++)
					{
						A+=A_pix[x+y*width];
						B+=B_pix[x+y*width];
					}
				}
				A=A/(width*height);
				B=B/(width*height);
				for (int x=0; x<width; x++)
				{
					for (int y=0; y<height; y++)
					{
						A_std+=(A_pix[x+y*width]-A)*(A_pix[x+y*width]-A);
						B_std+=(B_pix[x+y*width]-B)*(B_pix[x+y*width]-B);
					}
				}
				A_std=Math.sqrt(A_std/(width*height));
				B_std=Math.sqrt(B_std/(width*height));
				for (int x=0; x<width; x++)
				{
					for (int y=0; y<height; y++)
					{
						A_pix[x+y*width]=(float) ((A_pix[x+y*width]-A)/A_std);
						B_pix[x+y*width]=(float) ((B_pix[x+y*width]-B)/B_std);
					}
				}
				data[s]=(float) Profile_Z_Information.calculate_auto_corr(A_pix, B_pix, width, height, 0);
				labels[s]=s+1;
				new_pix[s+slices*f]=data[s];
				//IJ.log("Cross corr slice: "+s+" "+data[s]);
			}
		}
		Plot plot=new Plot("My plot", "X", "Y", labels, data);
		plot.show();
		new_img.show();
		new_img.updateAndDraw();
	}

}
