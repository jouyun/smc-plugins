package splugins;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import splugins.seeded_region_grow_3D_no_merge.MyIntPoint;

public class compute_3D_blob_statistics implements PlugIn {

	int width;
	int height;
	int slices;
	int cur_slice;
	int frames;
	int channels;
	int current_seed;
	int minimum_pix;
	int maximum_pix;
	int radA_low;
	int radA_high;
	int radB_low;
	int radB_high;
	byte color_idx;
	
	float [][][] whole_byte_img;
	float [][][][] raw_data;
	float threshold;
	byte [] output_img;
	float noise_background;
	float drop_threshold;
	float [] peak_intensities;
	float [] points_added;
	ImagePlus mask_img;
	
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
		@Override public boolean equals(Object o)
		{
			return (o instanceof MyIntPoint) && (this.x==((MyIntPoint) o).x) && (this.y==((MyIntPoint) o).y) && (this.z==((MyIntPoint) o).z);
		}
	}
	ArrayList <ArrayList <MyIntPoint>> b_list=new ArrayList<ArrayList <MyIntPoint>>();
	ArrayList <MyIntPoint> tmp_point_list=new ArrayList <MyIntPoint>();
	ArrayList <MyIntPoint> blob_point_list[];
	ArrayList <MyIntPoint> radA_point_list[];
	ArrayList <MyIntPoint> radB_point_list[];
	ArrayList<MyIntPoint> current_list;
	@Override
	public void run(String arg0) {
		
		ImagePlus imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		int max_num_points=65535;
	    
		blob_point_list=(ArrayList<MyIntPoint>[]) new ArrayList[max_num_points];
		radA_point_list=(ArrayList<MyIntPoint>[]) new ArrayList[max_num_points];
		radB_point_list=(ArrayList<MyIntPoint>[]) new ArrayList[max_num_points];
        for (int i=0; i<max_num_points; i++)
        {
        	blob_point_list[i]=new ArrayList<MyIntPoint>();
        	radA_point_list[i]=new ArrayList<MyIntPoint>();
        	radB_point_list[i]=new ArrayList<MyIntPoint>();
        }
        
        //Do the dialog box
        if (GetDialogParameters()==false) return;
		
        //Copy original image into a 3D friendly array
        raw_data=Dilate3D.make_3D_float_3D_multichannel(imp,  0);
        
        //Make a list of ArrayLists, keep track of which x,y,z go with each pixel value
        get_initial_blobs(blob_point_list, mask_img);
		
        //If going to display results in channel 3, leave this in
        for (int z=0; z<slices; z++) for (int x=0; x<width; x++) for (int y=0; y<height; y++) raw_data[x][y][z][2]=0;
        
        //For each item in blob_list
        for (int i=0; i<max_num_points; i++)
        {
        	current_list=blob_point_list[i];
        	if (current_list.size()<1) continue;
        	
        	//IJ.log("i: "+i+", "+current_list.size());
        	
        	//Make a copy for my_list
        	ArrayList <MyIntPoint> this_round_list=new ArrayList <MyIntPoint>();
        	
        	for (ListIterator jF=current_list.listIterator();jF.hasNext();)
			{
        		MyIntPoint tmp=(MyIntPoint)jF.next();
        		this_round_list.add(tmp);
			}
        	
        	//Loop until radA_low
        	step_dilation(radA_low, this_round_list);
        	
        	//Quantify points for all channels using current_list
        	quantify_points(raw_data, current_list, width, height, slices, channels);
        }
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		rslt.incrementCounter();
        rslt.show("Results");
        
        //Displays results (mostly for debugging)
        
        ImagePlus new_img=Dilate3D.make_3D_ImagePlusFloat3D_multichannel(raw_data, width, height, slices, channels);
        new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(channels, slices, frames);
        new_img.show();
        new_img.updateAndDraw();
	}
	
	private void quantify_points(float [][][][] data, ArrayList <MyIntPoint> list, int xs, int ys, int zs, int cs)
	{
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		rslt.incrementCounter();
		
		int num_points=0;
		float [] totals=new float[cs];
		
		for (ListIterator pF=list.listIterator(); pF.hasNext();)
		{
			MyIntPoint curpt=(MyIntPoint)pF.next();
			num_points++;
			for (int c=0; c<cs; c++)
			{
				totals[c]+=data[curpt.x][curpt.y][curpt.z][c];
			}
		}
		
		rslt.addValue("X", list.get(0).x);
		rslt.addValue("Y", list.get(0).y);
		rslt.addValue("Z", list.get(0).z);
		for (int c=0; c<cs; c++)
		{
			rslt.addValue("C"+(c+1), totals[c]/num_points);
		}
		
		//Here is my arbitrary painter, will paint channel 3 with points used based on threshold applied to channel 2
		for (ListIterator pF=list.listIterator(); pF.hasNext();)
		{
			MyIntPoint curpt=(MyIntPoint)pF.next();
			float tmp=totals[1]/num_points;
			if (tmp>threshold)
			{
				//data[curpt.x][curpt.y][curpt.z][2]=255;
				data[curpt.x][curpt.y][curpt.z][2]=tmp;
			}
			else
			{
				//data[curpt.x][curpt.y][curpt.z][2]=128;
				data[curpt.x][curpt.y][curpt.z][2]=tmp;
			}
		}
	}
	
	private void step_dilation(int num_steps, ArrayList <MyIntPoint> this_round_list)
	{
    	for (int s=0; s<num_steps; s++)
    	{
			for (ListIterator pF=this_round_list.listIterator(); pF.hasNext();)
			{
				MyIntPoint curpt=(MyIntPoint)pF.next();
				check_neighbor(curpt.x+1, curpt.y,curpt.z);
				check_neighbor(curpt.x-1, curpt.y, curpt.z);
				check_neighbor(curpt.x, curpt.y+1, curpt.z);
				check_neighbor(curpt.x, curpt.y-1, curpt.z);
				check_neighbor(curpt.x,curpt.y,curpt.z+1);
				check_neighbor(curpt.x,curpt.y,curpt.z-1);
			}
			this_round_list.clear();
			for (ListIterator pF=tmp_point_list.listIterator();pF.hasNext();)
			{
				MyIntPoint curpt=(MyIntPoint)pF.next();
				this_round_list.add(curpt);
			}
			tmp_point_list.clear();
    	}
	}
	
	private void get_initial_blobs(ArrayList <MyIntPoint> blob_list[], ImagePlus img)
	{
		MyIntPoint[] rtn=new MyIntPoint[0];
		int num_found=0; 
		for (int i=0; i<img.getStackSize(); i++)
		{
			short[] pix=(short [])img.getStack().getProcessor(i+1).getPixels();
			
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					int pix_val=pix[y*width+x]&0xffff;
					if (pix_val!=0)
					{
						MyIntPoint tmp=new MyIntPoint();
						tmp.set(x, y, i);
						blob_list[pix_val].add(tmp);
					}
				}
			}
		}		
	}

	private boolean GetDialogParameters()
	{
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		//IJ.log("Good ones:  "+admissibleImageList.length+"\n");		
		if (admissibleImageList.length == 0) return false;
		//IJ.log(""+admissibleImageList.length);
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Image as mask?");
		gd.addChoice("Mask image:", sourceNames, admissibleImageList[0].getTitle());
		gd.addNumericField("First shell lower radius", 5, 0);
		gd.addNumericField("First shell higher radius", 3, 0);
		gd.addNumericField("Second shell lower radius", 5, 0);
		gd.addNumericField("Second shell higher radius", 5, 0);
		gd.addNumericField("Threshold:  ", 1500, 0);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return false;
		}
		
		int tmpIndex=gd.getNextChoiceIndex();
		//IJ.log("First:  " + admissibleImageList[tmpIndex].getTitle() + "\n");
		
		mask_img=admissibleImageList[tmpIndex];
		radA_low=(int)gd.getNextNumber();
		radA_high=(int)gd.getNextNumber();
		radB_low=(int)gd.getNextNumber();
		radB_high=(int)gd.getNextNumber();
		threshold=(float)gd.getNextNumber();
		return true;
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
			if ((imp != null) && (imp.getType() == imp.GRAY16)) 
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
		MyIntPoint curpt=new MyIntPoint(x,y,z);
		if (current_list.contains(curpt)) return false;
		tmp_point_list.add(curpt);
		
		current_list.add(curpt);
		
		return true;
	}
}
