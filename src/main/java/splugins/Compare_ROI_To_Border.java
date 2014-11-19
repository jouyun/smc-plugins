package splugins;

import java.awt.Point;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.ListIterator;

import splugins.seeded_region_grow_3D.MyIntPoint;

public class Compare_ROI_To_Border implements PlugIn {

	int width;
	int height;
	int channels;
	int slices;
	int frames;
	int current_slice;
	int current_frame;
	ImagePlus img;
	byte [] binary;
	byte [] original_binary;
	class MyIntPoint
	{
		int x;
		int y;
		
	}
	ArrayList <Point> point_list=new ArrayList <Point>();
	ArrayList <Point> tmp_point_list=new ArrayList <Point>();
	@Override
	public void run(String arg0) {
		img=WindowManager.getCurrentImage();
		width=img.getWidth(); 
		height=img.getHeight();
		channels=img.getNChannels();
		slices=img.getNSlices();
		frames=img.getNFrames();
		current_slice=img.getSlice()-1;
		current_frame=img.getFrame()-1;
		int border_region=5;
		binary=new byte[width*height];
		original_binary=new byte[width*height];
		RoiManager rman=RoiManager.getInstance();
		GenericDialog gd=new GenericDialog("Border Patrol");
		gd.addNumericField("Border", 5, 0);
		gd.showDialog();
		border_region=(int)gd.getNextNumber();
		int rois=rman.getCount();

		//Make a list of all the points in the rois
		for (int i=0; i<rois; i++)
		{
			Roi roi=rman.getRoi(i);
			Polygon p=roi.getPolygon();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					Point P=new Point(x,y);
					if (p.contains(P)) 
					{
						point_list.add(P);
						binary[x+y*width]=1;
						original_binary[x+y*width]=1;
					}
				}
			}
		}
		
		//Find the averages for all the channels
		float [] averages=new float[channels];
		for (int c=0; c<channels; c++)
		{
			float [] pix=(float [])img.getStack().getProcessor(c+1+current_slice*channels+current_frame*channels*slices).getPixels();
			for (ListIterator jF=point_list.listIterator();jF.hasNext();)
			{
				Point curpt=(Point)jF.next();
				averages[c]+=pix[curpt.x+curpt.y*width];
			}
			IJ.log("Channel: "+(c+1)+": "+(averages[c]/point_list.size()));
			averages[c]=averages[c]/point_list.size();
		}
				
		//Now we have to grow
		int ctr;
		for (int i=0; i<border_region; i++)
		{
			ctr=0;
			for (ListIterator jF=point_list.listIterator();jF.hasNext();)
			{
				Point curpt=(Point)jF.next();
				if (check_neighbor(curpt.x+1, curpt.y)) ctr++;
				if (check_neighbor(curpt.x-1, curpt.y)) ctr++;
				if (check_neighbor(curpt.x, curpt.y+1)) ctr++;
				if (check_neighbor(curpt.x, curpt.y-1)) ctr++;
			}
			point_list.clear();
			for (ListIterator jF=tmp_point_list.listIterator();jF.hasNext();)
			{
				Point curpt=(Point)jF.next();
				point_list.add(curpt);
			}
			tmp_point_list.clear();
		}
		
		//Mask out ones that were in the original
		float [] border_averages=new float [channels];
		int [] border_counts=new int[channels];
		for (int c=0; c<channels; c++)
		{
			float [] pix=(float [])img.getStack().getProcessor(c+1+current_frame*channels*slices).getPixels();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					if (binary[x+y*width]==1&&original_binary[x+y*width]==0)
					{
						border_averages[c]+=pix[x+y*width];
						border_counts[c]+=1;
						//IJ.log("X: "+x+" Y: "+y);
					}
				}
			}
			IJ.log("Updated Border Channel: "+(c+1)+": "+(border_averages[c]/border_counts[c]));
		}
		
		//Show results
		ResultsTable rslt=ResultsTable.getResultsTable();
		rslt.incrementCounter();
		for (int c=0; c<channels; c++)
		{
			rslt.addValue("ROI "+(c+1), averages[c]);
			rslt.addValue("Border "+(c+1), (border_averages[c]/border_counts[c]));
		}
		rslt.addValue("Ratio ", (border_averages[0]/border_counts[0]/averages[0]));
		rslt.show("Results");
	}
	boolean check_neighbor(int x, int y)
	{
		if (x<0||x==width||y<0||y==height) return false;
		if (binary[y*width+x]!=0) return false;
		binary[y*width+x]=1;
		Point curpt=new Point(x, y);
		tmp_point_list.add(curpt);
		return true;
	}
}
