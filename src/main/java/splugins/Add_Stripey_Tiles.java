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

//*********************NOT IN USE***************************

public class Add_Stripey_Tiles implements PlugIn{
	public void run(String arg) {
		ImagePlus img;
		img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), depth=img.getStackSize();
		int cur_channel=img.getChannel()-1, cur_frame=img.getFrame()-1;
		
		GenericDialog dlg=new GenericDialog("3D segmentation");
		dlg.addNumericField("X dimension", 3, 0);
		dlg.addNumericField("Y dimension", 3, 0);
		dlg.addNumericField("Percent overlap", 20, 2);
		dlg.addNumericField("Amplitude", 200, 2);
		dlg.addNumericField("Width", 20, 2);
		dlg.addNumericField("Upper Threshold", 400, 2);
		dlg.addNumericField("Sparsity", 0.2, 3);
		
		dlg.showDialog();
		int x_tiles=(int) dlg.getNextNumber();
		int y_tiles=(int)dlg.getNextNumber();
		float overlap=(float) dlg.getNextNumber();
		float amplitude=(float) dlg.getNextNumber();
		float peak_width=(float) dlg.getNextNumber();
		float threshold=(float) dlg.getNextNumber();
		double sparsity=dlg.getNextNumber();
		int [][] convert_array=new int [x_tiles][y_tiles];
		for (int j=0; j<(y_tiles/2); j++)
		{
			for (int i=0; i<x_tiles; i++)
			{
				convert_array[i][j*2]=i+2*j*x_tiles;
				convert_array[i][j*2+1]=x_tiles+(x_tiles-i-1)+2*j*x_tiles;
				convert_array[i][j*2+1]=y_tiles-1-convert_array[i][j*2+1];
			}
		}
		if (y_tiles%2==1)
		{
			for (int i=0; i<x_tiles; i++)
			{
				convert_array[i][y_tiles-1]=i+(y_tiles-1)*x_tiles;
			}
		}
		
		//Do the x adjacent pairs
		for (int i=0; i<(y_tiles); i++)
		{
			for (int j=0; j<(x_tiles-1); j++)
			{
				float [] pixL=(float [])(img.getStack().getProcessor(convert_array[j][i]+1).getPixels());
				float [] pixR=(float [])(img.getStack().getProcessor(convert_array[j+1][i]+1).getPixels());
				int border_width=(int)Math.ceil((float)width*overlap/100.0);
				float peak_x=(float)Math.random()*(border_width/2)+(float)Math.floor(border_width/4);
				float peak_y=(float)Math.random()*(height/2)+(float)Math.floor(height/4);
				boolean above_thresh=false;
				for (int m=0; m<border_width; m++)
				{
					for (int n=0; n<height; n++)
					{
						if (pixR[n*width+m]>threshold) 
						{
							above_thresh=true;
							//IJ.log("above: "+i+" "+j);
							break;
						}
						
					}
					if (above_thresh) break;
				}
				if (above_thresh) continue;
				if (Math.random()>sparsity) continue;
				IJ.log("Doing one:  "+i+","+j);
				for (int m=0; m<border_width; m++)
				{
					for (int n=0; n<height; n++)
					{
						double mm=m, nn=n;
						float tmp=amplitude*(float)Math.exp(-1.0*((mm-peak_x)*(mm-peak_x)+(nn-peak_y)*(nn-peak_y))/2.0/peak_width/peak_width);
						pixL[m+width-border_width+n*width]=pixL[m+width-border_width+n*width]+tmp;
						pixR[m+n*width]=pixR[m+n*width]+tmp;
					}
				}
			}
		}
		for (int i=0; i<(y_tiles-1); i++)
		{
			for (int j=0; j<(x_tiles); j++)
			{
				float [] pixB=(float [])(img.getStack().getProcessor(convert_array[j][i]+1).getPixels());
				float [] pixT=(float [])(img.getStack().getProcessor(convert_array[j][i+1]+1).getPixels());
				int border_height=(int)Math.ceil((float)height*overlap/100.0);
				float peak_x=(float)Math.random()*(width/2)+(float)Math.floor(width/4);
				float peak_y=(float)Math.random()*(border_height/2)+(float)Math.floor(border_height/4);
				boolean above_thresh=false;
				for (int m=0; m<border_height; m++)
				{
					for (int n=0; n<width; n++)
					{
						if (pixB[n+m*width]>threshold) 
						{
							above_thresh=true;
							break;
						}
						
					}
					if (above_thresh) break;
				}
				if (above_thresh) continue;
				if (Math.random()>sparsity) continue;
				for (int m=0; m<border_height; m++)
				{
					for (int n=0; n<width; n++)
					{
						double mm=n, nn=m;
						float tmp=amplitude*(float)Math.exp(-1.0*((mm-peak_x)*(mm-peak_x)+(nn-peak_y)*(nn-peak_y))/2.0/peak_width/peak_width);
						pixB[m*width+n]=pixB[m*width+n]+tmp;
						pixT[(m+height-border_height)*width+n]=pixT[(m+height-border_height)*width+n]+tmp;
					}
				}
			}
		}
		/*
		//Do the y adjacent pairs
		for (int i=0; i<(y_tiles-1); i++)
		{
			for (int j=0; j<(x_tiles); j++)
			{
				float [] pixB=(float [])(img.getStack().getProcessor(convert_array[j][i]+1).getPixels());
				float [] pixT=(float [])(img.getStack().getProcessor(convert_array[j][i+1]+1).getPixels());
				float [] rand_array=new float[width];
				int step_size=10;
				for (int k=0; k<width/step_size; k++) 
				{
					float current_rand=amplitude*(float)(Math.random());
					for (int m=0; m<step_size; m++)	rand_array[k*step_size+m]=current_rand;
				}
				int startT=(int)Math.floor(((float)height)*(1.0-overlap/100));
				int startB=height-startT;
				for (int k=0; k<width; k++)
				{
					for (int m=0; m<startB; m++)
					{
						pixB[m*width+k]=pixB[m*width+k]+rand_array[k];
					}
					for (int m=startT; m<height; m++)
					{
						pixT[m*width+k]=pixT[m*width+k]+rand_array[k];
					}
				}
			}
		}*/
		img.updateAndDraw();
	}
}
