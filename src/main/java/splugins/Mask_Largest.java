package splugins;
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


public class Mask_Largest implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus img;
		img=WindowManager.getCurrentImage();
		int number_indices=100000;
		int [] indices=new int[number_indices];
		short [] pix=(short [])img.getProcessor().getPixels();
		int width=img.getWidth(), height=img.getHeight();
		for (int i=0; i<width*height; i++)
		{
			indices[0xffff&pix[i]]++;
		}
		int max=0, m_idx=0;
		for (int i=1; i<number_indices; i++)
		{
			if (indices[i]>max)
			{
				max=indices[i];
				m_idx=i;
			}
		}
		for (int i=0; i<width*height; i++)
		{
			if (pix[i]!=m_idx) pix[i]=0&0xffff;
			else pix[i]=255;
		}
		img.updateAndDraw();

	}

}
