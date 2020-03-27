package splugins;

import java.util.ArrayList;
import java.util.ListIterator;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import splugins.Count_Objects_In_Objects.Point;

public class Replace_3DCountMask_With_SingleSpot implements PlugIn {


	class Point
	{
		int x;
		int y;
		int z;
		Point(int xx, int yy, int zz)
		{
			x=xx;
			y=yy;
			z=zz;
		}
	}
	
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		int width=img.getWidth(), height=img.getHeight(), channels=img.getNChannels(), slices=img.getNSlices();
		
		
		ArrayList<Point>[] point_list=new ArrayList[100000];
		for (int j=0; j<point_list.length; j++) point_list[j]=new ArrayList<Point>();
		
		for (int s=0; s<slices; s++)
		{
			short [] pix=(short [])(img.getStack().getPixels(1+s*channels));
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					if (pix[x+y*width]==0) continue;
					Point p=new Point(x,y,s);
					/*if (point_list[pix[x+y*width]]==null) 
					{
						point_list[pix[x+y*width]]=new ArrayList<Point>();
						IJ.log("X,Y: "+ pix[x+y*width]);
					}*/
					point_list[pix[x+y*width]].add(p);
				}
			}
				
		}
		
		ImagePlus new_img=NewImage.createShortImage("Result", width, height, slices, NewImage.FILL_BLACK);
		
		
		for (int p=1; p<point_list.length; p++)
		{
			ArrayList<Point> current_list=point_list[p];
			ArrayList<Integer> object_list=new ArrayList<Integer>();
			int nucleoli_pixel_counter=0;
			
			float avg_x=0, avg_y=0, avg_z=0;
			for (ListIterator pF=current_list.listIterator(); pF.hasNext();)
			{
				Point curpt=(Point)pF.next();
				int x=curpt.x, y=curpt.y, z=curpt.z;
				avg_x+=x;
				avg_y+=y;
				avg_z+=z;
			}
			avg_x=Math.round(avg_x/(float)current_list.size());
			avg_y=Math.round(avg_y/(float)current_list.size());
			avg_z=Math.round(avg_z/(float)current_list.size());
			
			short [] pix =(short [])(new_img.getStack().getPixels(1+(int)avg_z));
			pix[(int)avg_x+(int)avg_y*width]=(short)p;
			
		}
		new_img.show();
		new_img.updateAndDraw();

	}
}
