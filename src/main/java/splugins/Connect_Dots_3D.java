package splugins;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import splugins.seeded_multipoint_adaptive_region_grow.MyIntPoint;

public class Connect_Dots_3D implements PlugIn {

	class MyPoint
	{
		float x;
		float y;
		float z;
		MyPoint(float a, float b, float c)
		{
			x=a;
			y=b;
			z=c;
		}
		MyPoint(MyPoint a)
		{
			x=a.x;
			y=a.y;
			z=a.z;
		}
		MyPoint to_distance()
		{
			MyPoint rtn = new MyPoint(x,y,z);
			rtn.x=(float) (x*8.0);
			rtn.y=(float) (y*8.0);
			rtn.z=(float) (z*50.0);
			return rtn;
		}
		MyPoint to_pixel()
		{
			MyPoint rtn = new MyPoint(x,y,z);
			rtn.x=(float) (x/8.0);
			rtn.y=(float) (y/8.0);
			rtn.z=(float) (z/50.0);
			return rtn;
		}
	}

	
	class LineDistance
	{
		float ax;
		float ay;
		float az;
		float nx;
		float ny;
		float nz;
		float bx;
		float by;
		float bz;
		
		//This should be the DISTANCE point not the pixel
		LineDistance(MyPoint a, MyPoint b)
		{
			ax=a.x;
			ay=a.y;
			az=a.z;
			bx=b.x;
			by=b.y;
			bz=b.z;
			
			nx=b.x-a.x;
			ny=b.y-a.y;
			nz=b.z-a.z;
			float n=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
			nx=nx/n;
			ny=ny/n;
			nz=nz/n;
		}
		float find_distance(MyPoint input)
		{
			float dx, dy, dz;
			dx=ax-input.x;
			dy=ay-input.y;
			dz=az-input.z;
			
			float dot = dx*nx+dy*ny+dz*nz;
			dx = dx-dot*nx;
			dy = dy-dot*ny;
			dz = dz-dot*nz;
			return dx*dx+dy*dy+dz*dz;
		}
		
		//float pDistance(x, y, x1, y1, x2, y2) 
		//https://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
		float pDistance(MyPoint input)
		{
			float A = input.x-ax;
			float B = input.y -ay;
			float BB = input.z-az;
			float C = bx-ax;
			float D = by-ay;
			float DD = bz-az;
			float dot = A*C + B*D +BB*DD;
			float len_sq = C*C+D*D+DD*DD;
			float param =-1;
			if (len_sq!=0)
			{
				param = dot/len_sq;
			}
			float xx, yy, zz;
			if (param<0)
			{
				xx=ax;
				yy=ay;
				zz=az;
			}
			else if (param>1)
			{
				xx=bx;
				yy=by;
				zz=bz;
			}
			else
			{
				xx=ax+param*C;
				yy=ay+param*D;
				zz=az+param*DD;
			}
			float dx = input.x-xx;
			float dy = input.y-yy;
			float dz = input.z-zz;
			return(dx*dx+dy*dy+dz*dz);
		}
	}
	ArrayList <MyPoint> pix_list=new ArrayList <MyPoint>();
	ArrayList <MyPoint> dis_list=new ArrayList <MyPoint>();
	ArrayList <MyPoint> img_dis_list=new ArrayList <MyPoint>();
	ArrayList <MyPoint> img_pix_list=new ArrayList <MyPoint>();
	int width;
	int height;
	int slices;
	@Override
	public void run(String arg) {
		ImagePlus img = WindowManager.getCurrentImage();
		width = img.getWidth();
		height = img.getHeight();
		slices = img.getImageStackSize();
		
		GenericDialog gd = new GenericDialog("Parameters");
		gd.addCheckbox("Value scales with distance from start?", true);
		gd.addCheckbox("Reverse order?", false);
		gd.addNumericField("Radius", 70, 0);
		gd.showDialog();
		boolean scale_values = gd.getNextBoolean();
		boolean reverse_order = gd.getNextBoolean();
		double radius = gd.getNextNumber();
		//********************GET THE LIST OF ROIS**************************************************
		RoiManager manager=RoiManager.getInstance();
    	int selected=manager.getSelectedIndex();
    	Roi current_roi=manager.getRoi(selected);
    	for (int r=0; r<manager.getCount(); r++) 
    	{
    		current_roi = manager.getRoi(r);
    		MyPoint pix_pt = new MyPoint((float)current_roi.getXBase(), (float)current_roi.getYBase(), (float)current_roi.getZPosition());
    		pix_list.add(pix_pt);
    		MyPoint dis_pt = pix_pt.to_distance();
    		dis_pt.to_distance();
    		dis_list.add(dis_pt);
    	}
    	//Make the list of all pixels in distance space
    	for (int x=0; x<width; x++)
    	{
    		for (int y=0; y<height; y++)
    		{
    			for (int z=0; z<slices; z++)
    			{
    				MyPoint pt = new MyPoint(x,y,z);
    				img_dis_list.add(pt.to_distance());
    			}
    		}
    	}
    	//********************LOOP OVER ALL LINE SEGMENTS****************************
    	for (int r=0; r<pix_list.size()-1; r++) 
    	//for (int r=0; r<200; r++)
    	{
    		LineDistance current_line = new LineDistance(dis_list.get(r), dis_list.get(r+1));
    		IJ.log("At: "+r);
    		for (int p=0; p<img_dis_list.size(); p++)
    		{
    			
    			//float distance = current_line.find_distance(img_dis_list.get(p));
    			float distance = current_line.pDistance(img_dis_list.get(p));
    			if (distance<radius*radius)
    			{
    				MyPoint real_pos = img_dis_list.get(p).to_pixel();
    				int x = (int)real_pos.x;
    				int y = (int)real_pos.y;
    				int z = (int)real_pos.z;
    				short [] pix = (short [])img.getStack().getPixels(z);
    				if (scale_values)
    				{
    					if (reverse_order) 
    					{
    						pix[x+width*y] = (short) (1000+(pix_list.size()-r)*10);
    					}
    					else
    					{
    						pix[x+width*y] = (short) ((r)*100);
    					}
    					
    				}
    				else
    				{
    					pix[x+width*y] = (short) ((short) 60000);
    				}
    				//
    			}
    		}
    	}

	}

}
