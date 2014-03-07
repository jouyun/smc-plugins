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
		int width=img.getWidth(), height=img.getHeight(), slices=img.getNSlices(), channels=img.getNChannels(), frames=img.getNFrames(); 
		
		GenericDialog gd=new GenericDialog("Histogram normalizer");
		gd.addNumericField("Sample every how many spots:  ", 1, 0);
		gd.addNumericField("Percentile_Max :  ", 90, 1);
		gd.addNumericField("Percentile_Min :  ", 10, 1);
		gd.addNumericField("Max :  ", 255, 0);
		gd.addNumericField("Min:  ", 255, 0);
		gd.showDialog();
		double sample=1.0/gd.getNextNumber();
		double pmax=gd.getNextNumber();
		double pmin=gd.getNextNumber();
		double max=gd.getNextNumber();
		double min=gd.getNextNumber();
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
		img.updateAndDraw();

	}

}
