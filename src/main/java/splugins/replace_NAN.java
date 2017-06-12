package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class replace_NAN implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
		GenericDialog dg=new GenericDialog("NaN");
		dg.addNumericField("Replacement value: ", -1, 0);
		dg.showDialog();
				
		float replace=(float)dg.getNextNumber();
		
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), slices=img.getImageStackSize();
		
		for (int s=0; s<slices; s++)
		{
			float [] pix=(float [])img.getStack().getProcessor(s+1).getPixels();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					if (Double.isNaN((double)pix[x+y*width])) pix[x+y*width]=replace;
				}
			}
		}
		img.updateAndDraw();

	}

}
