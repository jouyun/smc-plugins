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
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;


public class merge_objects_in_tiles implements PlugIn {
	
	int height;
	int width;
	int slices;
	int x_tiles;
	int y_tiles;
	int [][] lookup_table;

	class Worm {
		int [] indices=new int [20];
		int number_images=0;
		void add_point(int idx)
		{
			if (number_images==20) 
			{
				IJ.log("WHOAH, too many neighbors, something isn't right!");
				return;
			}
			indices[number_images]=idx;
			number_images++;
		}
	}
	
	@Override
	public void run(String arg) {
		x_tiles=20;
		y_tiles=20;
		ImagePlus img=WindowManager.getCurrentImage();
		height=img.getHeight();
		width=img.getWidth();
		slices=img.getImageStackSize()/2;
		GenericDialog gd = new GenericDialog("Merge Tiled Objects");
		gd.addNumericField("SNR of hit pixels:  ", 20, 1);
		gd.addNumericField("Hit pixels required for image:  ", 200, 1);
		gd.addNumericField("Border size in pixels:  ", 100, 1);
		gd.addNumericField("In border pixels required:  ", 10, 1);
		gd.addNumericField("X tiles:  ", 15, 0);
		gd.addNumericField("Y tiles:  ", 15, 0);
		gd.addStringField("Path to save to:  ", "C:\\Data\\For Beth\\dummy\\");
		gd.showDialog();
		
		float sigma_ratio=(float)gd.getNextNumber(); 
		int required_pixels=(int)gd.getNextNumber();
		int borderland=(int)gd.getNextNumber();
		int minimum_over=(int)gd.getNextNumber();
		x_tiles=(int)gd.getNextNumber();
		y_tiles=(int)gd.getNextNumber();
		String save_directory=gd.getNextString();
		
		
		lookup_table=new int[x_tiles][y_tiles];
		make_lookup_table();
		//*****************************************************		
		//First find dimmest image (ie lowest average signal), this will be our baseline
		double smallest_total=10000000000000000000000000000000000000.0;
		double smallest_sq_total=0;
		double sigma=0, average=0;
		int smallest_index=0;
		for (int i=0; i<slices; i++)
		{
			float [] current_img=(float [])img.getStack().getProcessor(2*i+1).convertToFloat().getPixels();
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
		average=smallest_total/(double)(width*height);
		sigma=Math.sqrt(smallest_sq_total/(float)(width*height)-average*average);
		//IJ.log("Index:  " + smallest_index + " sigma:  " + sigma);
		
		//*****************************************************
		//Now mark guys who have hits in them
		int [] state_array=new int[slices];
		
		for (int i=0; i<slices; i++)
		{
			float [] current_img=(float [])img.getStack().getProcessor(2*i+1).convertToFloat().getPixels();
			int current_pixels=0;
			for (int j=0; j<width*height; j++)
			{
				if (current_img[j]-average>sigma_ratio*sigma) current_pixels++;
			}
			IJ.log("Slice " + (i+1) + " had "+current_pixels+" over the threshold");
			if (current_pixels>required_pixels) 
			{
				state_array[i]=1;
				//IJ.log("Hit: " + (i+1));
			}
		}
		
		//*****************************************************
		//Go through all the marked ones and create worms
		ArrayList <Worm> worm_list=new ArrayList <Worm>();
		
		for (int i=0; i<slices; i++)
		{
			if (state_array[i]==0) continue;
			float [] current_img=(float [])img.getStack().getProcessor(2*i+1).convertToFloat().getPixels();
			//Find out which borders in this image have hits in them, NESW order
			boolean [] edges;
			edges=edge_check(current_img, borderland, minimum_over, average, sigma_ratio, sigma);
			//IJ.log("slice: " + (i+1) +" edges" + edges[0] +" " +edges[1] + " " + edges[2] + " " +edges[3]);
			Worm new_worm=new Worm();
			new_worm.add_point(i);
			state_array[i]=0;
			for (int j=0; j<4; j++)
			{
				if (!edges[j]) continue;
				int new_idx=find_index(i,j);
				if (new_idx<0) continue;
				if (state_array[new_idx]==0) continue;
				new_worm.add_point(new_idx);
				state_array[new_idx]=0;
				
				float [] new_img=(float [])img.getStack().getProcessor(2*new_idx+1).convertToFloat().getPixels();
				//Find out which borders in this image have hits in them, NESW order
				boolean [] new_edges;
				new_edges=edge_check(new_img, borderland, minimum_over, average, sigma_ratio, sigma);
				
				for (int k=0; k<4; k++)
				{
					if (!new_edges[k]) continue;
					int new_new_idx=find_index(new_idx,k);
					if (new_new_idx<0) continue;
					if (state_array[new_new_idx]==0) continue;
					new_worm.add_point(new_new_idx);
					state_array[new_new_idx]=0;
					
					float [] new_new_img=(float [])img.getStack().getProcessor(2*new_new_idx+1).convertToFloat().getPixels();
					boolean [] new_new_edges;
					new_new_edges=edge_check(new_new_img, borderland, minimum_over, average, sigma_ratio, sigma);
					for (int m=0; m<4; m++)
					{
						if (!new_new_edges[m]) continue;
						int new_new_new_idx=find_index(new_new_idx,m);
						if (new_new_new_idx<0) continue;
						if (state_array[new_new_new_idx]==0) continue;
						new_worm.add_point(new_new_new_idx);
						state_array[new_new_new_idx]=0;
						
						float [] new_new_new_img=(float [])img.getStack().getProcessor(2*new_new_new_idx+1).convertToFloat().getPixels();
						boolean [] new_new_new_edges;
						new_new_new_edges=edge_check(new_new_new_img, borderland, minimum_over, average, sigma_ratio, sigma);
						for (int n=0; n<4; n++)
						{
							if (!new_new_new_edges[n]) continue;
							int new_new_new_new_idx=find_index(new_new_new_idx,n);
							if (new_new_new_new_idx<0) continue;
							if (state_array[new_new_new_new_idx]==0) continue;
							new_worm.add_point(new_new_new_new_idx);
							state_array[new_new_new_new_idx]=0;
							
							float [] new_new_new_new_img=(float [])img.getStack().getProcessor(2*new_new_new_new_idx+1).convertToFloat().getPixels();
							boolean [] new_new_new_new_edges;
							new_new_new_new_edges=edge_check(new_new_new_new_img, borderland, minimum_over, average, sigma_ratio, sigma);
							for (int p=0; p<4; p++)
							{
								if (!new_new_new_new_edges[p]) continue;
								int new_new_new_new_new_idx=find_index(new_new_new_new_idx,p);
								if (new_new_new_new_new_idx<0) continue;
								if (state_array[new_new_new_new_new_idx]==0) continue;
								new_worm.add_point(new_new_new_new_new_idx);
								state_array[new_new_new_new_new_idx]=0;
								
								float [] new_new_new_new_new_img=(float [])img.getStack().getProcessor(2*new_new_new_new_new_idx+1).convertToFloat().getPixels();
								boolean [] new_new_new_new_new_edges;
								new_new_new_new_new_edges=edge_check(new_new_new_new_new_img, borderland, minimum_over, average, sigma_ratio, sigma);
								for (int q=0; q<4; q++)
								{
									if (!new_new_new_new_new_edges[p]) continue;
									int new_new_new_new_new_new_idx=find_index(new_new_new_new_new_idx,q);
									if (new_new_new_new_new_new_idx<0) continue;
									if (state_array[new_new_new_new_new_new_idx]==0) continue;
									new_worm.add_point(new_new_new_new_new_new_idx);
									state_array[new_new_new_new_new_new_idx]=0;
								}
							}
						}
					}
					
				}
			}
			//BIG CHANGE HERE 03272013
			if (new_worm.number_images>0) worm_list.add(new_worm);
		}
		int tmp=0;
		ImagePlus tmp_img, old_img;
		tmp_img=img;
		for (ListIterator jF=worm_list.listIterator();jF.hasNext();)
		{
			Worm current_worm=(Worm)jF.next();
			IJ.log("Worm: " + tmp);
			tmp++;
			
			//Begin new stuff
			for (int i=0; i<current_worm.number_images; i++) IJ.log("Img" + (current_worm.indices[i]+1));
			int [] my_square=new int[0];
			int x_size=0, y_size=0;
			int []bxy=new int[2];
			int bidx=0;
			boolean found_square=false;
			for (int i=1; i<6; i++)
			{
				x_size=i;
				for (int ii=1; ii<6; ii++)
				{
					y_size=ii;					
					my_square=new int[i*ii];
					for (int j=0; j<current_worm.number_images; j++)
					{
						int my_base=current_worm.indices[j];
						int [] my_base_idx=get_index(my_base);
						//0 is go out in NE direction, 1 is ES direction, 2 is SW direction, 3 is NW direction
						for (int k=0; k<4; k++)
						{
							//Make square
							for (int m=0; m<i; m++)
							{
								int x_flip=1;
								if (k>1) x_flip=-1;
								for (int n=0; n<ii; n++)
								{
									int y_flip=1;
									if (k==1||k==2) y_flip=-1;
									int xidx=my_base_idx[0]+m*x_flip;
									int yidx=my_base_idx[1]+n*y_flip;
									if (xidx<0||xidx>=x_tiles||yidx<0||yidx>=y_tiles) my_square[m*ii+n]=0;
									else my_square[m*ii+n]=lookup_table[yidx][xidx];
								}
							}
							//Check square to see if all are contained
							boolean all_there=true;
							for (int m=0; m<current_worm.number_images; m++)
							{
								boolean current_good=false;
								for (int n=0; n<i*ii; n++)
								{
									if (my_square[n]==current_worm.indices[m]) 
									{
										current_good=true;
										break;
									}
								}
								if (!current_good) 
								{
									all_there=false;
									break;
								}
							}
							if (all_there) 
							{
								found_square=true;
								break;
							}
						}
						if (found_square) break;
					}
					if (found_square) break;
				}
				if (found_square) break;
			}
			IJ.log("Square size x:  "+x_size+" Square size y: "+y_size);
			for (int j=0; j<x_size*y_size; j++) IJ.log("Including:  "+my_square[j]);
			ImagePlus new_img=NewImage.createFloatImage(("Img"), width, height, 2*x_size*y_size, NewImage.FILL_BLACK);
			new_img.setOpenAsHyperStack(true);
			new_img.setDimensions(2,1,x_size*y_size);
			int min=100000;
			bxy[0]=-1;
			bxy[1]=-1;
			for (int i=0; i<x_size*y_size; i++) 
			{
				if (my_square[i]<min)
				{
					min=my_square[i];
					bxy=get_index(my_square[i]);
				}
			}
			bidx=lookup_table[bxy[1]][bxy[0]];
			if(!(bidx%(2*x_tiles)==bidx%x_tiles))
			{
				bidx=bidx+x_size-1;
			}
			for (int i=0; i<x_size; i++)
			{
				for (int j=0; j<y_size; j++)
				{
					int [] my_base_idx=get_index(bidx);
					int my_index=lookup_table[my_base_idx[1]+j][my_base_idx[0]+i];
					
					float [] new_pix=(float [])new_img.getStack().getProcessor(1+2*(i*y_size+j)).getPixels();
					float [] old_pix=(float [])img.getStack().getProcessor(2*my_index+1).convertToFloat().getPixels();
					for (int k=0; k<width*height; k++) new_pix[k]=old_pix[k];
					
					new_pix=(float [])new_img.getStack().getProcessor(2+2*(i*y_size+j)).getPixels();
					old_pix=(float [])img.getStack().getProcessor(2*my_index+2).convertToFloat().getPixels();
					for (int k=0; k<width*height; k++) new_pix[k]=old_pix[k];
					
					/*
					int tidx=i*y_size+j;
					String sidx=""+tidx;
					if (tidx<10) sidx="0"+sidx;
					if (tidx<100) sidx="0"+sidx;
					if (tidx<1000) sidx="0"+sidx;
					
					
					IJ.save(new_img, save_directory+"Worm"+sidx+".tif");
					new_img.close();*/
				}
			}
			new_img.show();
			new_img.updateAndDraw();
			//IJ.save(new_img, save_directory+"Worm_tiles"+tmp+".tif");
			if (x_size>1||y_size>1)
			{
				String myarg="grid_size_x="+x_size+" grid_size_y="+y_size;
				IJ.runMacroFile("U:\\smc\\FiJi.app\\macros\\Call_From_Plugin_Worm_Stitch.ijm", myarg);
				ImagePlus tmpimg=WindowManager.getCurrentImage();
				IJ.save(tmpimg, save_directory+"Worm"+tmp+".tif");
				tmpimg.close();
			}
			else
			{
				new ContrastEnhancer().stretchHistogram(new_img, 0.5); 
				IJ.save(new_img, save_directory+"Worm"+tmp+".tif");
				new_img.close();
			}
			//new_img.close();
			if (x_tiles==y_tiles) continue;
			//OLD WAY BELOW
			/*for (int i=0; i<current_worm.number_images; i++)
			{
				IJ.log("Img" + (current_worm.indices[i]+1));
				ImagePlus new_img=NewImage.createFloatImage(("Img"+i), width, height, 2, NewImage.FILL_BLACK);
				new_img.setDimensions(2,1,1);
				float [] new_pix=(float [])new_img.getStack().getProcessor(1).getPixels();
				float [] old_pix=(float [])img.getStack().getProcessor(2*current_worm.indices[i]+1).convertToFloat().getPixels();
				for (int j=0; j<width*height; j++) new_pix[j]=old_pix[j];
				
				new_pix=(float [])new_img.getStack().getProcessor(2).getPixels();
				old_pix=(float [])img.getStack().getProcessor(2*current_worm.indices[i]+2).convertToFloat().getPixels();
				for (int j=0; j<width*height; j++) new_pix[j]=old_pix[j];
				
				
				new_img.show();
				new_img.updateAndDraw();
				if (i==0)
				{
					new_img.setTitle("Worm"+tmp);
					new_img.updateAndDraw();
					tmp_img=new_img;
				}
				else
				{
					IJ.run("Pairwise stitching", "first_image=Worm"+tmp+" second_image=Img"+i+" fusion_method=[Max. Intensity] check_peaks=5 compute_overlap x=0.0 y=0.0 registration_channel_image_1=[Only channel 1] registration_channel_image_2=[Only channel 1]");
					old_img=tmp_img;
					tmp_img=WindowManager.getCurrentImage();
					tmp_img.setTitle("Worm"+tmp);
					tmp_img.updateAndDraw();
					old_img.close();
					new_img.close();
				}
			}
			IJ.save(tmp_img, save_directory+"Worm"+tmp+".tif");
			tmp_img.close();*/
		}
	}
	boolean [] edge_check(float [] data, int borderland, int minimum_over, double average, double sigma_ratio, double sigma)
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
		if (ctr>minimum_over) rtn[0]=true;
		ctr=0;
		for (int j=0; j<height; j++)
		{
			for (int k=0; k<borderland; k++)
			{
				if (data[k+j*width]-average>sigma_ratio*sigma) ctr++;
			}
		}
		if (ctr>minimum_over) rtn[3]=true;
		ctr=0;
		for (int j=height-1; j>=height-borderland; j--)
		{
			for (int k=0; k<width; k++)
			{
				if (data[k+j*width]-average>sigma_ratio*sigma) ctr++;
			}
		}
		if (ctr>minimum_over) rtn[2]=true;
		ctr=0;
		for (int j=0; j<height; j++)
		{
			for (int k=width-1; k>=width-borderland; k--)
			{
				if (data[k+j*width]-average>sigma_ratio*sigma) ctr++;
			}
		}
		if (ctr>minimum_over) rtn[1]=true;
		return rtn;
	}
	
	int find_index(int base_index, int edge_number)
	{
		boolean right_snake=(base_index%(2*x_tiles)==base_index%x_tiles);
		int rtn=0;
		if (right_snake)
		{
			switch (edge_number)
			{
				case 0:	rtn=base_index+2*(x_tiles-base_index%x_tiles)-1;
				break;
				case 1: if ((base_index+1)%x_tiles==0) rtn=-1;
						else	rtn=base_index+1;
				break;
				case 2: if (base_index%x_tiles==0) rtn=-1;
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
				case 0:	rtn=base_index+2*(x_tiles-base_index%x_tiles)-1;
				break;
				case 1: if (base_index%x_tiles==0) rtn=-1;
						else rtn=base_index-1;
				break;
				case 2: if ((base_index+1)%x_tiles==0) rtn=-1; 
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
	int [] get_index(int base_index)
	{
		boolean right_snake=(base_index%(2*x_tiles)==base_index%x_tiles);
		int[] rtn=new int[2];
		if (right_snake)
		{
			rtn[0]=base_index%x_tiles;
			rtn[1]=(base_index-rtn[0])/x_tiles;
		}
		else
		{
			rtn[0]=x_tiles-(base_index%x_tiles)-1;
			rtn[1]=(base_index+rtn[0])/x_tiles;
		}
		return rtn;
	}
	//This assumes x_tiles==y_tiles
	void make_lookup_table()
	{
		if (x_tiles!=y_tiles) return;
		lookup_table[0][0]=0;
		for (int i=0; i<x_tiles; i++)
		{
			for (int j=i; j<x_tiles; j++)
			{
				if (j==0) continue;
				lookup_table[i][j]=find_index(lookup_table[i][j-1], 1);
			}
			for (int j=i; j<y_tiles; j++)
			{
				if (j==0) continue;
				lookup_table[j][i]=find_index(lookup_table[j-1][i], 0);
			}
		}
	}
	
}
