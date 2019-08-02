package splugins;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;


public class Normalize_Mean implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), slices=img.getImageStackSize();
		
		GenericDialog gd=new GenericDialog("Normalize_Unity");
		gd.addNumericField("Normalize to what mean:  ", 1E8, 0);
		gd.addCheckbox("Ignore zeros?", true);
		gd.showDialog();
		boolean ignore_zeros=gd.getNextBoolean();
		double norm=gd.getNextNumber();
		
		for (int i=0; i<slices; i++)
		{
			float [] pix=(float []) img.getStack().getProcessor(i+1).getPixels();
			float min=1000000000000000000000000f, max=-1000000000000000000000000f;
			double mean=0;
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
				mean=mean+t;
			}
			mean=mean/width/height;
			for (int j=0; j<width*height; j++)
			{
				pix[j]=(float)(pix[j]/mean*norm);
			}
		}

	}

}
