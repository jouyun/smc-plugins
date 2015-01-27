package splugins;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Stack;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.interpolation.UnivariateRealInterpolator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.NewImage;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Fix_Z_Drift_Manually implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
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
		
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		if (admissibleImageList.length == 0) return;
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Pick target image");
		gd.addChoice("Target image:", sourceNames, admissibleImageList[0].getTitle());
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		int tmpIndex=gd.getNextChoiceIndex();
		ImagePlus target_img;
		target_img=admissibleImageList[tmpIndex];
		int nwidth=target_img.getWidth(); 
		int nheight=target_img.getHeight();
		int nslices=target_img.getNSlices();
		int nframes=target_img.getNFrames();
		int nchannels=target_img.getNChannels();
		
		Roi roi = imp.getRoi();
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		rslt.incrementCounter();
		Calibration cal=imp.getCalibration();
		if(roi==null) return;
		Polygon p=roi.getPolygon();
		double [] x_vals=new double[height];
		double [] kx=new double [p.npoints];
		double [] ky=new double[p.npoints];
		for (int i=0; i<height; i++)
		{
			x_vals[i]=i;
		}
		for (int i=0; i<p.npoints; i++)
		{
			kx[i]=p.ypoints[i];
			ky[i]=p.xpoints[i];
		}
		double [] y_vals=linear_extrapolate_interpolate(kx, ky, x_vals);
		int shift_min=1000000000;
		int shift_max=-1000000000;
		for (int i=0; i<height; i++)
		{
			//IJ.log("X,Y: "+x_vals[i]+","+y_vals[i]);
			y_vals[i]=Math.round(y_vals[i]);
			if (y_vals[i]<shift_min) shift_min=(int) y_vals[i];
			
		}
		for (int i=0; i<height; i++)
		{
			y_vals[i]=y_vals[i]-shift_min;
			if (y_vals[i]>shift_max) shift_max=(int) y_vals[i];
		}
		ImagePlus new_img=NewImage.createFloatImage("NewImg", nwidth, nheight, nchannels*nframes*(nslices+shift_max), NewImage.FILL_BLACK);
		for (int c=0; c<nchannels; c++)
		{
			for (int s=0; s<nslices; s++)
			{
				for (int f=0; f<nframes; f++)
				{
					
					float [] pix=(float [])target_img.getStack().getProcessor(c+s*nchannels+f*nchannels*nslices+1).getPixels();
					float [] new_pix=(float [])new_img.getStack().getProcessor(c+(s+shift_max-(int)y_vals[f])*nchannels+f*nchannels*(nslices+shift_max)+1).getPixels();
					System.arraycopy(pix, 0, new_pix, 0, pix.length);
				}
			}
		}
		new_img.setDimensions(nchannels, nslices+shift_max, nframes);
		new_img.setOpenAsHyperStack(true);
		new_img.show();
		new_img.updateAndDraw();
	}
	
	public static double [] linear_extrapolate_interpolate(double [] kx, double [] ky, double [] wx)
	{
		double [] wy=new double[wx.length];
		int idx=0, kidx=0;
		if (wx[0]<kx[0])
		{
			double slope=(ky[1]-ky[0])/(kx[1]-kx[0]);
			while (wx[idx]<kx[0])
			{
				wy[idx]=ky[0]-slope*(kx[0]-wx[idx]);
				idx++;
			}
		}
		while (idx<wx.length)
		{
			if (wx[idx]==kx[kidx])
			{
				wy[idx]=ky[kidx];
				idx++;
				if (idx==wx.length) return wy;
			}
			kidx++;
			if (kidx<kx.length)
			{
				while (kx[kidx]<wx[idx])
				{
					kidx++;
					if (kidx==kx.length) break;
				}
			}
			//Check to see if we are at the end of the knowns and need to start extrapolating
			if (kidx==kx.length)
			{
				kidx--;
				double slope=(ky[kidx]-ky[kidx-1])/(kx[kidx]-kx[kidx-1]);
				while (idx<wx.length)
				{
					wy[idx]=slope*(wx[idx]-kx[kidx])+ky[kidx];
					idx++;
				}
				return wy;
			}
			//Interpolate everything between previous known val and current known val
			double s=(ky[kidx]-ky[kidx-1])/(kx[kidx]-kx[kidx-1]);
			while (wx[idx]<kx[kidx])
			{
				wy[idx]=s*(wx[idx]-kx[kidx-1])+ky[kidx-1];
				idx++;
				if (idx==wx.length) return wy;
			}
		}
		return wy;
	}
	/*********************Here is an alternative using apache commons*********************************
	 * 
	 * 
	 * @return
	 */
	
	/*double x[]={1, 2, 3, 4, 5};
	double y[]= {1, 2, 3, 2, 1};
	UnivariateRealInterpolator interpolator=new SplineInterpolator();
	try {
		UnivariateRealFunction function=interpolator.interpolate(x, y);
		float [] x_data=new float[100];
		float [] y_data=new float[100];
		for (int i=0; i<100; i++)
		{
			x_data[i]=0.0f+4.0f/100.0f*i;
			y_data[i]=(float) function.value(x_data[i]);
			IJ.log("X,Y: "+x_data[i]+","+y_data[i]);
		}
		Plot myplot=new Plot("Interp", "X", "Y", x_data, y_data);
		myplot.show();
	} catch (MathException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		IJ.log("Poop!");
	}
	if (x[0]==1) return;*/
	
	private ImagePlus[] createAdmissibleImageList () 
	{
		final int[] windowList = WindowManager.getIDList();
		//IJ.log(""+windowList[0]+"\n");
		final Stack stack = new Stack();
		for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) 
		{
			final ImagePlus imp = WindowManager.getImage(windowList[k]);
			//IJ.log(imp.getTitle()+"\n");
			if ((imp != null) && (imp.getType() == imp.GRAY32)) 
			{
				//IJ.log("got one");
				stack.push(imp);
			}
		}
		//IJ.log("Stack size:  " + stack.size() + "\n");
		final ImagePlus[] admissibleImageList = new ImagePlus[stack.size()];
		int k = 0;
		while (!stack.isEmpty()) {
			admissibleImageList[k++] = (ImagePlus)stack.pop();
		}
		if (k==0 && (windowList != null && windowList.length > 0 )){
			IJ.error("No float images, convert to float and try again");
		}
		return(admissibleImageList);
	} /* end createAdmissibleImageList */

}
