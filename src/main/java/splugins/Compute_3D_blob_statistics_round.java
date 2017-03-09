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

public class Compute_3D_blob_statistics_round implements PlugIn{

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
	float [][][] raw_data;
	float threshold;
	float aspect;
	byte [] output_img;
	float noise_background;
	float drop_threshold;
	float [] peak_intensities;
	float [] points_added;
	ImagePlus mask_img;
	ImagePlus img;
	
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
	ArrayList <MyIntPoint> tmp_point_list=new ArrayList <MyIntPoint>();
	ArrayList<MyIntPoint> current_list;
	@Override
	public void run(String arg0) {
		
		img=WindowManager.getCurrentImage();
		width=img.getWidth(); 
		height=img.getHeight();
		slices=img.getNSlices();
		frames=img.getNFrames();
		channels=img.getNChannels();
	    
        //Do the dialog box
        if (GetDialogParameters()==false) return;
		
        //Copy original image into a 3D friendly array
        //IJ.log("About to copy");
        //raw_data=Dilate3D.make_3D_float_3D_multichannel(imp,  0);
        IJ.log("Aspect: "+aspect);
        raw_data=new float[channels][][];
        for (int c=0; c<channels; c++)
        {
        	raw_data[c]=new float[slices][];
        	for (int z=0; z<slices; z++)
        	{
        		raw_data[c][z]=(float [])img.getStack().getPixels(1+c+z*channels);
        	}
        }
        
        //If going to display results in channel 3, leave this in
        for (int z=0; z<slices; z++) for (int x=0; x<width; x++) for (int y=0; y<height; y++) raw_data[2][z][x+y*width]=0;
        
        //Find a blob, process a blob        
		int num_found=0; 
		for (int i=0; i<mask_img.getStackSize(); i++)
		{
			byte[] pix=(byte [])mask_img.getStack().getProcessor(i+1).getPixels();
			
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					int pix_val=pix[y*width+x]&0xffff;
					if (pix_val!=0)
					{
						//Found a blob
						num_found++;
						current_list=new ArrayList <MyIntPoint>();
						MyIntPoint tmp=new MyIntPoint();
						tmp.set(x, y, i);
						//IJ.log(""+num_found+","+x+","+y+","+i);
						
						//Add it to list and make sure byte image has it marked
						//ArrayList <MyIntPoint> this_round_list=new ArrayList <MyIntPoint>();
						current_list.add(tmp);
						//this_round_list.add(tmp);
						((byte [])mask_img.getStack().getPixels(i+1))[x+width*y]=(byte)255;
						
						//Grow it
						step_dilation(radA_low, current_list);
						
						//Quantify it and clear it out of the byte image
						quantify_point(raw_data, current_list, width, height, slices, channels);
					}
				}
			}
		}
        
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		//rslt.incrementCounter();
        rslt.show("Results");
        
        //Displays results (mostly for debugging)
        
        /*ImagePlus new_img=Dilate3D.make_3D_ImagePlusFloat3D_multichannel(raw_data, width, height, slices, channels);
        new_img.setOpenAsHyperStack(true);
		new_img.setDimensions(channels, slices, frames);
        new_img.show();
        new_img.updateAndDraw();*/
	}
	
	private void quantify_point(float [][][] data, ArrayList <MyIntPoint> list, int xs, int ys, int zs, int cs)
	{
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		rslt.incrementCounter();
		
		int num_points=0;
		float [] totals=new float[cs];
		//IJ.log("List size:  "+list.size());
		
		for (ListIterator pF=list.listIterator(); pF.hasNext();)
		{
			MyIntPoint curpt=(MyIntPoint)pF.next();
			num_points++;
			for (int c=0; c<cs; c++)
			{
				//IJ.log("X,Y,Z: "+curpt.x+","+curpt.y+","+curpt.z);
				totals[c]+=data[c][curpt.z][curpt.x+curpt.y*width];
			}
			((byte [])mask_img.getStack().getPixels(curpt.z+1))[curpt.x+width*curpt.y]=(byte)0;
		}
		
		rslt.addValue("X", list.get(0).x);
		rslt.addValue("Y", list.get(0).y);
		rslt.addValue("Z", list.get(0).z);
		rslt.addValue("Image", img.getTitle());
		for (int c=0; c<cs; c++)
		{
			rslt.addValue("C"+(c+1), totals[c]/num_points);
		}
		
		//Here is my arbitrary painter, will paint channel 3 with points used based on threshold applied to channel 2
		for (ListIterator pF=list.listIterator(); pF.hasNext();)
		{
			MyIntPoint curpt=(MyIntPoint)pF.next();
			//float tmp=totals[1]/totals[0];
			float tmp=totals[1]/num_points;
			//IJ.log("z, x, y: "+curpt.z+","+curpt.x+","+curpt.y);
			if (tmp>threshold)
			{
				//data[curpt.x][curpt.y][curpt.z][2]=255;
				data[2][curpt.z][curpt.x+curpt.y*width]=tmp;
			}
			else
			{
				//data[curpt.x][curpt.y][curpt.z][2]=128;
				data[2][curpt.z][curpt.x+curpt.y*width]=tmp;
			}
		}
	}
	
	private void step_dilation(float radius, ArrayList <MyIntPoint> point_list)
	{
		int pix_rad=(int)Math.ceil(radius);
		int seed_x=point_list.get(0).x;
		int seed_y=point_list.get(0).y;
		int seed_z=point_list.get(0).z;
		int z_rad=(int)Math.ceil(radius/aspect);
		for (int z=-z_rad; z<z_rad+1; z++)
		{
	    	for (int x=-pix_rad; x<pix_rad+1; x++)
	    	{
	    		for (int y=-pix_rad; y<pix_rad+1; y++)
	    		{
	    			if (x*x+y*y+z*aspect*z*aspect<radius*radius)
	    			{
	    				//IJ.log("x,y,z: "+x+","+y+","+z+","+radius);
	    				int xx=seed_x+x, yy=seed_y+y, zz=seed_z+z;
	    				if (xx<0||xx>=width||yy<0||yy>=height||zz<0||zz>=slices) continue;
	    				MyIntPoint curpt=new MyIntPoint(xx, yy, zz);
	    				point_list.add(curpt);
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
		gd.addNumericField("Lateral axial aspect ratio", 4, 1);
		//gd.addNumericField("First shell higher radius", 3, 0);
		//gd.addNumericField("Second shell lower radius", 5, 0);
		//gd.addNumericField("Second shell higher radius", 5, 0);
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
		//radA_high=(int)gd.getNextNumber();
		//radB_low=(int)gd.getNextNumber();
		//radB_high=(int)gd.getNextNumber();
		aspect=(float)gd.getNextNumber();
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
		byte [] pix=(byte [])mask_img.getStack().getPixels(z+1);
		if (pix[x+width*y]!=0) return false;
		
		MyIntPoint curpt=new MyIntPoint(x,y,z);
		tmp_point_list.add(curpt);
		current_list.add(curpt);
		pix[x+width*y]=(byte)255;
		
		return true;
	}
}
