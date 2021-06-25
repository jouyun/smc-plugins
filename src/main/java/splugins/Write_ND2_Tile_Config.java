package splugins;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.DirectoryChooser;
import ij.plugin.PlugIn;

public class Write_ND2_Tile_Config implements PlugIn {

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		float [] x_pos= new float[100];
		float [] y_pos = new float[100];
		int starting_index=1;
		// Get the directory, find *.nd2 files
		// Loop through the directory
			// 
		GenericDialog gd=new GenericDialog("Which channel?");
		gd.addNumericField("Channel to use for stitch", 1, 0);
		gd.addChoice("Fusion method: ", new String[]{"Max. Intensity", "Linear Blending", "Intensity of random input tile"}, "Max. Intensity");
		gd.addCheckbox("Override scaling?", false);
		gd.addNumericField("Actual scaling: ", 0.0, 3);
		gd.showDialog();
		int stitch_channel=(int)gd.getNextNumber();
		String fusion_method=gd.getNextChoice();
		boolean override_scaling=gd.getNextBoolean();
		float scaler=(float)gd.getNextNumber();
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
				//IJ.run("Bio-Formats Importer", "open=["+image_path+"] color_mode=Default concatenate_series open_all_series view=Hyperstack stack_order=XYCZT");
				IJ.run("Bio-Formats Importer", "open=["+image_path+"] color_mode=Default view=Hyperstack stack_order=XYCZT");
				ImagePlus img=WindowManager.getCurrentImage();
				cs=img.getNChannels();
				ss=img.getNSlices();
				String imgInfo=img.getInfoProperty();
				String lines[]=imgInfo.split("\\r?\\n");
				for (int s=0; s<lines.length; s++)
				{
					if (lines[s].contains("dXPos"))
					{
						String txt=lines[s].substring(8);
						IJ.log(txt);
						x_pos[starting_index-1]=Float.parseFloat(txt);
						IJ.log("X: "+x_pos[starting_index-1]);
						txt=lines[s+1].substring(8);
						y_pos[starting_index-1]=Float.parseFloat(txt);
						
						WindowManager.getCurrentImage().setTitle("Img"+starting_index);
						concat_string=concat_string+"image"+starting_index+" ";
						starting_index++;
					}
				}
				
			}
		}

		IJ.log("Channels, slices, frames:  "+ cs+","+ss+","+(starting_index-1));  
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
		
		
		ImagePlus img=WindowManager.getCurrentImage();
		float pix_size=(float) ((float)img.getCalibration().pixelWidth*1.2);
		pix_size=(float) ((float)img.getCalibration().pixelWidth);
		if (override_scaling) 
		{
			pix_size=(float)scaler;
		}
		for (int j=0; j<x_pos.length; j++)
		{
			x_pos[j]=(float) (x_pos[j]/pix_size);
			y_pos[j]=(float) (y_pos[j]/pix_size);
		}
		
		//stitch_generic.stitch_img(WindowManager.getCurrentImage(), x_pos, y_pos, dir_name, stitch_channel-1, fusion_method);
		stitch_generic.write_tile_config_alternative_name(dir_name, x_pos, y_pos, "Tiffs");
	}

}
