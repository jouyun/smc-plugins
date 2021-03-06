package splugins;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;


public class Normalize_Unity implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), slices=img.getImageStackSize();
		
		GenericDialog gd=new GenericDialog("Normalize_Unity");
		gd.addCheckbox("Ignore zeros?", true);
		gd.showDialog();
		boolean ignore_zeros=gd.getNextBoolean();
		
		for (int i=0; i<slices; i++)
		{
			float [] pix=(float []) img.getStack().getProcessor(i+1).getPixels();
			float min=1000000000000000000000000f, max=-1000000000000000000000000f;
			for (int j=0; j<width*height; j++)
			{
				Float t=new Float(pix[j]);
				if (t.isNaN()) 
				{
					//pix[j]=0;
					continue;
				}
				if (ignore_zeros&&t==0)
				{
					continue;
				}
				if (t>max) max=t;
				if (t<min) min=t;
			}
			float norm=max-min;
			IJ.log("vals"+max+" "+min+" "+norm);
			for (int j=0; j<width*height; j++)
			{
				pix[j]=(pix[j]-min)/norm;
			}
		}

	}

}
