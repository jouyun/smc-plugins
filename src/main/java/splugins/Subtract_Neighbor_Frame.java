package splugins;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;


public class Subtract_Neighbor_Frame implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		
		ImagePlus new_img=DoSubtractNeighbor(img);
		
		new_img.updateAndDraw();
		new_img.show();
	}
	/**********************************************************************
	 * DoSubtractNeighbor- Subtracts a box car average of time series
	 * @param img Must be float
	 * @param window 
	 * @return ImagePlus of subtraction, first "window" frames will be unaltered
	 */
	public static ImagePlus DoSubtractNeighbor(ImagePlus img)
	{
		int width=img.getWidth(), height=img.getHeight(), slices=img.getStackSize();
		
		ImagePlus new_img=NewImage.createFloatImage("Result", width, height, slices, NewImage.FILL_BLACK);
		for (int i=2; i<slices; i++)
		{
			float [] new_pix=(float [])new_img.getStack().getPixels(i);
			float [] old_pix=(float [])img.getStack().getPixels(i);
			float [] older_pix=(float [])img.getStack().getPixels(i-1);
			for (int k=0; k<width*height; k++) 
			{
				new_pix[k]= ((float)old_pix[k]-older_pix[k]);
				if (new_pix[k]<0) new_pix[k]=0;
			}
		}
		return new_img;
	}
}
