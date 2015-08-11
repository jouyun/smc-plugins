package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import java.awt.Polygon;

public class Fix_Rotation_Translation_With_Line_Manually implements PlugIn{

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
		
		double [] k_angle=new double [rois.length];
		double [] k_centerx=new double[rois.length];
		double [] k_centery=new double[rois.length];
		
		for (int i=0; i<rois.length; i++)
		{
			Polygon p=rois[i].getPolygon();
			float x1=p.xpoints[0], x2=p.xpoints[1], y1=p.ypoints[0], y2=p.ypoints[1];
			IJ.log("X1,Y1,X2,Y2,Phi: "+x1+","+y1+","+x2+","+y2+","+rois[i].getAngle()+","+rois[i].getFloatAngle(x1, y1, x2, y2));

			k_angle[i]=rois[i].getAngle();
			k_centerx[i]=Math.floor((x2+x1)/2);
			k_centery[i]=Math.floor((y2+y1)/2);
			IJ.log("X,Y,Z: "+k_centerx[i]+","+k_centery[i]+","+k_angle[i]);
		}
		for (int i=0; i<slices; i++)
		{
			float x=(float) (width/2-k_centerx[i]);
			float y=(float) (height/2-k_centery[i]);
			IJ.log("Shift: "+i+","+x+","+y);
			ImageProcessor tmp=imp.getStack().getProcessor(i+1);
			tmp.setInterpolationMethod(ImageProcessor.BICUBIC);
			tmp.translate(x, y);
			tmp.rotate(k_angle[i]);
		}
		imp.updateAndDraw();
		
	}
}
