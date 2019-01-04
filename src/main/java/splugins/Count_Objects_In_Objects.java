package splugins;

import java.util.ArrayList;
import java.util.ListIterator;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import splugins.compute_3D_blob_statistics.MyIntPoint;

public class Count_Objects_In_Objects implements PlugIn {

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
		GenericDialog gd =new GenericDialog("Parameters");
		gd.addNumericField("Bigger object channel", 1, 0);
		gd.addNumericField("Smaller object channel", 2, 0);
		gd.showDialog();
		int big_channel=(int)(gd.getNextNumber()-1);
		int small_channel=(int)(gd.getNextNumber()-1);
		
		ArrayList<Point>[] point_list=new ArrayList[10000];
		for (int j=0; j<point_list.length; j++) point_list[j]=new ArrayList<Point>();
		
		for (int s=0; s<slices; s++)
		{
			short [] pix=(short [])(img.getStack().getPixels(1+big_channel+s*channels));
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
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		
		
		for (int p=1; p<point_list.length; p++)
		{
			ArrayList<Point> current_list=point_list[p];
			ArrayList<Integer> object_list=new ArrayList<Integer>();
			
			for (ListIterator pF=current_list.listIterator(); pF.hasNext();)
			{
				Point curpt=(Point)pF.next();
				int x=curpt.x, y=curpt.y, z=curpt.z;
				short [] pix =(short [])(img.getStack().getPixels(1+small_channel+curpt.z*channels));
				if (pix[x+y*width]==0) continue;
				
				if (!object_list.contains((int) pix[x+y*width])) object_list.add((int) pix[x+y*width]);
			}
			int number_small_spots=object_list.size();
			
			for (ListIterator pF=current_list.listIterator(); pF.hasNext();)
			{
				Point curpt=(Point)pF.next();
				int x=curpt.x, y=curpt.y, z=curpt.z;
				short [] pix =(short [])(new_img.getStack().getPixels(1+curpt.z));
				pix[x+y*width]=(short)(number_small_spots+1);
			}
			rslt.incrementCounter();
			rslt.addValue("Object ID", p);
			rslt.addValue("Number of nucleoli", number_small_spots);
		}
		rslt.show("Results");
		new_img.show();
		new_img.updateAndDraw();

	}

}
