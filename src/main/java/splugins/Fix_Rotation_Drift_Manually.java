package splugins;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class Fix_Rotation_Drift_Manually implements PlugIn {

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
			float x1=p.xpoints[0], x2=p.xpoints[1], y1=p.ypoints[0], y2=p.ypoints[1];
			IJ.log("X1,Y1,X2,Y2,Phi: "+x1+","+y1+","+x2+","+y2+","+rois[i].getAngle()+","+rois[i].getFloatAngle(x1, y1, x2, y2));
			
			kx[i]=rois[i].getAngle();
			ky[i]=0;
			kf[i]=manager.getSliceNumber(manager.getName(i));
			IJ.log("X,Y,Z: "+kx[i]+","+ky[i]+","+kf[i]);
		}
		double [] x_vals=Fix_Z_Drift_Manually.linear_extrapolate_interpolate(kf, kx, f_vals);
		double [] y_vals=Fix_Z_Drift_Manually.linear_extrapolate_interpolate(kf, ky, f_vals);
		
		for (int i=0; i<slices; i++)
		{
			//int x=(int) Math.floor(x_vals[i]-x_vals[0]);
			//int y=(int) Math.floor(y_vals[i]-y_vals[0]);
			float phi=(float) x_vals[i];
			IJ.log("Shift: "+i+","+phi);
			imp.getStack().getProcessor((int) f_vals[i]).setInterpolationMethod(ImageProcessor.BILINEAR);
			imp.getStack().getProcessor((int) f_vals[i]).rotate(phi);
		}
		imp.updateAndDraw();
	}

}
