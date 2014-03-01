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
	}
	ArrayList <MyIntPoint> point_list=new ArrayList <MyIntPoint>();
	ArrayList <MyIntPoint> tmp_point_list=new ArrayList <MyIntPoint>();
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
		dlg.showDialog();
		noise_background=(float) dlg.getNextNumber();
		drop_threshold=(float)dlg.getNextNumber();
		min_threshold=(float)dlg.getNextNumber();
		
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
        if(roi!=null)
        {
        	Polygon p=roi.getPolygon();
        	
        	for (int i=0; i<p.npoints; i++)
        	{
        		int grow_from_x=p.xpoints[i], grow_from_y=p.ypoints[i];
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
        	}
        }
        new_img.show();
        new_img.updateAndDraw();
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
}
