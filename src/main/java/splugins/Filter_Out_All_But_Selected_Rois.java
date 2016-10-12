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
import ij.gui.NewImage;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Filter_Out_All_But_Selected_Rois implements  MouseListener, KeyListener, PlugIn {

	ImageCanvas canvas;
	ImageWindow win;
	ImagePlus imp;
	int width, height;
	Wand wand;

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth();
		height=imp.getHeight();

		
		
		
		imp=WindowManager.getCurrentImage();
		win = imp.getWindow();
        canvas = win.getCanvas();
        imp.setDisplayMode(IJ.COMPOSITE);
        
        win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        
        win.addWindowListener(win);
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
		
		
		
		

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
		
		wand=new Wand(imp.getProcessor());
		wand.autoOutline(x, y);
		Roi roi=null;
		if (wand.npoints>0)
		{
			roi = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.TRACED_ROI);
			
		}
		
		Roi my_roi=new Roi(x,y,1,1);
		int z=imp.getCurrentSlice();
		
		RoiManager manager=RoiManager.getInstance();
		
		if (manager==null) manager=new RoiManager();
		//MyRoiManager manager=new MyRoiManager();
		
		//manager.addRoi(my_roi);
		manager.addRoi(roi);
		imp.setRoi(roi);

		imp.updateAndDraw();

		
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
		IJ.log("Key pressed: "+rtn);
		//if (rtn!='j'&&rtn!='q') return;
		if (rtn=='q')
		{
			canvas.removeMouseListener(this);
		    canvas.removeKeyListener(this);
		}
		if (rtn=='s')
		{
			RoiManager roiM=RoiManager.getInstance();
			for (int i=0; i<roiM.getCount(); i++)
			{
				Roi cur_roi=roiM.getRoi(i);
				Polygon P=cur_roi.getPolygon();
				int z=roiM.getSliceNumber(roiM.getName(i));
				int x=P.xpoints[0];
				int y=P.ypoints[0];
				IJ.log("X, Y, Z: "+x+","+y+","+z);
			}
		}
		if (rtn=='d')
		{
			//ResultsTable rslt=RoiManager.getInstance().multiMeasure(imp);
			/*ImagePlus new_img=NewImage.createByteImage("Img", width, height, imp.getNSlices(), NewImage.FILL_BLACK);
			String [] tmp=rslt.getHeadings();
			for (int i=0; i<tmp.length; i++)
			{
				IJ.log(tmp[i]);
			}
			for (int r=0; r<rslt.getCounter(); r++)
			{
				int x, y, z;
				x=Integer.parseInt(rslt.getStringValue("X1", r));
				y=Integer.parseInt(rslt.getStringValue("Y1", r));
				z=Integer.parseInt(rslt.getStringValue("Z", r));
				
				short [] cur_pix=(short [])imp.getStack().getProcessor(z).getPixels();
				byte [] new_pix=(byte [])new_img.getStack().getProcessor(z).getPixels();
				short good_val=cur_pix[x+y*width];
				for (int xy=0; xy<width*height; xy++)
				{
					if (cur_pix[xy]!=good_val) new_pix[xy]=(byte)255;
				}
			}
			new_img.show();
			new_img.updateAndDraw();*/
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
