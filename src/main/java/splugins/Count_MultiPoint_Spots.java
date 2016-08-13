package splugins;

import java.awt.Frame;
import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.text.TextWindow;

public class Count_MultiPoint_Spots implements PlugIn {
	ImagePlus imp;
	int width;
	int height;
	int slices;
	int frames;
	int channels;
	int cur_slice;
	int cur_frame;
	int cur_channel;
	@Override
	public void run(String arg0) 
	{
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
        Roi [] rois = manager.getRoisAsArray();
        
        
        Frame window=WindowManager.getFrame("Counting");
        ResultsTable the_table;
    	if (window==null)
    	{
    		the_table=new ResultsTable();
    	}
    	else
    	{
    		the_table=((TextWindow)window).getTextPanel().getResultsTable();
    		/*the_table.incrementCounter();
    		the_table.addValue("Image",imp.getTitle());
    		the_table.addValue("AvgAnisotropy",cum/ctr);
    		the_table.show("Anisotropy");*/
    	}
    	
        for (int r=0; r<rois.length; r++)
        {
        	Roi roi=rois[r];
        	Polygon p=roi.getPolygon();
        	int counter=0;
        	for (int i=0; i<p.npoints; i++)
        	{
        		if (p.xpoints[0]!=1) counter++;
        	}
        	the_table.incrementCounter();
        	the_table.addValue("Image", imp.getTitle());
    		the_table.addValue("Type", r+1);
    		the_table.addValue("Count", counter);
    		the_table.show("Counting");
        }
	}
}
