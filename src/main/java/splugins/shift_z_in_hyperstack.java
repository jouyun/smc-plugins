package splugins;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
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



public class shift_z_in_hyperstack implements PlugIn {

	@Override
	public void run(String arg) {
		
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		//IJ.log("Good ones:  "+admissibleImageList.length+"\n");		
		if (admissibleImageList.length == 0) return;
		//IJ.log(""+admissibleImageList.length);
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Shift Source and Target");
		gd.addChoice("Source image:", sourceNames, admissibleImageList[0].getTitle());
		gd.addChoice("Target image:", sourceNames, admissibleImageList[0].getTitle());
		gd.addNumericField("Shift amount:  ", 10, 0);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		ImagePlus source_img=admissibleImageList[gd.getNextChoiceIndex()];
		ImagePlus target_img=admissibleImageList[gd.getNextChoiceIndex()];
		int shift=(int)gd.getNextNumber();
		int x_dim=source_img.getWidth();
		int y_dim=source_img.getHeight();
		int channels=source_img.getNChannels();
		int slices=source_img.getNSlices();
		int frames=source_img.getNFrames();
		int frame=source_img.getFrame();
		ImageStack source_stack=source_img.getStack();
		ImageStack target_stack=target_img.getStack();
		for (int i=0; i<channels; i++)
		{
			//for (int j=0; j<frames; j++)
			{
				for (int k=0; k<slices; k++)
				{
					//IJ.log("channel:  " + i+" slice:  "+k+" shift:  "+frame);
					if (k+shift<0||k+shift>=slices) 
					{
						float [] target=(float [])target_img.getStack().getProcessor(1+(i)+k*channels+(frame-1)*channels*slices).getPixels();
						for (int m=0; m<x_dim*y_dim; m++) target[m]=0;
						//IJ.log("BAD ALL ZEROS channel:  " + i+" slice:  "+k+" shift:  "+shift);
						continue;
					}
					float [] src=(float [])source_img.getStack().getProcessor(1+(i)+(k-shift)*channels+(frame-1)*channels*slices).getPixels();
					float [] target=(float [])target_img.getStack().getProcessor(1+(i)+k*channels+(frame-1)*channels*slices).getPixels();
					for (int m=0; m<x_dim*y_dim; m++) target[m]=src[m];
				}
			}
		}
		target_img.show();
		target_img.updateAndDraw();
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
	}
}
