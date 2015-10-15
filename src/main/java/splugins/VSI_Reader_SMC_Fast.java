package splugins;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

public class VSI_Reader_SMC_Fast implements KeyListener, MouseListener, PlugIn {

	ImageCanvas canvas;
	ImageWindow win;
	ImagePlus img;
	
	int n_rois=0;
	String start_dir;
	ArrayList <Integer> master_list=new ArrayList <Integer>();
	class Meta_Data{
		int width;
		int height;
		double pixel_size;
		double origin_x;
		double origin_y;
	};
	class ROI_List {
		int file_index;
		int x_origin;
		int y_origin;
		int width_in_overview;
		int height_in_overview;
		int width_in_original;
		int height_in_original;
		String img_name;
	};
	ROI_List [] the_ROI_list;
	@Override
	public void run(String arg0) {
		
		String base_file=IJ.getFilePath("Choose file");
		start_dir=base_file.substring(0, base_file.lastIndexOf(File.separator)+1);
		
		GenericDialog gd=new GenericDialog("Number of first object associated with this overview:  ");
		gd.addNumericField("First object file number", 1, 0);
		gd.showDialog();
		int initial_val=(int) gd.getNextNumber();
		
		Meta_Data meta=get_meta_data(base_file);
		
		double overview_pw=meta.pixel_size;
		
		/***************Added to make less painful to select ROIs, using scaled down version instead**************/
		IJ.run("Bio-Formats Importer", "open="+base_file+" color_mode=Default view=Hyperstack stack_order=XYCZT series_4");
		img=WindowManager.getCurrentImage();
		overview_pw=overview_pw*8;
		/*****************End time saver**********************************/
		
		win = img.getWindow();
        canvas = win.getCanvas();
		win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        win.addWindowListener(win);
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
        
        int max_rois=100;
        the_ROI_list=new ROI_List[max_rois];
        for (int i=0; i<max_rois; i++) the_ROI_list[i]=new ROI_List();
		
		
		double top_left_overview_x=meta.origin_x;
		double top_left_overview_y=meta.origin_y;
		
		
		
		
		int overview_width=img.getWidth();
		int overview_height=img.getHeight();
		
		IJ.log("Corner: "+top_left_overview_x+","+top_left_overview_y);
		
		double old_y=-100000000;
		int ctr=0;
		for (int i=initial_val; i<10000; i++)
		{
			
			String fname="Image_"+IJ.pad(i,2)+".vsi";
			
			meta=get_meta_data(start_dir+fname);
			
			double zoom_pw=meta.pixel_size;
			int wid=meta.width;
			int hei=meta.height;
			double x_pos=meta.origin_x;
			double y_pos=meta.origin_y;
			
			if (old_y>y_pos) 
			{
				ctr=i-1;
				break;
			}
			int wid_over=(int) Math.floor(wid*zoom_pw/overview_pw);
			int hei_over=(int) Math.floor(hei*zoom_pw/overview_pw);

			int offset_x_pixel=(int) Math.floor((x_pos-top_left_overview_x)/overview_pw);
			int offset_y_pixel=(int) Math.floor((y_pos-top_left_overview_y)/overview_pw);

			the_ROI_list[n_rois].file_index=i;
			the_ROI_list[n_rois].x_origin=offset_x_pixel;
			the_ROI_list[n_rois].y_origin=offset_y_pixel;
			the_ROI_list[n_rois].width_in_overview=wid_over;
			the_ROI_list[n_rois].height_in_overview=hei_over;
			the_ROI_list[n_rois].width_in_original=wid;
			the_ROI_list[n_rois].height_in_original=hei;
			n_rois++;

			old_y=y_pos;
		}
		byte [] pix=(byte [])img.getProcessor().getPixels();
		for (int i=0; i<n_rois; i++)
		{
			for (int x=the_ROI_list[i].x_origin; x<the_ROI_list[i].x_origin+the_ROI_list[i].width_in_overview; x++)
			{
				for (int y=the_ROI_list[i].y_origin; y<the_ROI_list[i].y_origin+the_ROI_list[i].height_in_overview; y++)
				{
					pix[x+y*overview_width]=0;
				}
			}
		}
		img.show();
		img.updateAndDraw();
		
		

	}
	
	public Meta_Data get_meta_data(String fname)
	{
		ImageProcessorReader r = new ImageProcessorReader(
				new ChannelSeparator(LociPrefs.makeImageReader()));
		Meta_Data rtnval=new Meta_Data();
		try {
			try {
				
				
				r.setId(fname);
				rtnval.height=r.getSizeY();
				rtnval.width=r.getSizeX();
				
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		catch(IOException exc)
		{
			IJ.error(exc.getMessage());
		}
		return rtnval;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x, y;
		x=e.getX();
		y=e.getY();
		x=canvas.offScreenX(x);
		y=canvas.offScreenY(y);
		
		int rtn_idx=find_idx_containing_ROI(x,y);
		
		if (rtn_idx<0) return;
		
		Roi my_roi=new Roi(the_ROI_list[rtn_idx].x_origin,the_ROI_list[rtn_idx].y_origin,the_ROI_list[rtn_idx].width_in_overview,the_ROI_list[rtn_idx].height_in_overview);
		
		RoiManager manager=RoiManager.getInstance();
		
		if (manager==null)
		{
			manager=new RoiManager();
			
		}
		
		manager.addRoi(my_roi);
		manager.runCommand("Show All");
		
		//master_list.add(ROI_list[rtn_idx][0]);
		master_list.add(rtn_idx);

		img.updateAndDraw();
		WindowManager.setCurrentWindow(img.getWindow());
		
	}
	
	private int find_idx_containing_ROI(int x, int y)
	{
		for (int i=0; i<n_rois; i++)
		{
			if (the_ROI_list[i].x_origin<=x&&the_ROI_list[i].x_origin+the_ROI_list[i].width_in_overview>=x&&the_ROI_list[i].y_origin<=y&&the_ROI_list[i].y_origin+the_ROI_list[i].height_in_overview>=y) return i;
		}
		return -1;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		char rtn;
		rtn=e.getKeyChar();
		//if (rtn!='j'&&rtn!='q') return;
		if (rtn=='q')
		{
			canvas.removeMouseListener(this);
		    canvas.removeKeyListener(this);
		    String concat_list="";
		    if (master_list.size()>0)
		    {
		    	int max_width=0;
		    	int max_height=0;
		    	for (int i=0; i<master_list.size(); i++)
				{
					if (the_ROI_list[master_list.get(i)].width_in_original>max_width) max_width=the_ROI_list[master_list.get(i)].width_in_original;
					if (the_ROI_list[master_list.get(i)].height_in_original>max_height) max_height=the_ROI_list[master_list.get(i)].height_in_original;
		    		
		    	}
		    	
		    	for (int i=0; i<master_list.size(); i++)
				{
					String fname="Image_"+IJ.pad(the_ROI_list[master_list.get(i)].file_index,2)+".vsi";
					IJ.run("Bio-Formats Importer", "open="+start_dir+fname+" color_mode=Default view=Hyperstack stack_order=XYCZT series_1");
					IJ.run("Canvas Size...", "width="+max_width+" height="+max_height+" position=Center");
					WindowManager.getCurrentImage().setTitle("img"+(i+1));
					the_ROI_list[master_list.get(i)].img_name=WindowManager.getCurrentImage().getTitle();
					concat_list=concat_list+"image"+(i+1)+"="+the_ROI_list[master_list.get(i)].img_name+" ";
		    	}
		    }
		    concat_list=concat_list+"image"+(master_list.size()+1)+"=[-- None --]";
		    IJ.log(concat_list);
		    IJ.run("Concatenate...", "  title=Concatenated "+concat_list);
		}
		if (rtn=='d')
		{
			RoiManager.getInstance().select(RoiManager.getInstance().getCount()-1);
			RoiManager.getInstance().runCommand("Delete");
			master_list.remove(master_list.size()-1);
			WindowManager.setCurrentWindow(img.getWindow());
		}
		//run("Canvas Size...", "width=7000 height=5000 position=Center");
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
