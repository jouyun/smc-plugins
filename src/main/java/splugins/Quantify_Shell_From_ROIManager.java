package splugins;

import java.awt.Polygon;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Quantify_Shell_From_ROIManager implements PlugIn {

	int width;
	int height;
	int slices;
	int cur_slice;
	int frames;
	int cur_frame;
	int cur_channel;
	int channels;
	ImagePlus imp;
	@Override
	public void run(String arg0) {
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		
		 RoiManager manager=RoiManager.getInstance();
	     Roi [] rois = manager.getRoisAsArray();
	        
	     Polygon p=rois[0].getPolygon();
	     
	     
		

	}

}
