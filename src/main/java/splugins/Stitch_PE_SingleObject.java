package splugins;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;

public class Stitch_PE_SingleObject implements PlugIn {

	class Positions
	{
		ArrayList <Double> x;
		ArrayList <Double> y;
		Positions ()
		{
			x=new ArrayList<Double>();
			y=new ArrayList<Double>();
		}
	}
	
	@Override
	public void run(String arg0) {
		
		ImagePlus img=WindowManager.getCurrentImage();
		int original_channels=img.getNChannels(), original_slices=img.getNSlices();
		String [] choices={"Linear Blending", "Max. Intensity" };
		
		GenericDialog gd = new GenericDialog("Merge Tiled Objects");
		gd.addStringField("Path to save to:  ", "C:\\Users\\smc\\Desktop\\tmp\\");
		gd.addChoice("Fusion method: ", choices, "Linear Blending");
		gd.addCheckbox("Override calibration?", false);
		gd.addNumericField("Override pixel size:", 1.0, 1);
		gd.addCheckbox("Use a projection for stitching?", false);
		gd.addNumericField("If so, which channel: ", 1,0);
		gd.showDialog();
		
		Calibration my_cal=img.getCalibration();
		
		String save_directory=gd.getNextString()+File.separator;
		String save_tmp_directory=save_directory+"tmp/";
		String chosen_fusion=gd.getNextChoice();
		boolean linear_blend;
		if (chosen_fusion.contains("Blending")) linear_blend=true;
		else linear_blend=false;
		
		boolean override=gd.getNextBoolean();
		double over_calib=gd.getNextNumber();
		if (override) my_cal.pixelWidth=my_cal.pixelHeight=over_calib;
		boolean do_projection=gd.getNextBoolean();
		int projection_channel=(int)gd.getNextNumber()-1;
		
		File my_dir=new File(save_tmp_directory);
		my_dir.mkdir();
		
		
		String imgInfo=img.getInfoProperty();
		
		Positions pos_list=find_location_entries(imgInfo);
		
		if (do_projection)
		{
			ImagePlus new_img=Paste_Projection_To_Front.paste_project(img, projection_channel);
			img=new_img;
		}
		
		SaveMultipageImageSequence.save_sequence(img, save_tmp_directory);
		
		int width=img.getWidth(); 
		int height=img.getHeight();
		int cumulative_index=0;
		
		try 
		{
			FileOutputStream fos=new FileOutputStream(save_tmp_directory+"out.txt");
			Writer w= new BufferedWriter(new OutputStreamWriter(fos));
			w.write("# Define the number of dimensions we are working on\n");
			if (img.getNSlices()>1)
			{
				w.write("dim = 3\n\n# Define the image coordinates\n");
			}
			else
			{
				w.write("dim = 2\n\n# Define the image coordinates\n");
			}
			
			double x_base=pos_list.x.get(0);
			double y_base=pos_list.y.get(0);
			
			for (int i=0; i<pos_list.x.size(); i++)
			{
				double cur_x=pos_list.x.get(i);
				double cur_y=pos_list.y.get(i);
				IJ.log("Tiffs"+String.format("%04d", i)+".tif; ; ("+(cur_x-x_base)/my_cal.pixelWidth+", "+(cur_y-y_base)/my_cal.pixelWidth+")\n");
				if (img.getNSlices()>1)
				{
					w.write("Tiffs"+String.format("%04d", i)+".tif; ; ("+(cur_x-x_base)/my_cal.pixelWidth+", "+(cur_y-y_base)/my_cal.pixelWidth+",0.0)\n");
				}
				else
				{
					w.write("Tiffs"+String.format("%04d", i)+".tif; ; ("+(cur_x-x_base)/my_cal.pixelWidth+", "+(cur_y-y_base)/my_cal.pixelWidth+")\n");
				}
				
				cumulative_index++;
			}
			w.flush();
			w.close();
		}
		catch (Exception e) {}
		IJ.log("About to start processing this directory: "+save_tmp_directory+" and this file: out.txt");
		if (linear_blend)
		{
			IJ.run("Grid/Collection stitching", "type=[Positions from file] order=[Defined by TileConfiguration] directory=["+save_tmp_directory+"] layout_file=out.txt fusion_method=[Linear Blending] regression_threshold=0.30 max/avg_displacement_threshold=2.50 absolute_displacement_threshold=3.50 compute_overlap computation_parameters=[Save computation time (but use more RAM)] image_output=[Fuse and display]");
		}
		else
		{
			IJ.run("Grid/Collection stitching", "type=[Positions from file] order=[Defined by TileConfiguration] directory=["+save_tmp_directory+"] layout_file=out.txt fusion_method=[Max. Intensity] regression_threshold=0.30 max/avg_displacement_threshold=2.50 absolute_displacement_threshold=3.50 compute_overlap computation_parameters=[Save computation time (but use more RAM)] image_output=[Fuse and display]");
		}
		
		if (do_projection)
		{
			ImagePlus current_img=WindowManager.getCurrentImage();
			ImagePlus final_img=new Duplicator().run(current_img, 2, current_img.getNChannels(), 1, current_img.getNSlices(), 1, current_img.getNFrames());
			final_img.setOpenAsHyperStack(true);
			final_img.setDimensions(original_channels, original_slices, 1);
			current_img.close();
			final_img.show();
			final_img.updateAndDraw();
		}
	}
	
	
	public Positions find_location_entries(String info)
	{
		int ctr=1;
		int idx=info.indexOf("raw tile "+ctr);
		Positions rtn=new Positions();
		while (idx>=0)
		{
			String test_string="(raw tile "+ctr+") X Location = ";
			int xi=info.indexOf(test_string)+test_string.length();
			int xf=info.indexOf("\n", xi)-0;
			IJ.log(info.substring(xi, xf));
			double x=Double.parseDouble(info.substring(xi, xf));
			
			test_string="(raw tile "+ctr+") Y Location = ";
			int yi=info.indexOf(test_string)+test_string.length();
			int yf=info.indexOf("\n", yi)-0;
			IJ.log(info.substring(yi, yf));
			double y=Double.parseDouble(info.substring(yi, yf));
			
			rtn.x.add(x);
			rtn.y.add(y);
			
			ctr++;
			idx=info.indexOf("raw tile "+ctr);
		}
		return rtn;
	}
}
