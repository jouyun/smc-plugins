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
import java.util.Scanner;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileWriter;
import ij.io.OpenDialog;

//This will never work, stitches are not usually so simple as a 2x2 array fix for all points

public class Fix_Stitch_Config implements PlugIn {

	@Override
	public void run(String arg) {
		OpenDialog file_chooser=new OpenDialog("Choose file", "", "*.txt");
		String file_name=(file_chooser.getDirectory()+file_chooser.getFileName());
		Scanner s=null;
		int x_dim=5;
		int y_dim=8;
		float [][][] indices = new float[x_dim][y_dim][2];
		int ctr=0;
		//IJ.log("\\Clear");
		GenericDialog gd = new GenericDialog("In situ process");
		gd.addNumericField("X dimension", 5, 0);
		gd.addNumericField("Y dimension", 8, 0);
		gd.addNumericField("GuessX", 819, 1);
		gd.addNumericField("GuessY", 1050, 1);
		gd.addNumericField("Neighborhood to search over", 10, 0);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		float x_guess=803f;
		float y_guess=1060f;
		float neighborhood=10f;
		
		x_dim=(int)gd.getNextNumber();
		y_dim=(int)gd.getNextNumber();
		x_guess=(float)gd.getNextNumber();
		y_guess=(float)gd.getNextNumber();
		neighborhood=(float)gd.getNextNumber();
		try {
			//Open the file and get the data
			s= new Scanner(new BufferedReader(new FileReader(file_name)));
			String tmp;
			float x, y;
			s.nextLine();
			s.nextLine();
			s.nextLine();
			s.nextLine();
			while (s.hasNext())
			{
				tmp=s.nextLine();
				x=Float.parseFloat(tmp.substring(tmp.indexOf("(")+1, tmp.indexOf(",")));
				y=Float.parseFloat(tmp.substring(tmp.indexOf(", ")+1, tmp.indexOf(")")));
				indices[ctr%x_dim][ctr/x_dim][0]=x;
				indices[ctr%x_dim][ctr/x_dim][1]=y;
				//IJ.log("X:  "+x+" Y: "+y);
				ctr++;
			}
			s.close();
			x=1;
			//Find the best fit parameters
			float [][] good_ones=new float [1000][2];
			float [][] good_indices=new float [1000][2];
			int idx_ctr=0;
			for (int i=0; i<x_dim; i++)
			{
				for (int j=0; j<y_dim; j++)
				{
					if(indices[i][j][0]/(float)i>(x_guess-neighborhood)&&indices[i][j][0]/(float)i<(x_guess+neighborhood)&&indices[i][j][1]/(float)j>(y_guess-neighborhood)&&indices[i][j][1]/(float)j<(y_guess+neighborhood))
					{
						good_ones[idx_ctr][0]=indices[i][j][0];
						good_ones[idx_ctr][1]=indices[i][j][1];
						good_indices[idx_ctr][0]=i;
						good_indices[idx_ctr][1]=j;
						idx_ctr++;
					}
					if (i==0&&indices[i][j][1]/(float)j>(y_guess-neighborhood)&&indices[i][j][1]/(float)j<(y_guess+neighborhood))
					{
						good_ones[idx_ctr][0]=indices[i][j][0];
						good_ones[idx_ctr][1]=indices[i][j][1];
						good_indices[idx_ctr][0]=i;
						good_indices[idx_ctr][1]=j;
						idx_ctr++;
					}
					if (j==0&&indices[i][j][0]/(float)i>(x_guess-neighborhood)&&indices[i][j][0]/(float)i<(x_guess+neighborhood))
					{
						good_ones[idx_ctr][0]=indices[i][j][0];
						good_ones[idx_ctr][1]=indices[i][j][1];
						good_indices[idx_ctr][0]=i;
						good_indices[idx_ctr][1]=j;
						idx_ctr++;
					}
				}
			}
			float best_resid=100000000000000.0f;
			float aa=0, ab=0, ba=0, bb=0, current_R;
			for (float i=x_guess-neighborhood; i<x_guess+neighborhood; i+=0.5)
			{
				for (float j=0-neighborhood; j<0+neighborhood; j+=0.5)
				{
					for (float m=0-neighborhood; m<0+neighborhood; m+=0.5)
					{
						for (float n=y_guess-neighborhood; n<y_guess+neighborhood; n+=0.5)
						{
							current_R=0.0f;
							for (int k=0; k<idx_ctr; k++)
							{
								x=good_indices[k][0]*i+good_indices[k][1]*j;
								y=good_indices[k][0]*m+good_indices[k][1]*n;
								current_R+=(good_ones[k][0]-x)*(good_ones[k][0]-x)+(good_ones[k][1]-y)*(good_ones[k][1]-y);
								if (i==x_guess&&j==0&&n==y_guess&&m==0)
								{
									IJ.log("Real: "+good_ones[k][0]+","+good_ones[k][1]);
									IJ.log("Guess: "+x+","+y);
									IJ.log("Current R: "+current_R);
								}
								//IJ.log(""+good_ones[k][0]+","+good_ones[k][1]);
								//IJ.log(""+x+","+y);
							}
							//IJ.log(""+i+","+j+","+m+","+n+","+current_R);
							if (current_R<best_resid)
							{
								best_resid=current_R;
								aa=i;
								ab=j;
								ba=m;
								bb=n;
							}
						}
					}
					
				}
			}
			IJ.log(""+aa+","+ab);
			IJ.log(""+ba+","+bb);
			IJ.log("Best R: "+best_resid);
			for (int k=0; k<idx_ctr; k++)
			{
				x=good_indices[k][0]*aa+good_indices[k][1]*ab;
				y=good_indices[k][0]*ba+good_indices[k][1]*bb;
				IJ.log("Real: "+good_ones[k][0]+","+good_ones[k][1]);
				IJ.log("Guess: "+x+","+y);
				//IJ.log(""+good_ones[k][0]+","+good_ones[k][1]);
				//IJ.log(""+x+","+y);
			}
			
			
			//Output the new file, first will reopen the original and copy what we need to copy
			PrintWriter writer = new PrintWriter("C:\\duh.txt", "UTF-8");
			s= new Scanner(new BufferedReader(new FileReader(file_name)));
			tmp=s.nextLine();
			writer.println(tmp);
			tmp=s.nextLine();
			writer.println(tmp);
			tmp=s.nextLine();
			writer.println(tmp);
			tmp=s.nextLine();
			writer.println(tmp);
			
			for (int j=0; j<y_dim; j++)
			{
				for (int i=0; i<x_dim; i++)
				{
					tmp=s.nextLine();
					x=(float)i*aa+(float)j*ab;
					y=(float)i*ba+(float)j*bb;
					writer.println(tmp.substring(0,(tmp.indexOf("("))+1)+x+", "+y+")");
				}
			}
			s.close();
			writer.close();
			

		}
		catch (IOException e) {}

	}
	
	

}
