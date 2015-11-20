package splugins;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
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
import ij.text.TextWindow;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.IndexColorModel;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;

import ij.plugin.PlugIn;

public class seeded_multipoint_adaptive_region_grow implements PlugIn{

	ImagePlus imp;
	int width;
	int height;
	int slices;
	int frames;
	int channels;
	int cur_slice;
	int cur_frame;
	int cur_channel;
	float noise_background;
	float current_peak;
	float drop_threshold;
	float min_threshold;
	float min_object_size;
	float max_object_size;
	class MyIntPoint
	{
		int x;
		int y;
		int z;
		MyIntPoint(int a, int b)
		{
			x=a;
			y=b;
		}
		MyIntPoint(MyIntPoint a)
		{
			x=a.x;
			y=a.y;
			z=a.z;
		}
	}
	ArrayList <MyIntPoint> point_list=new ArrayList <MyIntPoint>();
	ArrayList <MyIntPoint> tmp_point_list=new ArrayList <MyIntPoint>();
	ArrayList <ArrayList <MyIntPoint>> cumulative_point_list=new ArrayList <ArrayList <MyIntPoint>>();
	byte[] output_array;
	float [] input_array;
	public void run(String arg)
	{
		noise_background=270;
		drop_threshold=0.2f;
		GenericDialog dlg=new GenericDialog("Region grow");
		dlg.addNumericField("Background", 270, 0);
		dlg.addNumericField("Drop threshold", 0.2, 1);
		dlg.addNumericField("Minimum threshold", -150.0, 1);
		dlg.addCheckbox("Quantify Results?", false);
		dlg.addNumericField("Minimum size of quantified object:  ", 5, 0);
		dlg.addNumericField("Maximum size of quantified object:  ", 20, 0);
		
		dlg.showDialog();
		noise_background=(float) dlg.getNextNumber();
		drop_threshold=(float)dlg.getNextNumber();
		min_threshold=(float)dlg.getNextNumber();
		boolean quantify= dlg.getNextBoolean();
		min_object_size=(float) dlg.getNextNumber();
		max_object_size=(float) dlg.getNextNumber();
		
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		//If there is already an roi selected, use it and return
		ImagePlus new_img=NewImage.createByteImage("Img", width, height, 1, NewImage.FILL_BLACK);
		output_array=(byte [])new_img.getProcessor().getPixels();
		input_array=(float [])imp.getProcessor().getPixels();
		Roi roi = imp.getRoi();
		double [][] points=new double[0][0];
        if(roi!=null)
        {
        	Polygon p=roi.getPolygon();
        	
        	points=new double[p.npoints][3];
        	for (int i=0; i<p.npoints; i++)
        	{
        		points[i][0]=input_array[p.ypoints[i]*width+p.xpoints[i]];
        		points[i][1]=p.xpoints[i];
        		points[i][2]=p.ypoints[i]; 
        	}
        	
        	Manual_Tracker.vector_sort(points,  true);
        	
            for (int i=0; i<points.length; i++)
            {
            	ArrayList <MyIntPoint> this_cumulative=new ArrayList <MyIntPoint> ();
            	
        		int grow_from_x=(int) points[i][1], grow_from_y=(int) points[i][2];
        		if (output_array[grow_from_y*width+grow_from_x]>0) continue;
        		current_peak=input_array[grow_from_y*width+grow_from_x];
        		MyIntPoint curpt=new MyIntPoint(grow_from_x, grow_from_y);
                point_list.add(curpt);
        		int ctr;
        		boolean done=false;
        		while (!done)
        		{
        			ctr=0;
        			for (ListIterator jF=point_list.listIterator();jF.hasNext();)
        			{
        				curpt=(MyIntPoint)jF.next();
        				if (check_neighbor(curpt.x+1, curpt.y)) ctr++;
        				if (check_neighbor(curpt.x-1, curpt.y)) ctr++;
        				if (check_neighbor(curpt.x, curpt.y+1)) ctr++;
        				if (check_neighbor(curpt.x, curpt.y-1)) ctr++;
        				
        				this_cumulative.add(new MyIntPoint (curpt));
        			}
        			point_list.clear();
        			for (ListIterator jF=tmp_point_list.listIterator();jF.hasNext();)
        			{
        				curpt=(MyIntPoint)jF.next();
        				point_list.add(curpt);
        			}
        			tmp_point_list.clear();
        			if (ctr==0) done=true;
        		}
        		if (this_cumulative.size()>=min_object_size&&this_cumulative.size()<=max_object_size)
        		{
        			cumulative_point_list.add(this_cumulative);
        		}
            }
        	new_img.show();
            new_img.updateAndDraw();
        }
        
        if (quantify) quantify_channels();
	}
	boolean check_neighbor(int x, int y)
	{
		if (x<0||x==width||y<0||y==height) return false;
		if (input_array[y*width+x]-noise_background<(current_peak-noise_background)*drop_threshold) return false;
		if (input_array[y*width+x]-noise_background<min_threshold) return false;
		if (output_array[y*width+x]!=0) return false;
		output_array[y*width+x]=(byte)255;
		float tt=input_array[y*width+x];
		MyIntPoint curpt=new MyIntPoint(x, y);
		tmp_point_list.add(curpt);
		return true;
	}
	void quantify_channels()
	{
		ResultsTable rslt;
		Frame window=WindowManager.getFrame("FRET Results");
		if (window==null) 
		{
			rslt=new ResultsTable();
		}
		else
		{
			rslt=((TextWindow)window).getTextPanel().getResultsTable();
		}
		int ctr=1;
		
		for (ListIterator jF=cumulative_point_list.listIterator();jF.hasNext();)
		{
			ArrayList <MyIntPoint> curlist=(ArrayList<MyIntPoint>)jF.next();
			rslt.incrementCounter();
			rslt.addValue("Image", imp.getTitle());
			rslt.addValue("Object", ctr);
			rslt.addValue("X", curlist.get(0).x);
			rslt.addValue("Y", curlist.get(0).y);
			for (int c=0; c<channels; c++)
			{
				float accumulator=0;
				float [] pix=(float [])imp.getStack().getProcessor(1+c+cur_slice*channels+cur_frame*channels*slices).getPixels();
				for (ListIterator subF=curlist.listIterator(); subF.hasNext();)
				{
					MyIntPoint mypt=(MyIntPoint) subF.next();
					accumulator+=pix[mypt.x+mypt.y*width];
				}
				rslt.addValue("Mean_"+(c+1), accumulator/curlist.size());
			}
			ctr++;
			
		}
		rslt.show("FRET Results");
	}
}
