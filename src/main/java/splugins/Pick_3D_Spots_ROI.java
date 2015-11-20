package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

import java.awt.Polygon;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Pick_3D_Spots_ROI implements KeyListener, MouseListener, PlugIn {

	ImageCanvas canvas;
	ImageWindow win;
	ImagePlus imp;
	int width, height, channels, slices, frames, cur_channel, cur_frame, cur_slice;
	int draw_radius, draw_inner_radius;
	float [][][][] backup_data;
	boolean roi_listening;
	@Override
	public void run(String arg0) {
		roi_listening=false;
		imp=WindowManager.getCurrentImage();
		win = imp.getWindow();
        canvas = win.getCanvas();
        
        win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        
        win.addWindowListener(win);
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
        
        width=imp.getWidth();
        height=imp.getHeight();
        channels=imp.getNChannels();
        slices=imp.getNSlices();
        frames=imp.getNFrames();
        
        cur_channel=imp.getChannel()-1;
        cur_frame=imp.getFrame()-1;
        cur_slice=imp.getSlice()-1;
        
        draw_radius=100;
        draw_inner_radius=64;
        
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
			/*IJ.log("Number windows: "+manager.getWindows().length);
			Window mywin=manager.getWindows()[0];
			
			mywin.removeKeyListener(IJ.getInstance());
			mywin.addKeyListener(this);*/
			
			//manager.setKeyListener(this);
			
			/*
			KeyListener[] listers=manager.getKeyListeners();
			IJ.log("Listeners: "+listers.length);
			for (int i=0; i<listers.length; i++) manager.removeKeyListener(listers[i]);
			
			listers=manager.getKeyListeners();
			IJ.log("Listeners: "+listers.length);
			
			Window [] wins=manager.getWindows();
			for (int i=0; i<wins.length; i++)
			{
				IJ.log(wins[i].getName());
				if (wins[i].getKeyListeners().length>0)
				{
					wins[i].removeKeyListener(wins[i].getKeyListeners()[0]);
					wins[i].addKeyListener(this);
				}
				
			}
			
			for (int i=0; i<manager.getComponentCount(); i++)
			{
				for (int j=0; j<manager.getComponent(i).getKeyListeners().length; j++)
				{
					manager.getComponent(i).removeKeyListener(manager.getComponent(i).getKeyListeners()[j]);
				}
				manager.getComponent(i).addKeyListener(this);
			}
			manager.addKeyListener(this);*/
			roi_listening=true;
		}
		
		
		float val=10000;
		
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
			for (int s=0; s<current_slice-1; s++)
			{
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
			for (int s=current_slice+2; s<slices; s++)
			{
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
			String cur_name=RoiManager.getInstance().getName(RoiManager.getInstance().getSelectedIndex());
			//IJ.log(cur_name);
			int fidx=cur_name.indexOf("-");
			int sidx=cur_name.indexOf("-",fidx+1);
			/*IJ.log("fidx, sidx: "+fidx+","+sidx);
			IJ.log(cur_name.substring(0, fidx));
			IJ.log(cur_name.substring(fidx+1, sidx));
			IJ.log(cur_name.substring(sidx+1,cur_name.length()));*/
			int raw_slice=Integer.parseInt(cur_name.substring(0, fidx));
			int y=Integer.parseInt(cur_name.substring(fidx+1, sidx));
			int x=Integer.parseInt(cur_name.substring(sidx+1, cur_name.length()));
			
			//IJ.log("X,Y,Slice: "+x+","+y+","+raw_slice);
			raw_slice--;
			int t_channel=raw_slice%channels;
			int t_slice=raw_slice/channels%slices;
			ReplaceSpot(x,y,t_channel,t_slice,0);
			imp.updateAndDraw();
			
			int current_roi=RoiManager.getInstance().getSelectedIndex();
			RoiManager.getInstance().runCommand("Delete");
			RoiManager.getInstance().select(current_roi);
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
