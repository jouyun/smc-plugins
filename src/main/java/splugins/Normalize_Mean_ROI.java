package splugins;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.PlugIn;

public class Normalize_Mean_ROI implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		Roi roi = img.getRoi();
        Polygon p=roi.getPolygon();
        GenericDialog dlg=new GenericDialog("Background");
		dlg.addNumericField("Background", 478, 0);
		dlg.showDialog();
		float background=(float)dlg.getNextNumber();
		
		for (int i=0; i<img.getStackSize(); i++)
		{
			float [] current_slice=(float []) img.getStack().getProcessor(i+1).getPixels();
			float average=0.0f;
			int count=0;
			for (int x=0; x<img.getWidth(); x++)
			{
				for (int y=0; y<img.getHeight(); y++)
				{
					Point P=new Point(x,y);
					if (p.contains(P))
					{
						average+=current_slice[x+y*img.getWidth()];
						count++;
					}
				}
			}
			average=average/count-background;
			for (int x=0; x<img.getWidth(); x++)
			{
				for (int y=0; y<img.getHeight(); y++)
				{
					current_slice[x+y*img.getWidth()]=(current_slice[x+y*img.getWidth()]-background)/average;
				}
			}
		}
		img.updateAndDraw();
	}

}
