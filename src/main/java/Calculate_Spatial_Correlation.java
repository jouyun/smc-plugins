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


public class Calculate_Spatial_Correlation implements PlugIn {

	@Override
	public void run(String arg) {
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		if (admissibleImageList.length == 0) return;
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Pick images");
		gd.addChoice("First:", sourceNames, admissibleImageList[0].getTitle());
		gd.addChoice("Second:", sourceNames, admissibleImageList[0].getTitle());
		//gd.addNumericField("S:  ", 0.003, 5);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		ImagePlus first_img=admissibleImageList[gd.getNextChoiceIndex()];
		ImagePlus second_img=admissibleImageList[gd.getNextChoiceIndex()];
		
		int width=first_img.getWidth(), height=first_img.getHeight();
		float [] first_pix=(float [])first_img.getProcessor().getPixels();
		float [] second_pix=(float [])second_img.getProcessor().getPixels();
		float [] first_copy = new float[width*height];
		float [] second_copy = new float[width*height];
		float mean=0, mean2=0, std=0, std2=0;
		for (int i=0; i<width*height; i++)
		{
			first_copy[i]=first_pix[i];
			second_copy[i]=second_pix[i];
			mean=mean+first_pix[i];
			mean2=mean2+second_pix[i];
			std=std+first_pix[i]*first_pix[i];
			std2=std2+second_pix[i]*second_pix[i];
		}
		mean=mean/(float)(width*height);
		mean2=mean2/(float)(width*height);
		std=std/(float)(width*height);
		std2=std2/(float)(width*height);
		std=std-mean*mean;
		std2=std2-mean2*mean2;
		float local_max1=0, local_max2=0;
		for (int i=width/2; i<3*width/4; i++)
		{
			for (int j=height/2; j<height*3/4; j++)
			{
				if (first_copy[i+j*width]>local_max1) local_max1=first_copy[i+j*width];
				if (second_copy[i+j*width]>local_max2) local_max2=second_copy[i+j*width];
			}
		}
		std=(float)Math.sqrt(std);
		std2=(float)Math.sqrt(std2);
		
		//Scale by local max
		//std=local_max1-mean;
		//std2=local_max2-mean2;
		
		for (int i=0; i<width*height; i++)
		{
			first_copy[i]=(first_copy[i]-mean)/std;
			second_copy[i]=(second_copy[i]-mean2)/std2;

			//first_copy[i]=(first_copy[i]-mean);
			//second_copy[i]=(second_copy[i]-mean2);
		}
		//Test code
		/*mean=0;
		mean2=0;
		std=0;
		std2=0;
		for (int i=0; i<width*height; i++)
		{
			mean=mean+first_copy[i];
			mean2=mean2+second_copy[i];
			std=std+first_copy[i]*first_copy[i];
			std2=std2+second_copy[i]*second_copy[i];
		}
		mean=mean/(float)(width*height);
		mean2=mean2/(float)(width*height);
		std=std/(float)(width*height);
		std2=std2/(float)(width*height);
		std=std-mean*mean;
		std2=std2-mean2*mean2;
		IJ.log(""+mean+","+mean2+","+std+","+std2);*/
		
		//End test code
		//ImagePlus new_img=NewImage.createFloatImage("NewImg", width, height, 1, NewImage.FILL_BLACK);
		//float [] new_pix=(float [])new_img.getProcessor().getPixels();
		float correlation=0, shifted_correlation1=0, shifted_correlation2=0, shifted_correlation3=0, shifted_correlation4=0;
		int x_shift=10, y_shift=10;
		int ctr=0; 
		for (int i=0; i<width; i++)
		{
			for (int j=0; j<height; j++)
			{
				ctr++;
				//new_pix[i+j*width]=first_copy[(i)%width+(j)%height*width]*second_copy[(i+x_shift)%width+(j+y_shift)%height*width];
				correlation=correlation+first_copy[i+j*width]*second_copy[i+j*width];
				shifted_correlation1=shifted_correlation1+first_copy[(i)%width+(j)%height*width]*second_copy[(i+x_shift)%width+(j+y_shift)%height*width];
				shifted_correlation2=shifted_correlation2+first_copy[(i)%width+(j)%height*width]*second_copy[(i+width-x_shift)%width+(j+y_shift)%height*width];
				shifted_correlation3=shifted_correlation3+first_copy[(i)%width+(j)%height*width]*second_copy[(i+width-x_shift)%width+(j+height-y_shift)%height*width];
				shifted_correlation4=shifted_correlation4+first_copy[(i)%width+(j)%height*width]*second_copy[(i+x_shift)%width+(j+height-y_shift)%height*width];
			}
		}
		correlation=correlation/(float)(width*height);
		shifted_correlation1=shifted_correlation1/(float)(width*height);
		shifted_correlation2=shifted_correlation2/(float)(width*height);
		shifted_correlation3=shifted_correlation3/(float)(width*height);
		shifted_correlation4=shifted_correlation4/(float)(width*height);
		//IJ.log("Correlation:  "+correlation);
		//IJ.log("Shifted correlation:  "+ (shifted_correlation1+shifted_correlation2+shifted_correlation3+shifted_correlation4)/4.0);
		IJ.log(""+(correlation-(shifted_correlation1+shifted_correlation2+shifted_correlation3+shifted_correlation4)/4.0));
		//new_img.show();
		//new_img.updateAndDraw();
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
