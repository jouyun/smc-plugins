package splugins;

import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import splugins.Compute_3D_blob_statistics_round.MyIntPoint;

public class Compute_3D_Point_statistics implements PlugIn{

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames();
		int current_channel=img.getC()-1, current_slice=img.getSlice()-1, current_frame=img.getFrame()-1;
		
		GenericDialog gd=new GenericDialog("Choose channels");
		gd.addNumericField("Mask channel", 1, 0);
		gd.addNumericField("Point channel", 1, 0);
		
		gd.showDialog();
		int mask_channel=(int)gd.getNextNumber();
		int point_channel=(int)gd.getNextNumber();
		
		//Find total volume (in pixels) of mask channel
		double total_volume=0;
		for (int s=0; s<slices; s++)
		{
			float [] pix=(float [])img.getStack().getProcessor(mask_channel+s*channels).getPixels();
			for (int p=0; p<pix.length; p++) if (pix[p]>0) total_volume++;
		}
		
		//Find point objects and quantify them
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		
		for (int s=0; s<slices; s++)
		{
			float [] point_pix=(float [])img.getStack().getProcessor(point_channel+s*channels).getPixels();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					if (!(point_pix[x+y*width]>0)) continue;
					rslt.incrementCounter();
					//IJ.log("X Y " +x+","+y);
					for (int c=0; c<channels; c++)
					{
						float [] pix=(float [])img.getStack().getProcessor(c+1+s*channels).getPixels();
						rslt.addValue("Channel"+(c+1), pix[x+y*width]);
					}
					rslt.addValue("Image", img.getTitle());
					rslt.addValue("Volume", total_volume);
				}
			}
		}
		rslt.show("Results");
		
	}
	
}