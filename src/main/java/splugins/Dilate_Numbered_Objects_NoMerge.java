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

public class Dilate_Numbered_Objects_NoMerge implements PlugIn {

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
	
	@SuppressWarnings("unchecked")
	public void run(String arg) {
		
		ImagePlus imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
	    

		//Find how man objects there are
		short max_object_number=0;
		for (int s=0; s<slices; s++)
		{
			short [] current_pix=(short [])imp.getStack().getProcessor(1+s).getPixels();
			for (int i=0; i<width*height; i++) 
			{
				if (current_pix[i]>max_object_number) max_object_number=current_pix[i];
			}
		}
        
		//Get the points for each object
		object_point_list=(ArrayList<MyIntPoint>[]) new ArrayList[(int) max_object_number+1];
		for (int m=0; m<max_object_number+1; m++)
		{
			object_point_list[m]=new ArrayList<MyIntPoint>();
		}		
		for (int s=0; s<slices; s++)
		{
			short [] current_pix=(short [])imp.getStack().getProcessor(1+s).getPixels();
			for (int x=0; x<width; x++) 
			{
				for (int y=0; y<height; y++)
				{
					if (current_pix[x+y*width]>0) 
					{
						int object_id=(int)current_pix[x+y*width];
						object_point_list[object_id].add(new MyIntPoint(x,y,s));
					}
				}
			}
		}
        
		//Loop over each object and dilate once as necessary (only x and y for now)
		for (int m=1; m<max_object_number; m++)
		{
			ArrayList <MyIntPoint> current_object=object_point_list[m];
			for (ListIterator pF=current_object.listIterator(); pF.hasNext();)
			{
				MyIntPoint curpt=(MyIntPoint)pF.next();
				short [] pix=(short [])imp.getStack().getProcessor(curpt.z+1).getPixels();
				int x=curpt.x, y=curpt.y;
				for (int xx=-1; xx<2; xx++)
				{
					for (int yy=-1; yy<2; yy++)
					{
						if (x+xx>=width||x+xx<0||y+yy>=height||y+yy<0) continue;
						if (pix[(x+xx)+(y+yy)*width]==0) pix[(x+xx)+(y+yy)*width]=(short)m; 
					}
				}
			}
		}
		

        imp.show();
        imp.updateAndDraw();
        
		
	}
}
