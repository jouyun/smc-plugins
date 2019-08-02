package splugins;

import static org.junit.Assert.assertArrayEquals;

import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.plugin.RGBStackMerge;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.frame.RoiManager;

public class PointROI_To_MaskChannel implements PlugIn {

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
		
		//Prepare new ImagePlus objects
		ImagePlus [] img_array = ChannelSplitter.split(imp);
		ImagePlus [] new_img_array=new ImagePlus [channels+2];
		
		for (int i=0; i<channels; i++) new_img_array[i]=img_array[i];
		for (int i=channels; i<channels+2; i++) new_img_array[i]=NewImage.createFloatImage("Tst", width,  height,  slices*frames, NewImage.FILL_BLACK);
		
		
		//Make lists of ROIs
		RoiManager manager=RoiManager.getInstance();
        Roi [] rois = manager.getRoisAsArray();
        
        int [] xs=new int[rois.length];
        int [] ys=new int[rois.length];
        int [] zs=new int[rois.length];
        int [] ts=new int[rois.length];
        
        for (int j=0; j<rois.length; j++)
        {
    		Roi roi = manager.getRoi(j);
    		float [][] points=new float[0][0];
            if(roi!=null)
            {
            	Polygon p=roi.getPolygon();
            	xs[j]=p.xpoints[0];
            	ys[j]=p.ypoints[0];
            	zs[j]=Math.max(roi.getZPosition()-1, 0);
            	ts[j]=Math.max(roi.getTPosition()-1, 0);
            }
        }
		
        //Mark the spots in the new img's last 2 channels
        for (int c=channels; c<channels+2; c++)
        {
        	for (int r=0; r<rois.length; r++)
        	{
        		float pix[]=(float[])new_img_array[c].getStack().getProcessor(1+zs[r]+ts[r]*slices).getPixels();
        		pix[xs[r]+ys[r]*width]=(float) 255.0;
        	}        	
        }
        
       //Apply blur to the 2 channels
        for (int c=channels; c<channels+2; c++)
        {
        	for (int s=0; s<slices; s++)
        	{
        		for (int f=0; f<frames; f++)
        		{
        			GaussianBlur gb=new GaussianBlur();
        			gb.blurGaussian(new_img_array[c].getStack().getProcessor(1+s+f*slices), 1.0);
        		}
        	}
        }
        
        ImagePlus new_img=NewImage.createFloatImage("Tst", width,  height,  slices*frames*(channels+2), NewImage.FILL_BLACK);
        for (int c=0; c<channels+2; c++)
        {
        	for (int f=0; f<frames; f++)
        	{
        		for (int z=0; z<slices; z++)
        		{
        			float [] npix=(float [])new_img.getStack().getProcessor(1+c+z*(channels+2)+f*(channels+2)*slices).getPixels();
        			float [] pix=(float [])new_img_array[c].getStack().getProcessor(1+z+f*slices).getPixels();
        			System.arraycopy(pix, 0, npix, 0, npix.length);
        		}
        	}
        }
        String img_name=imp.getTitle();
        imp.changes=false;
        imp.close();
        new_img.setDisplayMode(IJ.COMPOSITE);
		new_img.setOpenAsHyperStack(true); 
		new_img.setDimensions(channels+2, slices, frames);
		new_img.setDisplayRange(0, 255);
        new_img.updateAndDraw();
        new_img.show();
        new_img.setTitle(img_name);
        /*
		RGBStackMerge rgb=new RGBStackMerge();
		ImagePlus new_img=rgb.mergeHyperstacks(new_img_array, false);
		String img_name=imp.getTitle();
		new_img.show();
		new_img.updateAndDraw();
		imp.close();
		new_img.setTitle(img_name);*/
	}

}
