package splugins;

import java.util.ArrayList;
import java.util.List;
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
import ij.plugin.PlugIn;

public class Histogram_Normalize_Percentile implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus img=WindowManager.getCurrentImage();
		
		GenericDialog gd=new GenericDialog("Histogram normalizer");
		gd.addNumericField("Sample every how many spots:  ", 1, 0);
		gd.addNumericField("Percentile_Max :  ", 90, 1);
		gd.addNumericField("Percentile_Min :  ", 10, 1);
		gd.addNumericField("MyMax:  ", 255, 0);
		gd.addNumericField("MyMin: ", 0, 0);
		gd.addCheckbox("Whole Stack: ", true);
		gd.showDialog();
		double sample=gd.getNextNumber();
		double pmax=gd.getNextNumber();
		double pmin=gd.getNextNumber();
		double max=gd.getNextNumber();
		double min=gd.getNextNumber();
		boolean process_stack=gd.getNextBoolean();
		if (process_stack) Normalize(img, sample, pmax, pmin, max, min);
		else Normalize(img.getProcessor(), sample, pmax, pmin, max, min);
		img.updateAndDraw();
	}
	/*******************************************************************************************
	 * 					Normalize, normalizes a image based on percentiles
	 * @param img Image to normalize (must be float)
	 * @param sample Sample image every "sample" pixels (sorting a whole image takes a while)
	 * @param pmax The upper percentile (usually 90)
	 * @param pmin The lower percentile (usually 10)
	 * @param max What final pixel value will correspond to the upper percentile
	 * @param min What final pixel value will correspond to the lower percentile
	 */
	public static void Normalize(ImagePlus img, double sample, double pmax, double pmin, double max, double min)
	{
		int slices=img.getNSlices(), channels=img.getNChannels(), frames=img.getNFrames();
		sample=1.0/sample;
		for (int f=0; f<frames; f++)
		{
			for (int c=0; c<channels; c++)
			{
				for (int s=0; s<slices; s++)
				{
					List <Float> stk=new ArrayList<Float>();
					float [] pix = (float [])img.getStack().getProcessor(f*slices*channels+s*channels+c+1).getPixels();
					for (int i=0; i<pix.length; i++)
					{
						if (Math.random()<sample)
						{
							stk.add(pix[i]);
						}
					}
					Collections.sort(stk);
					int pminidx=(int)Math.floor(pmin/100.0*(float)stk.size());
					int pmaxidx=(int)Math.floor(pmax/100.0*(float)stk.size());
					float rmin=stk.get(pminidx);
					float rgap=stk.get(pmaxidx)-stk.get(pminidx);
					float ngap=(float)(max-min);
					float rn=rgap/ngap;
					for (int i=0; i<pix.length; i++)
					{
						pix[i]=(pix[i]-rmin)/rn+(float)min;
					}
				}
			}
		}
		
	}
	/*******************************************************************************************
	 * 					NormalizeByte, normalizes a byte image
	 * @param img Image to normalize, must be byte image
	 * @param sample Sample image every "sample" pixels (sorting a whole image takes a while)
	 * @param pmax The upper percentile (usually 90)
	 * @param pmin The lower percentile (usually 10)
	 * @param max What final pixel value will correspond to the upper percentile
	 * @param min What final pixel value will correspond to the lower percentile
	 */
	public static void NormalizeByte(ImagePlus img, double sample, double pmax, double pmin, double max, double min)
	{
		int slices=img.getNSlices(), channels=img.getNChannels(), frames=img.getNFrames();
		sample=1.0/sample;
		for (int f=0; f<frames; f++)
		{
			for (int c=0; c<channels; c++)
			{
				for (int s=0; s<slices; s++)
				{
					List <Float> stk=new ArrayList<Float>();
					float [] pix = (float [])img.getStack().getProcessor(f*slices*channels+s*channels+c+1).convertToFloat().getPixels();
					for (int i=0; i<pix.length; i++)
					{
						if (Math.random()<sample)
						{
							stk.add(pix[i]);
						}
					}
					Collections.sort(stk);
					int pminidx=(int)Math.floor(pmin/100.0*(float)stk.size());
					int pmaxidx=(int)Math.floor(pmax/100.0*(float)stk.size());
					float rmin=stk.get(pminidx);
					float rgap=stk.get(pmaxidx)-stk.get(pminidx);
					float ngap=(float)(max-min);
					float rn=rgap/ngap;
					byte [] byte_pix=(byte [])img.getStack().getProcessor(f*slices*channels+s*channels+c+1).getPixels();
					for (int i=0; i<pix.length; i++)
					{
						float tmp=((pix[i]-rmin)/rn+(float)min);
						byte_pix[i]=(byte)tmp;
						if (tmp<0) byte_pix[i]=(byte)0;
						if (tmp>255) byte_pix[i]=(byte)255;
					}
				}
			}
		}
	}
	/*******************************************************************************************
	 * 					Normalize, normalizes a image based on percentiles
	 * @param ip Image processor to normalize (must be float)
	 * @param sample Sample image every "sample" pixels (sorting a whole image takes a while)
	 * @param pmax The upper percentile (usually 90)
	 * @param pmin The lower percentile (usually 10)
	 * @param max What final pixel value will correspond to the upper percentile
	 * @param min What final pixel value will correspond to the lower percentile
	 */
	public static void Normalize(ImageProcessor ip, double sample, double pmax, double pmin, double max, double min)
	{
		sample=1.0/sample;
		List <Float> stk=new ArrayList<Float>();
		float [] pix = (float [])ip.getPixels();
		for (int i=0; i<pix.length; i++)
		{
			if (Math.random()<sample)
			{
				stk.add(pix[i]);
			}
		}
		Collections.sort(stk);
		int pminidx=(int)Math.floor(pmin/100.0*(float)stk.size());
		int pmaxidx=(int)Math.floor(pmax/100.0*(float)stk.size());
		float rmin=stk.get(pminidx);
		float rgap=stk.get(pmaxidx)-stk.get(pminidx);
		float ngap=(float)(max-min);
		float rn=rgap/ngap;
		for (int i=0; i<pix.length; i++)
		{
			pix[i]=(pix[i]-rmin)/rn+(float)min;
		}
	}

}
