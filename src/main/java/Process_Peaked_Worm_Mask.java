import java.awt.Point;
import java.util.ArrayList;
import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
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
import java.util.List;

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
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.IndexColorModel;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;

public class Process_Peaked_Worm_Mask implements PlugIn {

	int width, height;
	ImagePlus img;
	float [] mask_pix;
	float [] peak_pix;
	float [] img_pix;
	@Override
	public void run(String arg) {
		img=WindowManager.getCurrentImage();
		width=img.getWidth();
		height=img.getHeight();
		mask_pix=(float [])img.getStack().getProcessor(1).getPixels();
		peak_pix=(float [])img.getStack().getProcessor(2).getPixels();
		img_pix=(float [])img.getStack().getProcessor(3).getPixels();
		float [] new_slice=new float[width*height];
		
		GenericDialog gd=new GenericDialog("Process Peaked Worms");
		gd.addNumericField("Number of thresholds to try:  ", 100, 0);
		gd.showDialog();
		
		
		
		float initial_threshold=10000000f;
		float final_threshold=0f;
		for (int i=0; i<width*height; i++)
		{
			if (peak_pix[i]==0) continue;
			if (mask_pix[i]==0) img_pix[i]=0f;
			if (img_pix[i]<initial_threshold) initial_threshold=img_pix[i];
			if (img_pix[i]>final_threshold) final_threshold=img_pix[i];
		}

		float number_steps=(float)gd.getNextNumber();
		float step_size=(final_threshold-initial_threshold)/number_steps;
		find_blobs blobber=new find_blobs();
		ArrayList <ArrayList <Point>> rtn;
		int peaks=0;
		for (float t=initial_threshold; t<final_threshold; t+=step_size)
		{
			rtn=blobber.runme(img_pix, width, height, t);
			for (ListIterator jF=rtn.listIterator(); jF.hasNext();)
			{
				ArrayList <Point> current_list=(ArrayList <Point>)jF.next();
				int number_appearing=0;
				if (current_list.size()==1) continue;
				for (ListIterator iF=current_list.listIterator(); iF.hasNext();)
				{
					Point current_point=(Point)iF.next();
					if (img_pix[current_point.x+current_point.y*width]>=t+step_size) number_appearing++;
				}
				if (number_appearing>1) continue;
				for (ListIterator iF=current_list.listIterator(); iF.hasNext();)
				{
					Point current_point=(Point)iF.next();
					new_slice[current_point.x+current_point.y*width]=final_threshold;
				}
				peaks++;				
			}
		}
		int area=0;
		for (int i=0; i<width*height; i++)
		{
			if (mask_pix[i]!=0) area++;
		}
		IJ.log("" +peaks+","+area);
		img.getStack().addSlice(new FloatProcessor(width, height, (float[])new_slice));	
		img.updateAndDraw();

	}

}
