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


public class find_blobs {

	int [] status_array;
	ArrayList <Point> current_list;
	ArrayList <Point> current_new_list;
	ArrayList <Point> new_new_list;
	int width;
	int height;
	find_blobs()
	{
		
	}
	public ArrayList <ArrayList <Point>> runme(float [] pix, int w, int h, float threshold)
	{
		width=w;
		height=h;
		status_array=new int[width*height];
		ArrayList<ArrayList <Point>> rtnlist=new ArrayList<ArrayList <Point>>();
		current_new_list=new ArrayList<Point>();
		new_new_list=new ArrayList<Point>();
		current_list=new ArrayList<Point>();
		
		for (int i=0; i<width; i++)
		{
			for (int j=0; j<height; j++)
			{
				if (pix[j*width+i]<threshold) continue;
				status_array[j*width+i]=1;
			}
		}
		
		for (int i=0; i<width; i++)
		{
			for (int j=0; j<height; j++)
			{
				if (status_array[j*width+i]==0) continue;
				Point tmp=new Point(i, j);
				current_list=new ArrayList<Point>();
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
						Point current_point=(Point)jF.next();
						if (check_neighbor(current_point.x+1, current_point.y)) ctr++;
	    				if (check_neighbor(current_point.x-1, current_point.y)) ctr++;
	    				if (check_neighbor(current_point.x, current_point.y+1)) ctr++;
	    				if (check_neighbor(current_point.x, current_point.y-1)) ctr++;
					}
					current_new_list.clear();
					for (ListIterator jF=new_new_list.listIterator(); jF.hasNext();)
					{
						Point current_point=(Point)jF.next();
						current_new_list.add(current_point);
					}
					new_new_list.clear();
					if (ctr==0) no_new=true;
				}
				//Add this list to the master list
				rtnlist.add(current_list);
			}
		}
		return rtnlist;
	}
	
	boolean check_neighbor(int x, int y)
	{
		if (x<0||x==width||y<0||y==height) return false;
		if (status_array[x+y*width]==0) return false;
		Point tmp=new Point(x,y);
		new_new_list.add(tmp);
		current_list.add(tmp);
		status_array[x+y*width]=0;
		return true;
	}
}
