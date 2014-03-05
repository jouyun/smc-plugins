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
import ij.Prefs;
import ij.text.*;

public class test_lipid implements PlugIn {
	TextWindow twObjects;	//window to display results
	TextPanel tpObjects;	//textpanel within window to display results
	
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
	String imageName;

	int startradius = 5;
	int endradius = 13;
	float autoThresh = 760f;
	float threshold=900f;
	float varThresh = 10000f;
	int numSliceMask = 6;
	
	ImageStack autoThreshStack;
	int numChan;
	int numZ;
	int lipidChannel = 1;
	int autoChannel = 2;

	int startellipse=18;
	int endellipse=24;
	float ellpsthresh = 0.01f;	////Threshold to draw ellipses
	float circthresh = 0.002f;		////Threshold to calculate pixels for ellipses

//****************User input fields
	JTextField startR= new JTextField();
	JTextField endR= new JTextField();
	JTextField autoT= new JTextField();
	JTextField houghT= new JTextField();
	JTextField varT= new JTextField();
	JTextField numMask= new JTextField();
	final JComponent[] inputs = new JComponent[] {
                	new JLabel("Start Radius"),
                	startR,
                	new JLabel("End Radius"),
                	endR,
		new JLabel("Mask Slices (before and after)"),
                	numMask,
	            new JLabel("Auto-fluorecence Threshold"),
               	autoT,
		new JLabel("Hough Threshold"),
		houghT,
		new JLabel("Variance threshold"),
		varT
	};

///////////////////////  end add

	public void run(String arg) {
		//**********User input START
		getPrefs();
		startR.setText("" +startradius);
		endR.setText(""+endradius);
		numMask.setText(""+numSliceMask );
		autoT.setText(""+autoThresh );
		houghT.setText(""+threshold);
		varT.setText(""+varThresh );
		
		getUserInput();
		setPrefs();
		//**********User input END

		ImagePlus imp=WindowManager.getCurrentImage();
		imageName = imp.getTitle();
		width=imp.getWidth(); height=imp.getHeight();
		origStack=imp.getStack();
		stackSize = origStack.getSize();

		numChan = imp.getNChannels();
		numZ = imp.getNSlices();
		doThreshMaskChan2(imp);


		//float[] pixels=(float[])(imp.getProcessor()).getPixels();
		objects = new float[width*height];

		setup_pixel_shifts();

		retstack=new ImageStack(width,height);
		float[] origpixels=(float[])origStack.getPixels(1);

		ImageStack retstack=new ImageStack(width,height);

		// houghT and varT dimensions: [slices][radii][pixels]
		float[][][] houghT = new float[numZ][endradius - startradius+1][width*height];
		float[][][] varT = new float[numZ][endradius - startradius+1][width*height];
		
		//!!!!!!!!!!!!!!!!!---Change bounds of for-loop... not stackSize for only green channel
		for(int k=1;k<=numZ;k++){
			int indexlipid = imp.getStackIndex(lipidChannel,k,1);
			IJ.showStatus("transforming slice "+(k));
			float[] pixels=(float[])origStack.getPixels(indexlipid);
			Object[] houghReturn = do_hough_transform_slice(pixels,threshold,width,height,startradius,endradius);
			houghT [k-1] = (float[][])houghReturn[0];
			varT [k-1] = (float[][])houghReturn[1];
		}
		float[][] objStack = do_find_objects(houghT, varT);
		for(int q=0; q<numZ; q++){
			float[] temp = objStack[q];
			retstack.addSlice("",temp);
		}

		new ImagePlus("hough",retstack).show();
		String newtitle = "Hough Transforms:  HoughThresh = "+threshold+"  VarThresh = "+varThresh;
		String kernel ="  title=["+newtitle+"] image1=autoThresh image2=hough image3=[-- None --]";
		IJ.run("Concatenate...",kernel);
		kernel = "order=xyzct channels=2 slices="+numZ+" frames=1 display=Composite";
		IJ.run("Stack to Hyperstack...", kernel);
		ImagePlus impRET=WindowManager.getCurrentImage();
		impRET.setC(1);
		IJ.run("Grays");
		IJ.resetMinAndMax();
		impRET.setC(2);
		IJ.run("Green");
		IJ.resetMinAndMax();
	}

	void getUserInput(){
		JOptionPane.showMessageDialog(null, inputs, "Lipid Hough Input Parameters", JOptionPane.PLAIN_MESSAGE);
		startradius = Integer.parseInt(startR.getText());
		endradius =  Integer.parseInt(endR.getText());
		numSliceMask = Integer.parseInt(numMask.getText());
		autoThresh = Float.parseFloat(autoT.getText());
		threshold = Float.parseFloat(houghT.getText());
		varThresh = Float.parseFloat(varT.getText());
	}

	void setPrefs(){
		Prefs.set("rla.hym.lipidFinder.startradius",startradius);
		Prefs.set("rla.hym.lipidFinder.endradius ",endradius );
		Prefs.set("rla.hym.lipidFinder.numSliceMask ",numSliceMask );
		Prefs.set("rla.hym.lipidFinder.autoThresh ",autoThresh );
		Prefs.set("rla.hym.lipidFinder.threshold ",threshold );
		Prefs.set("rla.hym.lipidFinder.varThresh ",varThresh );
	}

	void getPrefs(){
		startradius = (int)Prefs.get("rla.hym.lipidFinder.startradius",startradius);
		endradius  = (int)Prefs.get("rla.hym.lipidFinder.endradius ",endradius );
		numSliceMask  = (int)Prefs.get("rla.hym.lipidFinder.numSliceMask ",numSliceMask );
		autoThresh = (float)Prefs.get("rla.hym.lipidFinder.autoThresh ",autoThresh );
		threshold = (float)Prefs.get("rla.hym.lipidFinder.threshold ",threshold );
		varThresh = (float)Prefs.get("rla.hym.lipidFinder.varThresh ",varThresh );
	}

	public void doThreshMaskChan2(ImagePlus imp){
		autoThreshStack =new ImageStack(width,height);
		for(int i = 1; i<=numZ; i++){
			float[] retPixels = new float[width*height];
			int indexlipid = imp.getStackIndex(lipidChannel,i,1);
			int indexauto = imp.getStackIndex(autoChannel,i,1);
			//float[] lipidPixels=new float[width*height];
			//float[] autoPixels=new float[width*height];
			float[] lipidPixels=(float[])origStack.getPixels(indexlipid);
			float[] autoPixels=(float[])origStack.getPixels(indexauto);
			for(int j =0; j<autoPixels.length; j++){
				if(autoPixels[j] >= autoThresh){
					retPixels[j] = 0;
					lipidPixels[j] =0;
				}else{
					retPixels[j] = lipidPixels[j];
				}
			}
			autoThreshStack.addSlice("",retPixels);
		}
		new ImagePlus("autoThresh",autoThreshStack).show();
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

	public float[] hough_transform(float[] image,int width,int height,int radius){
		int rindex=radius;
		float[] newimage=new float[width*height];
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				float integral=0.0f;
				for(int k=0;k<nangles[rindex];k++){
					int x=j+pixelshifts[rindex][0][k]; if(x<0){x+=width;} if(x>=width){x-=width;}
					int y=i+pixelshifts[rindex][1][k]; if(y<0){y+=height;} if(y>=height){y-=height;}
					integral+=image[x+y*width];
				}
				newimage[j+i*width]=integral/(float)nangles[rindex];
			}
		}
		return newimage;
	}

	public float[] hough_transform(byte[] image,int width,int height,int radius){
		int rindex=radius;
		float[] newimage=new float[width*height];
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				float integral=0.0f;
				for(int k=0;k<nangles[rindex];k++){
					int x=j+pixelshifts[rindex][0][k]; if(x<0){x+=width;} if(x>=width){x-=width;}
					int y=i+pixelshifts[rindex][1][k]; if(y<0){y+=height;} if(y>=height){y-=height;}
					if(image[x+y*width]==(byte)255){integral+=1.0f;}
				}
				newimage[j+i*width]=integral/(float)nangles[rindex];
			}
		}
		return newimage;
	}

/*
	public float[] fft_hough_transform(byte[] image,int width,int height,int radius){
		int largeaxis=width;
		if(height>width){largeaxis=height;}
		int po2length=(int)Math.ceil(Math.log(largeaxis)/Math.log(2.0));
		int newlength=(int)Math.pow(2.0,po2length);
		po4realfft2D fftclass=new po4realfft2D(newlength,newlength);
		//start by padding the image to a power of 2 length
		float[] newimage=new float[newlength*newlength];
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				newimage[j+i*newlength]=(image[j+i*width]==(byte)0) ? 0.0f:1.0f;
			}
		}
		float[] imimage=new float[newlength*newlength];
		//now do the fft
		fftclass.dorealfft2D(newimage,imimage,false);
		bessel bfunc=new bessel();
		//multiply by the radial bessel function
		for(int i=0;i<newlength;i++){
			double yval=(double)i;
			if(yval>newlength/2){yval-=(double)newlength;}
			for(int j=0;j<newlength;j++){
				double xval=(double)j;
				if(xval>newlength/2){xval-=(double)newlength;}
				double r=Math.sqrt(xval*xval+yval*yval);
				float multiplier=(float)bfunc.besselval(2.0*Math.PI*r*(double)radius/(double)newlength,0);
				newimage[j+i*newlength]*=multiplier;
				imimage[j+i*newlength]*=multiplier;
			}
		}
		//do the reverse fft
		fftclass.dorealfft2D(newimage,imimage,true);
		imimage=null;
		float[] cropped=new float[width*height];
		for(int i=0;i<height;i++){
			System.arraycopy(newimage,i*newlength,cropped,i*width,width);
		}
		return cropped;
	}
*/

	public byte[] inverse_hough_transform(byte[] image,int width,int height,int radius){
		//here we basically create a circle for every image pixel above threshhold
		int rindex=radius;
		byte[] newimage=new byte[width*height];
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				if(image[j+i*width]>(byte)0){
					for(int k=0;k<nangles[rindex];k++){
						int x=j+pixelshifts[rindex][0][k]; if(x<0){x+=width;} if(x>=width){x-=width;}
						int y=i+pixelshifts[rindex][1][k]; if(y<0){y+=height;} if(y>=height){y-=height;}
						newimage[x+y*width]=(byte)255;
					}
				}
			}
		}
		return newimage;
	}

	public byte[] inverse_hough_transform(float[] image,float threshhold,int width,int height,int radius){
		//here we basically create a circle for every image pixel above threshhold
		int rindex=radius;
		byte[] newimage=new byte[width*height];
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				if(image[j+i*width]>threshhold){
					for(int k=0;k<nangles[rindex];k++){
						int x=j+pixelshifts[rindex][0][k]; if(x<0){x+=width;} if(x>=width){x-=width;}
						int y=i+pixelshifts[rindex][1][k]; if(y<0){y+=height;} if(y>=height){y-=height;}
						newimage[x+y*width]=(byte)255;
					}
				}
			}
		}
		return newimage;
	}

	public void inverse_hough_transform(float[] image,byte[] newimage,float threshhold,int width,int height,int radius){
		//here we basically create a circle for every image pixel above threshhold
		int rindex=radius;
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				if(image[j+i*width]>threshhold){
					for(int k=0;k<nangles[rindex];k++){
						int x=j+pixelshifts[rindex][0][k]; if(x<0){x+=width;} if(x>=width){x-=width;}
						int y=i+pixelshifts[rindex][1][k]; if(y<0){y+=height;} if(y>=height){y-=height;}
						newimage[x+y*width]=(byte)255;
					}
				}
			}
		}
	}

	public float[][] do_find_objects(float[][][] pixStackArray, float[][][]varStackArray){
		//pixStackArray and varStackArray DIMENSIONS:  [slices][radii][pixels]

		//now create the object image by using a max>threshhold not mask approach
		float[][] outobjects=new float[stackSize ][width*height];
		float objid=100.0f;
		float max=0.0f;
		float maxvar = 0.0f;
		int numObjects = 0;
		twObjects = new TextWindow(imageName+"_RESULTS.xls","x\ty\tz\tradius\though\tvar\tparameters",null,900,700);
		tpObjects = twObjects.getTextPanel();
		//IJ.log("Image: "+imageName+ "  Hough: "+threshold+"  Var: "+varThresh + "  Auto: "+autoThresh +"  Rad: "+startradius +"-"+endradius +"  numMask: "+ numSliceMask );
		do{
			float[] temp = getMaxStack(pixStackArray,varStackArray);
			max=temp[0]; int maxx=(int)temp[1]; int maxy=(int)temp[2]; int maxrad = (int)temp[3]; maxvar = temp[4]; int maxslice = (int)temp[5];
			if(max>=threshold){
				String resultsKernel = maxx+"\t"+maxy+"\t"+(maxslice+1)+"\t"+maxrad+"\t"+max + "\t" +maxvar;
				tpObjects.appendLine(resultsKernel);
				//IJ.log("x = "+maxx+"    y = "+maxy+"   slice = "+(maxslice+1)+"    rad = "+maxrad+"    hough= "+max + "  var = " +maxvar);
				objid+=1.0;
				numObjects++;
				int x=0;
				int y=0;
				//***MASK ALL SLICES
				
					int maxradius=maxrad;
					int startx=maxx-maxradius; if(startx<0){startx=0;}
					int endx=maxx+maxradius; if(endx>=width){endx=width-1;}
					int starty=maxy-maxradius; if(starty<0){starty=0;}
					int endy=maxy+maxradius; if(endy>=height){endy=height-1;}
					for(int i=starty;i<=endy;i++){
						int ydiff=i-maxy;
						for(int j=startx;j<=endx;j++){
							int xdiff=j-maxx;
							double r=Math.sqrt(ydiff*ydiff+xdiff*xdiff);
							if(r<=(double)maxradius){
								if(outobjects[maxslice][j+i*width]==0.0f){
									//***SAVE OBJECT PIXELS
									outobjects[maxslice][j+i*width]=objid;
								}
								for(int q=0;q<=endradius-startradius;q++){
									for(int p=maxslice-numSliceMask ; p<=maxslice+numSliceMask ; p++){
										if(p>=0 && p<numZ){
											pixStackArray[p][q][j+i*width]=0.0f;
										}
									}
								}
								
							}
						}
					}
				
			} else {
				break;
			}
		}while(max>=threshold);
		int numLines = tpObjects.getLineCount();
		String addParam = "";
		for(int i = 0; i<6;i++){
			switch (i){
				case 0: addParam = "Image: "+imageName;
				break;
				case 1: addParam = "Hough: "+threshold;
				break;
				case 2: addParam = "Var: "+varThresh;
				break;
				case 3: addParam = "Auto: "+autoThresh;
				break;
				case 4: addParam = "Radius: "+startradius +"-"+endradius;
				break;
				case 5: addParam = "numMask: "+ numSliceMask;
				break;
			}			
			if(i<numLines){
				String temp = tpObjects.getLine(i);
				tpObjects.setLine(i,temp+"\t"+addParam);
			}else{
				String temp = "\t\t\t\t\t\t"+addParam;
				tpObjects.appendLine(temp);
			}
		}
		//tpObjects.updateColumnHeadings("ONE\tTWO\tTHREE\tParams");
		tpObjects.updateDisplay();
		//IJ.log("Number of Objects:  "+numObjects);
		return outobjects;
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
			houghstack1.addSlice("radius = "+i,tpixelsALL);
			varstack1.addSlice("radius = "+i,varpixALL);
			pixStackArray [i-startradius] = tpixels;
			varStackArray[i-startradius] = varpix;
			IJ.showProgress(i-startradius,endradius-startradius);
		}
		new ImagePlus("houghstack1",houghstack1).show();
		new ImagePlus("varstack1",varstack1).show();

		return new Object[]{pixStackArray, varStackArray};
	}

	public float[] getMaxStack(float[][][] pixData, float[][][] varData){
		float max=0;
		int maxx=0;
		int maxy=0;
		int maxrad=0;
		float maxvar=0;
		int maxslice =0;
		int range = endradius - startradius;
		for(int q=0; q<numZ;q++){
			for(int k =range; k>=0; k--){
				for(int i=0;i<height;i++){
					for(int j=0;j<width;j++){
						if(pixData[q][k][j+i*width]>=max){
							max=pixData[q][k][j+i*width];
							maxx=j;
							maxy=i;
							maxslice = q;
							maxrad = startradius+k;
							maxvar = varData[q][k][j+i*width];
						}
					}
				}
			}
		}
		float[] ret={max,(float)maxx,(float)maxy,maxrad, maxvar, maxslice};
		return ret;

	}

	public float[] getmax(float[] data,int width,int height){
		float max=0;
		int maxx=0;
		int maxy=0;
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				if(data[j+i*width]>max){
					max=data[j+i*width];
					maxx=j;
					maxy=i;
				}
			}
		}
		float[] ret={max,(float)maxx,(float)maxy};
		return ret;
	}

/*	public void clean_maxproj(){
		///// This approximates the ellipse by making a circle with radius equal to
		///// the minor axis and removing all those values from the maxprojellps array.
		///// clean_maxprojellps can be replaced if a function is made to fill all ellipses.

		int maxradius=endradius;
		int startx=maxx-maxradius; if(startx<0){startx=0;}
		int endx=maxx+maxradius; if(endx>=width){endx=width-1;}
		int starty=maxy-maxradius; if(starty<0){starty=0;}
		int endy=maxy+maxradius; if(endy>=height){endy=height-1;}
		for(int i=starty;i<=endy;i++){
			int ydiff=i-maxy;
			for(int j=startx;j<=endx;j++){
				int xdiff=j-maxx;
				double r=Math.sqrt(ydiff*ydiff+xdiff*xdiff);
				if(r<=(double)maxradius){
					maxprojellps[j+i*width]=0.0f;
				}
			}
		}
	}
*/
////////////////// start add

	public void rla_call_ellipse_hough_transform(byte[] origpixels) {

		//pixel_shifts();
		//ellpsstack=new ImageStack(width,height);	////Uncomment:  HOUGH STACK
		hough_transform_ellipse(origpixels,width,height);
		//new ImagePlus("Hough Transforms",ellpsstack).show();	////Uncomment:  HOUGH STACK
		inverse_hough_ellipse();
	}


	public void hough_transform_ellipse(byte[] pixels, int width, int height){

		maxprojellps=new float[width*height];
		thetaproj=new double[width*height];
		aproj=new int[width*height];
		bproj=new int[width*height];
		float[] drawmaxprojellps = new float[width*height];
	
		for(double theta= 0; theta<Math.PI; theta=theta+(Math.PI/4)){    ////theta is the angle of the major axis with respect to the x-axis
			double cos_theta = Math.cos(theta);
			double sin_theta = Math.sin(theta);
			IJ.showStatus("transforming theta"+(theta));
			for(int b=startellipse;b<=endellipse;b++){          ////// b is the length of the major axis
				int nangles =(int)(2.0*Math.PI*(double)b);
				double increment = 5.0/nangles;
				for(int a = (int)(2*b/3); a < b; a=a+2){          ////// a is the length of the minor axis
					//double da = (double)a;
					//double db = (double)b;
					//double nangles = Math.PI*(3*(da+db)-Math.sqrt((3*da+db)*(da+3*db)));	////Ramanujan approximation for perimeter takes too much time
					//double increment = 1.0/nangles;
					float[] newimage = new float[width*height];
					for(int i=0;i<height;i++){
						for(int j=0;j<width;j++){
							if(maxproj[j+i*width]>circthresh){	////comment out to calculate all pixels for ellipses
								float sumpix = 0.0f;
								for(double phi=0; phi<2*Math.PI; phi=phi+increment){   ///phi is the parametric parameter giving all x,y values
									double cos_phi = Math.cos(phi);
									double sin_phi = Math.sin(phi);
									int x= (int)(j +(b*cos_phi*cos_theta)-(a*sin_phi*sin_theta));
									int y = (int)(i + (b*cos_phi*sin_theta) + (a*sin_phi*cos_theta));
									if(x>0 && x<width && y>0 && y<height){
									//	sumpix += (float)(pixels[x+y*width] & 0xff);	////Cannot cast byte to float. Use "& 0xff" to convert the byte to a float.
										if(pixels[x+y*width]!=0){
											sumpix += 1.0f;		
										}
									}
									newimage[j+i*width] = 5*sumpix/((float)nangles);		////Multiply 5.0 for nangles circle correction factor
								}
							}
						}
					}
					IJ.showProgress((b-a)/b);
					//ellpsstack.addSlice("theta="+theta+" a="+a+" b="+b,newimage);	////Uncomment:  HOUGH STACK
					for(int k=0;k<width*height;k++){          /////check against maxprojellps
						if(newimage[k]>maxprojellps[k]){
							maxprojellps[k]=newimage[k];
							thetaproj[k]=theta;
							aproj[k]=a;
							bproj[k]=b;
							drawmaxprojellps[k]=newimage[k];
						}
					}
				}
			}
		}
		
		ImageStack mystack = new ImageStack(width,height);		////Uncomment:  MAX PROJECTION displayed
		mystack.addSlice("",drawmaxprojellps);
		new ImagePlus("MaxProj",mystack).show();
		
	}
	
	public void inverse_hough_ellipse(){
		ellpsid = 0.0f;
		//ellipsepixels = new float[width*height];
		getmaxellps();
		while(maxellps>ellpsthresh){
			ellpsid+=1;
			float [] temp = draw_ellipse();
			for(int i=0; i<width*height; i++){
				objects[i] += temp[i];
			}
			clean_maxprojellps();
			getmaxellps();
		}
		/*
		ImageStack mystack = new ImageStack(width,height);		////Pixels are now added to the circle image
		mystack.addSlice("",ellipsepixels);
		new ImagePlus("Ellipse found",mystack).show();
		*/
	}

	public void getmaxellps(){
		maxellps=maxprojellps[0];
		maxxellps=0;
		maxyellps=0;
		for(int i=0;i<height;i++){	
			for(int j=0;j<width;j++){
				if(maxprojellps[j+i*width]>maxellps){
					maxellps=maxprojellps[j+i*width];
					maxxellps=j;
					maxyellps=i;
				}
			}
		}
	}

	public float[] draw_ellipse(){
		
		float[] pixels = new float[height*width];
		int q = maxxellps + (maxyellps*width);		//// q is the current index for all arrays
		int nangles = (int)(2.0*Math.PI*(double)aproj[q]);
		double increment = 5.0/(nangles);
		double cos_theta= Math.cos(thetaproj[q]);
		double sin_theta= Math.sin(thetaproj[q]);
		
		for(double phi = 0; phi<2*Math.PI; phi= phi+increment){
			double cos_phi= Math.cos(phi);
			double sin_phi = Math.sin(phi);

			int x= (int)(maxxellps +(bproj[q]*(cos_phi*cos_theta)-aproj[q]*(sin_phi*sin_theta)));
			int y = (int)(maxyellps + (bproj[q]*(cos_phi*sin_theta) + aproj[q]*(sin_phi*cos_theta)));
			if(x>0 && x<width && y>0 && y<height){
				pixels[x+y*width] = ellpsid;
			}
		}
		
		return pixels;
	}

	public void clean_maxprojellps(){
		///// This approximates the ellipse by making a circle with radius equal to
		///// the minor axis and removing all those values from the maxprojellps array.
		///// clean_maxprojellps can be replaced if a function is made to fill all ellipses.

		int maxradius=aproj[maxxellps+width*maxyellps];
		int startx=maxxellps-maxradius; if(startx<0){startx=0;}
		int endx=maxxellps+maxradius; if(endx>=width){endx=width-1;}
		int starty=maxyellps-maxradius; if(starty<0){starty=0;}
		int endy=maxyellps+maxradius; if(endy>=height){endy=height-1;}
		for(int i=starty;i<=endy;i++){
			int ydiff=i-maxyellps;
			for(int j=startx;j<=endx;j++){
				int xdiff=j-maxxellps;
				double r=Math.sqrt(ydiff*ydiff+xdiff*xdiff);
				if(r<=(double)maxradius){
					maxprojellps[j+i*width]=0.0f;
				}
			}
		}
	}

/////////////////  end add

}
