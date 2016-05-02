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
import ij.plugin.frame.RoiManager;
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

public class find_blobs_3D_quantify implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus img;
		img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), depth=img.getNSlices();
		int cur_channel=img.getChannel()-1, cur_frame=img.getFrame()-1;
		
		GenericDialog dlg=new GenericDialog("3D segmentation");
		dlg.addNumericField("Threshold", 500, 0);
		dlg.addNumericField("Minimum size", 500, 0);
		dlg.addNumericField("Max size: ", 30000, 0);
		dlg.addNumericField("Channel to segment on: ", 1, 0);
		dlg.addNumericField("Filter ratio less than:", 1, 1);
		dlg.addCheckbox("Apply filter? ", true);
		dlg.addCheckbox("Make ROIs?" , true);
		dlg.showDialog();
		float threshold=(float) dlg.getNextNumber();
		int minimum_size=(int)dlg.getNextNumber();
		int maximum_size=(int)dlg.getNextNumber();
		int channel_to_segment=(int)dlg.getNextNumber();
		float ratio=(float)dlg.getNextNumber();
		boolean apply_filter=dlg.getNextBoolean();
		boolean make_ROIs=dlg.getNextBoolean();
		
		float [] pix=new float[width*height*depth];
		for (int i=0; i<depth; i++)
		{
			float [] tmp=(float [])img.getStack().getProcessor(cur_channel+i*img.getNChannels()+cur_frame*img.getNChannels()*depth+channel_to_segment).getPixels();
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

		Utility3D my3D=new Utility3D();
		short [] imgarray=my3D.blobarray_to_imgarray(rtn, width, height, depth);
		int marked_blobs=0;
		for (ListIterator jF=rtn.listIterator(); jF.hasNext();)
		{
			idx++;
			ArrayList <int []> current_list=(ArrayList <int []>)jF.next();
			
			//Throw out ones too small or too big
			if (current_list.size()<minimum_size) continue;
			if (current_list.size()>maximum_size) continue;
						
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
			
			if (apply_filter&&averages[0]<ratio*averages[3]) 
			{
				continue;
			}
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
			marked_blobs++;
			float average_x=0, average_y=0, average_z=0;
			for (ListIterator iF=current_list.listIterator(); iF.hasNext();)
			{
				int [] current_point=(int [])iF.next();
				
				byte [] new_pix=(byte []) new_img.getStack().getProcessor(current_point[2]+1).getPixels();
				new_pix[current_point[0]+current_point[1]*width]=(byte)marked_blobs;
				
				average_x+=current_point[0];
				average_y+=current_point[1];
				average_z+=current_point[2];
			}	
			
			if (make_ROIs)
			{
				//Roi my_roi=new Roi(average_x/current_list.size(),average_y/current_list.size(),1,1);
				PointRoi my_roi=new PointRoi(average_x/current_list.size(),average_y/current_list.size());
				WindowManager.setTempCurrentImage(img);
				img.setPosition(channel_to_segment, (int)Math.floor(average_z/current_list.size()), 1);
				RoiManager manager=RoiManager.getInstance();
				
				if (manager==null) manager=new RoiManager();
				
				manager.addRoi(my_roi);
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
