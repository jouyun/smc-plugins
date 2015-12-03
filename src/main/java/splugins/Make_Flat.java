package splugins;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Make_Flat implements PlugIn {

	ImagePlus imp;
	int width, height, channels, slices, frames, cur_channel, cur_frame, cur_slice;
	public void run(String arg0) {
		imp=WindowManager.getCurrentImage();
        
        width=imp.getWidth();
        height=imp.getHeight();
        channels=imp.getNChannels();
        slices=imp.getNSlices();
        frames=imp.getNFrames();
        
        cur_channel=imp.getChannel()-1;
        cur_frame=imp.getFrame()-1;
        cur_slice=imp.getSlice()-1;
        IJ.log("Everything ok?");
        RoiManager manager=RoiManager.getInstance();
        Roi [] rois = manager.getRoisAsArray();
        IJ.log("Rois:  "+rois.length);
        for (int r=0; r<rois.length; r++)
        {
        	IJ.log("ROI : "+r);
        	String cur_name=RoiManager.getInstance().getName(r);
            int fidx=cur_name.indexOf("-");
            int sidx=cur_name.indexOf("-",fidx+1);
            int raw_slice=Integer.parseInt(cur_name.substring(0, fidx));
            int y=Integer.parseInt(cur_name.substring(fidx+1, sidx));
            int x=Integer.parseInt(cur_name.substring(sidx+1, cur_name.length()));
    	
            raw_slice--;
            int t_channel=raw_slice%channels;
            int t_slice=raw_slice/channels%slices;
            IJ.log("X,Y,Slice: "+x+","+y+","+raw_slice);
        }
	}
        
}
