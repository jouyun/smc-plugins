package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Topography_To_Binary implements PlugIn {

	@Override
	public void run(String arg0) {
		GenericDialog dlg=new GenericDialog("Top to binary");
		dlg.addNumericField("meters per slice", 50, 0);
		dlg.showDialog();
		float m_per_slice=(float)dlg.getNextNumber();
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight();
		float [] pix=(float [])img.getProcessor().getPixels();
		float max=0f, min=100000000f;
		for (int i=0; i<pix.length; i++)
		{
			if (pix[i]==0) continue;
			if (pix[i]>max) max=pix[i];
			if (pix[i]<min) min=pix[i];
		}
		int number_slices=(int) Math.floor((max-min)/m_per_slice);
		ImagePlus new_img=NewImage.createByteImage("Projection", width, height, number_slices, NewImage.FILL_BLACK);
		for (int i=0; i<number_slices; i++)
		{
			byte [] new_pix=(byte [])new_img.getStack().getProcessor(i+1).getPixels();
			float current_thresh=max-m_per_slice*i;
			for (int j=0; j<new_pix.length; j++)
			{
				if (pix[j]>current_thresh) new_pix[j]=(byte) 250;
			}
		}
		new_img.show();
		new_img.updateAndDraw();

	}

}
