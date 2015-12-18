package splugins;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Make_Flat implements PlugIn {

	ImagePlus imp;
	int width, height, channels, slices, frames, cur_channel, cur_frame, cur_slice;
	public void run(String arg0) {
		imp=WindowManager.getCurrentImage();
		Calibration my_cal=imp.getCalibration();
        
        width=imp.getWidth();
        height=imp.getHeight();
        channels=imp.getNChannels();
        slices=imp.getNSlices();
        frames=imp.getNFrames();
        
        cur_channel=imp.getChannel()-1;
        cur_frame=imp.getFrame()-1;
        cur_slice=imp.getSlice()-1;
        RoiManager manager=RoiManager.getInstance();
        Roi [] rois = manager.getRoisAsArray();
        
        int [] A=get_value_of_ROI(0);
        int [] B=get_value_of_ROI(1);
        double dx=(A[0]-B[0])*my_cal.pixelWidth, dy=(A[1]-B[1])*my_cal.pixelHeight, dz=(A[2]-B[2])*my_cal.pixelDepth;
        int mx=(int)Math.floor((A[0]+B[0])/2), my=(int)Math.floor((A[1]+B[1])/2), mz=(int)Math.floor((A[2]+B[2])/2);
        int cx=(int)Math.floor(width/2), cy=(int)Math.floor(height/2), cz=(int)Math.floor(slices/2);
        
        double phi=Math.atan2(dy, dx)/2/3.1415926*360;
        double theta=Math.atan2(dz, Math.sqrt(dx*dx+dy*dy))/2/3.1415926*360;
        
        IJ.log("dx, dy, dz, phi, theta: "+dx+","+dy+","+dz+","+phi+","+theta);
        
        IJ.run("TransformJ Translate", "x-distance="+(cx-mx)+" y-distance="+(cy-my)+" z-distance="+(cz-mz)+" voxel interpolation=Linear background=0.0");
        IJ.run("TransformJ Rotate", "z-angle="+(-phi)+" y-angle="+(theta)+" x-angle=0 interpolation=Linear background=0.0 adjust");
	}
	
	int [] get_value_of_ROI(int idx)
	{
		int [] rtn=new int[5];
    	String cur_name=RoiManager.getInstance().getName(idx);
        int fidx=cur_name.indexOf("-");
        int sidx=cur_name.indexOf("-",fidx+1);
        int raw_slice=Integer.parseInt(cur_name.substring(0, fidx));
        int y=Integer.parseInt(cur_name.substring(fidx+1, sidx));
        int x=Integer.parseInt(cur_name.substring(sidx+1, cur_name.length()));
	
        raw_slice--;
        int t_channel=raw_slice%channels;
        int t_slice=raw_slice/channels%slices;
        
		rtn[0]=x;
		rtn[1]=y;
		rtn[2]=t_slice;
		rtn[3]=t_channel;
		rtn[4]=0;
		
		return rtn;
	}
        
}
