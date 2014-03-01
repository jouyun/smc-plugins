package splugins;
import ij.WindowManager;
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
import ij.measure.*;


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
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.io.IOException;
import java.io.FileWriter;


public class scale_3Channel implements PlugIn {

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		ImagePlus img;
		img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight();
		
		GenericDialog gd = new GenericDialog("Scales");
		gd.addNumericField("Red:  ", 1.0, 1);
		gd.addNumericField("Green:  ", 1.0, 1);
		gd.addNumericField("Blue:  ", 1.0, 1);
		gd.showDialog();
		float red_scale=(float)gd.getNextNumber(), green_scale=(float)gd.getNextNumber(), blue_scale=(float)gd.getNextNumber(); 
		byte [] red, green, blue;
		red=(byte [])img.getStack().getProcessor(1).getPixels();
		green=(byte [])img.getStack().getProcessor(2).getPixels();
		blue=(byte [])img.getStack().getProcessor(3).getPixels();
		
		for (int i=0; i<width*height; i++)
		{
			float rtmp=red[i]&0xff, gtmp=green[i]&0xff, btmp=blue[i]&0xff;
			rtmp=rtmp*red_scale;
			gtmp=gtmp*green_scale;
			btmp=btmp*blue_scale;
			red[i]=(byte)((int)rtmp&0xff);
			green[i]=(byte)((int)gtmp&0xff);
			blue[i]=(byte)((int)btmp&0xff);
		}
		img.updateAndDraw();

	}

}
