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

import java.util.Stack;

//The runme function will take a float image array and a threshold and find all contiguous blobs
//above the threshold

public class find_blobs_3D {

	int [] status_array;
	ArrayList <int []> current_list;
	ArrayList <int []> current_new_list;
	ArrayList <int []> new_new_list;
	int width;
	int height;
	int depth;
	find_blobs_3D()
	{
		
	}
	public ArrayList <ArrayList <int []>> runme(float [] pix, int w, int h, int d, float threshold)
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
					if (pix[k*width*height+j*width+i]<threshold) continue;
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
}
