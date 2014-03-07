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
	int [] status_array;
	ArrayList <int []> current_list;
	ArrayList <int []> current_new_list;
	ArrayList <int []> new_new_list;
	
	//This takes an already indexed short image and provides the ArrayList associated with it, it will create
	//an entry all the way up to the max value in input, many of which might be blank
	//NOTE:  Index 0 will have things that were labeled 1 in the input image
	public static ArrayList <ArrayList <int []>> find_labeled_blobs(short [] input, int w, int h, int d)
	{
		short max=0;
		for (int i=0; i<w*h*d; i++) 
		{
			if (input[i]>max) max=input[i];
		}
		ArrayList<ArrayList <int []>> rtnlist=new ArrayList<ArrayList <int []>>();
		for (int i=0; i<max; i++)
		{
			rtnlist.add(new ArrayList<int []>());
		}
		for (int i=0; i<d; i++) 
		{
			for (int j=0; j<h; j++)
			{
				for (int k=0; k<w; k++)
				{
					short tst=input[k+j*w+i*w*h];
					if (tst>(short)0) 
					{
						int [] tmp={k,j,i};
						rtnlist.get(tst-1).add(tmp);
					}
				}
			}
			
		}
		return rtnlist;
	}
	//This takes a (simply thresholded, but converted to short) image and creates the ArrayList for it
	public ArrayList <ArrayList <int []>> find_blobs(short [] pix, int w, int h, int d)
	{
		width=w;
		height=h;		
		depth=d;
		status_array=new int[width*height*depth];
		ArrayList<ArrayList <int []>> rtnlist=new ArrayList<ArrayList <int []>>();
		current_new_list=new ArrayList<int []>();
		new_new_list=new ArrayList<int []>();
		current_list=new ArrayList<int []>();
		IJ.log(""+pix.length);
		for (int k=0; k<depth; k++)
		{
			for (int i=0; i<width; i++)
			{
				for (int j=0; j<height; j++)
				{
					if (pix[k*width*height+j*width+i]==(short)0) continue;
					status_array[k*width*height+j*width+i]=1;
				}
			}
		}
		for (int k=0; k<depth; k++)
		{
			for (int i=0; i<width; i++)
			{
				for (int j=0; j<height; j++)
				{
					if (status_array[j*width+i+k*width*height]==0) continue;
					int [] tmp=new int[3];
					tmp[0]=i;
					tmp[1]=j;
					tmp[2]=k;
					current_list=new ArrayList<int []>();
					current_new_list.clear();
					current_list.add(tmp);
					current_new_list.add(tmp);
					status_array[j*width+i]=0;
					boolean no_new=false;
					while (no_new==false)
					{
						int ctr=0;
						for (ListIterator jF=current_new_list.listIterator(); jF.hasNext();)
						{
							int [] current_point=(int [])jF.next();
							if (check_neighbor(current_point[0]+1, current_point[1], current_point[2])) ctr++;
							if (check_neighbor(current_point[0]-1, current_point[1], current_point[2])) ctr++;
							if (check_neighbor(current_point[0], current_point[1]+1, current_point[2])) ctr++;
							if (check_neighbor(current_point[0], current_point[1]-1, current_point[2])) ctr++;
							if (check_neighbor(current_point[0], current_point[1], current_point[2]+1)) ctr++;
							if (check_neighbor(current_point[0], current_point[1], current_point[2]-1)) ctr++;
						}
						current_new_list.clear();
						for (ListIterator jF=new_new_list.listIterator(); jF.hasNext();)
						{
							int [] current_point=(int [])jF.next();
							current_new_list.add(current_point);
						}
						new_new_list.clear();
						if (ctr==0) no_new=true;
					}
					//Add this list to the master list
					rtnlist.add(current_list);
				}
			}
		}
		return rtnlist;
	}
	//This is the check neighbor for finding the extent of the blobs, it is NOT to be used for growing
	boolean check_neighbor(int x, int y, int z)
	{
		if (x<0||x==width||y<0||y==height||z<0||z==depth) return false;
		if (status_array[x+y*width+z*height*width]==0) return false;
		int [] tmp=new int [3];
		tmp[0]=x;
		tmp[1]=y;
		tmp[2]=z;
		new_new_list.add(tmp);
		current_list.add(tmp);
		status_array[x+y*width+z*width*height]=0;
		return true;
	}
	public void grow_until_neighbor (short [] input, int wid, int het, int dep)
	{
		width=wid;
		height=het;
		depth=dep;
		int max=0;
		pixel_array =input; 
		
		ArrayList <ArrayList <int []>> mylist = find_labeled_blobs(input, wid, het, dep);
		
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
					if (check_neighbor_grow(current_point[0]+1, current_point[1], current_point[2],i)) ctr++;
					if (check_neighbor_grow(current_point[0]-1, current_point[1], current_point[2],i)) ctr++;
					if (check_neighbor_grow(current_point[0], current_point[1]+1, current_point[2],i)) ctr++;
					if (check_neighbor_grow(current_point[0], current_point[1]-1, current_point[2],i)) ctr++;
					if (check_neighbor_grow(current_point[0], current_point[1], current_point[2]+1,i)) ctr++;
					if (check_neighbor_grow(current_point[0], current_point[1], current_point[2]-1,i)) ctr++;
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
	
	public void dilate_no_merge(short [] input, int wid, int het, int dep)
	{
		width=wid;
		height=het;
		depth=dep;
		int max=0;
		pixel_array =input; 
		
		ArrayList <ArrayList <int []>> mylist = find_labeled_blobs(input, wid, het, dep);
		
		boolean had_new=true;
		for (int i=0; i<max; i++)
		{
			current_list=new ArrayList<int []>();
			for (ListIterator jF=mylist.get(i).listIterator(); jF.hasNext();)
			{
				int [] current_point=(int [])jF.next();
				if (check_neighbor_grow(current_point[0]+1, current_point[1], current_point[2],i)) ;
				if (check_neighbor_grow(current_point[0]-1, current_point[1], current_point[2],i)) ;
				if (check_neighbor_grow(current_point[0], current_point[1]+1, current_point[2],i)) ;
				if (check_neighbor_grow(current_point[0], current_point[1]-1, current_point[2],i)) ;
				if (check_neighbor_grow(current_point[0], current_point[1], current_point[2]+1,i)) ;
				if (check_neighbor_grow(current_point[0], current_point[1], current_point[2]-1,i)) ;
			}
			mylist.get(i).clear();
			mylist.get(i).addAll(current_list);
		}
		for (int i=0; i<width*height*depth; i++) 
		{
			if (input[i]==(short)-1) input[i]=(short)0;
		}	
	}
	
	private boolean check_neighbor_grow(int x, int y, int z, int idx)
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
	public static ImagePlus imgarray_to_ip(short [] pix, int width, int height, int depth)
	{
		ImagePlus new_img=NewImage.createShortImage("MyImg", width, height, depth, NewImage.FILL_BLACK);
		for (int i=0; i<depth; i++)
		{
			short [] tmp=(short [])new_img.getStack().getProcessor(i+1).getPixels();
			for (int k=0; k<width*height; k++) tmp[k]=pix[i*width*height+k];
		}
		return new_img;
		
	}
	public static short [] ip_to_imgarray(ImagePlus img)
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
	public short [] blobarray_to_imgarray(ArrayList <ArrayList <int []>> mylist, int width, int height, int slices)
	{
		short [] rtn=new short [width*height*slices];
		for (int i=0; i<mylist.size(); i++)
		{
			for (ListIterator jF=mylist.get(i).listIterator(); jF.hasNext();)
			{
				int [] current_point=(int [])jF.next();
				rtn[current_point[0]+current_point[1]*width+current_point[2]*width*height]=(short)(i+1);
			}
		}
		return rtn;		
	}
}
