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

public class normalize_to_max_project implements PlugIn {

	int width;
	int height;
	int channels;
	int slices;
	int frames;
	public void run(String arg) {
		ImagePlus img;
		img=WindowManager.getCurrentImage();
		width=img.getWidth();
		height=img.getHeight();
		slices=img.getNSlices();
		channels=img.getNChannels();
		frames=img.getNFrames();
		float [] mins=new float[frames];
		float [] maxs=new float[frames];

		for (int i=0; i<channels; i++)
		{
			for (int j=0; j<frames; j++)
			{
				float min=10000000000f, max=0f;
				for(int k=0; k<slices; k++)
				{
					float [] pix=(float [])img.getStack().getProcessor(1+idx(i,k,j)).getPixels();
					for (int m=0; m<pix.length; m++) 
					{
						if (pix[m]>max) max=pix[m];
						if (pix[m]<min) min=pix[m];
					}
				}
				maxs[j]=max;
				mins[j]=min;
				
			}
			int bound=10;
			for (int j=0+bound; j<frames-bound; j++)
			{
				float tmax=0.0f, tmin=0.0f;
				for (int m=-bound; m<bound; m++)
				{
					tmax+=maxs[m+j];
					tmin+=mins[m+j];
				}
				maxs[j]=tmax/(float)(2*bound);
				mins[j]=tmin/(float)(2*bound);
			}
			for (int j=0; j<frames; j++)
			{
				for(int k=0; k<slices; k++)
				{
					float [] pix=(float [])img.getStack().getProcessor(1+idx(i,k,j)).getPixels();
					for (int m=0; m<pix.length; m++) 
					{
						pix[m]=10000f*(pix[m]-mins[j])/(maxs[j]-mins[j]);
					}
				}
			}
		}
		img.updateAndDraw();
	}

	int idx(int channel, int slice, int frame)
	{
		return frame*channels*slices+slice*channels+channel; 
	}

}


