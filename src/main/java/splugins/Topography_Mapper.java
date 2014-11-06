package splugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Topography_Mapper implements PlugIn {

	@Override
	public void run(String arg0) {
		String start_phrase="<elevation>";
		String stop_phrase="</elevation>";
		String location_phrase="<location>";
		float [] img_data=new float[512*512];
		ImagePlus new_img=NewImage.createFloatImage("Projection", 512, 512, 1, NewImage.FILL_BLACK);
		float [] pix=(float []) new_img.getProcessor().getPixels();
		//double km_per_line=.01;
		GenericDialog dlg=new GenericDialog("Mapper");
		dlg.addNumericField("Latitude SW corner:", 38.998042, 6);
		dlg.addNumericField("Longitude SW corner:", -94.605, 6);
		dlg.addNumericField("How wide (in km):", .01, 3);
		dlg.addNumericField("Starting index", 0, 0);
		dlg.showDialog();
		double start_lat=dlg.getNextNumber();
		double start_lon=dlg.getNextNumber();
		double km_per_line_lat=dlg.getNextNumber();
		int init=(int)dlg.getNextNumber();
		
		km_per_line_lat=km_per_line_lat/512;
		
		double km_per_lon=111.3*Math.cos(start_lat/360*2*3.141592653589);
		double final_longitude=512*km_per_line_lat/km_per_lon+start_lon;
		IJ.log("km per lon: "+km_per_lon);
		IJ.log("Start longitude: "+start_lon+" Final longitude: "+final_longitude);
		//if (init==0) return;
		try {
			IJ.log("Here I go!");
			for (int i=0; i<0+512; i++)
			{
				double latitude=start_lat+i*km_per_line_lat/111.3;
				String sent="https://maps.googleapis.com/maps/api/elevation/xml?path="+latitude+","+start_lon+"|"+latitude+","+final_longitude+"&samples=512";
				IJ.log(sent);
				URLConnection con=new URL(sent).openConnection();
				InputStream stream= con.getInputStream();
				String data=IOUtils.toString(stream,"ASCII");
				IJ.log(data);
				int offset=0;
				int counter=0;
				while (offset<data.length()-20)
				{
					String remaining_string=data.substring(offset, data.length());
					int start=remaining_string.indexOf("<elevation>"), end=remaining_string.indexOf("</elevation>");
					double elevation=Double.parseDouble(remaining_string.substring(start+start_phrase.length(), end-1));
					int next=remaining_string.indexOf(location_phrase,start)+location_phrase.length();
					if (remaining_string.indexOf(location_phrase,start)<1) offset=data.length();
					else offset+=next;
					IJ.log("index: "+counter+" elevation:" + elevation);
					img_data[counter+i*512]=(float)elevation;
					counter++;	
				}
				IJ.wait(50);
			}
			for (int i=0; i<512*512; i++)
			{
				pix[i]=img_data[i];
			}
			new_img.show();
			new_img.updateAndDraw();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
