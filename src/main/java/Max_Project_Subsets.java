
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
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

public class Max_Project_Subsets implements PlugIn{

	int width, height, channels, slices, frames, project_slices, new_slices;
	ImagePlus imp, new_img;
	@Override
	public void run(String arg) {
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth();
		height=imp.getHeight();
		channels=imp.getNChannels();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		GenericDialog gd=new GenericDialog("Project");
		gd.addNumericField("Frames to project over:  ", 5, 0);
		gd.showDialog();
		project_slices=(int)gd.getNextNumber();
		new_slices=slices-project_slices+1;
		new_img=NewImage.createImage("Img", width, height, new_slices*frames*channels, imp.getBitDepth(), NewImage.FILL_BLACK);
		short [][][][] pixel_array=new short [frames][slices][channels][];
		
		for (int i=0; i<frames; i++)
		{
			for (int j=0; j<slices; j++)
			{
				for (int k=0; k<channels; k++)
				{
					pixel_array[i][j][k]=(short [])imp.getStack().getProcessor(i*slices*channels+j*channels+k+1).getPixels();
				}
			}
		}
		
		for (int i=0; i<frames; i++)
		{
			for (int j=0; j<new_slices; j++)
			{
				for (int k=0; k<channels; k++)
				{
					short new_pix[]=(short [])new_img.getStack().getProcessor(i*new_slices*channels+j*channels+k+1).getPixels();
					for (int m=0; m<project_slices; m++)
					{
						short [] ptr=pixel_array[i][j+m][k];
						for (int n=0; n<width*height; n++)
						{
							if (new_pix[n]<ptr[n]) new_pix[n]=ptr[n];
						}
					}
				}
			}
		}
		new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(channels,  slices-project_slices+1, frames);
		new_img.show();
		new_img.updateAndDraw();
	}

	
}
