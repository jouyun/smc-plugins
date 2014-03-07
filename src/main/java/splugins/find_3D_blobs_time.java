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

public class find_3D_blobs_time implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus img;
		img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), depth=img.getNSlices(), frames=img.getNFrames();
		int cur_channel=img.getChannel()-1, cur_frame=img.getFrame()-1;
		
		GenericDialog dlg=new GenericDialog("3D segmentation");
		dlg.addNumericField("Threshold", 500, 0);
		dlg.addNumericField("Minimum size", 80, 0);
		dlg.showDialog();
		float threshold=(float) dlg.getNextNumber();
		int minimum_size=(int)dlg.getNextNumber();
		
		float [] pix=new float[width*height*depth];
		ImagePlus new_img=NewImage.createByteImage("TempImg", width, height, depth*frames, NewImage.FILL_BLACK);
		for (int j=0; j<frames; j++)
		{
			for (int i=0; i<depth; i++)
			{
				float [] tmp=(float [])img.getStack().getProcessor(cur_channel+i*img.getNChannels()+j*img.getNChannels()*depth+1).getPixels();
				for (int k=0; k<width*height; k++) pix[i*width*height+k]=tmp[k];
			}
			find_blobs_3D blobber=new find_blobs_3D();
			ArrayList <ArrayList <int []>> rtn;
		
			rtn=blobber.runme(pix, img.getWidth(), img.getHeight(), depth, threshold);
			int idx=0;
			for (ListIterator jF=rtn.listIterator(); jF.hasNext();)
			{
				idx++;
				ArrayList <int []> current_list=(ArrayList <int []>)jF.next();
				//Throw out ones too small
				if (current_list.size()<minimum_size) continue;
				double [] averages=new double[img.getNChannels()];
				for (ListIterator iF=current_list.listIterator(); iF.hasNext();)
				{
					int [] current_point=(int [])iF.next();
					
					byte [] new_pix=(byte []) new_img.getStack().getProcessor(current_point[2]+1+j*depth).getPixels();
					new_pix[current_point[0]+current_point[1]*width]=(byte)255;
				}
			}
		}
		new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(1, depth,  frames);
		new_img.show();
		new_img.updateAndDraw();
	}

}
