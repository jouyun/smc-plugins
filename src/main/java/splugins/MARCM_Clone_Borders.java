package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Line;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class MARCM_Clone_Borders implements MouseListener, PlugIn {

	ImageCanvas canvas;
	ImageWindow win;
	ImagePlus imp;
	ArrayList <Integer> current_list;
	int columns=10;
	@Override
	public void run(String arg0) {
		imp=WindowManager.getCurrentImage();
		win = imp.getWindow();
        canvas = win.getCanvas();
        //win.removeKeyListener(IJ.getInstance());
        //canvas.removeMouseListener(IJ.getInstance());
        //win.removeMouseListener(IJ.getInstance());
        if (Toolbar.getToolId()>Toolbar.CROSSHAIR) IJ.setTool(Toolbar.RECTANGLE);
        canvas.addMouseListener(this);
        current_list=new ArrayList <Integer>();
        ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		rslt.incrementCounter();
		rslt.addValue("Area", 10);
		for (int i=0; i<columns; i++)
		{
			rslt.addValue("NN_"+(i+1), 0);
		}
		for (int i=0; i<columns; i++)
		{
			rslt.addValue("CN_"+(i+1), 0);
		}
		for (int i=0; i<columns; i++)
		{
			rslt.addValue("CC_"+(i+1), 0);
		}

	}

	@Override
	public void mouseClicked(MouseEvent event) {
		/*Point current_point=canvas.getCursorLoc();
		current_list[current_list.length-1][0]=current_point.x;
		current_list[current_list.length-1][1]=current_point.y;*/
		
		int current_point=0;
		if (event.isShiftDown())
		{
			current_point=1;
		}
		if(event.isMetaDown()||event.isControlDown())
		{
			current_point=2;
		}
		Roi roi = imp.getRoi();
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		Calibration cal=imp.getCalibration();
		if(roi!=null)
		{
			current_list.add(current_point);	
			Polygon p=roi.getPolygon();
			double area=0.0;
			String tmp=new String("");
			for (int i=0; i<p.npoints; i++)
			{
				//IJ.log(""+p.xpoints[i]+","+p.ypoints[i]+","+current_list.get(i));
				tmp=tmp+current_list.get(i)+",";
			}
			//IJ.log(tmp);
			for (int x=0; x<imp.getWidth(); x++)
			{
				for (int y=0; y<imp.getHeight(); y++)
				{
					Point P=new Point(x,y);
					if (p.contains(P)) area++;
				}
			}
			
			if (event.getClickCount() == 2) {
				IJ.log("Double!");
				rslt.incrementCounter();
				
				for (int x=0; x<imp.getWidth(); x++)
				{
					for (int y=0; y<imp.getHeight(); y++)
					{
						Point P=new Point(x,y);
						if (p.contains(P)) area++;
					}
				}
				rslt.addValue("Area",area*cal.pixelHeight*cal.pixelWidth);
				String mytmp="";
				for (int i=0; i<p.npoints; i++)
				{
					double dx=(p.xpoints[(i+1)%p.npoints]-p.xpoints[i])*cal.pixelWidth;
					double dy=(p.ypoints[(i+1)%p.npoints]-p.ypoints[i])*cal.pixelHeight;
					if (current_list.get(i)==0)
					{
						rslt.addValue("NN_"+(i+1), Math.sqrt((dx*dx+dy*dy)));
						rslt.addValue("CN_"+(i+1), 0);
						rslt.addValue("CC_"+(i+1), 0);
						//rslt.show("Results");
						mytmp=mytmp+"NN,";
					}
					if (current_list.get(i)==1)
					{
						rslt.addValue("CN_"+(i+1), Math.sqrt((dx*dx+dy*dy)));
						rslt.addValue("NN_"+(i+1), 0);
						rslt.addValue("CC_"+(i+1), 0);
						//rslt.show("Results");
						mytmp=mytmp+"CN,";
					}
					if (current_list.get(i)==2)
					{
						rslt.addValue("CC_"+(i+1), Math.sqrt((dx*dx+dy*dy)));
						rslt.addValue("NN_"+(i+1), 0);
						rslt.addValue("CN_"+(i+1), 0);
						//rslt.show("Results");
						mytmp=mytmp+"CC,";
					}
					
				}
				for (int i=p.npoints; i<columns; i++)
				{
					rslt.addValue("NN_"+(i+1), 0);
					rslt.addValue("CN_"+(i+1), 0);
					rslt.addValue("CC_"+(i+1), 0);
				}
				rslt.show("Results");
				IJ.log(mytmp);
				current_list=new ArrayList<Integer>();
			}

		
		}
		
		
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
		if (e.isControlDown()&&e.isShiftDown())
		{
			canvas.removeMouseListener(this);
			return;
		}

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
