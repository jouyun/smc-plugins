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



public class shift_z_and_wrap implements PlugIn {

	@Override
	public void run(String arg) {
		ImagePlus imp=WindowManager.getCurrentImage();
		ImageStack mystack=imp.getStack();
		int width=imp.getWidth(), height=imp.getHeight();
		ImagePlus new_img=NewImage.createShortImage("TempImg", width, height, mystack.getSize(), NewImage.FILL_BLACK);
		
		GenericDialog gd = new GenericDialog("StackReg");
		gd.addNumericField("Shift:  ", 11, 0);
		gd.showDialog();
		short shift=(short)gd.getNextNumber();
		
		for (int i=0; i<mystack.getSize(); i++)
		{
			short[] src=(short [])mystack.getProcessor(i+1).getPixels();
			short[] target=(short [])new_img.getStack().getProcessor(((i+shift+mystack.getSize())%mystack.getSize())+1).getPixels();
			for (int j=0; j<width*height; j++) target[j]=src[j];
		}
		new_img.show();
		new_img.updateAndDraw();
	}

}
