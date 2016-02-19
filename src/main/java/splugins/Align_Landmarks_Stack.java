package splugins;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import mpicbg.ij.util.Util;
import mpicbg.models.Point;
import splugins.seeded_multipoint_adaptive_region_grow.MyIntPoint;

public class Align_Landmarks_Stack implements PlugIn {

	ImagePlus imp;
	int width, height, channels, slices, frames;
	int cur_channel, cur_frame, cur_slice;
	boolean last_roi;
	@Override
	public void run(String arg0) 
	{
		// TODO Auto-generated method stub
		//final List<Point> sourcePoints=Util.pointRoiToPoints((PointRoi)source.getRoi());
		imp=WindowManager.getCurrentImage();
		Calibration my_cal=imp.getCalibration();
        
        width=imp.getWidth();
        height=imp.getHeight();
        channels=imp.getNChannels();
        slices=imp.getNSlices();
        frames=imp.getNFrames();
        
        cur_channel=imp.getChannel()-1;
        cur_frame=imp.getFrame()-1;
        cur_slice=imp.getSlice()-1;
        RoiManager manager=RoiManager.getInstance();
        Roi [] rois = manager.getRoisAsArray();
        
        GenericDialog gd=new GenericDialog("Outline");
        gd.addCheckbox("Use last two ROIs as outline? ", true);
        gd.showDialog();
        last_roi=gd.getNextBoolean();

        float [] accumulated_x=new float [0];
        float [] accumulated_y=new float[0];
        int number_of_cuts=32;
        
        int how_many_to_breakup=0;
        if(last_roi) how_many_to_breakup=manager.getCount()-1;
        else how_many_to_breakup=manager.getCount();
        for (int j=0; j<how_many_to_breakup; j++)
        {
    		Roi roi = manager.getRoi(j);
    		float [][] points=new float[0][0];
            if(roi!=null)
            {
            	Polygon p=roi.getPolygon();
            	
            	points=new float[2][p.npoints];
            	double old_x=p.xpoints[0], old_y=p.ypoints[0];
            	float total_length=0;
            	for (int i=0; i<p.npoints; i++)
            	{
            		float x=p.xpoints[i];
            		float y=p.ypoints[i];
            		points[0][i]=x;
            		points[1][i]=y; 
            	}
            	total_length=calculate_distance_up_to(points[0], points[1], points[0].length-1);
            	float segment=(float) (total_length/number_of_cuts);
            	IJ.log("Total length: "+total_length);
            	
            	//Now go through the length of the line and add points to accumulated_x and accumulated_y
            	
            	//Add the first point
            	accumulated_x=add_float(accumulated_x, points[0][0]);
            	accumulated_y=add_float(accumulated_y, points[1][0]);
            	float current_pos=0;
            	int idx=0;
            	
            	//Find the intermediate points
            	for (int s=0; s<number_of_cuts-1; s++)
            	{
            		float current_target=(s+1)*segment;
            		while(current_pos<current_target)
            		{
            			float dx=points[0][idx+1]-points[0][idx];
            			float dy=points[1][idx+1]-points[1][idx];
            			current_pos=current_pos+(float)Math.sqrt(dx*dx+dy*dy);
            			idx++;
            			//IJ.log("idx,pos:  "+idx+","+current_pos);
            		}
            		//idx now sits on the point PAST the true point, must walk it back along that direction
            		idx--;
            		current_pos=calculate_distance_up_to(points[0], points[1], idx);
            		float gap=current_target-current_pos, pt_gap=db(points[0][idx], points[0][idx+1], points[1][idx], points[1][idx+1]);
            		accumulated_x=add_float(accumulated_x, points[0][idx]+gap/pt_gap*(points[0][idx+1]-points[0][idx]));
            		accumulated_y=add_float(accumulated_y, points[1][idx]+gap/pt_gap*(points[1][idx+1]-points[1][idx]));
            		IJ.log("Put one at:  "+(current_pos+gap)+","+current_target);
            	}
            	
            	//Add the last point
            	IJ.log("total length:  "+total_length);
            	IJ.log("distance to last: "+calculate_distance_up_to(points[0], points[1], points[0].length-1));
            	accumulated_x=add_float(accumulated_x, points[0][points[0].length-1]);
            	accumulated_y=add_float(accumulated_y, points[1][points[1].length-1]);
            }	
        }	
        if (last_roi)
        {
        	Roi roi = manager.getRoi(manager.getCount()-1);
    		float [][] points=new float[0][0];
            if(roi!=null)
            {
            	Polygon p=roi.getPolygon();
            	
            	points=new float[2][p.npoints];
            	double old_x=p.xpoints[0], old_y=p.ypoints[0];
            	float total_length=0;
            	for (int i=0; i<p.npoints; i++)
            	{
            		float x=p.xpoints[i];
            		float y=p.ypoints[i];
            		points[0][i]=x;
            		points[1][i]=y; 
            	}
            	accumulated_x=merge_floats(accumulated_x, points[0]);
            	accumulated_y=merge_floats(accumulated_y, points[1]);
            }
        }
        PointRoi myroi=new PointRoi(accumulated_x, accumulated_y, accumulated_x.length);
    	imp.setRoi(myroi);
    	imp.updateAndDraw();
	}
	public float [] merge_floats(float [] a, float [] b)
	{
		float [] rtn=new float[a.length+b.length];
		System.arraycopy(a, 0, rtn, 0, a.length);
		System.arraycopy(b, 0,  rtn,  a.length, b.length);
		return rtn;
	}
	public float [] add_float(float [] a, float b)
	{
		float [] rtn=new float[a.length+1];
		System.arraycopy(a, 0, rtn, 0, a.length);
		rtn[a.length]=b;
		return rtn;
	}
	public float calculate_distance_up_to(float [] x, float [] y, int idx)
	{
		if (idx==0) return 0;
		float dist=0;
		for (int i=0; i<idx; i++)
		{
			float dx=x[i]-x[i+1];
			float dy=y[i]-y[i+1];
			dist=dist+(float)Math.sqrt(dx*dx+dy*dy);
		}
		return dist;
	}
	
	public float db(float x1, float x2, float y1, float y2)
	{
		float dx=x2-x1;
		float dy=y2-y1;
		return (float)Math.sqrt(dx*dx+dy*dy);
	}

}
