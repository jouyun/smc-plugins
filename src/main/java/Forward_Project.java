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


public class Forward_Project implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus imp=WindowManager.getCurrentImage();
		int width=imp.getWidth(), height=imp.getHeight();
		
		GenericDialog gd = new GenericDialog("Forward Project");
		gd.addNumericField("Number of angles: ", 1800, 1);
		gd.addNumericField("Angle spacing:  ", 0.2, 2);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		int angles=(int)gd.getNextNumber();
		float angle_spacing=(float)gd.getNextNumber();
		ImagePlus A_img, B_img, mask_img;
		
		
		ImagePlus tmp_img, new_img;
		new_img=NewImage.createFloatImage("NewImg", width, angles, 1, NewImage.FILL_BLACK);
		float [] src=(float [])imp.getProcessor().getPixels();
		float [] target=(float [])new_img.getProcessor().getPixels();
		float [] tmp_target;
		for (int i=0; i<angles; i++)
		{
			tmp_img=NewImage.createFloatImage("TempImg", width, height, 1, NewImage.FILL_BLACK);
			tmp_target=(float [])tmp_img.getProcessor().getPixels();
		
			for (int j=0; j<width*height; j++) tmp_target[j]=src[j];
			//tmp_img.updateAndDraw();
			//tmp_img.show();
			//IJ.run("Rotate... ", "angle="+((float)i*angle_spacing)+" grid=1 interpolation=Bilinear");
			tmp_img.getProcessor().setInterpolationMethod(ImageProcessor.BILINEAR);
			tmp_img.getProcessor().rotate((float)i*angle_spacing);
			
			
			for (int k=0; k<width; k++)
			{
				for (int j=0; j<height; j++)
				{
					target[k+i*width]=target[k+i*width]+tmp_target[k+j*width];
				}
				target[k+i*width]=target[k+i*width]/(float)height;
			}
			tmp_img.changes=false;
			tmp_img.close();
		}
		new_img.updateAndDraw();
		new_img.show();
	}

}
