import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
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
import java.awt.image.IndexColorModel;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;

public class add_channel implements PlugIn{
	
	int width;
	int height;
	int slices;
	int cur_slice;
	int frames;
	int cur_frame;
	int cur_channel;
	int channels;
	ImagePlus imp;
	ImagePlus target_imp;
	ImagePlus new_imp;
	public void run (String arg)
	{
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		//IJ.log("Good ones:  "+admissibleImageList.length+"\n");		
		if (admissibleImageList.length == 0) return;
		//IJ.log(""+admissibleImageList.length);
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Do Dot Product");
		gd.addChoice("Target image:", sourceNames, admissibleImageList[0].getTitle());
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		int tmpIndex=gd.getNextChoiceIndex();
		target_imp=admissibleImageList[tmpIndex];
		new_imp=NewImage.createFloatImage("Img", width, height, slices*channels+slices, NewImage.FILL_BLACK);
		for (int i=0; i<slices; i++)
		{
			for (int j=0; j<channels; j++)
			{
				float [] new_tmp=(float []) new_imp.getStack().getProcessor(i*(channels+1)+j+1).getPixels();
				float [] old_tmp=(float []) imp.getStack().getProcessor(i*channels+j+1).getPixels();
				for (int m=0; m<width*height; m++)
				{
					new_tmp[m]=old_tmp[m];
				}				
			}
			float [] new_tmp=(float []) new_imp.getStack().getProcessor(i*(channels+1)+channels+1).getPixels();
			float [] old_tmp=(float []) target_imp.getStack().getProcessor(i+1).getPixels();
			for (int m=0; m<width*height; m++)
			{
				new_tmp[m]=old_tmp[m];
			}

		}
		new_imp.setOpenAsHyperStack(true);
		new_imp.setDimensions(channels+1, slices, frames);
		new_imp.show();
		new_imp.updateAndDraw();
		
		
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
			if ((imp != null) && (imp.getType() == imp.GRAY32)) 
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
