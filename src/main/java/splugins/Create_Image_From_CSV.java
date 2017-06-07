package splugins;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

public class Create_Image_From_CSV implements PlugIn {

	final int X=0;
	final int Y=1;
	final int Z=2;
	final int C1=3;
	final int C2=4;
	final int C3=5;
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		OpenDialog dlg=new OpenDialog("Find CSV");
		String fname=dlg.getPath();
		ImagePlus current_img=WindowManager.getCurrentImage();
		
		ImagePlus imp=NewImage.createFloatImage("Img", current_img.getWidth(), current_img.getHeight(), current_img.getNSlices()*3, NewImage.FILL_BLACK);
		
		float [][] point_data=new float[6][1000000];
		int total_points=0;
		
		try {
			FileInputStream fos=new FileInputStream(fname);
			BufferedReader r= new BufferedReader(new InputStreamReader(fos));
			r.mark(0);
			String line=null;
			String delims=",";
			int ctr=0;
			
			while ((line=r.readLine())!=null)
			{
				total_points++;
				if (ctr==0)
				{
					ctr++;
					continue;
				}
				String [] xy=line.split(delims);
				point_data[X][ctr-1]=Float.parseFloat(xy[1]);
				point_data[Y][ctr-1]=Float.parseFloat(xy[2]);
				point_data[Z][ctr-1]=Float.parseFloat(xy[3]);
				point_data[C1][ctr-1]=Float.parseFloat(xy[5]);
				point_data[C2][ctr-1]=Float.parseFloat(xy[6]);
				point_data[C3][ctr-1]=Float.parseFloat(xy[7]);
				ctr++;
			}
			r.mark(0);
			r.reset();
			r.close();
			
			IJ.log("Points:  "+total_points);
			
		}
		catch (Exception e) 
		{
			IJ.log("Guess it didn't exist");
		}
		
		if (point_data==null) return;
		
		for (int i=0; i<total_points; i++)
		{
			int x=(int) point_data[X][i], y=(int) point_data[Y][i], z=(int) point_data[Z][i];
			
			IJ.log("X,Y,Z:  "+x+","+y+","+z);
			
			float [] pix=(float [])imp.getStack().getPixels(z*3+1);
			for (int xx=x-1; xx<x+2; xx++)
			{
				for (int yy=y-1; yy<y+2; yy++)
				{
					if (xx<0||xx>=imp.getWidth()||yy<0||y>=imp.getHeight()) continue;
					pix[xx+yy*current_img.getWidth()]=point_data[C1][i];
				}
				
			}
			
			pix=(float [])imp.getStack().getPixels(z*3+2);
			for (int xx=x-1; xx<x+2; xx++)
			{
				for (int yy=y-1; yy<y+2; yy++)
				{
					if (xx<0||xx>=imp.getWidth()||yy<0||y>=imp.getHeight()) continue;
					pix[xx+yy*current_img.getWidth()]=point_data[C2][i];
				}
				
			}
			
			pix=(float [])imp.getStack().getPixels(z*3+3);
			for (int xx=x-1; xx<x+2; xx++)
			{
				for (int yy=y-1; yy<y+2; yy++)
				{
					if (xx<0||xx>=imp.getWidth()||yy<0||y>=imp.getHeight()) continue;
					pix[xx+yy*current_img.getWidth()]=point_data[C3][i];
				}
				
			}
			
		}
		imp.setDimensions(3, current_img.getNSlices(), 1);
		imp.setOpenAsHyperStack(true);
		imp.show();
		imp.updateAndDraw();
		

	}

}
