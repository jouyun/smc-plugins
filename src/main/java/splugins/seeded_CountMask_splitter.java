package splugins;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import splugins.Count_Objects_In_Objects.Point;
import splugins.seeded_region_grow_3D_watershed.MyIntPoint;

public class seeded_CountMask_splitter implements PlugIn {
	class Point
	{
		int x;
		int y;
		int z;
		Point(int xx, int yy, int zz)
		{
			x=xx;
			y=yy;
			z=zz;
		}
		public boolean equals (Object obj)
		{
			if (this==obj) return true;
			Point other=(Point) obj;
			return (other.x==x && other.y==y && other.z==z);
		}
	}

	int width;
	int height;
	int slices;
	int cur_slice;
	int frames;
	int channels;
	int current_seed;
	int minimum_pix;
	int maximum_pix;
	byte color_idx;
	
	short [][][] whole_byte_img;
	float [][][] raw_data;
	float [][][] border_data;
	float threshold;
	byte [] output_img;
	float noise_background;
	float drop_threshold;
	float [] peak_intensities;
	float [] points_added;
	int z_ratio;
	String border_image;
	int current_max_object_label;
	
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		width=img.getWidth(); 
		height=img.getHeight(); 
		channels=img.getNChannels(); 
		slices=img.getNSlices();
		
		
		Point [] seed_points=retrieve_seeds();
		
		current_max_object_label=0;
		ArrayList<Point>[] blob_list=new ArrayList[10000];
		for (int j=0; j<blob_list.length; j++) blob_list[j]=new ArrayList<Point>();
		
		for (int s=0; s<slices; s++)
		{
			short [] pix=(short [])(img.getStack().getPixels(1+s*channels));
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					if (pix[x+y*width]==0) continue;
					Point p=new Point(x,y,s);
					blob_list[pix[x+y*width]].add(p);
					if (pix[x+y*width]>current_max_object_label) current_max_object_label=pix[x+y*width];
				}
			}
		}
		
		//ImagePlus new_img=NewImage.createShortImage("Result", width, height, slices, NewImage.FILL_BLACK);
		
		
		for (int p=1; p<blob_list.length; p++)
		{
			ArrayList<Point> current_list=blob_list[p];
			//Find seed_points that are contained within the current blob
			ArrayList <Point> seeds_in_blob=new ArrayList<Point>();
			for (ListIterator pF=current_list.listIterator(); pF.hasNext();)
			{
				Point curpt=(Point)pF.next();
				int x=curpt.x, y=curpt.y, z=curpt.z;
				for (int s=0; s<seed_points.length; s++)
				{
					if (curpt.equals(seed_points[s]))
					{
						seeds_in_blob.add(seed_points[s]);
					}
				}
			}
			//Only continue processing blob if there is more than one seed point associated with this blob
			if (seeds_in_blob.size()<2) continue;
			
			//Now loop through all the points in the blob again, if closer to first seed, leave alone, if closer to one of the others,
			//adjust the value in the image
			for (ListIterator pF=current_list.listIterator(); pF.hasNext();)
			{
				Point curpt=(Point)pF.next();
				int x=curpt.x, y=curpt.y, z=curpt.z;
				
				double best_distance=1000000000000.0;
				int best_seed_index=-1;
				
				for (int s=0; s<seeds_in_blob.size(); s++)
				{
					double xx=(double)x-(double)seeds_in_blob.get(s).x;
					double yy=(double)y-(double)seeds_in_blob.get(s).y;
					double zz=(double)z-(double)seeds_in_blob.get(s).z;
    				
    				double dist=xx*xx+yy*yy+zz*zz*z_ratio*z_ratio;
    				if (dist<best_distance)
    				{
    					best_distance=dist;
    					best_seed_index=s;
    				}
				}
				//Now know the best seed, if it is 0 leave alone, if not, add index to current_max_object_label
				short [] pix=(short [])(img.getStack().getPixels(1+z));
				if (best_seed_index==0) continue;
				
				pix[x+width*y]=(short) (current_max_object_label+best_seed_index);
			}
			//After doing all of the points in the blob, readjust current_max_object_label to account for new seeds
			current_max_object_label=current_max_object_label+seeds_in_blob.size()-1;
		}
		img.updateAndDraw();

	}
	
	private Point[] retrieve_seeds()
	{
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		//IJ.log("Good ones:  "+admissibleImageList.length+"\n");		
		if (admissibleImageList.length == 0) return null;
		//IJ.log(""+admissibleImageList.length);
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Image as mask?");
		gd.addChoice("Mask image:", sourceNames, admissibleImageList[0].getTitle());
		gd.addNumericField("Threshold", 1000, 1);
		gd.addNumericField("Z ratio", 1, 0);
		gd.addStringField("Border Image", "Img.tif");
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return null;
		}
		
		int tmpIndex=gd.getNextChoiceIndex();
		//IJ.log("First:  " + admissibleImageList[tmpIndex].getTitle() + "\n");
		
		ImagePlus mask_img;
		mask_img=admissibleImageList[tmpIndex];
		threshold=(float)gd.getNextNumber();
		z_ratio=(int)gd.getNextNumber();
		border_image=gd.getNextString();
		return find_mask_points(mask_img);
	}
	
	private Point[] find_mask_points(ImagePlus img)
	{
		Point[] rtn=new Point[0];
		int num_found=0; 
		for (int i=0; i<img.getStackSize(); i++)
		{
			byte[] pix=(byte [])img.getStack().getProcessor(i+1).getPixels();
			
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					if (pix[y*width+x]!=0)
					{
						Point tmp=new Point(x, y, i);
						Point [] new_rtn=new Point[num_found+1];
						for (int j=0; j<rtn.length; j++) new_rtn[j]=new Point(rtn[j].x, rtn[j].y, rtn[j].z);
						new_rtn[num_found]=tmp;
						rtn=new_rtn;
						num_found++;
					}
				}
			}
		}
		return rtn;
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
			if ((imp != null) && (imp.getType() == imp.GRAY8)) 
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
