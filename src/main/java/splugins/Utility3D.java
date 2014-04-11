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
	
	/*****************************************************************************
	 * find_labeled_blobs
	 * @param input:  image array with blob pixels labeled with integers>1
	 * @param w
	 * @param h
	 * @param d
	 * @return:  An array list of an array list of the points associated with each blob
	 * Index i of array list corresponds to points that were value i+1 in input
	 */
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
	
	public static void filter_blob_array(ArrayList <ArrayList <int []>> inp, int min, int max)
	{
		for (int i=0; i<inp.size(); i++)
		{
			if (inp.get(i).size()<min||inp.get(i).size()>max) 
			{
				inp.remove(i);
				i--;
			}
		}
	}
	public static int [] find_center_of_mass(ArrayList <int []> myblob)
	{
		int [] rtn=new int [3];
		for (ListIterator jF=myblob.listIterator(); jF.hasNext();)
		{
			int [] current_point=(int [])jF.next();
			for (int i=0; i<3; i++) rtn[i]=rtn[i]+current_point[i];
		}
		for (int i=0; i<3; i++) rtn[i]=rtn[i]/myblob.size();
		return rtn;
	}
	/***********************************************************************************8
	 * find_blobs
	 * @param pix:  binary image (converted to 16bit) of blobs
	 * @param w
	 * @param h
	 * @param d
	 * @return:  An array list of an array list of the ponits associated with each blob
	 */
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
	public void grow_until_neighbor (short [] input, int wid, int het, int dep)
	{
		grow_until_neighbor(input, wid, het, dep, -1 );
	}
	public void grow_until_neighbor (short [] input, int wid, int het, int dep,int max_size)
	{
		width=wid;
		height=het;
		depth=dep;
		int max=0;
		pixel_array =input; 
		
		ArrayList <ArrayList <int []>> mylist = find_labeled_blobs(input, wid, het, dep);
		max=mylist.size();
		int [] point_count=new int [max];
		boolean had_new=true;
		while (had_new)
		{
			int ctr=0;
			for (int i=0; i<max; i++)
			{
				point_count[i]+=mylist.get(i).size();
				if (point_count[i]>max_size&&max_size>0) continue;
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
			IJ.log(""+ctr);
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
	}
	public void clean_fences(short [] input, int w, int h, int d)
	{
		for (int i=0; i<w*h*d; i++) 
		{
			if (input[i]==(short)-1) input[i]=(short)0;
		}	
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
	public static short [] ip_to_imgarray(ImagePlus img, int cur_channel, int cur_frame)
	{
		short [] pix=new short[img.getWidth()* img.getHeight()* img.getNSlices()];
		cur_channel--;
		cur_frame--;
		for (int i=0; i<img.getNSlices(); i++)
		{
			short [] tmp=(short [])img.getStack().getProcessor(cur_channel+i*img.getNChannels()+cur_frame*img.getNChannels()*img.getNSlices()+1).getPixels();
			for (int k=0; k<tmp.length; k++) pix[i*img.getWidth()*img.getHeight()+k]=tmp[k];
		}
		return pix;
	}
	public static short [] blobarray_to_imgarray(ArrayList <ArrayList <int []>> mylist, int w, int h, int s)
	{
		short [] rtn=new short [w*h*s];
		for (int i=0; i<mylist.size(); i++)
		{
			for (ListIterator jF=mylist.get(i).listIterator(); jF.hasNext();)
			{
				int [] current_point=(int [])jF.next();
				rtn[current_point[0]+current_point[1]*w+current_point[2]*w*h]=(short)(i+1);
			}
		}
		return rtn;		
	}
	
	public byte [] blobarray_to_byte_imgarray(ArrayList <ArrayList <int []>> mylist, int w, int h, int s)
	{
		byte [] rtn=new byte [w*h*s];
		for (int i=0; i<mylist.size(); i++)
		{
			for (ListIterator jF=mylist.get(i).listIterator(); jF.hasNext();)
			{
				int [] current_point=(int [])jF.next();
				rtn[current_point[0]+current_point[1]*w+current_point[2]*w*h]=(byte)255;
			}
		}
		return rtn;		
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
	
	
	
	/************************************************************************************
	 * check_neighbor
	 * @param x
	 * @param y
	 * @param z
	 * @return true if could add point to the list of current object, to be used with find_blobs only
	 */
	private boolean check_neighbor(int x, int y, int z)
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
	public ArrayList <int []> make_kernel(short [] pix, float lateral_res, float vertical_res, float radius)
	{
		ArrayList <int []> rtn=new ArrayList <int []> ();
		for (int i=-100; i<100; i++)
		{
			for (int j=-100; j<100; j++)
			{
				for (int k=-100; k<100; k++)
				{
					float x=i*lateral_res, y=j*lateral_res, z=k*vertical_res;
					if (x*x+y*y+z*z<radius)
					{
						int [] temp={i, j, k};
						rtn.add(temp);
					}
				}
			}
		}
		return rtn;		
	}
	/***********************************************************************
	 * apply_kernel_to_list:  Will take list of points from trackmate, and a kernel
	 * found from make_kernel and will to return blob_list
	 * @param hit_list ArrayList having int[4], x, y, z, index
	 * @param kernel
	 * @param w width of final image
	 * @param h height of final image
	 * @param d depth of final image
	 * @return
	 */
	public ArrayList <ArrayList <int []>> apply_kernel_to_list(ArrayList <int []> hit_list, ArrayList <int []> kernel, int w, int h, int d)
	{
		ArrayList <ArrayList <int []>> rtn=new ArrayList <ArrayList <int []>> ();
		for (ListIterator jF=hit_list.listIterator(); jF.hasNext();)
		{
			int [] current_point=(int [])jF.next();
			ArrayList <int []> current_list=new ArrayList <int []> ();
			for (ListIterator hF=hit_list.listIterator(); hF.hasNext();)
			{
				int [] kernel_point=(int []) hF.next();
				int x=current_point[0]+kernel_point[0];
				int y=current_point[1]+kernel_point[1];
				int z=current_point[2]+kernel_point[2];
				if (x<0||x>=w||y<0||y>=h||z<0||z>=d) continue;
				int temp[]={x,y,z};
				current_list.add(temp);
			}
			rtn.add(current_list);
		}
		return rtn;
	}
}
