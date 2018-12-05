package splugins;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.io.OpenDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import splugins.seeded_multipoint_adaptive_region_grow.MyIntPoint;

public class Peaks_From_CSV implements PlugIn {

	int width, height, channels, slices, frames, new_channels;
	ImagePlus img;
	@Override
	public void run(String arg0) {
		
		img=WindowManager.getCurrentImage();
		width=img.getWidth();
		height=img.getHeight();
		channels=img.getNChannels();
		slices=img.getNSlices();
		frames=img.getNFrames();
		Calibration cal=img.getCalibration();
		
		//Get the format, save the list of fields in field_list
		GenericDialog gd=new GenericDialog("Format");
		gd.addStringField("Fields (use semicolons to separate)", "");
		gd.showDialog();
		
		String format=gd.getNextString();
		String[] field_list=format.split(";");
		new_channels=field_list.length;
		
		OpenDialog od=new OpenDialog("Choose text file");
		
		//Files are small, will simply make an ArrayList that keeps track of all of the lines of the file
		ArrayList <String> file_data=new ArrayList <String> ();
		
		//Open the file and save the lines to the file_data object
		FileInputStream fos;
		try {
			fos = new FileInputStream(od.getPath());
			IJ.log(od.getPath());
			BufferedReader r= new BufferedReader(new InputStreamReader(fos));
			String line;
			while (((line=r.readLine())!=null))
			{
				file_data.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Process, turn into ArrayList array with size of number of fields
		class PointData{
			public int x;
			public int y;
			public int z;
			public String field;
			
			PointData(int a, int b, int c, String inp)
			{
				x=a;
				y=b;
				z=c;
				field=inp;
			}
		};
		ArrayList<PointData> [] data =new ArrayList[new_channels];
		for (int i=0; i<new_channels; i++) data[i]=new ArrayList<PointData>();
		IJ.log("Data: "+data[0].size());
		for (ListIterator file_iterator=file_data.listIterator();file_iterator.hasNext();)
		{
			String curstring=(String)file_iterator.next();
			String [] cur_splits=curstring.split(",");
			if (cur_splits.length<5) continue;
			for (int i=0; i<field_list.length; i++)
			{
				if (field_list[i].equals(cur_splits[6]))
				{
					IJ.log("Current string:  "+curstring);
					int x=(int)Math.round(Float.parseFloat(cur_splits[0])/1000.0/cal.pixelWidth);
					int y=(int)Math.round(Float.parseFloat(cur_splits[1])/1000.0/cal.pixelHeight);
					int z=(int)Math.round(Float.parseFloat(cur_splits[2])/1000.0/cal.pixelDepth);
					IJ.log(field_list[i]+","+x+","+y+","+z);
					data[i].add(new PointData(x,y,z, field_list[i]));
				}
			}
		}
		
		
		//Ready the new image
		ImagePlus new_img=NewImage.createByteImage("Result", (int)width, (int)height, new_channels*slices*frames, NewImage.FILL_BLACK);
		
		for (int i=0; i<data.length; i++)
		{
			ArrayList <PointData> cur_type=data[i];
			for (ListIterator spot_iterator=cur_type.listIterator(); spot_iterator.hasNext();)
			{
				PointData current_spot=(PointData) spot_iterator.next();
				byte [] pix=(byte [])new_img.getStack().getProcessor(i+new_channels*current_spot.z+1).getPixels();
				pix[current_spot.x+current_spot.y*width]=(byte) 255;
			}
		}
		new_img.setDimensions(new_channels,  slices,  frames);
		new_img.setOpenAsHyperStack(true);
		new_img.updateAndDraw();
		new_img.show();
		

	}

}
