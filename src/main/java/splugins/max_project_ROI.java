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

import java.awt.Polygon;
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

import splugins.seeded_multipoint_adaptive_region_grow.MyIntPoint;
import ij.plugin.PlugIn;

public class max_project_ROI implements PlugIn{
	
	public void run(String arg)
	{
		ImagePlus imp=WindowManager.getCurrentImage();
		int width=imp.getWidth(); 
		int height=imp.getHeight();
		int frames=imp.getNFrames();
		//If there is already an roi selected, use it and return
		Roi roi = imp.getRoi();
		for (int j=0; j<frames; j++)
        {
			if(roi!=null)
			{
				Polygon p=roi.getPolygon();
				float current_max=0.0f;
				float current_avg=0.0f;
				float [] pix=(float []) imp.getStack().getProcessor(j+1).getPixels();
				for (int i=0; i<p.npoints; i++)
				{
					float tmp=pix[p.xpoints[i]+p.ypoints[i]*width];
					if (tmp>current_max) current_max=tmp;
					current_avg+=tmp;
				}
				IJ.log(""+(current_avg/(float)p.npoints));
			}
        }
	}

}
