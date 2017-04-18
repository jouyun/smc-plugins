package splugins;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class Fix_XY_Drift_Manually implements PlugIn {

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
		
		double [] f_vals=new double[frames];
		
		double [] kf=new double [rois.length];
		double [] kx=new double[rois.length];
		double [] ky=new double[rois.length];
		
		GenericDialog gd = new GenericDialog("Interpolate?");
		gd.addCheckbox("Interpolate?", false);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		boolean interpolate=gd.getNextBoolean();
		
		for (int i=0; i<frames; i++)
		{
			f_vals[i]=i+1;
		}
		for (int i=0; i<rois.length; i++)
		{
			Polygon p=rois[i].getPolygon();
			kx[i]=p.xpoints[0];
			ky[i]=p.ypoints[0];
			int slice_number=manager.getSliceNumber(manager.getName(i));
			kf[i]=slice_number/channels/slices+1;
			IJ.log("X,Y,Z: "+kx[i]+","+ky[i]+","+kf[i]);
		}
		double [] x_vals=Fix_Z_Drift_Manually.linear_extrapolate_interpolate(kf, kx, f_vals);
		double [] y_vals=Fix_Z_Drift_Manually.linear_extrapolate_interpolate(kf, ky, f_vals);
		IJ.log("Bool: "+interpolate);
		
		for (int i=0; i<frames; i++)
		{
			float x=(float) (x_vals[i]-x_vals[0]);
			float y=(float) (y_vals[i]-y_vals[0]);
			IJ.log("Shift: "+i+","+x+","+y);
			if (interpolate==false)
			{
				x=Math.round(x);
				y=Math.round(y);
			}
			IJ.log("Shift: "+i+","+x+","+y);
			for (int c=0; c<channels; c++)
			{
				for (int s=0; s<slices; s++)
				{
					ImageProcessor tmp=imp.getStack().getProcessor((int) (f_vals[i]-1)*channels*slices+s*channels+c+1);
					tmp.setInterpolationMethod(ImageProcessor.BILINEAR);
					tmp.translate(-x, -y);
				}
			}
		}
		imp.updateAndDraw();



	}

}
