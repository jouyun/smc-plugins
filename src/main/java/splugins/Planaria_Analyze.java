package splugins;
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



public class Planaria_Analyze implements PlugIn{
	ImagePlus imp;
	int width;
	int height;
	class MyPoints{
		double x;
		double y;
	}
	class Worm {
		double major;
		double minor;
		double angle;
		double x_center;
		double y_center;
	}
	class MyIntPoints{
		int x;
		int y;
	}
	public double pointToLineDistance(MyPoints A, MyPoints B, MyPoints P)
	{
		double normalLength=Math.hypot(B.x-A.x,B.y-A.y);
		return Math.abs((P.x-A.x)*(B.y-A.y)-(P.y-A.y)*(B.x-A.x))/normalLength;
		
	}
	public void run(String args)
	{
		double shift=10.0, inner_major=0.5, inner_minor=0.5;
		double threshold=0.25;  //Somewhere between 0.6 and 0.75 seems to work
	
		
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		if (admissibleImageList.length == 0) return;
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("In situ process");
		gd.addChoice("Region image:", sourceNames, admissibleImageList[0].getTitle());
		gd.addChoice("In situ image:", sourceNames, admissibleImageList[0].getTitle());
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		int regionIndex=gd.getNextChoiceIndex();
		int insituIndex=gd.getNextChoiceIndex();
		
		ImagePlus region_img, mask_img, insitu_img;
		region_img=admissibleImageList[regionIndex];
		insitu_img=admissibleImageList[insituIndex];
		width=region_img.getWidth();
		height=region_img.getHeight();
		
		float[] insitu_pix=(float []) insitu_img.getProcessor().getPixels();
		short[] region_pix=(short []) region_img.getProcessor().getPixels();
		
		
		ResultsTable table=ResultsTable.getResultsTable();
		ImagePlus new_img=NewImage.createFloatImage("TempImg", width, height, 1, NewImage.FILL_BLACK);
		ImageStack new_stack=new_img.getStack();
		IJ.log("\\Clear");
		for (int i=0; i<table.getCounter(); i++)
		{
			Worm tmp=new Worm();
			tmp.major=table.getValue("Major", i);
			tmp.minor=table.getValue("Minor", i);
			tmp.angle=table.getValue("Angle", i);
			tmp.x_center=(int)table.getValue("XM", i);
			tmp.y_center=(int)table.getValue("YM", i);
			MyPoints A=new MyPoints(), B=new MyPoints(), C=new MyPoints(), P=new MyPoints();
			A.x=tmp.x_center;
			A.y=tmp.y_center;
			
			B.x=A.x+shift;
			B.y=A.y-Math.tan(tmp.angle/360.0*2.0*3.141592653589)*shift;
			C.x=A.x+shift;
			C.y=A.y-Math.tan((tmp.angle+90.0)/360.0*2.0*3.141592653589)*shift;
			float [] new_img_pix=(float [])new_img.getProcessor().getPixels();
			float[] current_pix=new float[width*height];
			int in_pix=0, out_pix=0, above_pix=0;
			double in_sum=0.0, out_sum=0.0, above_sum=0.0, out_std=0.0;
			double min=1000000000000.0;
			ArrayList <MyIntPoints> in_pixels=new ArrayList <MyIntPoints>();
			ArrayList <MyIntPoints> out_pixels=new ArrayList <MyIntPoints>();
			ArrayList <MyIntPoints> all_pixels=new ArrayList <MyIntPoints>();
			ArrayList <MyIntPoints> on_line_pixels=new ArrayList <MyIntPoints>();
			ArrayList <ArrayList> contiguous_objects=new ArrayList <ArrayList>();
			
			for (int j=0; j<width; j++)
			{
				for (int k=0; k<height; k++)
				{
					if ((double)(insitu_pix[j+k*width])<min) min=(double)(insitu_pix[j+k*width]);
					if (((region_pix[j+k*width]&0xffff)!=(i+1))) continue;
					//if ((double)(insitu_pix[j+k*width])<min) min=(double)(insitu_pix[j+k*width]);
					MyIntPoints tpt=new MyIntPoints();
					tpt.x=j;
					tpt.y=k;
					all_pixels.add(tpt);
					P.x=j;
					P.y=k;
					if (pointToLineDistance(A,C,P)>inner_major*tmp.major/2.0||pointToLineDistance(A,B,P)>inner_minor*tmp.minor/2.0)
					{
						out_pixels.add(tpt);
					}
					else
					{
						in_pixels.add(tpt);
						if (pointToLineDistance(A,B,P)<1) on_line_pixels.add(tpt);
					}
				}
			}
			//Have all the different classes of pixels
			//Now subtract the min
			for (ListIterator jF=all_pixels.listIterator();jF.hasNext();) 
			{ 
				MyIntPoints curpt = (MyIntPoints)jF.next();
				insitu_pix[curpt.x+curpt.y*width]=insitu_pix[curpt.x+curpt.y*width]-(float)min;
			}
			//Sum up the out regions, old noise calculation, now will use 33rd percentile along middle line
			/*for (ListIterator jF=out_pixels.listIterator();jF.hasNext();) 
			{ 
				MyIntPoints curpt = (MyIntPoints)jF.next();
				out_sum=out_sum+(double)(insitu_pix[curpt.x+curpt.y*width]);
			}
			out_sum=out_sum/(double)out_pix;*/
			
			out_pix=out_pixels.size();
			in_pix=in_pixels.size();
			
			float [] on_line_values=new float[on_line_pixels.size()];
			int tmpctr=0;
			for (ListIterator jF=on_line_pixels.listIterator(); jF.hasNext();)
			{
				MyIntPoints curpt = (MyIntPoints)jF.next();
				on_line_values[tmpctr]=insitu_pix[curpt.x+curpt.y*width];
				tmpctr++;
			}
			Arrays.sort(on_line_values);
			out_sum=on_line_values[(int)(on_line_pixels.size()/3)];
			
			//Find standard deviation of out region
			for (ListIterator jF=out_pixels.listIterator();jF.hasNext();) 
			{ 
				MyIntPoints curpt = (MyIntPoints)jF.next();
				out_std=out_std+((double)(insitu_pix[curpt.x+curpt.y*width])-out_sum)*((double)(insitu_pix[curpt.x+curpt.y*width])-out_sum);
			}
			
			out_std=Math.sqrt(out_std/(double)out_pix);
			
			for (ListIterator jF=in_pixels.listIterator();jF.hasNext();) 
			{ 
				MyIntPoints curpt = (MyIntPoints)jF.next();
				current_pix[curpt.x+curpt.y*width]=(float) ((float) (insitu_pix[curpt.x+curpt.y*width]-out_sum)/out_sum);
				new_img_pix[curpt.x+curpt.y*width]=(float) ((float) (insitu_pix[curpt.x+curpt.y*width]-out_sum)/out_sum);
			}
			
			//Do whole worm in new_img_pix for now
			for (ListIterator jF=all_pixels.listIterator();jF.hasNext();) 
			{ 
				MyIntPoints curpt = (MyIntPoints)jF.next();
				new_img_pix[curpt.x+curpt.y*width]=(float) ((float) (insitu_pix[curpt.x+curpt.y*width]-out_sum)/out_sum);
			}
			
			
			in_sum=0.0;
			float max_pix=-10.0f;
			int max_idx=-1, ctr=0;
			for (ListIterator jF=in_pixels.listIterator();jF.hasNext();) 
			{ 
				MyIntPoints curpt = (MyIntPoints)jF.next();
				if (current_pix[curpt.x+curpt.y*width]>threshold) 
				{
					above_pix++;
					above_sum=above_sum+current_pix[curpt.x+curpt.y*width];
				}
				in_sum=in_sum+current_pix[curpt.x+curpt.y*width];
			}
			/*Now try to find the max in_situ along the line in the middle of the worm
			 * and see how far up and down the line you can go and remain above threshold
			 */
			
			//This version finds the max on the line, found it sometimes found the septum
			//like object at the end when the pharynx was weakly stained, attempting to now 
			//find the point closest to the center of the worm that is above threshold
			
			boolean keep_going=false;
			MyIntPoints tmppt=new MyIntPoints();
			for (ListIterator jF=on_line_pixels.listIterator(); jF.hasNext();)
			{
				MyIntPoints curpt = (MyIntPoints)jF.next();
				if (current_pix[curpt.x+curpt.y*width]>threshold)
				{
					tmppt=curpt;
					keep_going=true;
					break;
				}
			}
			while (keep_going)
			{
				//Find contiguous region centered at this point
				ArrayList <MyIntPoints> current_contiguous=new ArrayList <MyIntPoints>();
				current_contiguous.add(tmppt);
				
				ctr=1;
				int old_counter=0;
				float PI=-3.141592653589f;
				new_img_pix[tmppt.x+tmppt.y*width]=PI;
				while (ctr>old_counter)
				{
					old_counter=ctr;
					for (ListIterator jF=on_line_pixels.listIterator(); jF.hasNext();)
					{
						MyIntPoints curpt = (MyIntPoints)jF.next();
						if (new_img_pix[curpt.x+curpt.y*width]==PI) continue;
						for (int m=-1; m<2; m++)
						{
							for (int n=-1; n<2; n++)
							{
								if (new_img_pix[curpt.x+m+(curpt.y+n)*width]==PI&&current_pix[curpt.x+m+(curpt.y+n)*width]>threshold)
								{
									new_img_pix[curpt.x+curpt.y*width]=PI;
									ctr++;
									current_contiguous.add(curpt);
									break;
								}
							}
							if (ctr>old_counter) break;
						}
					}
				}
				keep_going=false;
				for (ListIterator jF=on_line_pixels.listIterator(); jF.hasNext();)
				{
					MyIntPoints curpt = (MyIntPoints)jF.next();
					if (new_img_pix[curpt.x+curpt.y*width]>threshold)
					{
						tmppt=curpt;
						keep_going=true;
						break;
					}
				}
				contiguous_objects.add(current_contiguous);
				
			}
			//Now find longest chain
			int longest=0, lidx=0, ctrs=0;
			for (ListIterator jF=contiguous_objects.listIterator(); jF.hasNext();)
			{
				ArrayList <MyIntPoints> tmp_contig=(ArrayList)jF.next();
				if (tmp_contig.size()>longest)
				{
					longest=tmp_contig.size();
					lidx=ctrs;
				}
				ctrs++;
			}
			//Put back original pixels
			for (ListIterator jF=in_pixels.listIterator();jF.hasNext();) 
			{ 
				MyIntPoints curpt = (MyIntPoints)jF.next();
				new_img_pix[curpt.x+curpt.y*width]=current_pix[curpt.x+curpt.y*width];
			}
			//Paint the pixels with something
			float something=-1.1f;
			int longest_length=0;
			if (contiguous_objects.size()>0)
			{
				ArrayList<MyIntPoints> longest_contiguous=contiguous_objects.get(lidx);
				for (ListIterator jF=longest_contiguous.listIterator();jF.hasNext();)
				{
					MyIntPoints curpt = (MyIntPoints)jF.next();
					new_img_pix[curpt.x+curpt.y*width]=something;
				}
				longest_length=longest_contiguous.size();
			}
			
			
			
			
			/*for (ListIterator jF=on_line_pixels.listIterator(); jF.hasNext();)
			{
				MyIntPoints curpt = (MyIntPoints)jF.next();
				if (current_pix[curpt.x+curpt.y*width]>max_pix)
				{
					max_pix=new_img_pix[curpt.x+curpt.y*width];
					max_idx=ctr;
				}
				ctr++;				
			}
			MyIntPoints tmppt=(MyIntPoints)on_line_pixels.get(max_idx);
			
			ctr=1;
			int old_counter=0;
			float PI=-3.141592653589f;
			new_img_pix[tmppt.x+tmppt.y*width]=PI;
			while (ctr>old_counter)
			{
				old_counter=ctr;
				for (ListIterator jF=on_line_pixels.listIterator(); jF.hasNext();)
				{
					MyIntPoints curpt = (MyIntPoints)jF.next();
					if (new_img_pix[curpt.x+curpt.y*width]==PI) continue;
					for (int m=-1; m<2; m++)
					{
						for (int n=-1; n<2; n++)
						{
							if (new_img_pix[curpt.x+m+(curpt.y+n)*width]==PI&&current_pix[curpt.x+m+(curpt.y+n)*width]>threshold)
							{
								new_img_pix[curpt.x+curpt.y*width]=PI;
								ctr++;
								break;
							}
						}
						if (ctr>old_counter) break;
					}
				}
			}*/
			double line_distance=(double)(longest_length)/(double)on_line_pixels.size()*inner_major*tmp.major;
			IJ.log("Worm " + i+" had "+(double)above_pix/(double)(in_pix+out_pix)+" "+above_sum+" "+above_sum/(double)above_pix+" "+in_sum/(double)in_pix+" "+line_distance+" "+(line_distance/tmp.major)+"\n");
			
		}
		new_img.show();
		new_img.updateAndDraw();
	}
	
	private ImagePlus[] createAdmissibleImageList () 
	{
		final int[] windowList = WindowManager.getIDList();
		//IJ.log(""+windowList[0]+"\n");
		final Stack stack = new Stack();
		for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) 
		{
			final ImagePlus imp = WindowManager.getImage(windowList[k]);
			//IJ.log(imp.getTitle()+"\n");
			//if ((imp != null) && (imp.getType() == imp.GRAY32)) 
			{
				//IJ.log("got one");
				stack.push(imp);
			}
		}
		//IJ.log("Stack size:  " + stack.size() + "\n");
		final ImagePlus[] admissibleImageList = new ImagePlus[stack.size()];
		int k = 0;
		while (!stack.isEmpty()) {
			admissibleImageList[k++] = (ImagePlus)stack.pop();
		}
		if (k==0 && (windowList != null && windowList.length > 0 )){
			IJ.error("No float images, convert to float and try again");
		}
		return(admissibleImageList);
	} /* end createAdmissibleImageList */

}
//run("Analyze Particles...", "size=100000-Infinity circularity=0.00-1.00 show=Masks display clear");
//run("Find Connected Regions", "allow_diagonal display_one_image display_results regions_for_values_over=100 minimum_number_of_points=1 stop_after=-1");