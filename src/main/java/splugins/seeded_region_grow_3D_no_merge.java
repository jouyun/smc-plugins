package splugins;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class seeded_region_grow_3D_no_merge implements PlugIn {

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
	
	float [][][] whole_byte_img;
	float [][][] raw_data;
	float threshold;
	byte [] output_img;
	float noise_background;
	float drop_threshold;
	float [] peak_intensities;
	float [] points_added;
	int z_ratio;
	
	class MyIntPoint
	{
		int x=0;
		int y=0;
		int z=0;
		MyIntPoint(int a, int b, int c)
		{
			x=a;
			y=b;
			z=c;
		}
		void set(int a, int b, int c)
		{
			x=a;
			y=b;
			z=c;
		}
		MyIntPoint()
		{
			x=0;
			y=0;
			z=0;
		}
	}
	ArrayList <ArrayList <MyIntPoint>> point_list=new ArrayList<ArrayList <MyIntPoint>>();
	ArrayList <MyIntPoint> tmp_point_list=new ArrayList <MyIntPoint>();
	ArrayList <MyIntPoint> object_point_list[];
	
	public void run(String arg) {
		
		ImagePlus imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
	    

        
        MyIntPoint [] seed_points=retrieve_seeds();
        whole_byte_img=new float[width][height][slices];
        peak_intensities=new float[seed_points.length];
        points_added=new float[seed_points.length];
        for (int i=0; i<seed_points.length; i++)
        {
        	MyIntPoint tmp=seed_points[i];
        	whole_byte_img[tmp.x][tmp.y][tmp.z]=(byte) (i+1);
        }
        int number_seeds=seed_points.length;
        object_point_list=(ArrayList<MyIntPoint>[]) new ArrayList[number_seeds];
        for (int i=0; i<seed_points.length; i++)
        {
        	object_point_list[i]=new ArrayList<MyIntPoint>();
        }
        
        raw_data=Dilate3D.make_3D_float_3D(imp,  0,  0);

        for (int i=0; i<seed_points.length; i++)
        {
        	MyIntPoint tmp_pt=new MyIntPoint(seed_points[i].x, seed_points[i].y, seed_points[i].z);
        	ArrayList <MyIntPoint> tmp_list=new ArrayList <MyIntPoint>();
        	tmp_list.add(tmp_pt);
        	point_list.add(tmp_list);
        	peak_intensities[i]=raw_data[tmp_pt.x][tmp_pt.y][tmp_pt.z];
        	
        	object_point_list[i].add(tmp_pt);
        }
        color_idx=0;
        boolean done=false;
        int loops=0;
		while (!done)
		{
			loops++;
			current_seed=0;
			for (ListIterator jF=point_list.listIterator();jF.hasNext();)
			{
				current_seed++;
				ArrayList <MyIntPoint> my_list=(ArrayList <MyIntPoint>)jF.next();
				for (ListIterator pF=my_list.listIterator(); pF.hasNext();)
				{
					MyIntPoint curpt=(MyIntPoint)pF.next();
					check_neighbor(curpt.x+1, curpt.y,curpt.z);
					check_neighbor(curpt.x-1, curpt.y, curpt.z);
					check_neighbor(curpt.x, curpt.y+1, curpt.z);
					check_neighbor(curpt.x, curpt.y-1, curpt.z);
					if (loops%z_ratio==0) check_neighbor(curpt.x,curpt.y,curpt.z+1);
					if (loops%z_ratio==0) check_neighbor(curpt.x,curpt.y,curpt.z-1);
					/*if (check_neighbor(curpt.x+1, curpt.y,curpt.z)) ctr++;
					if (check_neighbor(curpt.x-1, curpt.y, curpt.z)) ctr++;
					if (check_neighbor(curpt.x, curpt.y+1, curpt.z)) ctr++;
					if (check_neighbor(curpt.x, curpt.y-1, curpt.z)) ctr++;
					if (check_neighbor(curpt.x,curpt.y,curpt.z+1)) ctr++;
					if (check_neighbor(curpt.x,curpt.y,curpt.z-1)) ctr++;*/
				}
				my_list.clear();
				for (ListIterator pF=tmp_point_list.listIterator();pF.hasNext();)
				{
					MyIntPoint curpt=(MyIntPoint)pF.next();
					my_list.add(curpt);
				}
				tmp_point_list.clear();
			}
			int ctr=0;
			for (int i=0; i<point_list.size(); i++)
			{
				ctr+=point_list.get(i).size();
			}
			if (ctr==0) done=true;
		}

        for (int i=0; i<seed_points.length; i++)
        {
        	if (object_point_list[i].size()<minimum_pix||object_point_list[i].size()>maximum_pix)
        	{
        		for (ListIterator pF=object_point_list[i].listIterator(); pF.hasNext();)
        		{
        			MyIntPoint curpt=(MyIntPoint)pF.next();
        			whole_byte_img[curpt.x][curpt.y][curpt.z]=0;
        		}
        	}
        }
		ImagePlus new_img=Dilate3D.make_3D_ImagePlusFloat3D(whole_byte_img, width, height, imp.getStackSize());
        new_img.show();
        new_img.updateAndDraw();
        
		
	}

	private MyIntPoint[] retrieve_seeds()
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
		gd.addNumericField("Noise background", 0, 1);
		gd.addNumericField("Drop Threshold", 0.2, 1);
		gd.addNumericField("Minimum size", 30, 0);
		gd.addNumericField("Maximum size", 500, 0);
		gd.addNumericField("Z ratio", 1, 0);
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
		noise_background=(float)gd.getNextNumber();
		drop_threshold=(float)gd.getNextNumber();
		minimum_pix=(int)gd.getNextNumber();
		maximum_pix=(int)gd.getNextNumber();
		z_ratio=(int)gd.getNextNumber();
		return find_mask_points(mask_img);
	}
	
	private MyIntPoint[] find_mask_points(ImagePlus img)
	{
		MyIntPoint[] rtn=new MyIntPoint[0];
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
						MyIntPoint tmp=new MyIntPoint();
						tmp.set(x, y, i);
						MyIntPoint [] new_rtn=new MyIntPoint[num_found+1];
						for (int j=0; j<rtn.length; j++) new_rtn[j]=new MyIntPoint(rtn[j].x, rtn[j].y, rtn[j].z);
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
	
	boolean check_neighbor(int x, int y, int z)
	{
		if (z<0||z==slices) return false;
		if (x<0||x==width||y<0||y==height) return false;
		if (raw_data[x][y][z]-noise_background<(peak_intensities[current_seed-1]-noise_background)*drop_threshold) return false;
		if (raw_data[x][y][z]<threshold) return false;
		if (whole_byte_img[x][y][z]!=0) return false;
		whole_byte_img[x][y][z]=current_seed;
		//whole_byte_img[x][y][z]=(float)(points_added);
		MyIntPoint curpt=new MyIntPoint(x, y,z);
		tmp_point_list.add(curpt);
		
		object_point_list[current_seed-1].add(new MyIntPoint(x,y,z));
		
		return true;
	}
}
