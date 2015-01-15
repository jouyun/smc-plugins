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

//This only works on byte images at the moment, it grows from the seed of the currently selected ROI when run OR
//lets you draw an ROI after run but only for pixels above the threshold, then press 'c' to get all pixels attached to that pixel

public class seeded_region_grow_3D_thresholded implements PlugIn,MouseListener, KeyListener, Measurements {

	int width;
	int height;
	int slices;
	int cur_slice;
	int frames;
	int cur_frame;
	int cur_channel;
	int channels;
	int threshold;
	//byte [][][] whole_img;
	byte [][] whole_img_new;
	byte [] output_img;
	ImagePlus new_img;
	ImagePlus imp;
	ImageCanvas canvas;
	ImageWindow win;
	class MyIntPoint
	{
		int x;
		int y;
		int z;
		MyIntPoint(int a, int b, int c)
		{
			x=a;
			y=b;
			z=c;
		}
	}
	ArrayList <MyIntPoint> point_list=new ArrayList <MyIntPoint>();
	ArrayList <MyIntPoint> tmp_point_list=new ArrayList <MyIntPoint>();
	
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
		//If there is already an roi selected, use it and return
		GenericDialog gd=new GenericDialog("Seeded threshold");
		gd.addNumericField("Threshold", 30, 0);
		gd.showDialog();
		threshold=(int)gd.getNextNumber();
		Roi roi = imp.getRoi();
        if(roi!=null)
        {
        	Rectangle r = roi.getBounds();
            int grow_from_x=(int)Math.floor(r.getCenterX());
            int grow_from_y=(int)Math.floor(r.getCenterY());
            //whole_img=new byte[width][height][slices];
            IJ.log("Starting copy");
            whole_img_new=new byte[slices][width*height];
            for (int i=0; i<slices; i++) {
            	byte [] slice_pixels=(byte [])imp.getStack().getProcessor(cur_frame*slices*channels+i*channels+cur_channel+1).convertToByte(false).getPixels();
            	System.arraycopy(slice_pixels, 0, whole_img_new[i], 0, width*height);
    		}
            
            IJ.log("Finishing copy");
            /*
            for (int i=0; i<slices; i++) {
            	byte [] slice_pixels=(byte [])imp.getStack().getProcessor(cur_frame*slices*channels+i*channels+cur_channel+1).convertToByte(false).getPixels();
    			for (int j=0; j<width; j++) {
    				for (int k=0; k<height; k++)
    				{				
    					whole_img[j][k][i]=slice_pixels[k*width+j];
    				}
    			}
    		}*/
            IJ.log("Creating image");
            new_img=NewImage.createByteImage("Img", width, height, slices, NewImage.FILL_BLACK);
            output_img=(byte []) new_img.getStack().getProcessor(cur_slice+1).getPixels();
            output_img[grow_from_y*width+grow_from_x]=(byte)255;
            MyIntPoint curpt=new MyIntPoint(grow_from_x, grow_from_y,cur_slice);
            point_list.add(curpt);
    		int ctr;
    		boolean done=false;
    		IJ.log("Starting loop");
    		while (!done)
    		{
    			ctr=0;
    			for (ListIterator jF=point_list.listIterator();jF.hasNext();)
    			{
    				curpt=(MyIntPoint)jF.next();
    				if (check_neighbor(curpt.x+1, curpt.y,curpt.z)) ctr++;
    				if (check_neighbor(curpt.x-1, curpt.y, curpt.z)) ctr++;
    				if (check_neighbor(curpt.x, curpt.y+1, curpt.z)) ctr++;
    				if (check_neighbor(curpt.x, curpt.y-1, curpt.z)) ctr++;
    				if (check_neighbor(curpt.x,curpt.y,curpt.z+1)) ctr++;
    				if (check_neighbor(curpt.x,curpt.y,curpt.z-1)) ctr++;
    			}
    			point_list=tmp_point_list;
    			tmp_point_list=new ArrayList <MyIntPoint>();
    			IJ.log("How many in queue: "+point_list.size());
    			/*point_list.clear();
    			for (ListIterator jF=tmp_point_list.listIterator();jF.hasNext();)
    			{
    				curpt=(MyIntPoint)jF.next();
    				point_list.add(curpt);
    			}
    			tmp_point_list.clear();*/
    			if (ctr==0) done=true;
    		}
            new_img.show();
            new_img.updateAndDraw();
            return;        	
        }
        	
		
		
		
		//Otherwise, go into interactive mode
		win = imp.getWindow();
        win.addWindowListener(win);
        canvas = win.getCanvas();
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
        
		
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	
	public void keyPressed(KeyEvent e) {
		char rtn;
		rtn=e.getKeyChar();
		if (rtn!='c'&&rtn!='q') return;
		if (rtn=='q')
		{
			canvas.removeMouseListener(this);
		    canvas.removeKeyListener(this);
		}
		Roi roi = imp.getRoi();
        if(roi==null)
             return ;
        Rectangle r = roi.getBounds();
        int grow_from_x=(int)Math.floor(r.getCenterX());
        int grow_from_y=(int)Math.floor(r.getCenterY());
        IJ.log("Starting copy");
        whole_img_new=new byte[slices][width*height];
        for (int i=0; i<slices; i++) {
        	byte [] slice_pixels=(byte [])imp.getStack().getProcessor(cur_frame*slices*channels+i*channels+cur_channel+1).convertToByte(false).getPixels();
        	System.arraycopy(slice_pixels, 0, whole_img_new[i], 0, width*height);
		}
        
        IJ.log("Finishing copy");
        /*
        whole_img=new byte[width][height][slices];
        for (int i=0; i<slices; i++) {
        	byte [] slice_pixels=(byte [])imp.getStack().getProcessor(cur_frame*slices*channels+i*channels+cur_channel+1).convertToByte(false).getPixels();
			for (int j=0; j<width; j++) {
				for (int k=0; k<height; k++)
				{				
					whole_img[j][k][i]=slice_pixels[k*width+j];
				}
			}
		}*/
        
        new_img=NewImage.createByteImage("Img", width, height, slices, NewImage.FILL_BLACK);
        output_img=(byte []) new_img.getStack().getProcessor(cur_slice+1).getPixels();
        output_img[grow_from_y*width+grow_from_x]=(byte)255;
        MyIntPoint curpt=new MyIntPoint(grow_from_x, grow_from_y,cur_slice);
        point_list.add(curpt);
		int ctr;
		boolean done=false;
		while (!done)
		{
			ctr=0;
			for (ListIterator jF=point_list.listIterator();jF.hasNext();)
			{
				curpt=(MyIntPoint)jF.next();
				if (check_neighbor(curpt.x+1, curpt.y,curpt.z)) ctr++;
				if (check_neighbor(curpt.x-1, curpt.y, curpt.z)) ctr++;
				if (check_neighbor(curpt.x, curpt.y+1, curpt.z)) ctr++;
				if (check_neighbor(curpt.x, curpt.y-1, curpt.z)) ctr++;
				if (check_neighbor(curpt.x,curpt.y,curpt.z+1)) ctr++;
				if (check_neighbor(curpt.x,curpt.y,curpt.z-1)) ctr++;
			}
			point_list.clear();
			for (ListIterator jF=tmp_point_list.listIterator();jF.hasNext();)
			{
				curpt=(MyIntPoint)jF.next();
				point_list.add(curpt);
			}
			tmp_point_list.clear();
			if (ctr==0) done=true;
		}
        new_img.show();
        new_img.updateAndDraw();
	}
	
	public void mousePressed(MouseEvent e) 
	{
	}		
	
	boolean check_neighbor(int x, int y, int z)
	{
		if (z<0||z==slices) return false;
		if (x<0||x==width||y<0||y==height) return false;
		int yy=y*width;
		byte [] current_pixels=(byte []) new_img.getStack().getProcessor(z+1).getPixels();
		if (current_pixels[yy+x]!=0) return false;
		if ((whole_img_new[z][x+yy]&0xff)<threshold) return false;
		current_pixels[y*width+x]=(byte)whole_img_new[z][x+yy];
		tmp_point_list.add(new MyIntPoint(x,y,z));
		return true;
	}
}
