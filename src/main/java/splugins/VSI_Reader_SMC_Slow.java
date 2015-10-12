package splugins;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class VSI_Reader_SMC_Slow implements PlugIn {

	@Override
	public void run(String arg0) {
		String base_file=IJ.getFilePath("Choose file");
		String start_dir=base_file.substring(0, base_file.lastIndexOf(File.separator)+1);
		IJ.run("Bio-Formats Importer", "open="+base_file+" color_mode=Default view=Hyperstack stack_order=XYCZT series_1");
		ImagePlus img=WindowManager.getCurrentImage();
		String imgInfo=img.getInfoProperty();
		double overview_pw=img.getCalibration().pixelWidth;
		
		String matcher="Origin #1 = ";
		int s=imgInfo.indexOf(matcher);
		int t=imgInfo.indexOf(",",s);
		int u=imgInfo.indexOf(")", t);
		double top_left_overview_x=Double.parseDouble(imgInfo.substring(s+matcher.length()+1, t));
		double top_left_overview_y=Double.parseDouble(imgInfo.substring(t+1, u));
		
		int [][]ROI_list=new int[100][5];
		int n_rois=0;
		
		int overview_width=img.getWidth();
		int overview_height=img.getHeight();
		
		IJ.log("Corner: "+top_left_overview_x+","+top_left_overview_y);
		
		double old_y=-100000000;
		int ctr=0;
		for (int i=1; i<3; i++)
		{
			
			String fname="Image_"+IJ.pad(i,2)+".vsi";
			IJ.run("Bio-Formats Importer", "open="+start_dir+fname+" color_mode=Default view=Hyperstack stack_order=XYCZT use_virtual_stack series_1");
			ImagePlus temp_img=WindowManager.getCurrentImage();
			imgInfo=temp_img.getInfoProperty();
			double zoom_pw=temp_img.getCalibration().pixelWidth;
			int wid=temp_img.getWidth();
			int hei=temp_img.getHeight();

			
			matcher="Origin #1 = ";
			s=imgInfo.indexOf(matcher);
			t=imgInfo.indexOf(",",s);
			u=imgInfo.indexOf(")", t);
			double x_pos=Double.parseDouble(imgInfo.substring(s+matcher.length()+1, t));
			double y_pos=Double.parseDouble(imgInfo.substring(t+1, u));
			IJ.log("i, X, Y: "+i+","+x_pos+","+y_pos);
			temp_img.close();
			if (old_y>y_pos) 
			{
				ctr=i-1;
				break;
			}
			int wid_over=(int) Math.floor(wid*zoom_pw/overview_pw);
			int hei_over=(int) Math.floor(hei*zoom_pw/overview_pw);

			int offset_x_pixel=(int) Math.floor((x_pos-top_left_overview_x)/overview_pw);
			int offset_y_pixel=(int) Math.floor((y_pos-top_left_overview_y)/overview_pw);

			ROI_list[n_rois][0]=i;
			ROI_list[n_rois][1]=offset_x_pixel;
			ROI_list[n_rois][2]=offset_y_pixel;
			ROI_list[n_rois][3]=wid_over;
			ROI_list[n_rois][4]=hei_over;
			n_rois++;

			old_y=y_pos;
		}
		byte [] pix=(byte [])img.getProcessor().getPixels();
		for (int i=0; i<n_rois; i++)
		{
			for (int x=ROI_list[i][1]; x<ROI_list[i][1]+ROI_list[i][3]; x++)
			{
				for (int y=ROI_list[i][2]; y<ROI_list[i][2]+ROI_list[i][4]; y++)
				{
					IJ.log("x,y: "+x+","+y);
					pix[x+y*overview_width]=0;
				}
			}
		}
		img.show();
		img.updateAndDraw();
		
		

	}

}
