package splugins;

import java.awt.Point;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

public class Generate_Blobs_3D implements PlugIn {

	int img_size=256;
	int z_size=16;
	int blob_size=5;
	int num_images=10;
	ImagePlus newimg;
	float [][] current_pix;
	float z_ratio=4;
	
	public class MyIntPoint {
		int x;
		int y;
		int z;
		MyIntPoint(int a, int b, int c)
		{
			x=a;
			y=b;
			z=c;
		}
		MyIntPoint(double a, double b, double c)
		{
			x=(int)Math.round(a);
			y=(int)Math.round(b);
			z=(int)Math.round(c);
		}
		MyIntPoint(MyDoublePoint a)
		{
			x=(int)Math.round(a.x);
			y=(int)Math.round(a.y);
			z=(int)Math.round(a.z);
		}
		boolean is_valid()
		{
			if (x<0||x>=newimg.getWidth()||y<0||y>=newimg.getHeight()||z<0||z>=z_size)
			{
				return false;
			}
			return true;
		}
	}
	
	public class MyDoublePoint {
		double x;
		double y;
		double z;
		MyDoublePoint(double a, double b, double c)
		{
			x=a;
			y=b;
			z=c;
		}
		public double distance(MyDoublePoint other)
		{
			double temp=(other.x-x)*(other.x-x)+(other.y-y)*(other.y-y)+z_ratio*z_ratio*(other.z-z)*(other.z-z);
			return (Math.sqrt(temp));
		}
	}
	
	public class blob_params {
		double theta;
		double phi;
		double a;
		double b;
		double c;
		MyDoublePoint F1;
		MyDoublePoint F2;
		MyDoublePoint S;
		ArrayList <MyIntPoint> pixels;
		ArrayList <MyIntPoint> edges;
		blob_params()
		{}
		//Absolute pixel locations are capitals
		//Shifts for a window are lowercase
		boolean make_blob()
		{
			S=new MyDoublePoint(Math.random()*img_size, Math.random()*img_size, Math.random()*z_size);
			theta=Math.random()*Math.PI;
			phi=Math.random()*2*Math.PI;
			Random RG=new Random();
			a=1.1*blob_size*(1+RG.nextGaussian()/5.0);
			b=0.9*blob_size*(1+RG.nextGaussian()/5.0);
			if (b>a)
			{
				double t=a;
				a=b;
				b=t;
			}
			c=Math.sqrt(a*a-b*b);
			F1=new MyDoublePoint(S.x+c*Math.sin(theta)*Math.cos(phi), S.y+c*Math.sin(theta)*Math.sin(phi), S.z+c*Math.cos(theta)/z_ratio);
			F2=new MyDoublePoint(S.x-c*Math.sin(theta)*Math.cos(phi), S.y-c*Math.sin(theta)*Math.sin(phi), S.z-c*Math.cos(theta)/z_ratio);
			pixels=new ArrayList<MyIntPoint>();
			edges=new ArrayList<MyIntPoint>();
			int scalar=6;
			int lat_size=blob_size*scalar+1;
			int half_lat=blob_size*scalar/2;
			int [][][]tmp_array=new int[z_size][lat_size][lat_size];
			
			//First check to see if it is already there, if so, skip it
			
			boolean already_there=false;
			for (int z=0; z<z_size; z++)
			{
				for (int x=0; x<lat_size; x++)
				{
					for (int y=0; y<lat_size; y++)
					{
						MyDoublePoint T=new MyDoublePoint(S.x+x-half_lat, S.y+y-half_lat, S.z+z-z_size/2);
						double dis=F1.distance(T)+F2.distance(T);
						if (dis<=2*a)
						{
							MyIntPoint TI=new MyIntPoint(T);
							if (!TI.is_valid()) continue;
							
							if (current_pix[TI.z][TI.x+TI.y*newimg.getWidth()]>0) already_there=true;
						}
					}
				}
			}
			if (already_there) return false;
			for (int z=0; z<z_size; z++)
			{
				for (int x=0; x<lat_size; x++)
				{
					for (int y=0; y<lat_size; y++)
					{
						MyDoublePoint T=new MyDoublePoint(S.x+x-half_lat, S.y+y-half_lat, S.z+z-z_size/2);
						double dis=F1.distance(T)+F2.distance(T);
						if (dis<=2*a)
						{
							MyIntPoint TI=new MyIntPoint(T);
							tmp_array[z][x][y]=1;
							pixels.add(TI);
						}
					}
				}
			}
			for (int z=0; z<z_size; z++)
			{
				for (int x=0; x<lat_size; x++)
				{
					for (int y=0; y<lat_size; y++)
					{
						if (tmp_array[z][x][y]!=1) continue;
						if (tmp_array[z][x][y+1]==0||tmp_array[z][x][y-1]==0||tmp_array[z][x+1][y]==0||tmp_array[z][x-1][y]==0||tmp_array[z+1][x][y]==0||tmp_array[z-1][x][y]==0)
						{
							MyIntPoint tmp=new MyIntPoint(new MyDoublePoint(S.x+x-half_lat, S.y+y-half_lat, S.z+z-z_size/2));
							edges.add(tmp);
						}
					}
				}
			}
			return true;
		}
	};

	@Override
	public void run(String arg0) {
		GenericDialog gd=new GenericDialog("Choose parameters");
		gd.addNumericField("Number of images", 10000, 0);
		gd.addNumericField("Blob size", 5, 0);
		gd.addNumericField("Number of blobs per image", 150,0);
		gd.addNumericField("Image width/height", 256,0);
		gd.addNumericField("Z Ratio", 4.0,0);
		gd.addCheckbox("Randomize number of blobs: ", false);
		gd.showDialog();
		
		num_images=(int)gd.getNextNumber();
		blob_size=(int)gd.getNextNumber();
		int number_blobs=(int)gd.getNextNumber();
		img_size=(int)gd.getNextNumber();
		z_ratio=(float) gd.getNextNumber();
		boolean randomize=gd.getNextBoolean();
		// TODO Auto-generated method stub
		z_size=(int)Math.ceil(img_size/z_ratio);
		newimg=NewImage.createFloatImage("Img", img_size, img_size, 2*num_images*z_size, NewImage.FILL_BLACK);
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		
		
		float [] [] [] pix=new float[num_images][z_size][];
		float [] [] [] edge=new float[num_images][z_size][];
		
		for (int f=0; f<num_images; f++)
		{
			for (int s=0; s<z_size; s++)
			{
				pix[f][s]=(float [])newimg.getStack().getProcessor(f*2*z_size+s*2+1).getPixels();
				edge[f][s]=(float [])newimg.getStack().getProcessor(f*2*z_size+s*2+2).getPixels();
			}
		}
		for (int f=0; f<num_images; f++)
		{
			//START WORKING HERE
			current_pix=pix[f];
			blob_params [] blobs;
			if (randomize) blobs=new blob_params[(int) Math.round(Math.random()*number_blobs)]; 
			else blobs=new blob_params[number_blobs];
			Random RG=new Random();
			
			int success_counter=0;
			for (int i=0; i<blobs.length; i++)
			{
				blobs[i]=new blob_params();
				if (blobs[i].make_blob()) success_counter++;
				for (ListIterator jF=blobs[i].pixels.listIterator();jF.hasNext();)
				{
					MyIntPoint curpt=(MyIntPoint)jF.next();
					if (curpt.is_valid()) 
					{
						pix[f][curpt.z][curpt.x+curpt.y*img_size]=(float) (255*(1+RG.nextGaussian()/12));
						edge[f][curpt.z][curpt.x+curpt.y*img_size]=2;
					}
					
				}
				for (ListIterator jF=blobs[i].edges.listIterator();jF.hasNext();)
				{
					MyIntPoint curpt=(MyIntPoint)jF.next();
					if (curpt.is_valid())
					{
						edge[f][curpt.z][curpt.x+curpt.y*img_size]=1;
					}
				}
			}
			if (randomize)
			{
				rslt.incrementCounter();
				rslt.addValue("Blobs", success_counter);
			}
			IJ.showProgress((double)f/(num_images-1));
			
		}
		if (randomize) rslt.show("Results");
		/*
		for (int i=0; i<blobs.length; i++)
		{
			blobs[i]=new blob_params();
			for (int x=(int) (blobs[i].x-2*blob_size); x<(int)(blobs[i].x+2*blob_size); x++)
			{
				for (int y=(int) (blobs[i].y-2*blob_size); y<(int) (blobs[i].y+2*blob_size); y++)
				{
					if (x<0||y<0||x>=img_size||y>=img_size) continue;
					double dis=Math.sqrt((x-blobs[i].xa)*(x-blobs[i].xa)+(y-blobs[i].ya)*(y-blobs[i].ya))+Math.sqrt((x-blobs[i].xb)*(x-blobs[i].xb)+(y-blobs[i].yb)*(y-blobs[i].yb));
					//IJ.log(""+x+","+y+","+(int)Math.floor(blobs[i].xa)+","+(int)Math.floor(blobs[i].ya)+","+(int)Math.floor(blobs[i].xb)+(int)Math.floor(blobs[i].yb));
					//IJ.log("Distance: "+dis+" Semi-major: "+blobs[i].a);
					if (dis<=2*blobs[i].a)
					{
						pix[x+img_size*y]=255;
					}
				}
			}
		}*/
		newimg.show();
		newimg.updateAndDraw();
		

	}

}
