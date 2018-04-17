package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Measure_3D_Distance_ROI implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		RoiManager manager=RoiManager.getInstance();
		Roi first=manager.getRoi(0);
		Roi second=manager.getRoi(1);
		
		Calibration cal=img.getCalibration();
		
		double z=(first.getZPosition()-second.getZPosition())*cal.pixelDepth;
		double x=(first.getXBase()-second.getXBase())*cal.pixelWidth;
		double y=(first.getYBase()-second.getYBase())*cal.pixelHeight;
		
		ResultsTable rslt=ResultsTable.getResultsTable();
		
		rslt.incrementCounter();
		rslt.addValue("Distance", Math.sqrt(x*x+y*y+z*z));
		rslt.show("Results");

	}

}
