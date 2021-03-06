package splugins;

import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Label_ROIs_3D implements KeyListener, MouseListener, PlugIn {

	ImageCanvas canvas;
	ImageWindow win;
	ImagePlus imp;
	int width, height, channels, slices, frames, cur_channel, cur_frame, cur_slice;
	int draw_radius, draw_inner_radius;
	float [][][][] backup_data;
	int draw_channel;
	float val;
	boolean roi_listening;
	@Override
	public void run(String arg0) {
		roi_listening=false;
		imp=WindowManager.getCurrentImage();
		win = imp.getWindow();
        canvas = win.getCanvas();
        IJ.run("32-bit");
        imp.setDisplayMode(IJ.COMPOSITE);
        
        win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        
        win.addWindowListener(win);
        //canvas.addMouseListener(this);
        canvas.addKeyListener(this);
        
        width=imp.getWidth();
        height=imp.getHeight();
        channels=imp.getNChannels();
        slices=imp.getNSlices();
        frames=imp.getNFrames();
        
        cur_channel=imp.getChannel()-1;
        cur_frame=imp.getFrame()-1;
        cur_slice=imp.getSlice()-1;
        
        draw_radius=20;
        draw_inner_radius=8;
        draw_channel=1;
        
        backup_data=new float[frames][slices][channels][width*height];
        for (int f=0; f<frames; f++)
        {
        	for (int s=0; s<slices; s++)
        	{
        		for (int c=0; c<channels; c++)
        		{
        			float [] pix = (float [])imp.getStack().getProcessor(1+c+s*channels+f*channels*slices).getPixels();
        			for (int i=0; i<pix.length; i++) backup_data[f][s][c][i]=pix[i];
        		}
        	}
        }
        
      //IJ.log("I'm in D");
        val=100000;
		
        RoiManager manager=RoiManager.getInstance();
        for (int r=0; r<manager.getCount(); r++)
        {
    		String cur_name=manager.getName(r);
    		Roi roi=manager.getRoi(r);
    		Polygon P=roi.getPolygon();
    		//IJ.log(cur_name);
    		int fidx=cur_name.indexOf("-");
    		int sidx=cur_name.indexOf("-",fidx+1);
    		int raw_slice=Integer.parseInt(cur_name.substring(0, fidx));
    		int y=Integer.parseInt(cur_name.substring(fidx+1, sidx));
    		int x=Integer.parseInt(cur_name.substring(sidx+1, cur_name.length()));
    		x=P.xpoints[0];
    		y=P.ypoints[0];
    		
    		raw_slice--;
    		int t_channel=raw_slice%channels;
    		int t_slice=raw_slice/channels%slices;
    		t_slice=raw_slice;
    		t_channel=draw_channel;
    		ReplaceSpot(x,y,t_channel,t_slice,val);
    		imp.updateAndDraw();
    		IJ.log(("X,Y,Z,C: "+x+","+y+","+t_channel+","+t_slice));
        }
        RoiManager.getInstance().select(1);
		WindowManager.setCurrentWindow(imp.getWindow());
		RoiManager.getInstance().runCommand("Deselect");
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x, y;
		x=e.getX();
		y=e.getY();
		x=canvas.offScreenX(x);
		y=canvas.offScreenY(y);
		//IJ.log("X,Y: "+x+","+y);
		Roi my_roi=new Roi(x,y,1,1);
		
		RoiManager manager=RoiManager.getInstance();
		
		if (manager==null) manager=new RoiManager();
		//MyRoiManager manager=new MyRoiManager();
		
		if (!roi_listening) 
		{
			roi_listening=true;
		}
		
		
		float val=100000;
		
		int current_slice=imp.getSlice()-1;
		int current_channel=imp.getChannel()-1;
		
		ReplaceSpot(x,y,current_channel, current_slice, val);
		
		manager.addRoi(my_roi);

		imp.updateAndDraw();
	}

	public void ReplaceSpot(int x, int y, int current_channel, int current_slice, float val)
	{
		
		for (int f=0; f<frames; f++)
        {
			//Do a point for all the points under 1 slice below current slice
			//for (int s=0; s<current_slice-1; s++)
			for (int s=current_slice-5; s<current_slice-1; s++)
			{
				if (s<0) continue;
        		//for (int c=0; c<channels; c++)
				for (int c=current_channel; c<current_channel+1; c++)
        		{
        			float [] pix = (float [])imp.getStack().getProcessor(1+c+s*channels+f*channels*slices).getPixels();
        			if (val==0)
        			{
        				pix[x+y*width]=backup_data[f][s][c][x+y*width];
        			}
        			else
        			{
        				pix[x+y*width]=val;
        			}
        		}
			}
			//Do a smaller circle for the slice just below current
			//for (int c=0; c<channels; c++)
			for (int c=current_channel; c<current_channel+1; c++)
    		{
				if (current_slice-1<0) break;
    			float [] pix = (float [])imp.getStack().getProcessor(1+c+(current_slice-1)*channels+f*channels*slices).getPixels();
    			for (int xx=0; xx<width; xx++)
    			{
    				for (int yy=0; yy<height; yy++)
    				{
    					float dx=xx-x, dy=yy-y;
    					float dr=dx*dx+dy*dy;
    					
    					if (draw_inner_radius/2<dr&&dr<draw_radius/2)
    					{
    	        			if (val==0)
    	        			{
    	        				pix[xx+yy*width]=backup_data[f][current_slice-1][c][xx+yy*width];
    	        			}
    	        			else
    	        			{
    	        				pix[xx+yy*width]=val;
    	        			}

    					}
    				}
    			}
    		}
			//Do a full circle for the current slice
			//for (int c=0; c<channels; c++)
			for (int c=current_channel; c<current_channel+1; c++)
    		{
    			float [] pix = (float [])imp.getStack().getProcessor(1+c+(current_slice)*channels+f*channels*slices).getPixels();
    			for (int xx=0; xx<width; xx++)
    			{
    				for (int yy=0; yy<height; yy++)
    				{
    					float dx=xx-x, dy=yy-y;
    					float dr=dx*dx+dy*dy;
    					
    					if (draw_inner_radius<dr&&dr<draw_radius)
    					{
    						if (val==0)
    	        			{
    	        				pix[xx+yy*width]=backup_data[f][current_slice][c][xx+yy*width];
    	        			}
    	        			else
    	        			{
    	        				pix[xx+yy*width]=val;
    	        			}
    					}
    				}
    			}
    		}
			//Do a smaller circle for the slice just above current
			//for (int c=0; c<channels; c++)
			for (int c=current_channel; c<current_channel+1; c++)
    		{
				if (current_slice+1>=slices) break;
    			float [] pix = (float [])imp.getStack().getProcessor(1+c+(current_slice+1)*channels+f*channels*slices).getPixels();
    			for (int xx=0; xx<width; xx++)
    			{
    				for (int yy=0; yy<height; yy++)
    				{
    					float dx=xx-x, dy=yy-y;
    					float dr=dx*dx+dy*dy;
    					
    					if (draw_inner_radius/2<dr&&dr<draw_radius/2)
    					{
    						if (val==0)
    	        			{
    	        				pix[xx+yy*width]=backup_data[f][current_slice+1][c][xx+yy*width];
    	        			}
    	        			else
    	        			{
    	        				pix[xx+yy*width]=val;
    	        			}
    					}
    				}
    			}
    		}
			//Do a point for all the points above 1 slice above current slice
			//for (int s=current_slice+2; s<slices; s++)
			for (int s=current_slice+2; s<current_slice+5; s++)
			{
				if (s>=slices) continue;
        		//for (int c=0; c<channels; c++)
				for (int c=current_channel; c<current_channel+1; c++)
        		{
        			float [] pix = (float [])imp.getStack().getProcessor(1+c+s*channels+f*channels*slices).getPixels();
        			if (val==0)
        			{
        				pix[x+y*width]=backup_data[f][s][c][x+y*width];
        			}
        			else
        			{
        				pix[x+y*width]=val;
        			}
        		}
			}
        }
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		char rtn;
		rtn=e.getKeyChar();
		IJ.log("Key pressed: "+rtn);
		//if (rtn!='j'&&rtn!='q') return;
		if (rtn=='q')
		{
			canvas.removeMouseListener(this);
		    canvas.removeKeyListener(this);
		}
		if (rtn=='j')
		{
			Roi current_roi=imp.getRoi();
			Polygon poly=current_roi.getPolygon();
			//IJ.log("x,y: "+poly.xpoints[0]+ ","+poly.ypoints[0]);
		}
		
		if (rtn=='l')
		{
			RoiManager.getInstance().select(RoiManager.getInstance().getSelectedIndex()+1);
			WindowManager.setCurrentWindow(imp.getWindow());
		}
		if (rtn=='o')
		{
			if (RoiManager.getInstance().getSelectedIndex()==0) return;
			RoiManager.getInstance().select(RoiManager.getInstance().getSelectedIndex()-1);
			WindowManager.setCurrentWindow(imp.getWindow());
		}
		if (rtn=='v')
		{
			IJ.run("In [+]");
		}
		if (rtn=='z')
		{
			IJ.run("Out [-]");
		}
		if (rtn=='x')
		{
			imp.setZ(imp.getZ()-1);
		}
		if (rtn=='c')
		{
			imp.setZ(imp.getZ()+1);
		}
		if (rtn=='d')
		{
			//IJ.log("I'm in D");
			RoiManager manager=RoiManager.getInstance();
			Roi roi=manager.getRoi(manager.getSelectedIndex());
			
    		String cur_name=manager.getName(manager.getSelectedIndex());
    		Polygon P=roi.getPolygon();
    		int fidx=cur_name.indexOf("-");
    		int sidx=cur_name.indexOf("-",fidx+1);
    		int raw_slice=Integer.parseInt(cur_name.substring(0, fidx));
    		int y;
    		int x;
    		x=P.xpoints[0];
    		y=P.ypoints[0];
	    		
	    	raw_slice--;
	    	int t_channel;
	    	int t_slice;
	    	t_slice=raw_slice;
	    	t_channel=draw_channel;
	    	ReplaceSpot(x,y,0,t_slice,0);
	    	ReplaceSpot(x,y,1,t_slice,0);
	    	ReplaceSpot(x,y,2,t_slice,0);
	    	imp.updateAndDraw();
	    	IJ.log(("X,Y,Z,C: "+x+","+y+","+t_channel+","+t_slice));
			
			int current_roi=RoiManager.getInstance().getSelectedIndex();
			RoiManager.getInstance().runCommand("Delete");
			RoiManager.getInstance().select(current_roi);
		}
		
		if (rtn=='m')
		{
			//IJ.log("I'm in D");
			RoiManager manager=RoiManager.getInstance();
			Roi roi=manager.getRoi(manager.getSelectedIndex());
			
    		String cur_name=manager.getName(manager.getSelectedIndex());
    		Polygon P=roi.getPolygon();
    		int fidx=cur_name.indexOf("-");
    		int sidx=cur_name.indexOf("-",fidx+1);
    		int raw_slice=Integer.parseInt(cur_name.substring(0, fidx));
    		int y;
    		int x;
    		x=P.xpoints[0];
    		y=P.ypoints[0];
	    		
	    	raw_slice--;
	    	int t_channel;
	    	int t_slice;
	    	t_slice=raw_slice;
	    	t_channel=draw_channel;
	    	ReplaceSpot(x,y,0,t_slice,0);
	    	ReplaceSpot(x,y,1,t_slice,0);
	    	ReplaceSpot(x,y,2,t_slice,0);
			imp.updateAndDraw();
	    	IJ.log(("X,Y,Z,C: "+x+","+y+","+t_channel+","+t_slice));
			
			ReplaceSpot(x,y,0,t_slice,val);
			imp.updateAndDraw();
			RoiManager.getInstance().select(RoiManager.getInstance().getSelectedIndex()+1);
			WindowManager.setCurrentWindow(imp.getWindow());
			
		}
		
		if (rtn=='r')
		{
			//IJ.log("R");
			IJ.run("Clear Results");
			ResultsTable rslt;
			rslt=ResultsTable.getResultsTable();
			for (int i=0; i<RoiManager.getInstance().getCount(); i++)
			{
				rslt.incrementCounter();
				String cur_name=RoiManager.getInstance().getName(i);
				int fidx=cur_name.indexOf("-");
				int sidx=cur_name.indexOf("-",fidx+1);
				int raw_slice=Integer.parseInt(cur_name.substring(0, fidx));
				int y=Integer.parseInt(cur_name.substring(fidx+1, sidx));
				int x=Integer.parseInt(cur_name.substring(sidx+1, cur_name.length()));
				raw_slice--;
				int t_channel=raw_slice%channels;
				int t_slice=raw_slice/channels%slices;
				rslt.addValue("X", x);
				rslt.addValue("Y", y);
				rslt.addValue("Slice", t_slice);
				rslt.addValue("Channel", t_channel);
				rslt.show("Results");
			}
			
		}

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
