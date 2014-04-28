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

public class Max_Project_With_Reference_AtFront implements PlugIn{
	public void run(String arg)
	{
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), stk_size=img.getStackSize();
		GenericDialog dlg=new GenericDialog("Max Project");
		dlg.addNumericField("Channels (including reference)", 2, 0);
		dlg.addNumericField("Frames", 1, 0);
		//dlg.addNumericField("Frame With Reference", 5, 0);
		dlg.showDialog();
		int channels=(int) dlg.getNextNumber();
		int frames=(int) dlg.getNextNumber();
		//int reference=(int) dlg.getNextNumber();
		//Will always assume z, then channels, then frames
		ImagePlus new_img=NewImage.createShortImage("Img", width, height, channels*frames, NewImage.FILL_BLACK);
		int images_per_frame=stk_size/frames;
		int slices=(images_per_frame-1)/(channels-1);
		for (int i=0; i<frames; i++)
		{
			short[] pix=(short []) img.getStack().getProcessor(i*images_per_frame+1).getPixels();
			short[] new_pix=(short []) new_img.getStack().getProcessor(i*channels+1).getPixels();
			for (int j=0; j<width*height; j++)
			{
				new_pix[j]=pix[j];
			}
			for (int k=0; k<(channels-1); k++)
			{
				short [] max=new short[width*height];
				for (int m=0; m<slices; m++)
				{
					short [] tmp_pix=(short []) img.getStack().getProcessor(i*images_per_frame+slices*k+m+2).getPixels();
					for (int n=0; n<width*height; n++)
					{
						if (max[n]<tmp_pix[n]) max[n]=tmp_pix[n];
					}
				}
				new_pix=(short []) new_img.getStack().getProcessor(i*channels+k+1+1).getPixels();
				for (int n=0; n<width*height; n++) new_pix[n]=max[n];
			}
		}
		new_img.show();
		new_img.updateAndDraw();
	}

}
