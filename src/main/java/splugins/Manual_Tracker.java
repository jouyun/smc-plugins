package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/******************************************************************
 * Manual_Tracker
 * @author smc
 *
 *	Need:  Run on a 2D timelapse with blobs in the channel you have selected when running.
 *	Also need:  A second image that is XYZ (although in theory using the same image for 2D tracking should work)
 *	Will do:  Will find a center of mass around the area you click, then look in the z space on that pixel to find
 *				the best Z, once found it:
 *		1.  Makes a results table entry with the calibrated position, pixel position, and intensities
 *		2.  Marks the spot in both images with a 0
 *
 *  Usage:  Clicking marks a position, shift clicking adds an all 0 entry (as a separator for moving on to a new cell),
 *  		control clicking deletes the last entry of the results table and reinstates the value for the two images
 */

public class Manual_Tracker implements PlugInFilter,MouseListener {
	ImagePlus imp, z_imp;
	int width;
	int height;
	int slices, frames, channels, cur_slice, cur_frame, cur_channel, z_slices;
	float lateral_res;
	float axial_res;
	ImageCanvas canvas;
	ImageWindow win;
	ResultsTable rslt;
	int auto_advance;
	int counter;
	class MyPoint
	{
		int x;
		int y;
		float sort_val;
		MyPoint()
		{
			x=0;
			y=0;
			sort_val=0;
		}
		MyPoint(int a, int b, float c)
		{
			x=a;
			y=b;
			sort_val=c;
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		
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
	public void mousePressed(MouseEvent e) {
		imp.setRoi((Roi)null);
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		Point current_point=canvas.getCursorLoc();
		if (e.isShiftDown()&&!e.isControlDown())
		{
			rslt.incrementCounter();
			rslt.addValue("X", 0);
			rslt.addValue("Y", 0);
			rslt.addValue("Z", 0);
			rslt.addValue("T", 0);
			rslt.show("Results");
			return;
		}
		if (e.isControlDown()&&e.isShiftDown())
		{
			canvas.removeMouseListener(this);
			return;
		}
		if (e.isControlDown()&&!e.isShiftDown())
		{
			int xx=(int)rslt.getValueAsDouble(6, rslt.size()-1);
			int yy=(int)rslt.getValueAsDouble(7, rslt.size()-1);
			int zz=(int)rslt.getValueAsDouble(8, rslt.size()-1);
			int ff=(int)rslt.getValueAsDouble(3, rslt.size()-1);
			int intI=(int)rslt.getValueAsDouble(4, rslt.size()-1);
			int intIP=(int)rslt.getValueAsDouble(5, rslt.size()-1);
			
			byte [] tmp=(byte []) z_imp.getStack().getProcessor(cur_channel+zz*channels+ff*z_slices*channels+1).getPixels();
			byte [] tmpP=(byte []) imp.getStack().getProcessor(cur_channel+ff*channels+1).getPixels();
			tmp[xx+yy*width]=(byte)intI;
			tmpP[xx+yy*width]=(byte)intIP;
			imp.updateAndDraw();
			z_imp.updateAndDraw();
			if (xx!=0) counter--;
			
			rslt.deleteRow(rslt.size()-1);
			rslt.show("Results");		
			rslt.updateResults();
			return;
		}
		int wdw=4;
		float fraction_to_keep=0.5f;
		//Add another entry
		if (e.getButton()==1)
		{
			if (e.isAltDown())
			{
				
			}
			//First, find the center of mass position in the neighborhood clicked
			float [] pix=(float []) imp.getStack().getProcessor(cur_channel+cur_slice*channels+cur_frame*slices*channels+1).convertToFloat().getPixels();
			List<MyPoint> my_collection=new ArrayList<MyPoint>();
			for (int x=-wdw; x<wdw+1; x++)
			{
				for (int y=-wdw; y<wdw+1; y++)
				{
					int xx=x+current_point.x;
					int yy=y+current_point.y;
					MyPoint temp_point=new MyPoint(xx,yy,pix[xx+yy*width]);
					my_collection.add(temp_point);
				}
			}
			Collections.sort(my_collection, new Comparator<MyPoint>()
			{
				public int compare(MyPoint A, MyPoint B)
				{
					return Float.compare(B.sort_val, A.sort_val);
				}
			});
			float cm_x_sum=0.0f, cm_y_sum=0.0f, cm_z_sum=0.0f, sort_total=0.0f, z_sort_total=0.0f;
			for (int i=0; i<Math.floor(my_collection.size()*fraction_to_keep); i++)
			{
				MyPoint tmp=my_collection.get(i);
				cm_x_sum=cm_x_sum+tmp.x*tmp.sort_val;
				cm_y_sum=cm_y_sum+tmp.y*tmp.sort_val;
				sort_total+=tmp.sort_val;
			}
			int cm_x=Math.round(cm_x_sum/sort_total);
			int cm_y=Math.round(cm_y_sum/sort_total);
			
			//Now find the z center of mass at that spot from the z reference image, HARDCODING FOR CHANNEL 1
			//If the base image is a projection, then find the best z
			if (slices==1)
			{
				IJ.log("Using projection");
				List<MyPoint> myz_collection=new ArrayList<MyPoint>();
				cur_channel=0;
				for (int s=0; s<z_slices; s++)
				{
					float [] tmp=(float []) z_imp.getStack().getProcessor(cur_channel+s*channels+cur_frame*z_slices*channels+1).convertToFloat().getPixels();
					myz_collection.add(new MyPoint(s,0,tmp[cm_x+cm_y*width]));
				}
				Collections.sort(myz_collection, new Comparator<MyPoint>()
						{
							public int compare(MyPoint A, MyPoint B)
							{
								return Float.compare(B.sort_val, A.sort_val);
							}
						});
				int z_slices_to_keep=3;
				for (int i=0; i<z_slices_to_keep; i++)
				{
					MyPoint tmp=myz_collection.get(i);
					cm_z_sum=cm_z_sum+tmp.x*tmp.sort_val;
					z_sort_total+=tmp.sort_val;
				}
			}
			else
			{
				IJ.log("Not using projection");
				List<MyPoint> myz_collection=new ArrayList<MyPoint>();
				cur_channel=0;
				int my_slice=imp.getSlice();
				for (int s=0; s<z_slices; s++)
				{
					float [] tmp=(float []) z_imp.getStack().getProcessor(cur_channel+s*channels+cur_frame*z_slices*channels+1).convertToFloat().getPixels();
					if (s+1==my_slice||s==my_slice||s-1==my_slice) myz_collection.add(new MyPoint(s,0,tmp[cm_x+cm_y*width]));
				}
				int z_slices_to_keep=3;
				for (int i=0; i<myz_collection.size(); i++)
				{
					MyPoint tmp=myz_collection.get(i);
					cm_z_sum=cm_z_sum+tmp.x*tmp.sort_val;
					z_sort_total+=tmp.sort_val;
				}
			}
				
			
			int cm_z=Math.round(cm_z_sum/z_sort_total);
			byte [] tmp=(byte []) z_imp.getStack().getProcessor(cur_channel+cm_z*channels+cur_frame*z_slices*channels+1).getPixels();
			byte [] tmpP=(byte []) imp.getStack().getProcessor(cur_channel+cur_frame*channels+1).getPixels();
			float intensity=0xff&tmp[cm_x+cm_y*width];
			float intensityP=0xff&tmpP[cm_x+cm_y*width];

			tmp[cm_x+cm_y*width]=0;
			tmpP[cm_x+cm_y*width]=0;
			z_imp.updateAndDraw();
			rslt.incrementCounter();
			rslt.addValue("X", cm_x_sum/sort_total*lateral_res);
			rslt.addValue("Y", cm_y_sum/sort_total*lateral_res);
			rslt.addValue("Z", cm_z_sum/z_sort_total*axial_res);
			rslt.addValue("T", cur_frame);
			rslt.addValue("I", intensity);
			rslt.addValue("IP", intensityP);
			rslt.addValue("XR", cm_x);
			rslt.addValue("YR", cm_y);
			rslt.addValue("ZR", cm_z);
			
			counter++;
			if (counter%auto_advance==0) imp.setPosition(cur_channel+1,cur_slice+1,cur_frame+2);
			rslt.show("Results");		
			imp.updateAndDraw();
			z_imp.updateAndDraw();
		}
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	private ImagePlus[] createAdmissibleImageList () 
	{
		final int[] windowList = WindowManager.getIDList();
		//IJ.log(""+windowList[0]+"\n");
		final Stack stack = new Stack();
		for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) 
		{
			final ImagePlus imp = WindowManager.getImage(windowList[k]);
			//IJ.log(imp.getTitle()+"\n");
			//if ((imp != null) && (imp.getType() == imp.GRAY32)) 
			{
				//IJ.log("got one");
				stack.push(imp);
			}
		}
		//IJ.log("Stack size:  " + stack.size() + "\n");
		final ImagePlus[] admissibleImageList = new ImagePlus[stack.size()];
		int k = 0;
		while (!stack.isEmpty()) {
			admissibleImageList[k++] = (ImagePlus)stack.pop();
		}
		if (k==0 && (windowList != null && windowList.length > 0 )){
			IJ.error("No float images, convert to float and try again");
		}
		return(admissibleImageList);
	} /* end createAdmissibleImageList */
	@Override
	public void run(ImageProcessor arg0) {
		imp=WindowManager.getCurrentImage();
        
        final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		if (admissibleImageList.length == 0) return;
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Do Dot Product");
		gd.addChoice("Z stack:", sourceNames, admissibleImageList[0].getTitle());
		gd.addNumericField("Clicks until auto-advance", 2, 0);
		gd.showDialog();
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		int tmpIndex=gd.getNextChoiceIndex();
		auto_advance=(int) gd.getNextNumber();
		z_imp=admissibleImageList[tmpIndex];

		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		z_slices=z_imp.getNSlices();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		lateral_res=(float) imp.getCalibration().pixelHeight;
		axial_res=(float) imp.getCalibration().pixelDepth;
		win = imp.getWindow();
        //win.addWindowListener(win);
        canvas = win.getCanvas();
        //win.removeKeyListener(IJ.getInstance());
        //canvas.removeMouseListener(IJ.getInstance());
        //win.removeMouseListener(IJ.getInstance());
        if (Toolbar.getToolId()>Toolbar.CROSSHAIR) IJ.setTool(Toolbar.RECTANGLE);
        canvas.addMouseListener(this);
        //win.addMouseListener(this);
        counter=0;
        
        
        rslt=ResultsTable.getResultsTable();
	
	}
	//This will sort collections of data, pivoting on the first column and moving the others with it
	public static void vector_sort(double [][] data, boolean ascending)
	{
		class VecPoint
		{
			double data[];
			VecPoint()
			{
			}
			VecPoint(double [] inp)
			{
				data=new double [inp.length];
				System.arraycopy(inp, 0, data, 0, inp.length);
			}
		}
		
		List<VecPoint> my_collection=new ArrayList<VecPoint>();
		for (int i=0; i<data.length; i++)
		{
			VecPoint temp_point=new VecPoint(data[i]);
			my_collection.add(temp_point);
		}
		if (ascending)
		{
			Collections.sort(my_collection, new Comparator<VecPoint>()
			{
				public int compare(VecPoint A, VecPoint B)
				{
					return Double.compare(B.data[0], A.data[0]);
				}
			});
		}
		else
		{
			Collections.sort(my_collection, new Comparator<VecPoint>()
			{
				public int compare(VecPoint A, VecPoint B)
				{
					return Double.compare(A.data[0], B.data[0]);
				}
			});
			
		}
		int i=0;
		for (ListIterator jF=my_collection.listIterator();jF.hasNext();)
		{
			VecPoint curpt=(VecPoint)jF.next();
			System.arraycopy(curpt.data, 0, data[i], 0, curpt.data.length);
			i++;
		}
	}
	@Override
	public int setup(String arg0, ImagePlus arg1) {
		this.imp=arg1;
		IJ.register(Manual_Tracker.class);
		return DOES_ALL+NO_CHANGES;
	}

}
