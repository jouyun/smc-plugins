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

public class find_blob_3D_tester implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus img;
		img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), depth=img.getNSlices();
		int cur_channel=img.getChannel()-1, cur_frame=img.getFrame()-1;
		
		GenericDialog dlg=new GenericDialog("3D segmentation");
		dlg.addNumericField("Threshold", 500, 0);
		dlg.addNumericField("Minimum size", 80, 0);
		dlg.addNumericField("Selection filter", 200, 2);
		dlg.addCheckbox("Ratiometric:  ", true);
		dlg.showDialog();
		float threshold=(float) dlg.getNextNumber();
		int minimum_size=(int)dlg.getNextNumber();
		float selection_criteria=(float) dlg.getNextNumber();
		boolean ratiometric=dlg.getNextBoolean();
		
		float [] pix=new float[width*height*depth];
		for (int i=0; i<depth; i++)
		{
			float [] tmp=(float [])img.getStack().getProcessor(cur_channel+i*img.getNChannels()+cur_frame*img.getNChannels()*depth+1).getPixels();
			for (int k=0; k<width*height; k++) pix[i*width*height+k]=tmp[k];
		}
		find_blobs_3D blobber=new find_blobs_3D();
		ArrayList <ArrayList <int []>> rtn;
		
		rtn=blobber.runme(pix, img.getWidth(), img.getHeight(), depth, threshold);
		
		ImagePlus new_img=NewImage.createByteImage("TempImg", width, height, depth, NewImage.FILL_BLACK);
		int idx=0;
		int number_big_enough=0;
		ResultsTable the_table=ResultsTable.getResultsTable();
		the_table.reset();
		//float [] minmax=Histogram_Normalize_Percentile.Find_Percentiles(img, 100, 99, 10, 1, 1);
		//IJ.log("minmax: "+minmax[0]+","+minmax[1]+","+minmax[2]);
		Utility3D my3D=new Utility3D();
		ArrayList <ArrayList <int []>> expanded_rtn=(ArrayList <ArrayList <int []>>) rtn.clone();
		short [] imgarray=my3D.blobarray_to_imgarray(expanded_rtn, width, height, depth);
		my3D.dilate_no_merge(imgarray, rtn, width, height, depth, 2);
		int marked_blobs=0;
		for (ListIterator jF=rtn.listIterator(); jF.hasNext();)
		{
			idx++;
			ArrayList <int []> current_list=(ArrayList <int []>)jF.next();
			
			//Throw out ones too small
			if (current_list.size()<minimum_size) continue;
			
			/*Experimental*/
			//Do neighborhood investigation
			ArrayList <int []> expanded_list=expanded_rtn.get(idx-1);
			
			//Tabulate statistics for expanded blob
			double [] expanded_averages=new double[img.getNChannels()];
			for (ListIterator iF=expanded_list.listIterator(); iF.hasNext();)
			{
				int [] current_point=(int [])iF.next();
				for (int i=0; i<img.getNChannels(); i++)
				{
					float [] old_pix=(float []) img.getStack().getProcessor(i+(current_point[2])*img.getNChannels()+cur_frame*img.getNChannels()*depth+1).getPixels();
					expanded_averages[i]=expanded_averages[i]+old_pix[current_point[0]+current_point[1]*width];
				}
			}

			for (int i=0; i<img.getNChannels(); i++) 
			{
				expanded_averages[i]=expanded_averages[i]/(double)expanded_list.size();
			}
			/*End experimental*/
			
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
			//If meets criteria, log in results
			
			if (img.getNChannels()>1)
			{
				if (ratiometric) 
				{
					if (averages[0]/averages[cur_channel]<selection_criteria) continue;
				}
				else
				{
					if (averages[0]<selection_criteria) continue;
				}
				
				//if (expanded_averages[0]<(minmax[1]-minmax[0])*selection_criteria+minmax[0]||expanded_averages[0]>(minmax[1]-minmax[0])*0.4+minmax[0]) continue;
				//if (expanded_averages[0]<(minmax[1]-minmax[0])*selection_criteria+minmax[0]) continue;
				//if (expanded_averages[0]<minmax[2]*selection_criteria+minmax[0]) continue;
			}
			the_table.incrementCounter();
			the_table.addValue("Volume", (double)current_list.size());
			
			int [] tmp_pt=current_list.get(0);
			the_table.addValue("X", tmp_pt[0]);
			the_table.addValue("Y", tmp_pt[1]);
			the_table.addValue("Z", tmp_pt[2]);
			
			for (int i=0; i<img.getNChannels(); i++) 
			{
				the_table.addValue("Channel"+(i+1), expanded_averages[i]);
			}
			marked_blobs++;
			for (ListIterator iF=current_list.listIterator(); iF.hasNext();)
			{
				int [] current_point=(int [])iF.next();
				
				byte [] new_pix=(byte []) new_img.getStack().getProcessor(current_point[2]+1).getPixels();
				new_pix[current_point[0]+current_point[1]*width]=(byte)255;
			}			
		}
		
		/*byte [] byte_arr=Dilate3D.make_3D_byte(new_img, 0, 0);
		byte [] new_byte_arr=Dilate3D.dilate_me(byte_arr, width, height, depth);
		ImagePlus new_img2=Dilate3D.make_3D_ImagePlusByte(new_byte_arr, width, height, depth);
		new_img2.show(); 
		new_img2.updateAndDraw();*/
		the_table.incrementCounter();
		the_table.addValue("Total", number_big_enough);
		the_table.show("Results");
		//IJ.log(""+marked_blobs+"/"+number_big_enough);
		new_img.show();
		new_img.updateAndDraw();
		
	}

}
