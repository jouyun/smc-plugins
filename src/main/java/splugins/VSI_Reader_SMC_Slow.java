package splugins;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class VSI_Reader_SMC_Slow implements KeyListener, MouseListener, PlugIn {

	ImageCanvas canvas;
	ImageWindow win;
	ImagePlus img;
	int [][]ROI_list=new int[100][7];
	int n_rois=0;
	String start_dir;
	ArrayList <Integer> master_list=new ArrayList <Integer>();
	@Override
	public void run(String arg0) {
		
		String base_file=IJ.getFilePath("Choose file");
		start_dir=base_file.substring(0, base_file.lastIndexOf(File.separator)+1);
		IJ.run("Bio-Formats Importer", "open="+base_file+" color_mode=Default view=Hyperstack stack_order=XYCZT series_1");
		img=WindowManager.getCurrentImage();
		String imgInfo=img.getInfoProperty();
		double overview_pw=img.getCalibration().pixelWidth;
		
		/***************Added to make less painful to select ROIs, using scaled down version instead**************/
		img.close();
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
		
		
		String matcher="Origin #1 = ";
		int s=imgInfo.indexOf(matcher);
		int t=imgInfo.indexOf(",",s);
		int u=imgInfo.indexOf(")", t);
		double top_left_overview_x=Double.parseDouble(imgInfo.substring(s+matcher.length()+1, t));
		double top_left_overview_y=Double.parseDouble(imgInfo.substring(t+1, u));
		
		
		
		
		int overview_width=img.getWidth();
		int overview_height=img.getHeight();
		
		IJ.log("Corner: "+top_left_overview_x+","+top_left_overview_y);
		
		double old_y=-100000000;
		int ctr=0;
		for (int i=1; i<10000; i++)
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
			ROI_list[n_rois][5]=wid;
			ROI_list[n_rois][6]=hei;
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
					pix[x+y*overview_width]=0;
				}
			}
		}
		img.show();
		img.updateAndDraw();
		
		

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
		
		Roi my_roi=new Roi(ROI_list[rtn_idx][1],ROI_list[rtn_idx][2],ROI_list[rtn_idx][3],ROI_list[rtn_idx][4]);
		
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
			if (ROI_list[i][1]<=x&&ROI_list[i][1]+ROI_list[i][3]>=x&&ROI_list[i][2]<=y&&ROI_list[i][2]+ROI_list[i][4]>=y) return i;
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
		    
		    if (master_list.size()>0)
		    {
		    	int max_width=0;
		    	int max_height=0;
		    	for (int i=0; i<master_list.size(); i++)
				{
					if (ROI_list[master_list.get(i)][5]>max_width) max_width=ROI_list[master_list.get(i)][5];
					if (ROI_list[master_list.get(i)][6]>max_height) max_height=ROI_list[master_list.get(i)][6];
		    		
		    	}
		    	for (int i=0; i<master_list.size(); i++)
				{
					String fname="Image_"+IJ.pad(ROI_list[master_list.get(i)][0],2)+".vsi";
					IJ.run("Bio-Formats Importer", "open="+start_dir+fname+" color_mode=Default view=Hyperstack stack_order=XYCZT series_1");
					IJ.run("Canvas Size...", "width="+max_width+" height="+max_height+" position=Center");
		    	}
		    }
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
