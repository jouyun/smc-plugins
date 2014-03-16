package splugins;
import ij.plugin.PlugIn;
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
import java.text.SimpleDateFormat;

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
import ij.io.OpenDialog;

public class Bin_Image implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus imp=WindowManager.getCurrentImage();
		ImagePlus newimg=DoBin(imp);
		newimg.show();
		newimg.updateAndDraw();
	}
	public static ImagePlus DoBin(ImagePlus imp)
	{
		int width=imp.getWidth(), height=imp.getHeight(), slices=imp.getStackSize();
		ImagePlus newimg=new ImagePlus("Binned", ImageStack.create(width/2, height/2, slices,32));
		for (int i=0; i<slices; i++)
		{
			float [] src_pixels=(float [])imp.getStack().getProcessor(i+1).convertToFloat().getPixels();
			float [] tgt_pixels=(float [])newimg.getStack().getProcessor(i+1).convertToFloat().getPixels();
			for (int j=0; j<width/2; j++)
			{
				for (int k=0; k<height/2; k++)
				{
					float tmp=0;
					tmp+=src_pixels[k*2*width+j*2];
					tmp+=src_pixels[k*2*width+j*2+1];
					tmp+=src_pixels[(k*2+1)*width+j*2];
					tmp+=src_pixels[(k*2+1)*width+j*2+1];
					tgt_pixels[k*width/2+j]=tmp;
				}
			}
		}
		return newimg;
	}

}
