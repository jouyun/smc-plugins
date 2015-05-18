package splugins;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Fix_XY_Jump_Manually implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus imp;
		int width, height, slices, frames, channels, cur_slice, cur_frame, cur_channel;
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
		
		double [] f_vals=new double[slices];
		
		double [] kf=new double [rois.length];
		double [] kx=new double[rois.length];
		double [] ky=new double[rois.length];
		
		for (int i=0; i<slices; i++)
		{
			f_vals[i]=i+1;
		}
		for (int i=0; i<rois.length; i++)
		{
			Polygon p=rois[i].getPolygon();
			kx[i]=p.xpoints[0];
			ky[i]=p.ypoints[0];
			kf[i]=manager.getSliceNumber(manager.getName(i));
			IJ.log("X,Y,Z: "+kx[i]+","+ky[i]+","+kf[i]);
		}
		IJ.log("Here");
		for (int i=(int)kf[1]-1; i<slices; i++)
		{
			int x=(int) Math.floor(kx[1]-kx[0]);
			int y=(int) Math.floor(ky[1]-ky[0]);
			IJ.log("Shift: "+i+","+x+","+y);
			imp.getStack().getProcessor(i+1).translate(-x, -y);
		}
		imp.updateAndDraw();

	}

}
