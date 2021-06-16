package splugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Percentile_Finder implements PlugIn {

	@Override
	public void run(String arg) {
		// GenericDialog gd=new GenericDialog("Histogram normalizer");
		ImagePlus img = WindowManager.getCurrentImage();
		GenericDialog gd = new GenericDialog("Percentile Finder");
		gd.addNumericField("Channel:  ", 1, 0);
		gd.showDialog();
		double channel=gd.getNextNumber();
		int chann = (int) channel;
		List <Float> stk=new ArrayList<Float>();
		
		float [] pix = (float [])img.getStack().getProcessor(chann+1).convertToFloat().getPixels();
		for (int i=0; i<pix.length; i++)
		{
			stk.add(pix[i]);
		}
		Collections.sort(stk);
		int pminidx=(int)Math.floor(1.0/100.0*(float)stk.size());
		int pmaxidx=(int)Math.floor(99.0/100.0*(float)stk.size());
		IJ.log("1st percentile:  "+stk.get(pminidx)+"  99th percentile:  "+stk.get(pmaxidx));
	}

}
