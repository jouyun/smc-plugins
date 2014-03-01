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

public class Pick_Cells_Manually implements PlugInFilter, MouseListener, KeyListener, Measurements
{

	ImagePlus imp;
	float[] source_spectrum;
	float[][] source_pixels ;
	float[][][] img_array;
	boolean[][] checked_array;
	byte[] output_img;
	int size;
	int x_dim, y_dim;
	ImageCanvas canvas;
	ImageWindow win;
	Frame front_frame;
	boolean image_created;
	boolean cleared_bad;
	
	int grow_from_x;
	int grow_from_y;
	float cutoff;
	
	ImagePlus new_img;
	ImageStack new_stack;
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_32;
	}
	public void run(ImageProcessor ip) 
	{
		front_frame=WindowManager.getFrontWindow();
		win = imp.getWindow();
        win.addWindowListener(win);
        canvas = win.getCanvas();
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
        image_created=false;
        cleared_bad=false;
		
		
		
		//ImageProcessor r=new_img.getProcessor();
		//r.setMinAndMax(990.0, 998.0);
		
		
	}
	
	void InitializeArrays()
	{
		for (int i=0; i<x_dim; i++)
		{
			for (int j=0; j<y_dim; j++)
			{
				checked_array[i][j]=false;
			}
		}
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
		ImagePlus target_img;
		target_img=imp;
		x_dim=target_img.getWidth();
		y_dim=target_img.getHeight();
		ImageStack stack = target_img.getStack();
		ImageProcessor p = stack.getProcessor(1);
		
        getZAxisProfile();
		
		img_array=new float[x_dim][y_dim][size];
		checked_array=new boolean[x_dim][y_dim];
		copy_data(target_img);
		if (!image_created) 
		{
			new_img=NewImage.createByteImage("Img", x_dim, y_dim, 2, NewImage.FILL_BLACK);
			new_stack=new_img.getStack();
		}
		else 
		{
			/*ImageStack stk=new_img.getStack();
			ByteProcessor new_slice;
			new_slice=new ByteProcessor(x_dim, y_dim);
			stk.addSlice(new_slice);	
			new_img.updateAndDraw();*/
		}
		InitializeArrays();
		process_data( );
		
		
		
		if (!image_created) new_img.show();
		new_img.updateAndDraw();
		WindowManager.setCurrentWindow(win);
		WindowManager.setWindow(front_frame);
		WindowManager.toFront(front_frame);
		image_created=true;
	}
	
	public void mousePressed(MouseEvent e) 
	{
		//Gets the Z values through a single point at (x,y).
		/*int x, y;
		x=e.getX();
		y=e.getY();
		ImagePlus target_img;
		target_img=imp;
		x_dim=target_img.getWidth();
		y_dim=target_img.getHeight();
		ImageStack stack = target_img.getStack();
		
        getZAxisProfile(x,y);
		
		img_array=new float[x_dim][y_dim][size];
		checked_array=new boolean[x_dim][y_dim];
		copy_data(target_img);
		new_img=NewImage.createByteImage("Img", x_dim, y_dim, 1, NewImage.FILL_BLACK);
		InitializeArrays();
		process_data( );
		
		
		new_img.show();
		new_img.updateAndDraw();
		WindowManager.setCurrentWindow(win);
		WindowManager.setWindow(front_frame);
		WindowManager.toFront(front_frame);*/
	}		   
	
	void getZAxisProfile() {
        Roi roi = imp.getRoi();
        if(roi==null)
             return ;
        ImageStack stack = imp.getStack();
        size = stack.getSize(); 
        source_spectrum = new float[size];
        Rectangle r = roi.getBoundingRect();
        grow_from_x=(int)Math.floor(r.getCenterX());
        grow_from_y=(int)Math.floor(r.getCenterY());
        Calibration cal = imp.getCalibration();
        
        //ROI with Area > 0
        for (int i=1; i<=size; i++) {
            ImageProcessor ip = stack.getProcessor(i);
            ip.setRoi(roi);
            ImageStatistics stats = ImageStatistics.getStatistics(ip, MEAN, cal);
            source_spectrum[i-1] = (float)stats.mean;
			//IJ.log(""+source_spectrum[i-1] +"\n");
        }
    }
	
	void copy_data(ImagePlus target_img)
	{
		ImageStack stack = target_img.getStack();
		int num_pixels;
		num_pixels=x_dim*y_dim;
		source_pixels=new float[size][];
        for (int i=0; i<size; i++) {
			ImageProcessor tp = stack.getProcessor(i+1);
			source_pixels[i]= (float[])tp.getPixels();
			for (int j=0; j<x_dim; j++) {
				for (int k=0; k<y_dim; k++)
				{				
					img_array[j][k][i]=source_pixels[i][k*x_dim+j];
				}
			}
		}
	}
	
	void process_data()
	{
		/*ImageStack stack = new_img.getStack();
        int new_size= stack.getSize();
		ImageProcessor tp = stack.getProcessor(new_size);
		output_img = (byte[])tp.getPixels();*/
		output_img=new byte[x_dim*y_dim];
		
		boolean done=false;
		output_img[grow_from_y*x_dim+grow_from_x]=(byte)255;
		cutoff=31.80f;  //31.85 some holes, not fully growing
		cutoff=0.99375f;  //31.85 some holes, not fully growing
		cutoff=0.95f;
		int ctr;
		while (!done)
		{
			ctr=0;
			for (int i=0; i<x_dim; i++)
			{
				for (int j=0; j<y_dim; j++)
				{
					if (output_img[j*x_dim+i]==(byte)0) continue;
					if (check_neighbor(i+1, j)) ctr++;
					if (check_neighbor(i-1, j)) ctr++;
					if (check_neighbor(i, j+1)) ctr++;
					if (check_neighbor(i, j-1)) ctr++;
				}
			}
			if (ctr==0) done=true;
		}
		new_stack.addSlice("" ,output_img);
		if (new_stack.getSize()==4&&!cleared_bad)
		{
			//new_stack.deleteSlice(2);
			//new_stack.deleteSlice(1);
			cleared_bad=true;
		}
		imp.updateAndDraw();
	}
	
	boolean check_neighbor(int x, int y)
	{
		if (x<0||x==x_dim||y<0||y==y_dim) return false;
		if (checked_array[x][y]) return false;
		checked_array[x][y]=true;
		float tmp=0;
		for (int m=0; m<size; m++)
		{
			tmp=tmp+img_array[x][y][m]*source_spectrum[m];
		}
		tmp=tmp/(float)size;
		if (tmp<cutoff) return false;
		output_img[y*x_dim+x]=(byte)255;
		ImageStack stack = imp.getStack();
		for (int i=0; i<size; i++) 
		{
			ImageProcessor tp = stack.getProcessor(i+1);
			source_pixels[i][x+y*x_dim]=0;
		}
		return true;
	}
	
}
