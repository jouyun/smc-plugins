package splugins;
import ij.plugin.PlugIn;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ShortProcessor;
import ij.plugin.ContrastEnhancer;




// Java 1.1
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.IndexColorModel;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;


public class merge_objects_in_tiles_v2 implements PlugIn {
	
	

	
	
	@Override
	public void run(String arg) {
		ImagePlus img=WindowManager.getCurrentImage();
		
		GenericDialog gd = new GenericDialog("Merge Tiled Objects");
		gd.addNumericField("SNR of hit pixels:  ", 15, 1);
		gd.addNumericField("Hit pixels required for image:  ", 10000, 1);
		gd.addNumericField("Border size in pixels:  ", 100, 1);
		gd.addNumericField("In border pixels required:  ", 100, 1);
		gd.addNumericField("X tiles:  ", 6, 0);
		gd.addNumericField("Y tiles:  ", 5, 0);
		gd.addStringField("Path to save to:  ", "/home/smc/Data/LCC/H3Pquantification_p53_zfp1_RNAi_20141214/set2/p53-d12/tmp/");
		gd.showDialog();
		
		float sigma_ratio=(float)gd.getNextNumber(); 
		int required_pixels=(int)gd.getNextNumber();
		int borderland=(int)gd.getNextNumber();
		int minimum_over=(int)gd.getNextNumber();
		int x_tiles=(int)gd.getNextNumber();
		int y_tiles=(int)gd.getNextNumber();
		String save_directory=gd.getNextString();
		
		ArrayList <int []> the_list=Find_Contiguous_Objects(img, x_tiles, y_tiles, sigma_ratio, required_pixels, borderland, minimum_over);
		
		
		int width=img.getWidth(); 
		int height=img.getHeight();
		int successful_counter=0;
		for (int j=0; j<the_list.size(); j++)
		{
			try {
				FileOutputStream fos=new FileOutputStream(save_directory+"out.txt");
				Writer w= new BufferedWriter(new OutputStreamWriter(fos));
				w.write("# Define the number of dimensions we are working on\n");
				w.write("dim = 2\n\n# Define the image coordinates\n");
				
				int [] my_list=the_list.get(j);
				for (int i=0; i<my_list.length; i++)
				{
					int [] idc=get_index(my_list[i], x_tiles, y_tiles);
					IJ.log("idx: "+my_list[i]+" x: "+idc[0]+" y: "+idc[1]);
					w.write("Tiffs"+String.format("%04d", my_list[i])+".tif; ; ("+(idc[0]*(width*0.8))+", "+((y_tiles-idc[1]-1)*(height*0.8))+")\n");
					
				}
				w.flush();
				w.close();
				IJ.runMacroFile("Call_From_Plugin_Dummy_Stitch.ijm", "type=[Positions from file] order=[Defined by TileConfiguration] directory="+save_directory+" layout_file=out.txt fusion_method=[Linear Blending] regression_threshold=0.30 max/avg_displacement_threshold=2.50 absolute_displacement_threshold=3.50 compute_overlap subpixel_accuracy computation_parameters=[Save computation time (but use more RAM)] image_output=[Fuse and display]");
				if (WindowManager.getCurrentImage().getTitle().equals("Fused")) 
				{
					WindowManager.getCurrentImage().setTitle("Fused_"+(successful_counter+1));
					successful_counter++;
				}
				//if(j==0) return;
			}
			catch (Exception e) {}
		}
		
		//Img0007.tif; ; (819.0, 0.0)
		
	}
	static public ArrayList <int []> Find_Contiguous_Objects(ImagePlus img, int x_tiles, int y_tiles, float sigma_ratio, int required_pixels, int borderland, int minimum_over)
	{
		class Tissue {
			int [] indices=new int [200];
			int number_images=0;
			void add_point(int idx)
			{
				if (number_images==200) 
				{
					IJ.log("WHOAH, too many neighbors, something isn't right!");
					return;
				}
				indices[number_images]=idx;
				number_images++;
			}
		}
		
		int height;
		int width;
		int slices;
		int channels;
		int frames;
		int current_slice;
		int current_frame;
		int current_channel;
		
		width=img.getWidth(); 
		height=img.getHeight();
		channels=img.getNChannels();
		slices=img.getNSlices();
		frames=img.getNFrames();
		current_slice=img.getSlice()-1;
		current_frame=img.getFrame()-1;
		current_channel=img.getChannel()-1;

		
		//*****************************************************		
		//First find dimmest image (ie lowest average signal), this will be our baseline
		double smallest_total=10000000000000000000000000000000000000.0;
		double smallest_sq_total=0;
		double sigma=0, average=0;
		int smallest_index=0;
		for (int i=0; i<frames; i++)
		{
			float [] current_img=(float [])img.getStack().getProcessor(current_channel+current_slice*channels+channels*slices*i+1).convertToFloat().getPixels();
			double tmp_sum=0.0;
			double tmp_sq_sum=0;
			for (int j=0; j<width*height; j++)
			{
				tmp_sum+=current_img[j];
				tmp_sq_sum+=(current_img[j]*current_img[j]);
			}
			if (tmp_sum<smallest_total)
			{
				smallest_total=tmp_sum;
				smallest_index=i;
				smallest_sq_total=tmp_sq_sum;
			}
		}
		float [] pix=(float [])img.getStack().getProcessor(current_channel+current_slice*channels+channels*slices*smallest_index+1).convertToFloat().getPixels();
		double[] vals=Percentile_Threshold.find_average_sigma(pix, width, height, 10);
		average=vals[0];
		sigma=vals[1];
		//average=smallest_total/(double)(width*height);
		//sigma=Math.sqrt(smallest_sq_total/(float)(width*height)-average*average);
		IJ.log("Index:  " + smallest_index + " average: "+average+" sigma:  " + sigma);
		
		//*****************************************************
		//Now mark guys who have hits in them
		int [] state_array=new int[frames];
		
		for (int i=0; i<frames; i++)
		{
			float [] current_img=(float [])img.getStack().getProcessor(current_channel+current_slice*channels+channels*slices*i+1).convertToFloat().getPixels();
			int current_pixels=0;
			for (int j=0; j<width*height; j++)
			{
				if (current_img[j]-average>sigma_ratio*sigma) current_pixels++;
			}
			IJ.log("Frame " + (i+1) + " had "+current_pixels+" over the threshold");
			if (current_pixels>required_pixels) 
			{
				state_array[i]=1;
				IJ.log("Hit: " + (i+1));
			}
		}
		
		boolean [][] edge_state_array=new boolean [frames][4];
		for (int i=0; i<frames; i++)
		{
			float [] current_img=(float [])img.getStack().getProcessor(current_channel+current_slice*channels+channels*slices*i+1).convertToFloat().getPixels();
			edge_state_array[i]=edge_check(current_img, borderland, minimum_over, average, sigma_ratio, sigma, width, height);
			
			int [] xy=get_index(i,x_tiles,y_tiles);
			IJ.log("Frame,x,y: "+i+","+xy[0]+","+xy[1]);
			IJ.log("Status: "+edge_state_array[i][0]+","+edge_state_array[i][1]+","+edge_state_array[i][2]+","+edge_state_array[i][3]);
		}
		
		ArrayList <Tissue> Tissue_list=new ArrayList <Tissue>();
		for (int i=0; i<frames; i++)
		{
			if (state_array[i]<1) continue;
			state_array[i]=-1;
			Tissue new_Tissue=new Tissue();
			ArrayList<Integer> current_new_list=new ArrayList<Integer>();
			new_Tissue.add_point(i);
			current_new_list.add(i);
			while (current_new_list.size()>0)
			{
				ArrayList<Integer> temp_list=new ArrayList<Integer>();
				for (ListIterator jF=current_new_list.listIterator();jF.hasNext();)
				{
					Integer current_idx=(Integer)jF.next();
					for (int d=0; d<4; d++)
					{
						int nidx=find_index(current_idx,d, x_tiles, y_tiles);
						//IJ.log("Current: "+current_idx+", next: :"+ nidx);
						if (nidx<0) continue;
						//IJ.log("Is valid neighbor, checking next's direction"+((d+2)%4));
						if (!edge_state_array[nidx][(d+2)%4]) continue;
						//IJ.log("Edge array says it's a candidate");
						if (state_array[nidx]==-1) continue;
						//IJ.log("Added");
						state_array[nidx]=-1;
						new_Tissue.add_point(nidx);
						temp_list.add(nidx);
					}
				}
				current_new_list=temp_list;
			}
			Tissue_list.add(new_Tissue);
		}
		ArrayList <int []> return_list=new ArrayList <int []> ();
		for (ListIterator jF=Tissue_list.listIterator();jF.hasNext();)
		{
			Tissue current_Tissue=(Tissue)jF.next();
			int [] this_list=new int[current_Tissue.number_images];
			IJ.log("New Tissue");
			for (int i=0; i<current_Tissue.number_images; i++)
			{
				IJ.log("Idx: "+current_Tissue.indices[i]);
				this_list[i]=current_Tissue.indices[i];
			}
			return_list.add(this_list);
		}
		return return_list;
	}
	
	static public boolean [] edge_check(float [] data, int borderland, int minimum_over, double average, double sigma_ratio, double sigma, int width, int height)
	{
		int ctr=0;
		boolean [] rtn=new boolean[4];
		for (int j=0; j<borderland; j++)
		{
			for (int k=0; k<width; k++)
			{
				if (data[k+j*width]-average>sigma_ratio*sigma) ctr++;
			}
		}
		IJ.log("N: "+ctr);
		if (ctr>minimum_over) rtn[0]=true;
		ctr=0;
		for (int j=0; j<height; j++)
		{
			for (int k=0; k<borderland; k++)
			{
				if (data[k+j*width]-average>sigma_ratio*sigma) ctr++;
			}
		}
		IJ.log("W: "+ctr);
		if (ctr>minimum_over) rtn[3]=true;
		ctr=0;
		for (int j=height-1; j>=height-borderland; j--)
		{
			for (int k=0; k<width; k++)
			{
				if (data[k+j*width]-average>sigma_ratio*sigma) ctr++;
			}
		}
		IJ.log("S: "+ctr);
		if (ctr>minimum_over) rtn[2]=true;
		ctr=0;
		for (int j=0; j<height; j++)
		{
			for (int k=width-1; k>=width-borderland; k--)
			{
				if (data[k+j*width]-average>sigma_ratio*sigma) ctr++;
			}
		}
		IJ.log("E: "+ctr);
		if (ctr>minimum_over) rtn[1]=true;
		return rtn;
	}
	
	static public int find_index(int base_index, int edge_number, int x_tiles, int y_tiles)
	{
		boolean right_snake=(base_index%(2*x_tiles)==base_index%x_tiles);
		int rtn=0;
		if (right_snake)
		{
			switch (edge_number)
			{
				case 2:	rtn=base_index+2*(x_tiles-base_index%x_tiles)-1;
				break;
				case 1: if ((base_index+1)%x_tiles==0) rtn=-1;
						else	rtn=base_index+1;
				break;
				case 0: if (base_index%x_tiles==0) rtn=-1;
						else rtn=base_index-2*(base_index%x_tiles)-1;
				break;
				case 3: rtn=base_index-1;
				break;
			}
		}
		else
		{
			switch (edge_number)
			{
				case 2:	rtn=base_index+2*(x_tiles-base_index%x_tiles)-1;
				break;
				case 1: if (base_index%x_tiles==0) rtn=-1;
						else rtn=base_index-1;
				break;
				case 0: if ((base_index+1)%x_tiles==0) rtn=-1; 
						else rtn=base_index-2*(base_index%x_tiles)-1;
				break;
				case 3: rtn=base_index+1;
				break;
				default:
					break;
			}
			
		}
		if (rtn>=x_tiles*y_tiles) rtn=-1;
		if (rtn<0) rtn=-1;
		return rtn;
	}
	static public int [] get_index(int base_index, int x_tiles, int y_tiles)
	{
		boolean right_snake=(base_index%(2*x_tiles)==base_index%x_tiles);
		int[] rtn=new int[2];
		if (right_snake)
		{
			rtn[0]=base_index%x_tiles;
			rtn[1]=y_tiles-(base_index-rtn[0])/x_tiles-1;
		}
		else
		{
			rtn[0]=x_tiles-(base_index%x_tiles)-1;
			rtn[1]=y_tiles-(base_index+rtn[0])/x_tiles-1;
		}
		return rtn;
	}
	
}
