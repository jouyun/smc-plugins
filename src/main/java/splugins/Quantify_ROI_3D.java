package splugins;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Quantify_ROI_3D implements PlugIn {
	ImagePlus img;
	int width;
	int height;
	int channels;
	int slices;
	int frames;
	
	class Point3D 
	{
		int x;
		int y;
		int z;
		
		Point3D(int xx, int yy, int zz)
		{
			x=xx;
			y=yy;
			z=zz;
		}
		
		public Point3D(Point3D next) {
			// TODO Auto-generated constructor stub
			this.x=next.x;
			this.y=next.y;
			this.z=next.z;
		}

		public boolean equals(Object object)
	    {
	        boolean sameSame = false;

	        if (object != null && object instanceof Point3D)
	        {
	        	Point3D o=(Point3D) object;
	            sameSame = ((this.x == o.x)&&(this.y==o.y)&&(this.z==o.z));
	        }

	        return sameSame;
	    }
	};
	
	Point3D [] get_neighbors(Point3D input)
	{
		Point3D [] rtn=new Point3D [4];
		rtn[0]=new Point3D(input.x+1,input.y, input.z);
		rtn[1]=new Point3D(input.x-1,input.y, input.z);
		rtn[2]=new Point3D(input.x,input.y+1, input.z);
		rtn[3]=new Point3D(input.x,input.y-1, input.z);
		return rtn;
	}

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		img=WindowManager.getCurrentImage();
		width=img.getWidth();
		height=img.getHeight();
		channels=img.getNChannels();
		slices=img.getNSlices();
		frames=img.getNFrames();
		
		RoiManager manager=RoiManager.getInstance();
        Roi [] rois = manager.getRoisAsArray();
        int z_up_down=1;
        
        GenericDialog gd=new GenericDialog("Taper");
        gd.addNumericField("Z taper depth", 1, 0);
        for (int c=0; c<channels; c++)
        {
        	gd.addNumericField("Channel "+c+" threshold", 0, 0);
        }
        
        gd.showDialog();
        z_up_down=(int)gd.getNextNumber();
        
        float [] thresholds=new float[channels];
        for (int c=0; c<channels; c++)
        {
        	thresholds[c]=(float)gd.getNextNumber();
        }
        
        //Get the list of points in every ROI
        ArrayList <ArrayList <Point3D>> TheList=new ArrayList <ArrayList <Point3D>> () ;
        for (int i=0; i<rois.length; i++) TheList.add(new ArrayList<Point3D>());
		
        for (int r=0; r<rois.length; r++)
        {
        	ArrayList <Point3D> current_roi_list=TheList.get(r);
        	Rectangle rec=rois[r].getBounds();
        	for (int x=rec.x; x<rec.x+rec.width; x++)
        	{
        		for (int y=rec.y; y<rec.y+rec.height; y++)
        		{
        			int z_slice=rois[r].getZPosition()-1;
        			Point P=new Point(x,y);
        			if (!(rois[r].getPolygon().contains(P))) continue;
    				Point3D pt= new Point3D(x,y,z_slice);
    				current_roi_list.add(pt);
        		}
        	}
        }
        
        /*
        for (int z=0; z<slices; z++)
        {
        	for (int x=0; x<width; x++)
        	{
        		for (int y=0; y<height; y++)
        		{
        			Point P=new Point(x,y);
        			for (int r=0; r<rois.length; r++)
        			{
        				int z_slice=rois[r].getZPosition()-1;
        				
        				ArrayList <Point3D> current_roi_list=TheList.get(r);
        				
        				if (!(rois[r].getPolygon().contains(P)&&z==z_slice)) continue;
        				Point3D pt= new Point3D(x,y,z);
        				current_roi_list.add(pt);
        			}
        		}
        	}
        }*/
        
        IJ.log("Done finding points");
        //Create the tapered list
        ArrayList <ArrayList <Point3D>> GrownList=new ArrayList <ArrayList <Point3D>>();
        for (int r=0; r<rois.length; r++)
        {
        	ArrayList <Point3D> my_list=(ArrayList <Point3D>) TheList.get(r).clone();
        	ArrayList <Point3D> cur_level_list=(ArrayList <Point3D>) TheList.get(r).clone();
        	for (int z_shift=1; z_shift<=z_up_down; z_shift++)
        	{
        		ArrayList <Point3D> shriv_list=new ArrayList <Point3D>();
        		for (ListIterator jF=cur_level_list.listIterator();jF.hasNext();)
        		{
        			Point3D cp=(Point3D) jF.next();
        			Point3D [] neighbors=get_neighbors(cp);
        			
        			boolean is_in=true;
        			
        			if (!cur_level_list.contains(neighbors[0])) is_in=false;
        			if (!cur_level_list.contains(neighbors[1])) is_in=false;
        			if (!cur_level_list.contains(neighbors[2])) is_in=false;
        			if (!cur_level_list.contains(neighbors[3])) is_in=false;
        			
        			if (is_in)
        			{
        				shriv_list.add(new Point3D(cp.x, cp.y, cp.z+1));
        			}
        			
        		}
        		//Make the new list the current level list to be evaluated next loop
        		cur_level_list=shriv_list;
        		
        		//Make a negative z version of this list
        		ArrayList <Point3D> neg_list=new ArrayList <Point3D>();
        		for (ListIterator jF=shriv_list.listIterator();jF.hasNext();)
        		{
        			Point3D cp=new Point3D((Point3D) jF.next());
        			cp.z=cp.z-2*z_shift;
        			neg_list.add(cp);
        		}
        		//appends both positive and negative lists to my_list
        		my_list.addAll(shriv_list);
        		my_list.addAll(neg_list);
        	}
        	//Create full grown list for this roi
        	GrownList.add(my_list);
        }
        
        //Quantify results
		ResultsTable rslt=ResultsTable.getResultsTable();
        for (ListIterator jF=GrownList.listIterator();jF.hasNext();)  //Loop over objects
		{
        	float [] sums=new float[channels];
        	float [] valids=new float[channels];
        	
			ArrayList <Point3D> cur_list=(ArrayList <Point3D>) jF.next();
			for (ListIterator jjF=cur_list.listIterator();jjF.hasNext();)
			{
				Point3D cp=(Point3D) jjF.next();
				
				for (int c=0; c<channels; c++)
				{
					if (cp.z<0||cp.z>=slices) continue;
					float [] pix=(float []) img.getStack().getPixels(cp.z*channels+c+1);
					valids[c]++;
					if (pix[cp.x+cp.y*width]<thresholds[c]) continue;
					sums[c]+=pix[cp.x+cp.y*width];
				}				
			}
			rslt.incrementCounter();
			rslt.addValue("Volume", valids[0]);
			for (int c=0; c<channels; c++)
			{
				rslt.addValue("IntDen"+c, sums[c]);
			}
		}
        rslt.show("Results");
		

	}

}
