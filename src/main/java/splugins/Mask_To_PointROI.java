package splugins;

import static org.junit.Assert.assertArrayEquals;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.plugin.RGBStackMerge;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.frame.RoiManager;

public class Mask_To_PointROI implements PlugIn {

	int width;
	int height;
	int slices;
	int cur_slice;
	int frames;
	int cur_frame;
	int cur_channel;
	int channels;
	ImagePlus imp;
	ImagePlus target_imp;
	ImagePlus new_imp;
	public void run (String arg)
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
		
		GenericDialog gd=new GenericDialog("Choose channel");
		gd.addNumericField("Channel", 0, 0);
		gd.showDialog();
		int channel=(int)gd.getNextNumber();
		
		//Make lists of ROIs
		RoiManager manager=RoiManager.getInstance();
		if (manager==null) manager=new RoiManager();
        Roi [] rois = manager.getRoisAsArray();
        
        
        for (int f=0; f<frames; f++)
        {
        	for (int z=0; z<slices; z++)
        	{
    			float [] pix = (float []) imp.getStack().getProcessor(channel+z*channels+f*slices*channels).getPixels();
    			for (int x=0; x<width; x++)
    			{
    				for (int y=0; y<height; y++)
    				{
    					if (pix[x+y*width]>0) 
    					{
    						PointRoi roi=new PointRoi(x, y);
    						//roi.setPosition(channel, z+1, f+1);
    						roi.setPosition(channel+z*channels+f*slices*channels);
    						manager.addRoi(roi);
    						manager.getRoi(manager.getCount()-1).setPosition(channel, z+1, f+1);
    					}
    				}
    			}
        	}
        }
	}

}
