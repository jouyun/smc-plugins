package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import java.lang.Float;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class rla_lipid_params_finder implements PlugIn {
	int[][][] pixelshifts;
	int[] nangles;
	float[] maxproj;

//////////////////////// start add
	int width;
	int height;
	ImageStack origStack;
	ImageStack retstack;	
	ImageStack ellpsstack;
	float[] maxprojellps;
	double[] thetaproj;
	int[] aproj;
	int[] bproj;
	float maxellps;
	int maxxellps;
	int maxyellps;
	float ellpsid;
	float[] objects;		//// defined in the inverse_hough_ellipse routine
	int stackSize;

	int startradius = 3;
	int endradius = 30;
//	float autoThresh = 760f;
	float threshold=800f;
	float varThresh = 12000f;
//	int numSliceMask = 6;
	
	ImageStack autoThreshStack;
	int numChan;
	int numZ;
	int lipidChannel = 1;
	int autoChannel = 2;



///////////////////////  end add

	public void run(String arg) {

		ImagePlus imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); height=imp.getHeight();
		origStack=imp.getStack();
		stackSize = origStack.getSize();

		numChan = imp.getNChannels();
		numZ = imp.getNSlices();


		setup_pixel_shifts();

		retstack=new ImageStack(width,height);
		float[] origpixels=(float[])origStack.getPixels(1);

		ImageStack retstack=new ImageStack(width,height);

		// houghT and varT dimensions: [slices][radii][pixels]
		//float[][][] houghT = new float[stackSize][endradius - startradius+1][width*height];
		//float[][][] varT = new float[stackSize][endradius - startradius+1][width*height];

		//!!!!!!!!!!!!!!!!!---Change bounds of for-loop... not stackSize for only green channel
		for(int k=0;k<stackSize ;k++){
			IJ.showStatus("transforming slice "+(k+1));
			float[] pixels=(float[])origStack.getPixels(k+1);
			Object[] houghReturn = do_hough_transform_slice(pixels,threshold,width,height,startradius,endradius);
			//houghT [k] = (float[][])houghReturn[0];
			//varT [k] = (float[][])houghReturn[1];
		}
		doMakeHyperstack();
	}

	void doMakeHyperstack(){
		IJ.run("Concatenate...", "  title=[Parameter Finder] image1=houghstack1 image2=varstack1 image3=[-- None --]");
		IJ.run("Stack to Hyperstack...", "order=xyzct channels=2 slices=28 frames=1 display=Grayscale");
	}

	public void setup_pixel_shifts(){
		pixelshifts=new int[endradius+1][2][];
		nangles=new int[endradius+1];
		for(int radius=1;radius<=endradius;radius++){
			int rindex=radius;
			double dangle=1.0/(double)radius;
			nangles[rindex]=(int)(2.0*Math.PI*(double)radius);
			pixelshifts[rindex][0]=new int[nangles[rindex]];
			pixelshifts[rindex][1]=new int[nangles[rindex]];
			for(int i=0;i<nangles[rindex];i++){
				double x=(double)radius*Math.cos(dangle*(double)i);
				double y=(double)radius*Math.sin(dangle*(double)i);
				pixelshifts[rindex][0][i]=(int)x;
				pixelshifts[rindex][1][i]=(int)y;
			}
		}
	}

	public Object[] hough_transform_VAR(float[] image,int width,int height,int radius){
		int rindex=radius;
		float[] newimage=new float[width*height];
		float[] varimage=new float[width*height];
		float[] newimageALL=new float[width*height];
		float[] varimageALL=new float[width*height];
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				float integral=0.0f;
				float intensities[] = new float[nangles[rindex]];
				for(int k=0;k<nangles[rindex];k++){
					int x=j+pixelshifts[rindex][0][k]; if(x<0){x+=width;} if(x>=width){x-=width;}
					int y=i+pixelshifts[rindex][1][k]; if(y<0){y+=height;} if(y>=height){y-=height;}
					intensities[k] = image[x+y*width];
					integral+=image[x+y*width];
				}
				float variance = getVariance(intensities);
				float integralValue = integral/(float)nangles[rindex];
					//Take all values in order to show image
					newimageALL[j+i*width]=integralValue;
					varimageALL[j+i*width] = variance;
				if(integralValue >=threshold && variance<varThresh){
					newimage[j+i*width]=integralValue;
					varimage[j+i*width] = variance;
				}else{
					newimage[j+i*width]=0;
					varimage[j+i*width]=0;
				}
			}
		}
		return new Object[]{newimage, varimage, newimageALL, varimageALL};
	}

	float getMean(float[] data){
		int size = data.length;
		float sum = 0.0f;
		for(float a : data){
	            	sum += a;
		}	
	            return sum/size;
	}

	float getVariance(float[] data){
		int size = data.length;
	            float mean = getMean(data);
	            float temp = 0.0f;
	            for(float a :data){
			temp += (mean-a)*(mean-a);
		}
            	    return temp/size;
	}

	public Object[] do_hough_transform_slice(float[] image,float threshhold,int width,int height,int startradius,int endradius){

		//start by looping through the radii and getting the maximum projection hough transform along with a maximum radius image
		maxproj=new float[width*height];
		int[] radproj=new int[width*height];
		ImageStack houghstack1=new ImageStack(width,height);
		ImageStack varstack1=new ImageStack(width,height);
		float[][] pixStackArray = new float[endradius - startradius+1][width*height];
		float[][] varStackArray = new float[endradius - startradius+1][width*height];
//	float[] tpixels = null;
//	float[] varpix = null;

		for(int i=startradius;i<=endradius;i++){
			//get the hough transform
			//float[] tpixels = hough_transform(image,width,height,i);
			Object[] calcData= hough_transform_VAR(image,width,height,i);
			float[] tpixels = (float[])calcData[0];
			float[] varpix = (float[])calcData[1];
			float[] tpixelsALL = (float[])calcData[2];
			float[] varpixALL = (float[])calcData[3];
			houghstack1.addSlice("Hough  R = "+i,tpixelsALL);
			varstack1.addSlice("Var  R = "+i,varpixALL);
			pixStackArray [i-startradius] = tpixels;
			varStackArray[i-startradius] = varpix;
			IJ.showProgress(i-startradius,endradius-startradius);
		}
		new ImagePlus("houghstack1",houghstack1).show();
		new ImagePlus("varstack1",varstack1).show();

		return new Object[]{pixStackArray, varStackArray};
	}

}
