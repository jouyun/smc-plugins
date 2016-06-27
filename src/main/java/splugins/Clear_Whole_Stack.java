package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Clear_Whole_Stack implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int cur_slice=img.getCurrentSlice();
		for (int s=0; s<img.getStackSize(); s++)
		{
			img.setSlice(s+1);
			IJ.run("Clear");
		}

	}
}
