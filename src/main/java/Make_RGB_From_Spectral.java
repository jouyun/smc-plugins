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



public class Make_RGB_From_Spectral implements PlugIn {
	public void run(String arg) 
	{
		ImagePlus imp=WindowManager.getCurrentImage();
		int width=imp.getWidth(), height=imp.getHeight(), slices=imp.getNSlices(), channels=imp.getNChannels(), frames=imp.getNFrames();
		ImagePlus newimg=new ImagePlus("Separated", ImageStack.create(width, height, 3*slices*frames,32));
		for (int i=0; i<frames; i++)
		{
			for (int j=0; j<slices; j++)
			{
				float new_pix[]=(float [])newimg.getStack().getProcessor(i*3*slices+j*3+2+1).getPixels();
				float ctr=0;
				for (int k=5; k<11; k++)
				{
					float original_pix[]=(float [])imp.getStack().getProcessor(i*channels*slices+j*channels+k).convertToFloat().getPixels();
					for (int m=0; m<width*height; m++) new_pix[m]=new_pix[m]+original_pix[m];
					ctr++;
				}
				for (int m=0; m<width*height; m++) new_pix[m]=new_pix[m]/ctr;
				new_pix=(float [])newimg.getStack().getProcessor(i*3*slices+j*3+1+1).getPixels();
				ctr=0;
				for (int k=13; k<17; k++)
				{
					float original_pix[]=(float [])imp.getStack().getProcessor(i*channels*slices+j*channels+k).convertToFloat().getPixels();
					for (int m=0; m<width*height; m++) new_pix[m]=new_pix[m]+original_pix[m];
					ctr++;
				}
				for (int m=0; m<width*height; m++) new_pix[m]=new_pix[m]/ctr;
				new_pix=(float [])newimg.getStack().getProcessor(i*3*slices+j*3+0+1).getPixels();
				ctr=0;
				for (int k=20; k<28; k++)
				{
					float original_pix[]=(float [])imp.getStack().getProcessor(i*channels*slices+j*channels+k).convertToFloat().getPixels();
					for (int m=0; m<width*height; m++) new_pix[m]=new_pix[m]+original_pix[m];
					ctr++;
				}
				for (int m=0; m<width*height; m++) new_pix[m]=new_pix[m]/ctr;
			}
		}
		newimg.setDisplayRange(0, 20000);
		newimg.setOpenAsHyperStack(true);
		newimg.setDimensions(3, slices, frames);
		newimg.show();
		newimg.updateAndDraw();
		IJ.run("Make Composite", "display=Composite");
	}
}
