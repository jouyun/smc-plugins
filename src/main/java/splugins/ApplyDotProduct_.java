package splugins;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import ij.util.Tools;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import ij.measure.*;
import java.awt.Rectangle;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.IndexColorModel;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;

public class ApplyDotProduct_ implements PlugInFilter, Measurements{
	ImagePlus imp;
	float[] source_spectrum;
	float[][][] img_array;
	float[][] output_img;
	int x_dim, y_dim, target_channels, target_slices, target_frames, source_channels, source_slices, source_frames, cur_slice, cur_frame;
	ImagePlus new_img;
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_32;
	}
	public void run(ImageProcessor ip) 
	{
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		//IJ.log("Good ones:  "+admissibleImageList.length+"\n");		
		if (admissibleImageList.length == 0) return;
		//IJ.log(""+admissibleImageList.length);
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Do Dot Product");
		gd.addChoice("Target image:", sourceNames, admissibleImageList[0].getTitle());
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		int tmpIndex=gd.getNextChoiceIndex();
		//IJ.log("First:  " + admissibleImageList[tmpIndex].getTitle() + "\n");
		
		ImagePlus target_img;
		target_img=admissibleImageList[tmpIndex];
		x_dim=target_img.getWidth();
		y_dim=target_img.getHeight();
        target_channels=target_img.getNChannels();
        target_slices=target_img.getNSlices();
        target_frames=target_img.getNFrames();
        source_channels=imp.getNChannels();
        source_slices=imp.getNSlices();
        source_frames=imp.getNFrames();
        cur_slice=imp.getSlice()-1;
        cur_frame=imp.getFrame()-1;
		ImageStack stack = target_img.getStack();
		ImageProcessor p = stack.getProcessor(1);
		
        getCAxisProfile();
		
		img_array=new float[x_dim][y_dim][source_channels];
		new_img=NewImage.createFloatImage("NewImg", x_dim, y_dim, target_slices*target_frames, NewImage.FILL_BLACK);
		new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(1, target_slices, target_frames);
		copy_process_data(target_img);
		
		
		
		new_img.show();
		new_img.updateAndDraw();
		ImageProcessor r=new_img.getProcessor();
		r.setMinAndMax(990.0, 998.0);
		
		
	}
	
	void getCAxisProfile() {
        Roi roi = imp.getRoi();
        if(roi==null)
             return ;
        source_spectrum = new float[source_channels];
        Rectangle r = roi.getBoundingRect();
        Calibration cal = imp.getCalibration();
        //ROI with Area > 0
        for (int i=0; i<source_channels; i++) {
        	ImageProcessor ip = imp.getStack().getProcessor(source_channels*source_slices*cur_frame+source_channels*cur_slice+i+1);
            ip.setRoi(roi);
            ImageStatistics stats = ImageStatistics.getStatistics(ip, MEAN, cal);
            source_spectrum[i] = (float)stats.mean;
			//IJ.log(""+source_spectrum[i-1] +"\n");
        }
    }
	void copy_process_data(ImagePlus target_img)
	{
		for (int f=0; f<target_frames; f++)
		{
			for (int s=0; s<target_slices; s++)
			{
				//Copy data to array, not necessary, just being lazy
				for (int i=0; i<source_channels; i++) 
				{
					ImageProcessor tp = target_img.getStack().getProcessor(target_channels*target_slices*f+target_channels*s+i+1);
					float[] pixels = (float[])tp.getPixels();
					for (int j=0; j<x_dim; j++) {
						for (int k=0; k<y_dim; k++)
						{				
							img_array[j][k][i]=pixels[j*y_dim+k];
						}
					}
				}
				IJ.log(""+new_img.getStack().getSize()+" "+(1*target_slices*f+1*s+1));
				ImageProcessor tp = new_img.getStack().getProcessor(1*target_slices*f+1*s+1);
				float[] pixels = (float[])tp.getPixels();
					
				for (int j=0; j<x_dim; j++) {
					for (int k=0; k<y_dim; k++)
					{	
						float tmp=0;
						for (int m=0; m<source_channels; m++)
						{
							tmp=tmp+img_array[j][k][m]*source_spectrum[m];
						}
						//if (tmp>31) 
						pixels[j*y_dim+k]=tmp*1000f/(float)source_channels;
					}
				}
				
			}
		}
	}
	/*void copy_data(ImagePlus target_img)
	{
		ImageStack stack = target_img.getStack();
		int num_pixels;
		num_pixels=x_dim*y_dim;
        for (int i=0; i<size; i++) {
			ImageProcessor tp = stack.getProcessor(i+1);
			float[] pixels = (float[])tp.getPixels();
			for (int j=0; j<x_dim; j++) {
				for (int k=0; k<y_dim; k++)
				{				
					img_array[j][k][i]=pixels[j*y_dim+k];
				}
			}
		}
	}
	void process_data()
	{
		ImageProcessor tp = new_img.getProcessor();
		float[] pixels = (float[])tp.getPixels();
			
		for (int j=0; j<x_dim; j++) {
			for (int k=0; k<y_dim; k++)
			{	
				float tmp=0;
				for (int m=0; m<size; m++)
				{
					tmp=tmp+img_array[j][k][m]*source_spectrum[m];
				}
				if (tmp>31) pixels[j*y_dim+k]=tmp*1000f/32f;
			}
		}
	}*/
	
	private ImagePlus[] createAdmissibleImageList () 
	{
		final int[] windowList = WindowManager.getIDList();
		//IJ.log(""+windowList[0]+"\n");
		final Stack stack = new Stack();
		for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) 
		{
			final ImagePlus imp = WindowManager.getImage(windowList[k]);
			//IJ.log(imp.getTitle()+"\n");
			if ((imp != null) && (imp.getType() == imp.GRAY32)) 
			{
				//IJ.log("got one");
				stack.push(imp);
			}
		}
		//IJ.log("Stack size:  " + stack.size() + "\n");
		final ImagePlus[] admissibleImageList = new ImagePlus[stack.size()];
		int k = 0;
		while (!stack.isEmpty()) {
			admissibleImageList[k++] = (ImagePlus)stack.pop();
		}
		if (k==0 && (windowList != null && windowList.length > 0 )){
			IJ.error("No float images, convert to float and try again");
		}
		return(admissibleImageList);
	} /* end createAdmissibleImageList */
}