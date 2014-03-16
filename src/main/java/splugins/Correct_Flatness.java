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

public class Correct_Flatness implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus img=WindowManager.getCurrentImage();
		
		GenericDialog gd = new GenericDialog("In situ process");
		gd.addNumericField("XCenter", 628.0, 1);
		gd.addNumericField("YCenter", 600.0, 1);
		gd.addNumericField("XWidth", 650, 1);
		gd.addNumericField("YWidth", 650, 1);
		gd.addNumericField("Background", 100, 1);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}				
		
		float xcenter=(float)gd.getNextNumber();
		float ycenter=(float)gd.getNextNumber();
		float xwidth=(float)gd.getNextNumber();
		float ywidth=(float)gd.getNextNumber();
		float background=(float)gd.getNextNumber();
		
		ImagePlus new_img=DoCorrect(img, xcenter, ycenter, xwidth, ywidth, background);
		new_img.show();
		new_img.updateAndDraw();
	}
	/******************************************************************************
	 * DoCorrect- For compensating for a non-flat illumination field, assumes Gaussian
	 * @param img ImagePlus object to be corrected (must be float)
	 * @param xcenter Center of Gaussian in X
	 * @param ycenter Center of Gaussian in Y
	 * @param xwidth Width of Gaussian in X direction (pixels)
	 * @param ywidth Width of Gaussian in Y direction (pixels)
	 * @param background The background of detector when totally dark
	 * @return New ImagePlus object containing corrected image
	 */
	public static ImagePlus DoCorrect(ImagePlus img, float xcenter, float ycenter, float xwidth, float ywidth, float background)
	{
		int width=img.getWidth(), height=img.getHeight(), slices=img.getStackSize();
		ImagePlus new_img=NewImage.createFloatImage("Result", width, height, slices, NewImage.FILL_BLACK);
		float gaussian[][]=new float[width][height];
		for (int i=0; i<width; i++)
		{
			for (int j=0; j<height; j++)
			{
				float x=(float)i;
				float y=(float)j;
				//gaussian[i][j]=(float) (1.0/2.0/3.141592653/ywidth/xwidth*(float)Math.exp(-1.0/2.0*((x-xcenter)*(x-xcenter)/xwidth/xwidth+(y-ycenter)*(y-ycenter)/ywidth/ywidth)));
				gaussian[i][j]=(float) Math.exp(-1.0/2.0*((x-xcenter)*(x-xcenter)/xwidth/xwidth+(y-ycenter)*(y-ycenter)/ywidth/ywidth));
			}
		}
		float [] new_pix, old_pix;
		for (int k=0; k<slices; k++)
		{
			new_pix=(float [])new_img.getStack().getProcessor(k+1).getPixels();
			old_pix=(float [])img.getStack().getProcessor(k+1).convertToFloat().getPixels();
			for (int i=0; i<width; i++)
			{
				for (int j=0; j<height; j++)
				{
					new_pix[i+j*width]=(old_pix[i+j*width]-background)/gaussian[i][j];
				}
			}
		}
		return new_img;
	}
}
