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

public class Threshold_Percentile implements PlugIn{

	public void run(String arg) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight();
		float [] pix=(float [])img.getProcessor().convertToFloat().getPixels();
		float [] pix_copy=new float[width*height];
		for (int i=0; i<width*height; i++) pix_copy[i]=pix[i];
		Arrays.sort(pix_copy);
		
		GenericDialog gd = new GenericDialog("Set Threshold Percentile");
		gd.addNumericField("Threshold (%age):  ", 90, 1);
		gd.showDialog();
		
		float prctile=(float)gd.getNextNumber(); 
		float threshold=pix_copy[(int)(prctile/100.0*(float)width*(float)height)];
		
		ImagePlus new_img=NewImage.createByteImage(("Thresholded"), width, height, 1, NewImage.FILL_BLACK);
		byte [] new_pix=(byte [])new_img.getProcessor().getPixels();
		
		for (int i=0; i<width*height; i++) if (pix[i]>threshold) new_pix[i]=(byte)255;
		new_img.show();
		new_img.updateAndDraw();
	}

}
