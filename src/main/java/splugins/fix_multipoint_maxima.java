package splugins;

import java.awt.Polygon;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.PlugIn;

public class fix_multipoint_maxima implements PlugIn{

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		Roi roi=img.getRoi();
		Polygon poly=roi.getPolygon();
		
		GenericDialog gd=new GenericDialog("Parameters");
		gd.addNumericField("Size of box to check over: ", 5, 0);
		gd.showDialog();
		
		int box=(int)gd.getNextNumber()/2;
		
		int[] new_x=new int[poly.npoints];
		int[] new_y=new int[poly.npoints];
		float [] data=(float [])img.getProcessor().convertToFloatProcessor().getPixels();
		for (int i=0; i<poly.npoints; i++)
		{
			int x_start=poly.xpoints[i];
			int y_start=poly.ypoints[i];
			float max=data[x_start+y_start*img.getWidth()];
			int x_max=x_start;
			int y_max=y_start;
			for (int x=x_start-box; x<=x_start+box; x++)
			{
				for (int y=y_start-box; y<=y_start+box; y++)
				{
					if (x<0||x>=img.getWidth()||y<0||y>=img.getHeight()) continue;
					if (data[x+y*img.getWidth()]>max)
					{
						max=data[x+y*img.getWidth()];
						x_max=x;
						y_max=y;
					}
				}
			}
			new_x[i]=x_max;
			new_y[i]=y_max;
		}
		PointRoi newPoly =new PointRoi(new_x, new_y, new_x.length);
		img.setRoi(newPoly);
		
	}

}
