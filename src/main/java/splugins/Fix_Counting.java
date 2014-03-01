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

public class Fix_Counting implements PlugIn, MouseListener, KeyListener {
	ImagePlus imp;
	int width;
	int height;
	int slices, frames, channels, cur_slice, cur_frame, cur_channel;
	ImageCanvas canvas;
	ImageWindow win;
	public void run(String arg) {
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		win = imp.getWindow();
        win.addWindowListener(win);
        canvas = win.getCanvas();
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) 
	{
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		if (e.isControlDown())
		{
			canvas.removeMouseListener(this);
		    canvas.removeKeyListener(this);
			
		}
		else
		{
			if (e.isAltDown())
			{
				Point current_point=canvas.getCursorLoc();
				float pix [] = (float [])imp.getStack().getProcessor(cur_slice*channels+cur_frame*channels*slices+1).getPixels();
				for (int i=-2; i<3; i++)
				{
					for (int j=-2; j<3; j++)
					{
						pix[current_point.x-1+i+(current_point.y-1+j)*width]=0.0f;
					}
				}
				//IJ.log("X: "+current_point.x+" Y: "+current_point.y);
				imp.updateAndDraw();
			}
			else
			{
				if (e.isShiftDown())
				{
					Point current_point=canvas.getCursorLoc();
					float pix [] = (float [])imp.getStack().getProcessor(cur_slice*channels+cur_frame*channels*slices+1).getPixels();
					for (int i=-1; i<2; i++)
					{
						for (int j=-1; j<2; j++)
						{
							pix[current_point.x-1+i+(current_point.y-1+j)*width]=255.0f;
						}
					}
					//	IJ.log("X: "+current_point.x+" Y: "+current_point.y);
					imp.updateAndDraw();
				}
			}
		}
		
	}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		char rtn;
		rtn=e.getKeyChar();
		
	}
	
	public void mousePressed(MouseEvent e) 
	{
	}		
}
