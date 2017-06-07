package splugins;

import java.awt.Point;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.PlugIn;

public class Generate_Blobs implements PlugIn {

	static int img_size=256;
	static int blob_size=5;
	static int num_images=10000;
	
	public class MyIntPoint {
		int x;
		int y;
		MyIntPoint(int a, int b)
		{
			x=a;
			y=b;
		}
	}
	
	public class blob_params {
		double x;
		double y;
		double theta;
		double a;
		double b;
		double xa;
		double ya;
		double xb;
		double yb;
		ArrayList <MyIntPoint> pixels;
		ArrayList <MyIntPoint> edges;
		blob_params()
		{
			x=Math.random()*img_size;
			y=Math.random()*img_size;
			theta=Math.random()*2*Math.PI;
			Random RG=new Random();
			a=1.1*blob_size*(1+RG.nextGaussian()/5.0);
			b=0.9*blob_size*(1+RG.nextGaussian()/5.0);
			if (b>a)
			{
				double t=a;
				a=b;
				b=t;
			}
			double c=Math.sqrt(a*a-b*b);
			double ct=Math.cos(theta);
			double st=Math.sin(theta);
			xa=x+c*ct;
			xb=x-c*ct;
			ya=y+c*st;
			yb=y-c*st;
			pixels=new ArrayList<MyIntPoint>();
			edges=new ArrayList<MyIntPoint>();
			int scalar=6;
			int [][]tmp_array=new int[scalar*blob_size+1][scalar*blob_size+1];
			for (int xx=0; xx<blob_size*scalar+1; xx++)
			{
				for (int yy=0; yy<blob_size*scalar+1; yy++)
				{
					double cx=x+xx-scalar/2*blob_size;
					double cy=y+yy-scalar/2*blob_size;
					double dis=Math.sqrt((cx-xa)*(cx-xa)+(cy-ya)*(cy-ya))+Math.sqrt((cx-xb)*(cx-xb)+(cy-yb)*(cy-yb));
					if (dis<=2*a)
					{
						tmp_array[xx][yy]=1;
						MyIntPoint tmp = new MyIntPoint ((int)Math.round(cx),(int)Math.round(cy));
						pixels.add(tmp);
					}
				}
			}
			
			for (int xx=0; xx<blob_size*scalar+1; xx++)
			{
				for (int yy=0; yy<blob_size*scalar+1; yy++)
				{
					if (tmp_array[xx][yy]!=1) continue;
					if (xx<0||xx>=blob_size*scalar+1||yy<0||yy>=blob_size*scalar+1) continue;
					if (tmp_array[xx+1][yy]==0||tmp_array[xx-1][yy]==0||tmp_array[xx][yy+1]==0||tmp_array[xx][yy-1]==0)
					{
						double cx=x+xx-scalar/2*blob_size;
						double cy=y+yy-scalar/2*blob_size;
						MyIntPoint tmp = new MyIntPoint ((int)Math.round(cx),(int)Math.round(cy));
						edges.add(tmp);
					}
				}
			}
			
		}
	};

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus newimg=NewImage.createFloatImage("Img", img_size, img_size, 2*num_images, NewImage.FILL_BLACK);
		for (int f=0; f<num_images; f++)
		{
			
			float [] pix=(float [])newimg.getStack().getProcessor(f+1).getPixels();
			float [] edge=(float [])newimg.getStack().getProcessor(num_images+f+1).getPixels();
			blob_params [] blobs=new blob_params[150];
			Random RG=new Random();
			
			for (int i=0; i<blobs.length; i++)
			{
				blobs[i]=new blob_params();
				for (ListIterator jF=blobs[i].pixels.listIterator();jF.hasNext();)
				{
					MyIntPoint curpt=(MyIntPoint)jF.next();
					if (curpt.x>=0&&curpt.x<img_size&&curpt.y>=0&&curpt.y<img_size) pix[curpt.x+curpt.y*img_size]=(float) (255*(1+RG.nextGaussian()/12));
				}
				for (ListIterator jF=blobs[i].edges.listIterator();jF.hasNext();)
				{
					MyIntPoint curpt=(MyIntPoint)jF.next();
					if (curpt.x>=0&&curpt.x<img_size&&curpt.y>=0&&curpt.y<img_size) edge[curpt.x+curpt.y*img_size]=1;
				}
			}
			IJ.showProgress((double)f/(num_images-1));
			
		}
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
