package splugins;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

public class Find_Nearest_Peak implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		float [] pix=(float [])img.getProcessor().getPixels();
		Roi r=img.getRoi();
		Polygon p=r.getPolygon();
		int x=p.xpoints[0];
		int y=p.ypoints[0];
		GenericDialog gd=new GenericDialog("Choose");
		gd.addNumericField("Search radius", 2, 1);
		gd.showDialog();
		float radius=(float)gd.getNextNumber();
		int start_x=(int)Math.floor(x-radius);
		int start_y=(int)Math.floor(y-radius);
		int end_x=(int)Math.ceil(x+radius);
		int end_y=(int)Math.ceil(y+radius);
		float current_max=-1000;
		int best_x=-1;
		int best_y=-1;
		for (int tx=start_x; tx<=end_x; tx++)
		{
			for (int ty=start_y; ty<=end_y; ty++)
			{
				if (pix[tx+ty*img.getWidth()]>current_max)
				{
					current_max=pix[tx+ty*img.getWidth()];
					best_x=tx;
					best_y=ty;
				}
			}
		}
		IJ.log("X,Y: "+best_x+","+best_y);
		
		ResultsTable rslt=ResultsTable.getResultsTable();
		rslt.incrementCounter();
		rslt.addValue("X", best_x);
		rslt.addValue("Y", best_y);
		rslt.addValue("Error", (x-best_x)*(x-best_x)+(y-best_y)*(y-best_y));
		rslt.show("Results");

	}

}
