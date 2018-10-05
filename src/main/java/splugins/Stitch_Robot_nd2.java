package splugins;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Stitch_Robot_nd2 implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
		
		
		
		GenericDialog gd=new GenericDialog("Dialog");
		gd.addStringField("Directory: ", "/n/tmp/");
		gd.addNumericField("Plate", 0, 0);
		gd.addNumericField("Well", 0, 0);
		gd.addNumericField("Object", 0, 0);
		gd.addNumericField("Channel to use for stitch?", 1, 0);
		gd.addCheckbox("Override scaling?", false);
		gd.addNumericField("Pixel Size", 0.780, 3);
		gd.addChoice("Fusion method: ", new String[]{"Max. Intensity", "Linear Blending", "Intensity of random input tile"}, "Max. Intensity");
		gd.showDialog();
		
		String base_dir=gd.getNextString()+File.separator;
		int plate=(int)gd.getNextNumber();
		int well=(int)gd.getNextNumber();
		int object=(int)gd.getNextNumber();
		int stitch_channel=(int)gd.getNextNumber();
		boolean override_scaling=gd.getNextBoolean();
		double pixel_scaling=gd.getNextNumber();
		String fusion_method=gd.getNextChoice();
		String base_image_name="Plate"+IJ.pad(plate, 3)+"_Well"+well+"_Count"+IJ.pad(object,5)+"_Point";
		
		
		File folder = new File(base_dir);
		String[] file_list=folder.list();
		IJ.log(base_image_name);
		
		boolean got_series=false;
		boolean done=false;
		int current_idx=-1;
		String config_path="";
		ImagePlus img;
		int starting_index=1;
		String concat_string="  title=Img open ";
		for (int p=0; p<1000; p++)
		{
			boolean found_it=false;
			String current_name=base_image_name+IJ.pad(p,4);
			for (int i=0; i<file_list.length; i++)
			{
				if ((file_list[i].contains(current_name)&&file_list[i].endsWith("nd2")))
				{
					current_idx=i;
					found_it=true;
				}
			}
			if (!found_it) break;
			got_series=true;
			String image_path=base_dir+file_list[current_idx];
			IJ.log("About to open: "+image_path);
			IJ.run("Bio-Formats Importer", "open="+image_path+" color_mode=Default concatenate_series open_all_series view=Hyperstack stack_order=XYCZT");
			WindowManager.getCurrentImage().setTitle("Img"+starting_index);
			concat_string=concat_string+"image"+starting_index+" ";
			starting_index++;
		}
		if (starting_index==1) return;
		
		concat_string=concat_string+"image"+starting_index+"=[-- None --]";
		
		config_path=base_dir+"Plate"+IJ.pad(plate, 3)+"_Well"+well+"_Object"+(object)+"_StageConfig.csv";
		img=WindowManager.getCurrentImage();
		int cs=img.getNChannels(), ss=img.getNSlices();
		if (starting_index==2) 
		{
			img.setTitle("Fused");
			return;
		}
		IJ.run("Concatenate...", concat_string);
		IJ.run("Stack to Hyperstack...", "order=xyczt(default) channels="+cs+" slices="+ss+" frames="+(starting_index-1)+" display=Grayscale");


		
		IJ.log(config_path);
		
		float [][] xy_pts=get_points_from_file(config_path);
		
		
		img=WindowManager.getCurrentImage();
		IJ.run("Flip Vertically", "stack");
		IJ.run("Flip Horizontally", "stack");
		
		
		float pix_size=(float) ((float)img.getCalibration().pixelWidth*1.2);
		if (override_scaling) 
		{
			pix_size=(float)pixel_scaling;
		}
		for (int j=0; j<xy_pts[0].length; j++)
		{
			xy_pts[0][j]=(float) (xy_pts[0][j]/pix_size);
			xy_pts[1][j]=(float) (xy_pts[1][j]/pix_size);
		}
		
		stitch_generic.stitch_img(WindowManager.getCurrentImage(), xy_pts[0], xy_pts[1], base_dir, stitch_channel-1, fusion_method);

	}
	public static float[][] get_points_from_file(String filename)
	{
		try {
			FileInputStream fos=new FileInputStream(filename);
			BufferedReader r= new BufferedReader(new InputStreamReader(fos));
			r.mark(0);
			String line=null;
			String delims=",";
			int ctr=0;
			float [][] tmp=new float [2][10000];
			while ((line=r.readLine())!=null)
			{
				if (ctr==0)
				{
					ctr++;
					continue;
				}
				String [] xy=line.split(delims);
				tmp[0][ctr-1]=Float.parseFloat(xy[0]);
				tmp[1][ctr-1]=Float.parseFloat(xy[1]);
				ctr++;
			}
			r.close();
			float [][] rtn=new float[2][ctr-1];
			for (int i=0; i<ctr-1; i++)
			{
				rtn[0][i]=tmp[0][i];
				rtn[1][i]=tmp[1][i];
			}
			return rtn;
		}
		catch (Exception e) 
		{
			IJ.log("Guess it didn't exist");
		}
		return null;
	}

}
