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

public class find_colocalization_candidates_3D implements PlugIn {

	@Override
	public void run(String arg) {
		int lateral_half=20;
		int z_clip=5;
		
		ImagePlus img;
		img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), depth=img.getNSlices();
		int cur_channel=img.getChannel()-1, cur_frame=img.getFrame()-1;
		
		GenericDialog dlg=new GenericDialog("3D segmentation");
		dlg.addNumericField("Threshold", 1000, 0);
		dlg.addNumericField("Minimum size", 80, 0);
		dlg.addNumericField("Selection filter", 0.5, 2);
		dlg.showDialog();
		float threshold=(float) dlg.getNextNumber();
		int minimum_size=(int)dlg.getNextNumber();
		float selection_criteria=(float) dlg.getNextNumber();
		
		float [] pix=new float[width*height*depth];
		for (int i=0; i<depth; i++)
		{
			float [] tmp=(float [])img.getStack().getProcessor(cur_channel+i*img.getNChannels()+cur_frame*img.getNChannels()*depth+1).getPixels();
			for (int k=0; k<width*height; k++) pix[i*width*height+k]=tmp[k];
		}
		find_blobs_3D blobber=new find_blobs_3D();
		ArrayList <ArrayList <int []>> rtn;
		
		rtn=blobber.runme(pix, img.getWidth(), img.getHeight(), depth, threshold);
		
		
		int idx=0;
		int number_big_enough=0;
		ResultsTable the_table=ResultsTable.getResultsTable();
		the_table.reset();
		for (ListIterator jF=rtn.listIterator(); jF.hasNext();)
		{
			idx++;
			ArrayList <int []> current_list=(ArrayList <int []>)jF.next();
			//Throw out ones too small
			if (current_list.size()<minimum_size) continue;
			double [] averages=new double[img.getNChannels()];
			number_big_enough++;
			//Tabulate statistics
			for (ListIterator iF=current_list.listIterator(); iF.hasNext();)
			{
				int [] current_point=(int [])iF.next();
				for (int i=0; i<img.getNChannels(); i++)
				{
					float [] old_pix=(float []) img.getStack().getProcessor(i+(current_point[2])*img.getNChannels()+cur_frame*img.getNChannels()*depth+1).getPixels();
					averages[i]=averages[i]+old_pix[current_point[0]+current_point[1]*width];
				}
			}
			for (int i=0; i<img.getNChannels(); i++) 
			{
				averages[i]=averages[i]/(double)current_list.size();
			}
			
			if (img.getNChannels()>1)
			{
				if (averages[0]/averages[cur_channel]<selection_criteria) continue;
				//if (averages[0]<selection_criteria) continue;
			}
			//If meets criteria, log in results
			the_table.incrementCounter();
			the_table.addValue("Volume", (double)current_list.size());
			
			int [] tmp_pt=current_list.get(0);
			the_table.addValue("X", tmp_pt[0]);
			the_table.addValue("Y", tmp_pt[1]);
			the_table.addValue("Z", tmp_pt[2]);
			for (int i=0; i<img.getNChannels(); i++) 
			{
				the_table.addValue("Channel"+(i+1), averages[i]);
			}
		}
		ImagePlus new_img=NewImage.createFloatImage("TempImg", lateral_half*2, lateral_half*2, 2*z_clip*the_table.getCounter(), NewImage.FILL_BLACK);
		for (int i=0; i<the_table.getCounter(); i++)
		{
			int [] tmp_pt=new int[3];
			tmp_pt[0]=(int)the_table.getValue("X", i);
			tmp_pt[1]=(int)the_table.getValue("Y", i);
			tmp_pt[2]=(int)the_table.getValue("Z", i);
			for (int current_z=0; current_z<z_clip; current_z++)
			{
				if (current_z+tmp_pt[2]>=depth) continue;
				//Copy first channel
				float [] new_pix=(float []) new_img.getStack().getProcessor((i*z_clip+current_z)*2+1).getPixels();
				float [] old_pix=(float []) img.getStack().getProcessor(2*(tmp_pt[2]+current_z)+1).getPixels();
				for (int j=-lateral_half; j<lateral_half; j++)
				{
					for (int k=-lateral_half; k<lateral_half; k++)
					{
						new_pix[(k+lateral_half)*lateral_half*2+j+lateral_half]=old_pix[(tmp_pt[1]+k)*width+tmp_pt[0]+j];
					}
				}
				//Copy segmented channel
				new_pix=(float []) new_img.getStack().getProcessor((i*z_clip+current_z)*2+cur_channel+1).getPixels();
				old_pix=(float []) img.getStack().getProcessor(2*(tmp_pt[2]+current_z)+2).getPixels();
				for (int j=-lateral_half; j<lateral_half; j++)
				{
					for (int k=-lateral_half; k<lateral_half; k++)
					{
						new_pix[(k+lateral_half)*lateral_half*2+j+lateral_half]=old_pix[(tmp_pt[1]+k)*width+tmp_pt[0]+j];
					}
				}
			}
		}
		new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(2, z_clip, the_table.getCounter());
		the_table.show("Results");
		IJ.log(""+number_big_enough);
		new_img.show();
		new_img.updateAndDraw();
	}

}


