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
 

public class test_calling implements PlugIn, Measurements{
	public void run(String Arguments)
	{
		int width, height;
		ImagePlus imp=WindowManager.getCurrentImage();
		width=imp.getWidth();
		height=imp.getHeight();
		byte [] mask_pix=new byte[width*height];
		float [] insitu_pix=new float[width*height];
		short [] premask_pix=new short[width*height];
		short[] red_pix, green_pix, blue_pix;
		red_pix=(short []) imp.getStack().getProcessor(1).getPixels();
		green_pix=(short []) imp.getStack().getProcessor(2).getPixels();
		blue_pix=(short []) imp.getStack().getProcessor(3).getPixels();
		int s_max=65536;
		int red, green, blue;
		for (int i=0; i<width*height; i++) 
		{
			red=s_max-(int)(red_pix[i]&0xffff);
			green=s_max-(int)(green_pix[i]&0xffff);
			blue=s_max-(int)(blue_pix[i]&0xffff);
			//insitu_pix[i]=(float)((red+green)/2-blue);
			insitu_pix[i]=(float)((red+green)/2);
			premask_pix[i]=(short)(s_max-(int)(blue_pix[i]&0xffff));
		}
		int s, t;
		s=red_pix[0];
		t=red_pix[0]&0xffff;
		ImagePlus insitu_img, premask_img;
		premask_img=new ImagePlus("New Image", new ShortProcessor(width,height,premask_pix,null));
		insitu_img=new ImagePlus("New Image", new FloatProcessor(width,height,insitu_pix,null));
		//new_img.show();
		//new_img.updateAndDraw();
		insitu_img.show();
		insitu_img.updateAndDraw();
		
		ImageStatistics stats = premask_img.getProcessor().getStatistics();
		AutoThresholder thresholder = new AutoThresholder();
		int threshold = thresholder.getThreshold("Default", stats.histogram);
		double real_threshold=(double)(threshold+2)*stats.binSize+stats.min;
		
		IJ.log("bin "+stats.binSize+"\n");
		IJ.log("min "+stats.histMin+"\n");
		IJ.log("thresh "+threshold+"\n");
		IJ.log("thresh "+real_threshold+"\n");
		//***************FOR SOME REASON I HAD TO FLIP THE < to > for the mask on 9-26, this also required inverting in the macro before the second analyze particles call**********/
		for (int i=0; i<width*height; i++)
		{
			int pix=(int)(premask_pix[i]&0xffff);
			if ((double)pix>real_threshold) mask_pix[i]=(byte)255;
		}
		ImagePlus mask_img;
		mask_img=new ImagePlus("New Image", new ByteProcessor(width,height,mask_pix,null));
		mask_img.show();
		mask_img.updateAndDraw();
		IJ.run("Set Measurements...", "area mean centroid center fit integrated redirect=None decimal=3");
		IJ.run("Analyze Particles...", "size=100000-840000 circularity=0.00-1.00 show=Masks display exclude clear");
		ImagePlus masked_img=WindowManager.getCurrentImage();
		masked_img.setTitle("WormMask");
		mask_img.close();
		insitu_img.setTitle("InSitu");
	}
}
/*
tmp=getTitle();
run("Invert");

run("Split Channels");
selectWindow("C3-"+tmp);
rename("B");
selectWindow("C2-"+tmp);
rename("G");
selectWindow("C1-"+tmp);
rename("R");
imageCalculator("Average create 32-bit", "R","G");
rename("AvgGR");
imageCalculator("Subtract create 32-bit", "AvgGR","B");
rename("InSitu");
selectWindow("AvgGR");
close();
selectWindow("B");
setAutoThreshold("Default dark");
run("Convert to Mask");
rename("WormMask");
run("Set Measurements...", "area mean centroid center fit integrated redirect=None decimal=3");
run("Analyze Particles...", "size=100000-Infinity circularity=0.00-1.00 show=Masks clear");
rename("tmp");
selectWindow("WormMask");
close();
selectWindow("tmp");
rename("WormMask");
number_worms=nResults;
run("Find Connected Regions", "allow_diagonal display_one_image display_results regions_for_values_over=100 minimum_number_of_points=1 stop_after=-1");
rename("Regions");
selectWindow("WormMask");
run("Set Measurements...", "area mean centroid center fit integrated redirect=None decimal=3");
run("Analyze Particles...", "size=100000-Infinity circularity=0.00-1.00 show=Nothing display clear");
*/