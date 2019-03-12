package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Parse_JSON_Spots implements PlugIn {

	@Override
	public void run(String arg0) {
		IJ.open("/n/projects/smc/public/SMC/SyGlass/63x_0 Days_WT_H32_S3.tif");
		ImagePlus img=WindowManager.getCurrentImage();
		Calibration calib=img.getCalibration();
		JSONParser parser = new JSONParser();
		try {
			JSONObject a = (JSONObject) parser.parse(new FileReader("/n/projects/smc/public/SMC/SyGlass/VPS_Spots.json"));

			JSONArray points = (JSONArray) a.get("countingPoints");

			RoiManager manager=RoiManager.getRoiManager();
			
		    for (Object c : points)
		    {
		    	JSONObject current=(JSONObject) c;
		    	double x=(double) current.get("x");
		    	double y=(double) current.get("y");
		    	double z=(double) current.get("z");
		    	x=calib.getRawX(x+calib.getX(img.getWidth())/2);
		    	y=calib.getRawY(y+calib.getY(img.getHeight())/2);
		    	z=calib.getRawZ(z+calib.getZ(img.getNSlices())/2);
		    	int xx=(int)Math.ceil(x);
		    	int yy=(int)Math.ceil(y);
		    	int zz=(int)Math.ceil(z);
		    	Roi new_roi=new Roi(xx, yy, 1,1);
		    	new_roi.setPosition(zz);
		    	
		    	manager.addRoi(new_roi);
		    	
		    	
		    }
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
