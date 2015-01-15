package splugins;

import java.awt.Point;
import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Measure_Perimeters implements PlugIn {

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
		Roi roi = imp.getRoi();
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		rslt.incrementCounter();
		Calibration cal=imp.getCalibration();
		if(roi!=null)
		{
			
			Polygon p=roi.getPolygon();
			double area=0.0;
			for (int x=0; x<imp.getWidth(); x++)
			{
				for (int y=0; y<imp.getHeight(); y++)
				{
					Point P=new Point(x,y);
					if (p.contains(P)) area++;
				}
			}
			rslt.addValue("Area",area*cal.pixelHeight*cal.pixelWidth);
			for (int i=0; i<p.npoints; i++)
			{
				double dx=(p.xpoints[(i+1)%p.npoints]-p.xpoints[i])*cal.pixelWidth;
				double dy=(p.ypoints[(i+1)%p.npoints]-p.ypoints[i])*cal.pixelHeight;
				Line line_roi=new Line(p.xpoints[i], p.ypoints[i],p.xpoints[(i+1)%p.npoints],p.ypoints[(i+1)%p.npoints]);
				manager.addRoi(line_roi);
				manager.select(manager.getCount()-1);
				manager.runCommand("Rename", "Segment_"+(i+1));
				rslt.addValue("Segment_"+(i+1), Math.sqrt((dx*dx+dy*dy)));
				rslt.show("Results");
			}
		}
		
		

	}

}
