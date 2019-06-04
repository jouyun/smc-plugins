package splugins;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import splugins.VSI_Reader_SMC_Fast.Meta_Data;
import splugins.VSI_Reader_SMC_Fast.My_Frame;
import splugins.VSI_Reader_SMC_Fast.My_Location;
import splugins.VSI_Reader_SMC_Fast.My_ROI;
import splugins.VSI_Reader_SMC_Fast.ROI_List;

public class Export_VSI_InOrder implements PlugIn {

	ImageCanvas canvas;
	ImageWindow win;
	ImagePlus img;
	
	int n_rois=0;
	String start_dir;
	ArrayList <Integer> series_list=new ArrayList <Integer>();
	ArrayList <My_ROI> master_list=new ArrayList <My_ROI>();
	int [][] tray_list;
	String prelude;
	double overview_pw;
	double top_left_overview_x;
	double top_left_overview_y;
	
	class Meta_Data{
		int width;
		int height;
		double pixel_size;
		double origin_x;
		double origin_y;
	};


	String output_path;
	@Override
	public void run(String arg0) {
		
		String file_name=IJ.getFilePath("Choose file");

		GenericDialog gd=new GenericDialog("Zoom factor");
		//gd.addStringField("Directory for saving: ", "");
		gd.addNumericField("Zoom of highress:  ", 1, 0);
		gd.showDialog();
		int dezoom_scale=(int)Math.floor(gd.getNextNumber());
		//output_path=gd.getNextString();
		
		int first_series=6;
		
		int number_series=Get_Number_Series(file_name);
		
		
		
		for (int i=1; i<number_series; i++)
		{
			get_series_data(file_name, i);
		}
		
		int number_images=series_list.size();
		
		double [][] organizer =new double [number_images][3];
		for (int i=0; i<series_list.size(); i++)
		{
			Meta_Data md=get_meta_data(file_name, series_list.get(i));
			organizer[i][0]=md.origin_x;
			organizer[i][1]=md.origin_y;
			organizer[i][2]=series_list.get(i);
		}
		
		Manual_Tracker.vector_sort(organizer, false);
		
		for (int i=0; i<number_images; i++)
		{
			int cidx=(int) organizer[i][2];
			IJ.log(file_name);
			IJ.log(""+(cidx));
			IJ.run("Bio-Formats Importer", "open=["+file_name+"] color_mode=Default view=Hyperstack stack_order=XYCZT series_"+(cidx+dezoom_scale));
			if (WindowManager.getCurrentImage().getNSlices()>1) IJ.run("Z Project...", "projection=[Average Intensity]");
			IJ.run("Save As Tiff", "save=["+file_name+"_Img"+IJ.pad(i, 4)+".tif]");
			IJ.run("Close All");
		}
	}
	

	public int Get_Number_Series(String fname)
	{
		ImageProcessorReader r = new ImageProcessorReader(
				new ChannelSeparator(LociPrefs.makeImageReader()));
		Meta_Data rtnval=new Meta_Data();
		try {
			try {
				
				
				r.setId(fname);
				return(r.getSeriesCount());
			}catch (FormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		catch(IOException exc)
		{
			IJ.error(exc.getMessage());
		}
		return 0;
	}
	
	public Meta_Data get_meta_data(String fname, int series)
	{
		ImageProcessorReader r = new ImageProcessorReader(
				new ChannelSeparator(LociPrefs.makeImageReader()));
		Meta_Data rtnval=new Meta_Data();
		try {
			try {
				
				
				r.setId(fname);
				IJ.log("Series count: "+r.getSeriesCount()+","+r.getSeries());
				if (series>r.getSeriesCount()) return null;
				r.setSeries(series);
				IJ.log("Current series: "+r.getSeries());
				rtnval.height=r.getSizeY();
				rtnval.width=r.getSizeX();
				
				IJ.log(""+r.getSizeC());
				
				
				String origin=(String)r.getSeriesMetadataValue("Origin #1");
				String origin_x=origin.substring(1, origin.indexOf(","));
				String origin_y=origin.substring(origin.indexOf(",")+1, origin.length()-1);
				
				String calib=(String)r.getSeriesMetadataValue("Calibration #1");
				calib=calib.substring(1, calib.indexOf(","));
				
				rtnval.origin_x=Double.parseDouble(origin_x);
				rtnval.origin_y=Double.parseDouble(origin_y);
				rtnval.pixel_size=Double.parseDouble(calib);
				
				IJ.log("wid, hei, ox, oy, pix:  "+rtnval.width+","+rtnval.height+","+rtnval.origin_x+","+rtnval.origin_y+","+rtnval.pixel_size);
				
			} catch (FormatException e) {
				IJ.log("Ooops I caught something");
				e.printStackTrace();
				
			}
			
		}
		catch(IOException exc)
		{
			IJ.error(exc.getMessage());
		}
		return rtnval;
	}
	
	public void get_series_data(String fname, int series)
	{
		ImageProcessorReader r = new ImageProcessorReader(
				new ChannelSeparator(LociPrefs.makeImageReader()));
		Meta_Data rtnval=new Meta_Data();
		try {
			try {
				
				
				r.setId(fname);
				IJ.log("Series count: "+r.getSeriesCount()+","+r.getSeries());
				if (series>r.getSeriesCount()) return;
				r.setSeries(series);
				IJ.log("Current series: "+r.getSeries());
				rtnval.height=r.getSizeY();
				rtnval.width=r.getSizeX();
				
				IJ.log(""+r.getSizeC());
				
				Hashtable meta=r.getSeriesMetadata();
				if (meta.containsKey("Origin #1"))
				{
					IJ.log("I had it");
					series_list.add(series);
				}
				
			} catch (FormatException e) {
				IJ.log("Ooops I caught something");
				e.printStackTrace();
				
			}
			
		}
		catch(IOException exc)
		{
			IJ.error(exc.getMessage());
		}
	}




}
