package splugins;
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

public class Rolling_Time_Subtract implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus img=WindowManager.getCurrentImage();
		GenericDialog gd = new GenericDialog("Title");
		
		gd.addNumericField("How big of a window:  ", 5, 0);
		gd.addCheckbox("Truncate negatives? ", false);
		gd.showDialog();
		
		int window =(int)gd.getNextNumber(); 
		boolean truncate=gd.getNextBoolean();
		ImagePlus new_img;
		if (img.getNChannels()>1) 
		{
			new_img=DoStackSubtract(img, window);
		}
		else new_img=DoSubtract(img, window, truncate);
		
		new_img.updateAndDraw();
		new_img.show();
	}
	public static ImagePlus DoSubtract(ImagePlus img, int window, boolean truncate)
	{
		ImagePlus my_img=DoSubtract(img, window);
		int width=img.getWidth(), height=img.getHeight(), slices=img.getStackSize();
		//Now subtract old from new and put in new_img
		for (int i=window; i<slices; i++)
		{
			float [] new_pix=(float [])my_img.getStack().getPixels(i);
			for (int k=0; k<width*height; k++) if (truncate&&new_pix[k]<0) new_pix[k]=0;
		}
		return my_img;
	}
	/**********************************************************************
	 * DoSubtract- Subtracts a box car average of time series
	 * @param img Must be float
	 * @param window 
	 * @return ImagePlus of subtraction, first "window" frames will be unaltered
	 */
	public static ImagePlus DoSubtract(ImagePlus img, int window)
	{
		int width=img.getWidth(), height=img.getHeight(), slices=img.getStackSize();
		
		ImagePlus new_img=NewImage.createFloatImage("Result", width, height, slices, NewImage.FILL_BLACK);
		//Find average, put in new_img
		for (int i=window; i<slices; i++)
		{
			float [] new_pix=(float [])new_img.getStack().getPixels(i);
			for (int j=0; j<window; j++)
			{
				float [] old_pix=(float [])img.getStack().getPixels(i-j);
				for (int k=0; k<width*height; k++) new_pix[k]= (new_pix[k]+(float)old_pix[k]);
			}
			for (int k=0; k<width*height; k++) new_pix[k]=(new_pix[k]/(float)window);
		}
		//Now subtract old from new and put in new_img
		for (int i=window; i<slices; i++)
		{
			float [] new_pix=(float [])new_img.getStack().getPixels(i);
			float [] old_pix=(float [])img.getStack().getPixels(i);
			for (int k=0; k<width*height; k++) new_pix[k]= ((float)old_pix[k]-new_pix[k]);
		}
		return new_img;
	}
	
	public static ImagePlus DoStackSubtract(ImagePlus img, int window)
	{
		int channels=img.getNChannels(), slices=img.getNSlices(), frames=img.getNFrames(), channel=img.getChannel()-1, frame=img.getFrame()-1, slice=img.getSlice()-1;
		int width=img.getWidth();
		int height=img.getHeight();
		ImagePlus new_img=NewImage.createFloatImage("Result", width, height, frames*channels*slices, NewImage.FILL_BLACK);
		for (int s=0; s<slices; s++)
		{
			for (int c=0; c<channels; c++)
			{
				for (int f=window; f<frames; f++)
				{
					float [] new_pix=(float [])new_img.getStack().getPixels(1+c+s*channels+f*channels*slices);
					for (int j=0; j<window; j++)
					{
						float [] old_pix=(float [])img.getStack().getPixels(1+c+s*channels+(f-j)*channels*slices);
						for (int k=0; k<width*height; k++) new_pix[k]=new_pix[k]+(float)old_pix[k];
					}
					for (int k=0; k<width*height; k++) new_pix[k]=(new_pix[k]/(float)window);
				}
				for (int f=window; f<slices; f++)
				{
					float [] new_pix=(float [])new_img.getStack().getPixels(1+c+s*channels+f*channels*slices);
					float [] old_pix=(float [])img.getStack().getPixels(1+c+s*channels+f*channels*slices);
					for (int k=0; k<width*height; k++) new_pix[k]= ((float)old_pix[k]-new_pix[k]);
				}
			}
		}
		return new_img;
	}

}
