package splugins;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class Fix_XY_Jump_Manually_Channels implements PlugIn {

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
		
		double [] f_vals=new double[channels];
		
		double [] kf=new double [rois.length];
		double [] kx=new double[rois.length];
		double [] ky=new double[rois.length];
		
		
		for (int i=0; i<rois.length; i++)
		{
			Polygon p=rois[i].getPolygon();
			kx[i]=p.xpoints[0];
			ky[i]=p.ypoints[0];
			int idx=manager.getSliceNumber(manager.getName(i));
			kf[i]=(idx-1)%channels;
			//kf[i]=manager.getSliceNumber(manager.getName(i));
			IJ.log("X,Y,F: "+kx[i]+","+ky[i]+","+kf[i]);
		}
		IJ.log("Here");
	
		for (int c=(int)kf[1]; c<channels; c++)
		{
			for (int f=0; f<frames; f++)
			{
				for (int z=0; z<slices; z++)
				{
					float x=(float) Math.floor(kx[1]-kx[0]);
					float y=(float) Math.floor(ky[1]-ky[0]);
					IJ.log("Shift: "+c+","+x+","+y);
					int idx=(f*channels*slices)+z*channels+c+1;
					imp.getStack().getProcessor(idx).setInterpolationMethod(ImageProcessor.BILINEAR);
					
					imp.getStack().getProcessor(idx).translate(-x, -y);				
				}
			}
		}
		imp.updateAndDraw();
	}
}
