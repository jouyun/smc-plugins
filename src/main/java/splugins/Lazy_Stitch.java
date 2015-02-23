package splugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Lazy_Stitch implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus imp;
		int width, height, slices, frames, channels, cur_slice, cur_frame, cur_channel;
		imp=WindowManager.getCurrentImage();
        
        GenericDialog gd = new GenericDialog("Do Dot Product");
		//gd.addChoice("Z stack:", sourceNames, admissibleImageList[0].getTitle());
		gd.addNumericField("Percent overlap", 20, 0);
		gd.addNumericField("X tiles: ", 10, 0);
		gd.addNumericField("Y tiles: ", 10, 0);
		gd.addCheckbox("Flip X?", false);
		gd.addCheckbox("Flip Y?", false);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		double overlap=gd.getNextNumber();
		int x_tiles=(int)gd.getNextNumber();
		int y_tiles=(int)gd.getNextNumber();
		boolean flip_x=gd.getNextBoolean();
		boolean flip_y=gd.getNextBoolean();
		
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		
		int new_width=(int) Math.round(width*x_tiles*(1-overlap));
		int new_height=(int) Math.round(height*y_tiles*(1-overlap));
		
		ImagePlus new_img=NewImage.createShortImage("MyImg", new_width, new_height, slices*channels, NewImage.FILL_BLACK);
	}

}
