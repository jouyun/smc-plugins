package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
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
import java.awt.image.IndexColorModel;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;

class point { 
	int src; 
	int tar;
	point() { src=0; tar=0; };
	point(int a, int b)
	{
		src=a;
		tar=b;
	}
	void set(int a, int b)
	{
		src=a;
		tar=b;
	}
};

public class Dot_Source_Target_Masks implements PlugInFilter, Measurements{
	ImagePlus imp;
	float[][] source_spectrum;
	float[][][] img_array;
	float[][] output_img;
	int size;
	int x_dim, y_dim;
	ImagePlus new_img;
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_32;
	}
	public void run(ImageProcessor ip) 
	{
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		//IJ.log("Good ones:  "+admissibleImageList.length+"\n");		
		if (admissibleImageList.length == 0) return;
		//IJ.log(""+admissibleImageList.length);
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Do Dot Product");
		gd.addChoice("Source image:", sourceNames, admissibleImageList[0].getTitle());
		gd.addChoice("Target image:", sourceNames, admissibleImageList[0].getTitle());
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		int sourceIndex=gd.getNextChoiceIndex();
		int targetIndex=gd.getNextChoiceIndex();
		//IJ.log("First:  " + admissibleImageList[tmpIndex].getTitle() + "\n");
		
		ImagePlus target_img, source_img;
		source_img=admissibleImageList[sourceIndex];
		target_img=admissibleImageList[targetIndex];
		x_dim=target_img.getWidth();
		y_dim=target_img.getHeight();
		ImageStack stack = imp.getStack();
        size = stack.getSize();
		ImageStack target_stack = target_img.getStack();
		ImageStack source_stack = source_img.getStack();
		new_img=NewImage.createFloatImage("NewImg", x_dim, y_dim, source_stack.getSize(), NewImage.FILL_BLACK);
		ImageStack new_stack=new_img.getStack();
		img_array=new float[x_dim][y_dim][size];
		source_spectrum=new float[source_stack.getSize()][size];
		float[][] target_spectrum=new float[target_stack.getSize()][size];
		copy_data(imp);
		
		for (int i=0; i<source_stack.getSize(); i++)
		{
			IJ.showProgress(i, source_stack.getSize());
			ImageProcessor newproc=new_stack.getProcessor(i+1);
			float [] new_pixels=(float [])(newproc.getPixels());
			ImageProcessor p = source_stack.getProcessor(i+1);
			byte[] source_pixels=(byte []) p.getPixels();
			int counter=0;
			for (int j=0; j<size; j++) source_spectrum[i][j]=0.0f;
			for (int j=0; j<x_dim; j++)
			{
				for (int k=0; k<y_dim; k++)
				{
					if (source_pixels[k*x_dim+j]<0)
					{
						for (int m=0; m<size; m++) source_spectrum[i][m]=source_spectrum[i][m]+img_array[j][k][m];
						counter++;
					}
				}
			}
			for (int j=0; j<size; j++) source_spectrum[i][j]=source_spectrum[i][j]/(float)counter;
			norm_spectrum(source_spectrum[i]);
			/*for (int j=0; j<x_dim; j++)
			{
				for (int k=0; k<y_dim; k++)
				{
					float tmp=0.0f;
					for (int m=0; m<size; m++)
					{
						tmp=tmp+source_spectrum[i][m]*img_array[j][k][m];
					}
					new_pixels[k*x_dim+j]=tmp*1000f/32f;
				}
			}*/
		}
		new_img.show();
		new_img.updateAndDraw();
		ImageProcessor r=new_img.getProcessor();
		r.setMinAndMax(990.0, 998.0);
		
		for (int i=0; i<target_stack.getSize(); i++)
		{
			ImageProcessor newproc=new_stack.getProcessor(i+1);
			float [] new_pixels=(float [])(newproc.getPixels());
			ImageProcessor p = target_stack.getProcessor(i+1);
			byte[] target_pixels=(byte []) p.getPixels();
			int counter=0;
			for (int j=0; j<size; j++) target_spectrum[i][j]=0.0f;
			for (int j=0; j<x_dim; j++)
			{
				for (int k=0; k<y_dim; k++)
				{
					if (target_pixels[k*x_dim+j]<0)
					{
						for (int m=0; m<size; m++) target_spectrum[i][m]=target_spectrum[i][m]+img_array[j][k][m];
						counter++;
					}
				}
			}
			for (int j=0; j<size; j++) target_spectrum[i][j]=target_spectrum[i][j]/(float)counter;
			norm_spectrum(target_spectrum[i]);
		}
		
		float[][] dot_prod_array= new float[source_stack.getSize()][target_stack.getSize()];
		
		for (int i=0; i<source_stack.getSize(); i++)
		{
			for (int j=0; j<target_stack.getSize(); j++)
			{
				dot_prod_array[i][j]=0.0f;
				for (int m=0; m<size; m++) dot_prod_array[i][j]=dot_prod_array[i][j]+source_spectrum[i][m]*target_spectrum[j][m];
			}
		}
		
		byte[][] source_ranks=new byte[target_stack.getSize()][3];
float cutoff=31.85f;		
		for (int i=0; i<target_stack.getSize(); i++)
		{
			float best=0.0f;
			int best_idx=-1;
			for (int j=0; j<source_stack.getSize(); j++)
			{
				if (!(dot_prod_array[j][i]>best)||dot_prod_array[j][i]<cutoff) continue;
				best=dot_prod_array[j][i];
				best_idx=j+1;
			}
			if (best_idx>-1) source_ranks[i][0]=(byte) best_idx;
		}
		
		
		
		ArrayList<point> pair_list;
		pair_list=new ArrayList<point>();
		point tmp;
		tmp=new point(0,0);
		tmp.set(0,0);
		
		for (int i=0; i<target_stack.getSize(); i++)
		{
			if (source_ranks[i][0]>0) 
			{
				tmp=new point(0,0);
				tmp.set(source_ranks[i][0], i+1);
				pair_list.add(tmp);
			}
		}
		for (int i=0; i<pair_list.size(); i++) 
		{
			tmp=pair_list.get(i);
			IJ.log("Target " + tmp.tar+ " goes best with source " + tmp.src+"\n");
		}
		
		ImagePlus mask_img=NewImage.createByteImage("Masked Image", x_dim, y_dim, pair_list.size(), NewImage.FILL_BLACK);
		ImageStack mask_stack=mask_img.getStack();
		byte[] mask_pix;
		byte[] src_pix;
		byte[] tar_pix;
		for (int i=0; i<pair_list.size(); i++) 
		{
			tmp=pair_list.get(i);
			ImageProcessor mask_ip=mask_stack.getProcessor(i+1);
			ImageProcessor src_ip=source_stack.getProcessor(tmp.src);
			ImageProcessor tar_ip=target_stack.getProcessor(tmp.tar);
			mask_pix=(byte [])mask_ip.getPixels();
			src_pix=(byte [])src_ip.getPixels();
			tar_pix=(byte [])tar_ip.getPixels();
			
			for (int j=0; j<x_dim*y_dim; j++)
			{
				if (src_pix[j]<0||tar_pix[j]<0) mask_pix[j]=1;
			}
		}
		mask_img.show();
		mask_img.updateAndDraw();
		
		
		
		
		
		
	}
	public float find_mean(float[] inp)
	{
		float rtn=0;
		for (int i=0; i<inp.length; i++)
		{
			rtn=rtn+inp[i];
		}
		return rtn/(float)(inp.length);
	}
	public float find_std(float[] inp, float avg)
	{
		float rtn=0, tmp;
		for (int i=0; i<inp.length; i++)
		{
			tmp=(avg-inp[i]);
			rtn=rtn+tmp*tmp;
		}
		return (float)Math.sqrt(rtn/(float)(inp.length));
	}
	public void norm_spectrum(float[] inp)
	{
		float avg;
		float std;
		avg=find_mean(inp);
		std=find_std(inp, avg);
		for (int i=0; i<inp.length; i++)
		{
			inp[i]=(inp[i]-avg)/std;
		}
	}
	
	void copy_data(ImagePlus target_img)
	{
		ImageStack stack = target_img.getStack();
		int num_pixels;
		num_pixels=x_dim*y_dim;
        for (int i=0; i<size; i++) {
			ImageProcessor tp = stack.getProcessor(i+1);
			float[] pixels = (float[])tp.getPixels();
			for (int j=0; j<x_dim; j++) {
				for (int k=0; k<y_dim; k++)
				{				
					img_array[j][k][i]=pixels[k*x_dim+j];
				}
			}
		}
	}
	
	void process_data()
	{
		/*ImageProcessor tp = new_img.getProcessor();
		float[] pixels = (float[])tp.getPixels();
			
		for (int j=0; j<x_dim; j++) {
			for (int k=0; k<y_dim; k++)
			{	
				float tmp=0;
				for (int m=0; m<size; m++)
				{
					tmp=tmp+img_array[j][k][m]*source_spectrum[m];
				}
				if (tmp>31) pixels[j*y_dim+k]=tmp*1000f/32f;
			}
		}*/
	}
	
	private ImagePlus[] createAdmissibleImageList () 
	{
		final int[] windowList = WindowManager.getIDList();
		//IJ.log(""+windowList[0]+"\n");
		final Stack stack = new Stack();
		for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) 
		{
			final ImagePlus imp = WindowManager.getImage(windowList[k]);
			//IJ.log(imp.getTitle()+"\n");
			//if ((imp != null) && (imp.getType() == imp.GRAY32)) 
			{
				//IJ.log("got one");
				stack.push(imp);
			}
		}
		//IJ.log("Stack size:  " + stack.size() + "\n");
		final ImagePlus[] admissibleImageList = new ImagePlus[stack.size()];
		int k = 0;
		while (!stack.isEmpty()) {
			admissibleImageList[k++] = (ImagePlus)stack.pop();
		}
		if (k==0 && (windowList != null && windowList.length > 0 )){
			IJ.error("No float images, convert to float and try again");
		}
		return(admissibleImageList);
	} /* end createAdmissibleImageList */
}