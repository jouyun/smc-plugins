package splugins;

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

public class Utility3D  {

	int width, height, depth;
	short pixel_array[];
	ArrayList<int []> current_list;
	public void grow_until_neighbor (short [] input, int wid, int het, int dep)
	{
		width=wid;
		height=het;
		depth=dep;
		int max=0;
		pixel_array =input; 
		for (int i=0; i<width*height*depth; i++) 
		{
			if (input[i]>max) max=input[i];
		}
		ArrayList<ArrayList <int []>> mylist=new ArrayList<ArrayList <int []>>();
		for (int i=0; i<max; i++)
		{
			mylist.add(new ArrayList<int []>());
		}
		for (int i=0; i<depth; i++) 
		{
			for (int j=0; j<height; j++)
			{
				for (int k=0; k<width; k++)
				{
					short tst=input[k+j*width+i*width*height];
					if (tst>(short)0) 
					{
						int [] tmp={k,j,i};
						mylist.get(tst-1).add(tmp);
					}
				}
			}
			
		}
		boolean had_new=true;
		while (had_new)
		{
			int ctr=0;
			for (int i=0; i<max; i++)
			{
				current_list=new ArrayList<int []>();
				for (ListIterator jF=mylist.get(i).listIterator(); jF.hasNext();)
				{
					int [] current_point=(int [])jF.next();
					if (check_neighbor(current_point[0]+1, current_point[1], current_point[2],i)) ctr++;
					if (check_neighbor(current_point[0]-1, current_point[1], current_point[2],i)) ctr++;
					if (check_neighbor(current_point[0], current_point[1]+1, current_point[2],i)) ctr++;
					if (check_neighbor(current_point[0], current_point[1]-1, current_point[2],i)) ctr++;
					if (check_neighbor(current_point[0], current_point[1], current_point[2]+1,i)) ctr++;
					if (check_neighbor(current_point[0], current_point[1], current_point[2]-1,i)) ctr++;
				}
				mylist.get(i).clear();
				mylist.get(i).addAll(current_list);
			}
			if (ctr==0) had_new=false;
		}
		for (int i=0; i<width*height*depth; i++) 
		{
			if (input[i]==(short)-1) input[i]=(short)0;
		}	
	}
	
	boolean check_neighbor(int x, int y, int z, int idx)
	{
		if (x<0||x==width||y<0||y==height||z<0||z==depth) return false;
		if (pixel_array[x+y*width+z*height*width]==idx+1) return false;
		if (pixel_array[x+y*width+z*height*width]>0)  //Crashed into neighbor! 
		{
			pixel_array[x+y*width+z*height*width]=(short)-1;
			return false;
		}
		if (pixel_array[x+y*width+z*height*width]<0) return false;
		pixel_array[x+y*width+z*height*width]=(short)(idx+1);
		int [] tmp={x,y,z};
		current_list.add(tmp);
		return true;
	}
	public static ImagePlus array_to_ip(short [] pix, int width, int height, int depth)
	{
		ImagePlus new_img=NewImage.createShortImage("MyImg", width, height, depth, NewImage.FILL_BLACK);
		for (int i=0; i<depth; i++)
		{
			short [] tmp=(short [])new_img.getStack().getProcessor(i+1).getPixels();
			for (int k=0; k<width*height; k++) tmp[k]=pix[i*width*height+k];
		}
		return new_img;
		
	}
	public static short [] ip_to_array(ImagePlus img)
	{
		short [] pix=new short[img.getWidth()* img.getHeight()* img.getNSlices()];
		int cur_channel=img.getChannel()-1, cur_frame=img.getFrame()-1;
		for (int i=0; i<img.getNSlices(); i++)
		{
			short [] tmp=(short [])img.getStack().getProcessor(cur_channel+i*img.getNChannels()+cur_frame*img.getNChannels()*img.getNSlices()+1).getPixels();
			for (int k=0; k<tmp.length; k++) pix[i*img.getWidth()*img.getHeight()+k]=tmp[k];
		}
		return pix;
	}
	
}
