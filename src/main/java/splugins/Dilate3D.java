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

public class Dilate3D {
	public static byte [] dilate_me(byte [] input, int width, int height, int depth)
	{
		byte [] newer_arr=new byte[width*height*depth];
		for (int i=0; i<depth; i++)
		{
			for (int j=0; j<height; j++)
			{
				for (int k=0; k<width; k++)
				{
					if (check_neighbors(input, k, j, i, width, height, depth)) 
					{
						int idx=i*width*height+j*width+k;
						newer_arr[i*width*height+j*width+k]=(byte)255;
					}
					else newer_arr[i*width*height+j*width+k]=(byte)0;
				}
			}
		}
		return newer_arr;
	}
	
	public static boolean check_neighbors(byte [] input, int x, int y, int z, int width, int height, int depth)
	{
		if (x>0&&input[z*depth*height+y*width+x-1]!=0) return true;
		if (y>0&&input[z*depth*height+(y-1)*width+x]!=0) return true;
		if (x<width&&input[z*depth*height+y*width+x+1]!=0) return true;
		if (y<height&&input[z*depth*height+(y+1)*width+x]!=0) return true;
		if (z>0&&input[(z-1)*depth*height+(y)*width+x]!=0) return true;
		if (z<height&&input[(z+1)*depth*height+(y)*width+x]!=0) return true;
		return false;
		
	}
	
	public static float [] make_3D_float(ImagePlus img, int cur_channel, int cur_frame)
	{
		int width=img.getWidth();
		int height=img.getHeight();
		int depth=img.getNSlices();
		float [] pix=new float[width*height*depth];
		for (int i=0; i<depth; i++)
		{
			float [] tmp=(float [])img.getStack().getProcessor(cur_channel+i*img.getNChannels()+cur_frame*img.getNChannels()*depth+1).getPixels();
			for (int k=0; k<width*height; k++) pix[i*width*height+k]=tmp[k];
		}
		return pix;
	}
	
	public static byte [] make_3D_byte(ImagePlus img, int cur_channel, int cur_frame)
	{
		int width=img.getWidth();
		int height=img.getHeight();
		int depth=img.getNSlices();
		byte [] pix=new byte[width*height*depth];
		depth=depth;
		for (int i=0; i<depth; i++)
		{
			byte [] tmp=(byte [])img.getStack().getProcessor(cur_channel+i*img.getNChannels()+cur_frame*img.getNChannels()*depth+1).getPixels();
			for (int k=0; k<width*height; k++) 
			{
				pix[i*width*height+k]=tmp[k];
			}
		}
		return pix;
	}
	
	public static ImagePlus make_3D_ImagePlusByte(byte [] pix, int width, int height, int depth)
	{
		ImagePlus new_img=NewImage.createByteImage("Dilated", width, height, depth, NewImage.FILL_BLACK);
		for (int i=0; i<depth; i++)
		{
			byte [] tmp=(byte [])new_img.getStack().getProcessor(i+1).getPixels();
			for (int k=0; k<width*height; k++) tmp[k]=pix[i*width*height+k];
		}
		return new_img;
	}

}
