package splugins;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

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
	class My_Location
	{
		int tray;
		int slide;
		int counter;
	};
	class My_ROI {
		int counter;
		int x_origin;
		int y_origin;
		int width_in_overview;
		int height_in_overview;
		int width_in_original;
		int height_in_original;
		String fname;
	};
	class My_Frame
	{
		int frame;
		int tray;
		int slide;
		ArrayList <My_ROI> loc_list;
	};
	ROI_List [] the_ROI_list;
	My_ROI [][][] full_ROI_list;
	ArrayList <My_Frame> frame_list;
	@Override
	public void run(String arg0) {
		
		String base_directory=IJ.getDirectory("");
		
		//Populate organizational database
		get_frame_list(base_directory);
		
		/************Get overview stack**************/
		String concat_list="";
		int tmp_ctr=1;
		for (ListIterator<My_Frame> jF=frame_list.listIterator(); jF.hasNext();)
		{
			My_Frame cur_frame=jF.next();
			IJ.log("Doing frame: "+cur_frame.frame+","+cur_frame.tray+","+cur_frame.slide);
			String base_file=base_directory+File.separator+prelude+IJ.pad(cur_frame.tray, 2)+"_"+IJ.pad(cur_frame.slide, 2)+"_"+IJ.pad(1, 2)+File.separator+"Image_Overview.vsi";
			IJ.run("Bio-Formats Importer", "open="+base_file+" color_mode=Default view=Hyperstack stack_order=XYCZT series_4");
			WindowManager.getCurrentImage().setTitle("img"+(tmp_ctr));
			concat_list=concat_list+"image"+tmp_ctr+"="+WindowManager.getCurrentImage().getTitle()+" ";
			tmp_ctr++;
		}
		//Concatenate it
		if (frame_list.size()>1)
		{
			concat_list=concat_list+"image"+tmp_ctr+"=[-- None --]";
			IJ.log(concat_list);
			IJ.run("Concatenate...", "  title=Concatenated "+concat_list);
		}
		else
		{
			WindowManager.getCurrentImage().setTitle("Concatenated");
		}
		IJ.run("16-bit");
		img=WindowManager.getCurrentImage();
		
		/************Draw rois on appropriate slice***************/
		for (int f=0; f<frame_list.size(); f++)
		{
			My_Frame cur_frame=frame_list.get(f);
			short [] pix=(short []) img.getStack().getProcessor((f)*img.getNChannels()+1).getPixels(); 
			for (ListIterator <My_ROI> fF=cur_frame.loc_list.listIterator(); fF.hasNext();)
			{
				My_ROI cur_ROI=fF.next();
				paint_box(pix, cur_ROI.x_origin, cur_ROI.y_origin, cur_ROI.width_in_overview, cur_ROI.height_in_overview, img.getWidth(), img.getHeight());
			}
		}

		img.updateAndDraw();
		IJ.run("Enhance Contrast", "saturated=0.35");
		
		win = img.getWindow();
        canvas = win.getCanvas();
		win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        win.addWindowListener(win);
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
	}
	
	public String generate_full_file_name(String direc, int tray, int slide, int counter)
	{
		//First have to figure out name of this folder's contained vsi file (it VARIES for no obvious reason!)
		String fname=direc+prelude+IJ.pad(tray, 2)+"_"+IJ.pad(slide, 2)+"_"+IJ.pad(counter, 2)+File.separator;
		File[] file_list=(new File(fname)).listFiles();
		String[] file_names=new String[file_list.length];
		for (int i=0; i<file_list.length; i++) 
		{
			if (file_list[i].getName().endsWith(".vsi"))
			{
				fname=fname+file_list[i].getName();
				break;
			}
		}
		//String rtn=direc+prelude+IJ.pad(tray, 2)+"_"+IJ.pad(slide, 2)+"_"+IJ.pad(counter, 2)+File.separator+"Image_01.vsi";
		
		String tmp=direc+prelude+IJ.pad(tray, 2)+"_"+IJ.pad(slide, 2)+"_"+IJ.pad(counter, 2)+File.separator;
		return(fname);
	}
	
	public String generate_full_file_overview_name(String direc, int tray, int slide)
	{
		String rtn=direc+prelude+IJ.pad(tray, 2)+"_"+IJ.pad(slide, 2)+"_"+IJ.pad(1, 2)+File.separator+"Image_Overview.vsi";
		String tmp=direc+prelude+IJ.pad(tray, 2)+"_"+IJ.pad(slide, 2)+"_"+IJ.pad(1, 2)+File.separator;
		return(rtn);
	}
	
	public void paint_box(short [] pix, int x0, int y0, int w, int h, int wmax, int hmax)
	{
		IJ.log("Painting box:  "+x0+","+y0+","+w+","+h);
		for (int x=x0; x<x0+w; x++)
		{
			int y=y0;
			if (x>0&&x<wmax&&y>0&&y<hmax) pix[x+y*wmax]=0;
			y=y0+h;
			if (x>0&&x<wmax&&y>0&&y<hmax) pix[x+y*wmax]=0;
		}
		for (int y=y0; y<y0+h; y++)
		{
			int x=x0;
			if (x>0&&x<wmax&&y>0&&y<hmax) pix[x+y*wmax]=0;
			x=x0+w;
			if (x>0&&x<wmax&&y>0&&y<hmax) pix[x+y*wmax]=0;
		}
	}
	
	public void get_frame_list(String fname)
	{
		File[] file_list=(new File(fname)).listFiles();
		String[] file_names=new String[file_list.length];
		for (int i=0; i<file_list.length; i++) file_names[i]=file_list[i].getName();
		Arrays.sort(file_names);
		frame_list=new ArrayList <My_Frame> ();
		
		//Do a first pass to generate frame list
		for (int i=0; i<file_names.length; i++)
		{
			String name=file_names[i];
			IJ.log(name);
			My_Location cur_loc=parse_fname(name);
			
			My_Frame the_frame=null; 
			for (ListIterator<My_Frame> jF=frame_list.listIterator(); jF.hasNext();)
			{
				My_Frame cur_frame=jF.next();
				if (cur_frame.tray!=cur_loc.tray||cur_frame.slide!=cur_loc.slide) continue;
				the_frame=cur_frame;
				IJ.log("Was already in:  "+cur_frame.tray+","+cur_frame.slide);
				break;
			}
			if (the_frame==null)
			{
				the_frame=new My_Frame();
				the_frame.frame=frame_list.size()+1;
				the_frame.tray=cur_loc.tray;
				the_frame.slide=cur_loc.slide;
				the_frame.loc_list=new ArrayList <My_ROI>();
				frame_list.add(the_frame);
				//Create but DO NOT ADD!  If you are creating a loc it is the overview image
				//and that should not be included in the list of high-res images
				
				//Also since this is definitely an overview image we should get the overview_pw, etc.
				Meta_Data meta=get_meta_data(generate_full_file_overview_name(fname, cur_loc.tray, cur_loc.slide));
				overview_pw=meta.pixel_size*8;
				top_left_overview_x=meta.origin_x;
				top_left_overview_y=meta.origin_y;
				continue;
			}
		}
		
		//Do second pass to fill in roi_lists
		for (int i=0; i<file_names.length; i++)
		{
			String name=file_names[i];
			IJ.log(name);
			My_Location cur_loc=parse_fname(name);
			
			My_Frame the_frame=null; 
			for (ListIterator<My_Frame> jF=frame_list.listIterator(); jF.hasNext();)
			{
				My_Frame cur_frame=jF.next();
				if (cur_frame.tray!=cur_loc.tray||cur_frame.slide!=cur_loc.slide) continue;
				the_frame=cur_frame;
				//IJ.log("Found it at:  "+cur_frame.tray+","+cur_frame.slide);
				break;
			}			
			//Only still here if we found one that was not new, so create My_ROI and append to list
			if (cur_loc.counter!=1) 
			{
				//IJ.log("About to try:  "+cur_loc.tray+","+cur_loc.slide+","+cur_loc.counter);
				My_ROI new_ROI=get_ROI_from_file(generate_full_file_name(fname, cur_loc.tray, cur_loc.slide, cur_loc.counter), cur_loc.counter);
				the_frame.loc_list.add(new_ROI);
			}
		}
	}
	
	public My_Location parse_fname(String inp)
	{
		My_Location rtn=new My_Location();
		int last=inp.lastIndexOf("_");
		int nxt_last=inp.lastIndexOf("_", last-1);
		int first=inp.lastIndexOf("_", nxt_last-1);
		if (first<0) first=0;
		else first=first+1;
		
		String tray=inp.substring(first,  nxt_last);
		String slide=inp.substring(nxt_last+1, last);
		String ctr=inp.substring(last+1);
		//IJ.log("Here they are:  "+tray+","+slide+","+ctr);
		rtn.tray=Integer.parseInt(tray);
		rtn.slide=Integer.parseInt(slide);
		rtn.counter=Integer.parseInt(ctr);
		
		if (prelude==null) prelude=inp.substring(0,first);
		return rtn;
	}
	
	public My_ROI get_ROI_from_file(String fname, int counter)
	{
		My_ROI rtn=new My_ROI();
		
		IJ.log(fname);
		
		Meta_Data meta=get_meta_data(fname);
		
		double zoom_pw=meta.pixel_size;
		int wid=meta.width;
		int hei=meta.height;
		double x_pos=meta.origin_x;
		double y_pos=meta.origin_y;
		int wid_over=(int) Math.floor(wid*zoom_pw/overview_pw);
		int hei_over=(int) Math.floor(hei*zoom_pw/overview_pw);
		
		IJ.log("meta: "+x_pos+","+y_pos+","+zoom_pw+","+overview_pw+","+wid_over+","+hei_over);
		
		int offset_x_pixel=(int) Math.floor((x_pos-top_left_overview_x)/overview_pw);
		int offset_y_pixel=(int) Math.floor((y_pos-top_left_overview_y)/overview_pw);
		
		IJ.log("Processed:  "+zoom_pw+","+overview_pw+","+offset_x_pixel+","+offset_y_pixel);
		
		rtn.counter=counter;
		rtn.x_origin=offset_x_pixel;
		rtn.y_origin=offset_y_pixel;
		rtn.width_in_overview=wid_over;
		rtn.height_in_overview=hei_over;
		rtn.width_in_original=(int)Math.ceil(wid);
		rtn.height_in_original=(int)Math.ceil(hei);
		rtn.fname=fname;
		
		return rtn;
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
				
				//IJ.log("wid, hei, ox, oy, pix:  "+rtnval.width+","+rtnval.height+","+rtnval.origin_x+","+rtnval.origin_y+","+rtnval.pixel_size);
				
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
		
		int cur_slice=img.getT();
		My_Frame cur_frame=frame_list.get(cur_slice-1);
		
		
		My_ROI the_ROI=find_ROI_containing(cur_frame, x, y);
		
		if (the_ROI==null) return;
		
		Roi my_roi=new Roi(the_ROI.x_origin, the_ROI.y_origin, the_ROI.width_in_overview, the_ROI.height_in_overview);
		
		RoiManager manager=RoiManager.getInstance();
		
		if (manager==null)
		{
			manager=new RoiManager();
			
		}
		
		manager.addRoi(my_roi);
		manager.runCommand("Show All");
		manager.select(manager.getCount()-1);
		
		master_list.add(the_ROI);

		img.updateAndDraw();
		WindowManager.setCurrentWindow(img.getWindow());
		
	}
	
	My_ROI find_ROI_containing(My_Frame the_frame, int x, int y)
	{
		for (ListIterator<My_ROI> jF=the_frame.loc_list.listIterator(); jF.hasNext();)
		{
			My_ROI cur=jF.next();
			if (cur.x_origin<=x&&cur.x_origin+cur.width_in_overview>=x&&cur.y_origin<=y&&cur.y_origin+cur.height_in_overview>=y) return cur;
		}
		return null;
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
		if (rtn=='m')
		{
			GenericDialog gd=new GenericDialog("Zoom factor");
			gd.addNumericField("Zoom of highress:  ", 1, 0);
			gd.showDialog();
			int dezoom_scale=(int)Math.floor(gd.getNextNumber());
			
		    String concat_list="";
		    if (master_list.size()>0)
		    {
		    	int max_width=0;
		    	int max_height=0;
		    	for (int i=0; i<master_list.size(); i++)
				{
		    		My_ROI r=master_list.get(i);
		    		if (r.width_in_original/Math.pow(2, dezoom_scale-1)>max_width) max_width=(int)Math.ceil(r.width_in_original/Math.pow(2, dezoom_scale-1));
		    		if (r.height_in_original/Math.pow(2, dezoom_scale-1)>max_height) max_height=(int)Math.ceil(r.height_in_original/Math.pow(2, dezoom_scale-1));
		    	}
		    	
		    	for (int i=0; i<master_list.size(); i++)
				{
		    		My_ROI r=master_list.get(i);
					IJ.run("Bio-Formats Importer", "open="+r.fname+" color_mode=Default view=Hyperstack stack_order=XYCZT series_"+dezoom_scale);
					IJ.run("Canvas Size...", "width="+max_width+" height="+max_height+" position=Center");
					WindowManager.getCurrentImage().setTitle("img"+(i+1));
					concat_list=concat_list+"image"+(i+1)+"="+WindowManager.getCurrentImage().getTitle()+" ";
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
		if (rtn=='q')
		{
			canvas.removeMouseListener(this);
		    canvas.removeKeyListener(this);
		}
		//run("Canvas Size...", "width=7000 height=5000 position=Center");
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
