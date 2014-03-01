package splugins;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
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


public class make_gaussian implements PlugIn {

	@Override
	public void run(String arg) {
		GenericDialog dlg=new GenericDialog("Gauss maker");
		dlg.addNumericField("Sigma", 10, 0);
		dlg.addNumericField("Width", 256, 0);
		dlg.addNumericField("Height", 256, 0);
		dlg.showDialog();
		double sigma=dlg.getNextNumber();
		int width=(int)dlg.getNextNumber();
		int height=(int)dlg.getNextNumber();
		ImagePlus imp=new ImagePlus("Gauss", new FloatProcessor(width, height));
		float [] pix=(float [])imp.getProcessor().getPixels();
		for (int j=0; j<imp.getWidth(); j++)
		{
			for (int i=0; i<imp.getHeight(); i++)
			{
				pix[i*imp.getWidth()+j]=(float)Math.exp(-(Math.pow(i-height/2,2)+Math.pow(j-width/2,2))/2.0/sigma/sigma);
			}
		}
		imp.show();
		imp.updateAndDraw();

	}

}
