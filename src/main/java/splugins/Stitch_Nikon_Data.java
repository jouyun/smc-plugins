package splugins;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.DirectoryChooser;
import ij.plugin.PlugIn;
import splugins.Stitch_PE_Data.blocks_wrapper;

public class Stitch_Nikon_Data implements PlugIn {

	@Override
	public void run(String arg0) {
		float [] x_pos= new float[100];
		float [] y_pos = new float[100];
		int starting_index=1;
		// Get the directory, find *.nd2 files
		// Loop through the directory
			// 
		GenericDialog gd=new GenericDialog("Which channel?");
		gd.addNumericField("Channel to use for stitch", 1, 0);
		gd.showDialog();
		int stitch_channel=(int)gd.getNextNumber();
		DirectoryChooser dir=new DirectoryChooser("Choose the directory");
		String dir_name=dir.getDirectory();
		File folder=new File(dir_name);
		File [] file_list=folder.listFiles();
		String concat_string="  title=Img open ";
		int cs=0, ss=0;
		
		for (int f=0; f<file_list.length; f++)
		{
			if (file_list[f].getName().endsWith(".nd2"))
			{
				String image_path=dir_name+file_list[f].getName();
				IJ.log(image_path);
				IJ.run("Bio-Formats Importer", "open=["+image_path+"] color_mode=Default concatenate_series open_all_series view=Hyperstack stack_order=XYCZT");
				ImagePlus img=WindowManager.getCurrentImage();
				cs=img.getNChannels();
				ss=img.getNSlices();
				String imgInfo=img.getInfoProperty();
				String lines[]=imgInfo.split("\\r?\\n");
				for (int s=0; s<lines.length; s++)
				{
					if (lines[s].contains("m_dXYPositionX0"))
					{
						String txt=lines[s].substring(18);
						IJ.log(txt);
						x_pos[starting_index-1]=Float.parseFloat(txt);
						IJ.log("X: "+x_pos[starting_index-1]);
						txt=lines[s+1].substring(18);
						y_pos[starting_index-1]=Float.parseFloat(txt);
						
						WindowManager.getCurrentImage().setTitle("Img"+starting_index);
						concat_string=concat_string+"image"+starting_index+" ";
						starting_index++;
					}
				}
				
			}
		}
		concat_string=concat_string+"image"+starting_index+"=[-- None --]";
		IJ.run("Concatenate...", concat_string);
		IJ.run("Stack to Hyperstack...", "order=xyczt(default) channels="+cs+" slices="+ss+" frames="+(starting_index-1)+" display=Grayscale");
		
		//Remake the x_pos and y_pos arrays
		float [] tmp=new float[starting_index-1];
		for (int f=0; f<starting_index-1; f++)
		{
			tmp[f]=x_pos[f];
		}
		x_pos=tmp;
		tmp=new float[starting_index-1];
		for (int f=0; f<starting_index-1; f++)
		{
			tmp[f]=y_pos[f];
		}
		y_pos=tmp;
		
		IJ.run("Flip Vertically", "stack");
		IJ.run("Flip Horizontally", "stack");
		
		ImagePlus img=WindowManager.getCurrentImage();
		float pix_size=(float) ((float)img.getCalibration().pixelWidth*1.2);
		/*if (override_scaling) 
		{
			pix_size=(float)pixel_scaling;
		}*/
		for (int j=0; j<x_pos.length; j++)
		{
			x_pos[j]=(float) (x_pos[j]/pix_size);
			y_pos[j]=(float) (y_pos[j]/pix_size);
		}
		
		stitch_generic.stitch_img(WindowManager.getCurrentImage(), x_pos, y_pos, dir_name, stitch_channel-1);

	}

}
