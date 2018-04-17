package splugins;

import java.awt.Point;
import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Clear_Outside_ROIs implements PlugIn {

	@Override
	public void run(String arg0) {
		
		ImagePlus img=WindowManager.getCurrentImage();
		int height=img.getHeight(), width=img.getWidth(), channels=img.getNChannels(), frames=img.getNFrames(), slices=img.getNSlices();
		
		RoiManager rman=RoiManager.getInstance();
		int rois=rman.getCount();
		
		boolean [] mask=new boolean[width*height];

		//Make a list of all the points in the rois
		for (int i=0; i<rois; i++)
		{
			Roi roi=rman.getRoi(i);
			Polygon p=roi.getPolygon();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					Point P=new Point(x,y);
					if (p.contains(P)) 
					{
						mask[x+width*y]=true;
					}
				}
			}
		}
		
		for (int s=0; s<img.getStackSize(); s++)
		{
			byte [] pix=(byte [])img.getStack().getProcessor(s+1).getPixels();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					if (!mask[x+width*y]) pix[x+width*y]=0; 
				}
			}
		}
		img.updateAndDraw();

	}

}
