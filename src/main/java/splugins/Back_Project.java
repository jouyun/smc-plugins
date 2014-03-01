package splugins;
import ij.plugin.PlugIn;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import ij.measure.Measurements;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import ij.process.AutoThresholder;
import ij.process.StackStatistics;
import ij.process.ImageStatistics;
import java.awt.Color;
import java.util.*;
import java.io.*;
import java.lang.Float;


// Java 1.1
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.IndexColorModel;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.io.IOException;
import java.io.FileWriter;


public class Back_Project implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus img=WindowManager.getCurrentImage();
		int angles=img.getHeight(), width=img.getWidth();
		
		float [] old_pix=(float [])img.getProcessor().getPixels();
		GenericDialog gd = new GenericDialog("Forward Project");
		gd.addNumericField("Angle spacing:  ", 0.2, 2);
		gd.addNumericField("New height in pixels: ", 2048, 0);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		float angle_spacing=(float)gd.getNextNumber();
		int new_height=(int)gd.getNextNumber();
		
		/*ImagePlus new_img=NewImage.createFloatImage("Img", width, new_height, angles, NewImage.FILL_BLACK);
		for (int i=0; i<angles; i++)
		{
			float [] new_pix=(float [])new_img.getStack().getProcessor(i+1).getPixels();
			for (int j=0; j<new_height; j++)
			{
				for (int k=0; k<width; k++)
				{
					new_pix[j*width+k]=old_pix[k+width*i];
					//new_pix[j+k*height]=j+k*height;
				}
			}
		}
		new_img.show();
		new_img.updateAndDraw();*/
		ImagePlus tmp_img=NewImage.createFloatImage("Tmp", width, new_height, 1, NewImage.FILL_BLACK);
		ImagePlus new_img=NewImage.createFloatImage("BackProjected", width, new_height, 1, NewImage.FILL_BLACK);
		float [] tmp_pix=(float [])tmp_img.getProcessor().getPixels();
		float [] new_pix=(float [])new_img.getProcessor().getPixels();
		tmp_img.getProcessor().setInterpolationMethod(ImageProcessor.BILINEAR);
		for (int i=0; i<angles; i++)
		{
			for (int j=0; j<new_height; j++)
			{
				for (int k=0; k<width; k++)
				{
					tmp_pix[j*width+k]=old_pix[k+width*i];
				}
			}			
			tmp_img.getProcessor().rotate((float)i*angle_spacing);
			for (int j=0; j<new_height*width; j++)
			{
				new_pix[j]=new_pix[j]+tmp_pix[j];
			}
		}
		for (int j=0; j<new_height*width; j++) new_pix[j]=new_pix[j]/(float)angles;
		new_img.updateAndDraw();
		new_img.show();
		IJ.run("Flip Vertically");
	}

}
