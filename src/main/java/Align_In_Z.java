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


public class Align_In_Z implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus imp=WindowManager.getCurrentImage();
		int width=imp.getWidth(), height=imp.getHeight(), channel=imp.getChannel()-1, slice=imp.getSlice()-1, frame=imp.getFrame()-1, channels=imp.getNChannels(), slices=imp.getNSlices(), frames=imp.getNFrames();
		float [] current_base=(float [])imp.getProcessor().getPixels();
		int [] shifts=new int[frames];
		ImagePlus new_img=NewImage.createFloatImage("TempImg", width, height, imp.getImageStackSize(), NewImage.FILL_BLACK);
		for (int i=0; i<frames; i++)
		{
			float current_mean=get_mean(current_base, width, height);
			float [] vals=new float[slices];
			for (int j=0; j<slices; j++)
			{
				float [] compare_pix=(float [])imp.getStack().getProcessor(1+channel+j*channels+i*channels*slices).getPixels();
				float compare_mean=get_mean(compare_pix, width, height);
				for (int m=0; m<width*height; m++)
				{
					vals[j]=vals[j]+(current_base[m]-current_mean)*(compare_pix[m]-compare_mean)/(float)(height*width);
				}
			}
			int best_slice=-1;
			float best_cross=-10000000;
			for (int j=0; j<slices; j++)
			{
				if (vals[j]>best_cross)
				{
					best_cross=vals[j];
					best_slice=j;
				}
			}
			current_base=(float [])imp.getStack().getProcessor(1+channel+best_slice*channels+i*channels*slices).getPixels();
			IJ.log("Best at time: "+i+" was: "+(slice-best_slice));
			shifts[i]=slice-best_slice;
			
			
		}
		
		for (int i=0; i<frames; i++)
		{
			IJ.log("Frame " + i+" shift was "+shifts[i]);
			for (int c=0; c<channels; c++)
			{
				for (int j=0; j<slices; j++)
				{
					int new_idx=j+shifts[i];
					if ((new_idx>=slices)||(new_idx<0)) continue;
					float [] src=(float [])imp.getStack().getProcessor(1+c+j*channels+i*channels*slices).getPixels();
					float [] target=(float [])new_img.getStack().getProcessor(1+c+((new_idx))*channels+i*channels*slices).getPixels();
					for (int m=0; m<width*height; m++) target[m]=src[m];
				}
			}
		}
		new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(channels,  slices, frames);
		new_img.show();
		new_img.updateAndDraw();

	}
	public float get_mean(float [] img, int width, int height)
	{
		float rtn=0.0f;
		for (int i=0; i<width*height; i++) rtn+=img[i];
		rtn=rtn/(float)(width*height);
		return rtn;
	}

}
