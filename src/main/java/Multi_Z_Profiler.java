import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import ij.util.Tools;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import ij.measure.*;
import java.awt.Rectangle;

    /*
      This plugin continuously generates Z-Axis profile plots as a selection 
      is moved or resized through the XY Coordinate Plane.
      The y-axis scale can be fixed in the Edit>Options>Profile Plot Options dialog.
      Works best on a fast machine.
    */

 public class Multi_Z_Profiler implements PlugInFilter, MouseListener, MouseMotionListener, Measurements, KeyListener {
    ImagePlus img;
    ImageCanvas canvas;
    ImageStatistics stats;
    PlotWindow pwin;
    public double[] y;
    public double[] x;
	public double[][] y_list;
	int num_spectra;
    String xLabel;
    String yLabel;
    boolean listenersRemoved;
	int size;
	

    public int setup(String arg, ImagePlus img) {
         if (IJ.versionLessThan("1.31i"))
            return DONE;
            this.img = img;
        if (!isSelection()) {
            IJ.showMessage("Dynamic Z-Axis Profiler", "Image selection required.");
            return DONE;
        } else
            return DOES_ALL+NO_CHANGES;
  }

    public void run(ImageProcessor ip) {
        Integer id = new Integer(img.getID());
        if (img.getStackSize()<2) {
            IJ.showMessage("Dynamic Z-Axis Profiler", "This command requires a stack.");
            return;
        }
        ImageWindow win = img.getWindow();
        win.addWindowListener(win);
        canvas = win.getCanvas();
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
        Roi roi = img.getRoi();  
        y = getZAxisProfile();
        if (y!=null) {
            x = new double[y.length];
            Calibration cal = img.getCalibration();
            for (int i=0; i<x.length; i++)
                x[i] = i*cal.pixelDepth;
            xLabel = cal.getUnits();
            yLabel = cal.getValueUnit();
            updateProfile(x, y);
            positionPlotWindow();
        }
		num_spectra=0;
    }
  
	double[] getZAxisProfile() {
        Roi roi = img.getRoi();
        if(roi==null)
             return null;
        ImageStack stack = img.getStack();
        size = stack.getSize(); 
        double[] values = new double[size];
        Rectangle r = roi.getBoundingRect();
        Calibration cal = img.getCalibration();
        //ROI with Area > 0
        for (int i=1; i<=size; i++) {
            ImageProcessor ip = stack.getProcessor(i);
            ip.setRoi(roi);
            ImageStatistics stats = ImageStatistics.getStatistics(ip, MEAN, cal);
            values[i-1] = (double)stats.mean;
        }
        double[] extrema = Tools.getMinMax(values);
        if (Math.abs(extrema[1])==Double.MAX_VALUE)
            return null;
        else
            return values;
    }
    
   void positionPlotWindow() {
        IJ.wait(500);
        if (pwin==null || img==null) return;
           ImageWindow iwin = img.getWindow();
        if (iwin==null) return;
           Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
           Dimension plotSize = pwin.getSize();
           Dimension imageSize = iwin.getSize();
        if (plotSize.width==0 || imageSize.width==0) return;
           Point imageLoc = iwin.getLocation();
        int w = imageLoc.x+imageSize.width+10;
        if (w+plotSize.width>screen.width)
           w = screen.width-plotSize.width;
        pwin.setLocation(w, imageLoc.y);
        iwin.toFront();
   }

    public void mousePressed(MouseEvent e) {
//Gets the Z values through a single point at (x,y).            
             Roi roi = img.getRoi();
             ImageStack stack = img.getStack();
             int size = stack.getSize(); 
             double[] values = new double[size];
             Rectangle r = roi.getBoundingRect();
             if((r.width==0 || r.height==0) || (r.width==1 && r.height==1)){
                int xpoint = e.getX();
                int ypoint = e.getY();
                float[] cTable = img.getCalibration().getCTable();
                for (int p=1; p<=size; p++){
                   ImageProcessor ip = stack.getProcessor(p);
                   ip.setCalibrationTable(cTable);
                   values[p-1] = ip.getPixelValue(xpoint, ypoint);
                }
            y = values;
            updateProfile(x, y);
            }
    }
   
	public void keyPressed(KeyEvent e) {
		char rtn;
		rtn=e.getKeyChar();
		if (rtn=='c') num_spectra=0;
		if (rtn!='g') return;
		ImageStack stack = img.getStack();
		int size=stack.getSize();
		double[][] tmp;
		if (num_spectra>0)
		{
			tmp=new double[num_spectra][size];
			for (int i=0; i<num_spectra; i++)
			{
				for (int j=0; j<size; j++)
				{
					tmp[i][j]=y_list[i][j];
				}
			}
		}
		else
		{
			tmp=new double[1][size];
		}
		
		y_list=new double[num_spectra+1][size];
		
		for (int i=0; i<num_spectra; i++)
		{
			for (int j=0; j<size; j++)
			{
				y_list[i][j]=tmp[i][j];
			}
		}
		num_spectra=num_spectra+1;
		for (int j=0; j<size; j++)
		{
			y_list[num_spectra-1][j]=y[j];
		}
		//updateProfile(x, y);
	}
	
    public void mouseDragged(MouseEvent e) {
             y = getZAxisProfile();
             updateProfile(x, y);
    }
    
    public void keyReleased(KeyEvent e) {
             y = getZAxisProfile();
               updateProfile(x, y);
    }
  
    void updateProfile(double[] x, double[] y) {
		if (!isSelection())
			return;
		checkPlotWindow();
		if (listenersRemoved || y==null || y.length==0)
			return;
		ImageStack stack = img.getStack();
		int n = stack.getSize();
		normalize(y);
		Plot plot = new Plot("profile", xLabel, yLabel, x, y);
		
		double ymin = ProfilePlot.getFixedMin();
		double ymax= ProfilePlot.getFixedMax();
		if (!(ymin==0.0 && ymax==0.0)) {
			double[] a = Tools.getMinMax(x);
			double xmin=a[0]; double xmax=a[1];
			plot.setLimits(xmin, xmax, ymin, ymax);
		}
		
		if (pwin==null)
			pwin = plot.show();
		else
		{
			if (num_spectra==0)	
			{
				
				/*double[] expand;
				expand=new double[n];
				for (int i=0; i<n; i++) expand[i]=1000000000000000.0*x[i];
				//Plot plots = new Plot("profile", xLabel, yLabel, x, x);
				plot.addPoints(x, expand, Plot.LINE);*/
				pwin.drawPlot(plot);
			}
			else
			{
				for (int i=0; i<num_spectra; i++)
				{
					normalize(y_list[i]);
					
					switch(i) {
						case 0: plot.setColor(Color.BLUE);
								break;
						case 1: plot.setColor(Color.GREEN);
								break;
						case 2: plot.setColor(Color.MAGENTA);
								break;
						case 3: plot.setColor(Color.ORANGE);
								break;
						case 4: plot.setColor(Color.PINK);
								break;
						case 5: plot.setColor(Color.YELLOW);
								break;
						default: plot.setColor(Color.BLACK);
								break;
					}
					plot.addPoints(x, y_list[i], Plot.LINE);
				}
				
				plot.setColor(Color.BLACK);
				pwin.drawPlot(plot);
				
			}
		}
	}

    // returns true if there is a line or area selection
    boolean isSelection() {
        if (img==null)
            return false;
        Roi roi = img.getRoi();
        if (roi==null)
            return false;
        int roiType = roi.getType();
        if (roiType<=Roi.FREELINE)
            return true;
       else
            return false;
    }

    // stop listening for mouse and key events if the plot window has been closed
    void checkPlotWindow() {
       if (pwin==null)
           return;
       if (pwin.isVisible()) 
           return;
       ImageWindow iwin = img.getWindow();
       if (iwin==null)
            return;
       canvas = iwin.getCanvas();
       canvas.removeMouseListener(this);
       canvas.removeMouseMotionListener(this);
       canvas.removeKeyListener(this);
       pwin = null;
       listenersRemoved = true;
    }

	void normalize(double input[])
	{
		double max;
		max=-10000.0;
		for (int i=0; i<size; i++) if (input[i]>max) max=input[i];
		for (int i=0; i<size; i++) input[i]=input[i]/max;
	}
	
    //public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}   
    public void mouseEntered(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

}
