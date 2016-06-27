package splugins;
import java.util.List;
import java.util.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
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
import ij.plugin.PlugIn;


public class sCMOS_Filter_Bad_Pixels implements PlugIn {

	ImagePlus img;
	int height;
	int width;
	int slices;
	@Override
	public void run(String arg) {
		img=WindowManager.getCurrentImage();
		width=img.getWidth();
		height=img.getHeight();
		slices=img.getStackSize();
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		final String[] replacement_types=new String[3];
		replacement_types[0]="Random neighbor";
		replacement_types[1]="Median";
		replacement_types[2]="Average";
		//IJ.log("Good ones:  "+admissibleImageList.length+"\n");		
		if (admissibleImageList.length == 0) return;
		//IJ.log(""+admissibleImageList.length);
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Do Dot Product");
		gd.addChoice("Target image:", sourceNames, admissibleImageList[0].getTitle());
		gd.addChoice("How to replace:  ", replacement_types, replacement_types[0]);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		int tmpIndex=gd.getNextChoiceIndex();
		ImagePlus target_img;
		target_img=admissibleImageList[tmpIndex];
		int choice_index=gd.getNextChoiceIndex();
		
		byte[] byte_pix=(byte []) target_img.getProcessor().getPixels();
		
		switch (img.getType()) 
		{
			case ImagePlus.GRAY16: 
			{
				for (int s=0; s<slices; s++)
				{
					short [] pix=(short []) img.getStack().getProcessor(s+1).getPixels();
					IJ.log("Slice:  "+(s+1));
					for (int i=0; i<width; i++)
					{
						for (int j=0; j<height; j++)
						{
							if (byte_pix[i+j*width]==0) continue;
							short tmp=0;
							short num_pix=0;
							List <Short> stk=new ArrayList<Short>();
							if (i>0) 
							{
								if (byte_pix[i-1+j*width]==0)
								{
									tmp+=pix[i-1+j*width];
									stk.add(pix[i-1+j*width]);
									num_pix++;
								}						
							}
							if (i<width-1) 
							{
								if (byte_pix[i+1+j*width]==0)
								{
									tmp+=pix[i+1+j*width];
									stk.add(pix[i+1+j*width]);
									num_pix++;
								}
							}
							if (j>0) 
							{
								if (byte_pix[i+(j-1)*width]==0)
								{
									tmp+=pix[i+(j-1)*width];
									stk.add(pix[i+(j-1)*width]);
									num_pix++;
								}
							}
							if (j<height-1) 
							{
								if (byte_pix[i+(j+1)*width]==0)
								{
									tmp+=pix[i+(j+1)*width];
									stk.add(pix[i+(j+1)*width]);
									num_pix++;
								}
							}
							if (choice_index==0)
							{
								if (num_pix>0) pix[i+j*width]=stk.get((int)num_pix/2);
							}
							else
							{
								if (choice_index==1)
								{
									Collections.sort(stk);
									if (num_pix>0) pix[i+j*width]=stk.get((int)num_pix/2);
								}
								else
								{
									if (num_pix>0) pix[i+j*width]=(short) (tmp/num_pix);
								}
							}
						}
					}
				}
				break;
			}
			case ImagePlus.GRAY32: 
			{
				for (int s=0; s<slices; s++)
				{
					float [] pix=(float []) img.getStack().getProcessor(s+1).getPixels();
					IJ.log("Slice:  "+(s+1));
					for (int i=0; i<width; i++)
					{
						for (int j=0; j<height; j++)
						{
							if (byte_pix[i+j*width]==0) continue;
							float tmp=0;
							float num_pix=0;
							List <Float> stk=new ArrayList<Float>();
							if (i>0) 
							{
								if (byte_pix[i-1+j*width]==0)
								{
									tmp+=pix[i-1+j*width];
									stk.add(pix[i-1+j*width]);
									num_pix++;
								}						
							}
							if (i<width-1) 
							{
								if (byte_pix[i+1+j*width]==0)
								{
									tmp+=pix[i+1+j*width];
									stk.add(pix[i+1+j*width]);
									num_pix++;
								}
							}
							if (j>0) 
							{
								if (byte_pix[i+(j-1)*width]==0)
								{
									tmp+=pix[i+(j-1)*width];
									stk.add(pix[i+(j-1)*width]);
									num_pix++;
								}
							}
							if (j<height-1) 
							{
								if (byte_pix[i+(j+1)*width]==0)
								{
									tmp+=pix[i+(j+1)*width];
									stk.add(pix[i+(j+1)*width]);
									num_pix++;
								}
							}
							if (choice_index==0)
							{
								if (num_pix>0) pix[i+j*width]=stk.get((int)num_pix/2);
							}
							else
							{
								if (choice_index==1)
								{
									Collections.sort(stk);
									if (num_pix>0) pix[i+j*width]=stk.get((int)num_pix/2);
								}
								else
								{
									if (num_pix>0) pix[i+j*width]=tmp/num_pix;
								}
							}
						}
					}
				}
				break;
			}
			default: 
			{
				IJ.error("Unexpected image type");
				return;
			}
		}
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
			if ((imp != null))
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
