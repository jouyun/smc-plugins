package splugins;
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

public class Percentile_Threshold implements PlugIn{
	public void run(String args)
	{
		double prctile=0.995;
		ImagePlus imp=WindowManager.getCurrentImage();
		double width=imp.getWidth(), height=imp.getHeight();
		
		
		GenericDialog gd = new GenericDialog("In situ process");
		gd.addNumericField("Percentile", 10.0, 1);
		gd.addNumericField("SNR", 8, 1);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		prctile=(float)gd.getNextNumber();
		float SNR=(float)gd.getNextNumber();
		
		
		List <Float> stk=new ArrayList<Float>();
		float [] pix=(float []) imp.getProcessor().getPixels();
		Random rand=new Random();
		int frac_to_include=10;
		for (int i=0; i<width*height; i++)
		{
			if (pix[i]==0.0f) continue;
			if (i%frac_to_include!=rand.nextInt(frac_to_include)) continue;
			stk.add(pix[i]);
		}
		Collections.sort(stk);
		double average=0.0f;
		double		sigma=0.0f;
		int num_pix=0;
		for (int i=0; i<(int)(stk.size()*prctile/100.0f); i++)
		{
			average=average+(double)stk.get(i);
			sigma=sigma+(double)stk.get(i)*(double)stk.get(i);
			num_pix++;
		}
		average=average/(double)(num_pix);
		sigma=(double)Math.sqrt(sigma/(double)(num_pix)-average*average);
		ImagePlus new_img=NewImage.createByteImage("Result", (int)width, (int)height, 1, NewImage.FILL_BLACK);
		
		byte [] new_pix=(byte [])new_img.getProcessor().getPixels();
		
		float thresh=stk.get((int)(stk.size()*prctile/100));
		IJ.log("Threshold:  "+sigma);
		IJ.log("SNR*Threshold:  " +(sigma*SNR));
		
		for (int i=0; i<width*height; i++)
		{
			if ((double)pix[i]-average<sigma*(double)SNR) continue;
			new_pix[i]=(byte)255;
		}
		
		new_img.show();
		new_img.updateAndDraw();
	}
	
}

