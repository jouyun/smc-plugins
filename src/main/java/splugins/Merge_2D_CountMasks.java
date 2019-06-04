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

public class Merge_2D_CountMasks implements PlugIn {

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
		public boolean equals(Object object)
		{
			boolean same=false;
			if (object!=null && object instanceof MyIntPoint)
			{
				if (x==((MyIntPoint) object).x && y==((MyIntPoint)object).y)
				{
					same=true;
				}
			}
			return same;
		}
	}
	ArrayList <ArrayList <MyIntPoint>> point_list=new ArrayList<ArrayList <MyIntPoint>>();
	ArrayList <MyIntPoint> tmp_point_list=new ArrayList <MyIntPoint>();
	ArrayList <MyIntPoint> object_point_list[];
	int [] actual_object;
	int[] max_idx_this_slice;
	ImagePlus imp;
	public void merge_objects(int idA, int idB)
	{
		if (idA>idB)
		{
			for (int i=0; i<actual_object.length; i++)
			{
				if (actual_object[i]==idA) actual_object[i]=idB;
			}
		}
		if (idB>idA)
		{
			for (int i=0; i<actual_object.length; i++)
			{
				if (actual_object[i]==idB) actual_object[i]=idA;
			}
		}
	}
	public boolean list_contains(ArrayList <MyIntPoint> lst, MyIntPoint pt)
	{
		for (ListIterator pF=lst.listIterator(); pF.hasNext();)
		{
			MyIntPoint curpt=(MyIntPoint)pF.next();
			if (curpt.x==pt.x&&curpt.y==pt.y) return true;
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	public void run(String arg) {
		
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
	    

		//Find how many objects there are
		int max_object_number=0;
		for (int s=0; s<slices; s++)
		{
			short [] current_pix=(short [])imp.getStack().getProcessor(1+s).getPixels();
			for (int i=0; i<width*height; i++) 
			{
				int curval=current_pix[i]&0xffff;
				if (curval>max_object_number) max_object_number=curval;
			}
		}
        //IJ.log("Max object number: " + max_object_number);
		
		//Get the points for each object
		object_point_list=(ArrayList<MyIntPoint>[]) new ArrayList[(int) max_object_number+1];
		for (int m=0; m<max_object_number+1; m++)
		{
			object_point_list[m]=new ArrayList<MyIntPoint>();
		}	
		max_idx_this_slice=new int[slices+1];
		int current_max=0;
		for (int s=0; s<slices; s++)
		{
			short [] current_pix=(short [])imp.getStack().getProcessor(1+s).getPixels();
			for (int x=0; x<width; x++) 
			{
				for (int y=0; y<height; y++)
				{
					int idx=current_pix[x+y*width]&0xffff;
					if (idx>0) 
					{
						object_point_list[(int)idx].add(new MyIntPoint(x,y,s));
						if (idx>current_max) current_max=idx;
					}
				}
			}
			max_idx_this_slice[s+1]=current_max;
			//IJ.log("Slice: "+(s+1) +" max: "+ max_idx_this_slice[s+1]);
		}
        
		//Initialize actual_object array
		actual_object = new int [max_object_number+1];
		for (int i=0; i<max_object_number+1; i++) actual_object[i]=i;
		
		//Loop through z, merge two objects when necessary
		/*for (int z=0; z<slices-1; z++) 
		{
			int min_s=max_idx_this_slice[z]+1;
			int max_s=max_idx_this_slice[z+1];
			int min_t=max_s+1;
			int max_t=max_idx_this_slice[z+2];
			IJ.showProgress(z, slices);
			for (int s=min_s; s<max_s+1; s++)
			{
				for (int t=min_t; t<max_t+1; t++)
				{
					if (object_point_list[t].size()>object_point_list[s].size())
					{
						ArrayList <MyIntPoint> s_object=object_point_list[s];
						int frac=0;
						for (ListIterator pF=s_object.listIterator(); pF.hasNext();)
						{
							MyIntPoint curpt=(MyIntPoint)pF.next();
							if (list_contains(object_point_list[t], curpt)) frac++;
						}
						if ((double)frac/(double)s_object.size()<0.5) continue;
					}
					else
					{
						ArrayList <MyIntPoint> t_object=object_point_list[t];
						int frac=0;
						for (ListIterator pF=t_object.listIterator(); pF.hasNext();)
						{
							MyIntPoint curpt=(MyIntPoint)pF.next();
							if (list_contains(object_point_list[s], curpt)) frac++;
						}
						if ((double)frac/(double)t_object.size()<0.5) continue;
						
					}
					//If I am here, then these two objects need merging
					//IJ.log("Merging:  "+s+"  "+ t);
					merge_objects(actual_object[s], actual_object[t]);
				}
			}
		}*/
		alternative_searcher();
		IJ.showProgress(slices, slices);
		IJ.log("Done merging");
		//Loop over each object and dilate once as necessary (only x and y for now)
		for (int m=1; m<max_object_number; m++)
		{
			ArrayList <MyIntPoint> current_object=object_point_list[m];
			for (ListIterator pF=current_object.listIterator(); pF.hasNext();)
			{
				MyIntPoint curpt=(MyIntPoint)pF.next();
				short [] pix=(short [])imp.getStack().getProcessor(curpt.z+1).getPixels();
				int x=curpt.x, y=curpt.y;
				pix[x+y*width]=(short) actual_object[m];
			}
		}
		

        imp.show();
        imp.updateAndDraw();
        
		
	}
	//Make this faster, it is incredibly slow, so that it will look for all of the objects that are above it at all using the original image first
	public void alternative_searcher()
	{
		for (int z=0; z<slices-1; z++) 
		{
			IJ.showProgress(z, slices);
			
			int min_s=max_idx_this_slice[z]+1;
			int max_s=max_idx_this_slice[z+1];
			short [] current_pix=(short [])imp.getStack().getProcessor(2+z).getPixels();
			
			for (int s=min_s; s<max_s+1; s++)
			{
				//First find all possible t's that might be above
				ArrayList <Integer> t_list=new ArrayList <Integer> ();
				
				//Loop over all points in this s object, add anything above it to t_list
				ArrayList <MyIntPoint> s_object=object_point_list[s];
				for (ListIterator pF=s_object.listIterator(); pF.hasNext();)
				{
					MyIntPoint curpt=(MyIntPoint)pF.next();
					int idx=current_pix[curpt.x+curpt.y*width]&0xffff;
					if (idx<1) continue;
					if (!t_list.contains(idx)) t_list.add(idx);
				}
				
				for (ListIterator ppF=t_list.listIterator(); ppF.hasNext();)
				{
					int t=(Integer)ppF.next();
					if (object_point_list[t].size()>object_point_list[s].size())
					{
						int frac=0;
						for (ListIterator pF=s_object.listIterator(); pF.hasNext();)
						{
							MyIntPoint curpt=(MyIntPoint)pF.next();
							if (list_contains(object_point_list[t], curpt)) frac++;
						}
						if ((double)frac/(double)s_object.size()<0.5) continue;
					}
					else
					{
						ArrayList <MyIntPoint> t_object=object_point_list[t];
						int frac=0;
						for (ListIterator pF=t_object.listIterator(); pF.hasNext();)
						{
							MyIntPoint curpt=(MyIntPoint)pF.next();
							if (list_contains(object_point_list[s], curpt)) frac++;
						}
						if ((double)frac/(double)t_object.size()<0.5) continue;
						
					}
					//If I am here, then these two objects need merging
					//IJ.log("Merging:  "+s+"  "+ t);
					merge_objects(actual_object[s], actual_object[t]);
				}
				
			}
		}
	}
}
