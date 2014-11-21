package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Dilate3D_NoClash implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames(), channel=img.getChannel()-1, frame=img.getFrame()-1, slice=img.getSlice()-1;
		int width=img.getWidth();
		int height=img.getHeight();
		//ImagePlus new_img=NewImage.createFloatImage("Tmp", slices, frames, 1, NewImage.FILL_BLACK);
		short [] pix=(short [])img.getProcessor().convertToShort(false).getPixels();
		Utility3D my3D=new Utility3D();
		my3D.grow_until_neighbor(pix, width, height, slices);
		//for (int i=0; i<rtn.length; i++) pix[i]=rtn[i];
		img.updateAndDraw();

	}

}
