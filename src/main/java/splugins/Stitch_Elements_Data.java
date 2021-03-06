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
import ij.plugin.PlugIn;

public class Stitch_Elements_Data implements PlugIn {

	static Double number_worms;
	class worm_wrapper
	{
		String name;
		ArrayList <Double> x_loc;
		ArrayList <Double> y_loc;
	};
	@Override
	public void run(String arg0) {
		
		ImagePlus img=WindowManager.getCurrentImage();
		number_worms=(double) 0;
		
		GenericDialog gd = new GenericDialog("Merge Tiled Objects");
		gd.addStringField("Path to save to:  ", "/home/smc/Data/SMC/SimpleStitchTest/A2/");
		gd.showDialog();
		
		Calibration my_cal=img.getCalibration();
		
		String save_directory=gd.getNextString();
		String save_tmp_directory=save_directory+"tmp"+File.separator;
		
		File my_dir=new File(save_tmp_directory);
		my_dir.mkdir();
		
		String imgInfo=img.getInfoProperty();
		
		worm_wrapper my_worm=get_worm(imgInfo);
		
				
		IJ.runMacroFile("SaveMultipageImageSequence.ijm", save_tmp_directory);
		
		
		int width=img.getWidth(); 
		int height=img.getHeight();
		int cumulative_index=0;

			try {
				FileOutputStream fos=new FileOutputStream(save_tmp_directory+"out.txt");
				IJ.log(save_tmp_directory+"out.txt");
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
				
				double x_base=my_worm.x_loc.get(0);
				double y_base=my_worm.y_loc.get(0);
				
				for (int i=0; i<my_worm.x_loc.size(); i++)
				{
					IJ.log("Tiffs"+String.format("%04d", cumulative_index)+".tif; ; ("+(my_worm.x_loc.get(i)-x_base)/my_cal.pixelWidth+", "+(my_worm.y_loc.get(i)-y_base)/my_cal.pixelWidth+")\n");
					if (img.getNSlices()>1)
					{
						w.write("Tiffs"+String.format("%04d", cumulative_index)+".tif; ; ("+(my_worm.x_loc.get(i)-x_base)/my_cal.pixelWidth+", "+(my_worm.y_loc.get(i)-y_base)/my_cal.pixelWidth+", 0.0)\n");
					}
					else
					{
						w.write("Tiffs"+String.format("%04d", cumulative_index)+".tif; ; ("+(my_worm.x_loc.get(i)-x_base)/my_cal.pixelWidth+", "+(my_worm.y_loc.get(i)-y_base)/my_cal.pixelWidth+")\n");
					}
					
					cumulative_index++;
				}
				w.flush();
				w.close();
			}
			catch (Exception e) {}
			IJ.log("About to start processing this directory: "+save_tmp_directory+" and this file: out.txt");
			IJ.run("Grid/Collection stitching", "type=[Positions from file] order=[Defined by TileConfiguration] directory=["+save_tmp_directory+"] layout_file=out.txt fusion_method=[Max. Intensity] regression_threshold=0.30 max/avg_displacement_threshold=2.50 absolute_displacement_threshold=3.50 compute_overlap computation_parameters=[Save computation time (but use more RAM)] image_output=[Fuse and display]");
			//WindowManager.getCurrentImage().setTitle("Worm"+(j+1));

	}
	
	public static String getResult()
	{
		return number_worms.toString();
	}
	
	public worm_wrapper get_worm(String imgInfo)
	{
		int number_tiles=0;
		while (imgInfo.indexOf("series "+(number_tiles+1))>-1)
		{
			number_tiles++;
		}
		number_tiles--;
		
		worm_wrapper rtn=new worm_wrapper();
		rtn.x_loc=new ArrayList<Double>();
		rtn.y_loc=new ArrayList<Double>();
		
		for (int i=0; i<number_tiles; i++)
		{
			String test_string="series "+(i+1)+") X position = ";
			int start_idx=imgInfo.indexOf(test_string);
			int end_idx=imgInfo.indexOf("\n",start_idx+1);
			double x_val=Double.parseDouble(imgInfo.substring(start_idx+test_string.length(), end_idx));

			test_string="series "+(i+1)+") Y position = ";
			start_idx=imgInfo.indexOf(test_string);
			end_idx=imgInfo.indexOf("\n",start_idx+1);
			double  y_val=Double.parseDouble(imgInfo.substring(start_idx+test_string.length(), end_idx));
			
			rtn.x_loc.add(x_val);
			rtn.y_loc.add(y_val);
		}
		
		return rtn;
	}
	public ArrayList<worm_wrapper> find_location_entries(String info)
	{
		int idx=0;
		ArrayList<worm_wrapper> rtn=new ArrayList<worm_wrapper>();
		
		//Get the names of the individual tile acquisitions
		
		
		int tt=0;
		while (idx>-1)
		{
			String screen_string="series "+idx+")";
			int tidx=info.indexOf(screen_string)+screen_string.length();
			int newline_pos=info.indexOf("\n", tidx);
			String tmps=info.substring(tidx, newline_pos-1);
			
			IJ.log(tmps);
			if (!tmps.contains("IsRGB"))
			{
				IJ.log("Skipping");
				idx++;
				continue;
			}
			IJ.log("NEW ONE");
			IJ.log(screen_string+","+tidx);
			if (info.indexOf(screen_string)<0)
			{
				idx=-1;
				break;
			}
			int eidx=info.indexOf("(raw", tidx);
			IJ.log("idx:  "+idx+","+tidx+","+eidx);
			String tstring=info.substring(tidx+1,eidx);
			
			IJ.log(tstring);
			worm_wrapper tmp=new worm_wrapper();
			tmp.name=tstring;
			rtn.add(tmp);
			idx++;
			
			boolean stop_me=false;
			while (!stop_me)
			{
				screen_string="Series "+idx+" Name =";
				tidx=info.indexOf(screen_string)+screen_string.length();
				if (info.indexOf(screen_string)<0) 
				{
					idx=-1;
					break;
				}
				eidx=info.indexOf("(raw", tidx);
				IJ.log("idx:  "+idx+","+tidx+","+eidx);
				String bstring=info.substring(tidx+1,eidx);
				if (!bstring.equals(tstring)) stop_me=true;
				idx++;
			}
			
		}
		
		for (ListIterator<worm_wrapper> myI=rtn.listIterator(); myI.hasNext();)
		{
			worm_wrapper cur=myI.next();
			cur.x_loc=new ArrayList<Double>();
			cur.y_loc=new ArrayList<Double>();
			idx=0;
			int num_tiles=1;
			String screenX_string=cur.name+"(raw tile "+num_tiles+") X Location = ";
			String screenY_string=cur.name+"(raw tile "+num_tiles+") Y Location = ";
			
			//IJ.log(screenX_string);
			//IJ.log("idx, num_tiles"+idx+","+num_tiles);
			
			while (info.indexOf(screenX_string)>-1)
			{				
				int tidx=info.indexOf(screenX_string)+screenX_string.length();
				int eidx=info.indexOf(cur.name, tidx)-1;
				String X=info.substring(tidx, eidx);
				
				tidx=info.indexOf(screenY_string)+screenY_string.length();
				eidx=info.indexOf(cur.name, tidx)-1;
				String Y=info.substring(tidx, eidx);
				
				//IJ.log("Name,tile,X,Y: "+cur.name+","+num_tiles+","+X+","+Y);
				cur.x_loc.add(Double.parseDouble(X));
				cur.y_loc.add(Double.parseDouble(Y));
				
				num_tiles++;
				screenX_string=cur.name+"(raw tile "+num_tiles+") X Location = ";
				screenY_string=cur.name+"(raw tile "+num_tiles+") Y Location = ";
				//IJ.log("idx, num_tiles"+idx+","+num_tiles);
			}
		}
		
		for (ListIterator<worm_wrapper> myIter=rtn.listIterator(); myIter.hasNext();)
		{
			worm_wrapper tmp=myIter.next();
			for (int i=0; i<tmp.x_loc.size(); i++)
			{
				IJ.log("Worm,tile,X,Y: "+tmp.name+","+(i+1)+","+tmp.x_loc.get(i)+","+tmp.y_loc.get(i));
			}
		}
		
		return rtn;
	}
	
}
