package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class Correct_Flatness_Using_Sample implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		ImagePlus new_img=Correct_Flatness.DoCorrectUsingSample(img);
		new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(img.getNChannels(), img.getNSlices(), img.getNFrames());
		new_img.show();
		new_img.updateAndDraw();
		
	}

}
