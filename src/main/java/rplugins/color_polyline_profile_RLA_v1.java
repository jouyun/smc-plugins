package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import java.lang.*;

//Updates in new version:
//1. Asks whether your polygon / line segment is enclosed
//2. Asks whether you want to display Straightened Images
//3. Asks whether you want to run a Cross Correlation


public class color_polyline_profile_RLA_v1 implements PlugIn {

	public void run(String arg) {
		GenericDialog gd=new GenericDialog("Options");
		int linewidth =5;
		gd.addNumericField("Profile Width",linewidth,0);
		gd.addCheckbox("Enclosed",true);		//RLA
		gd.addCheckbox("Straightened Images",false);		//RLA
		gd.addCheckbox("Cross Correlate",false);		//RLA
		gd.showDialog();
		if(gd.wasCanceled()){return;}
		linewidth=(int)gd.getNextNumber();
		boolean enclosed = gd.getNextBoolean();	//RLA
		boolean straightimg = gd.getNextBoolean();	//RLA
		boolean crosscorr = gd.getNextBoolean();	//RLA

		//IJ.run("Thresh NaN to Zero");		//Sets any NaN pixels to zero

		ImagePlus imp1=WindowManager.getCurrentImage();
			int Nchan = imp1.getNChannels();
			//IJ.log(""+Nchan);
			//imp.setPosition(3);
			//ImagePlus ch1=imp.getChannel(1);

			int width=imp1.getWidth();
			int height=imp1.getHeight();
			PolygonRoi roi=(PolygonRoi)imp1.getRoi();
			Rectangle r = roi.getBounds();
	for(int chan=1; chan<=Nchan; chan++){
		ij.WindowManager.setTempCurrentImage(imp1);
		imp1.setPosition(chan);
		ImagePlus imp=WindowManager.getCurrentImage();

		FloatProcessor fp=(FloatProcessor)imp.getProcessor();
		float[] pixels=(float[])fp.getPixels();


		int[] xvals1=roi.getXCoordinates();
		int[] yvals1=roi.getYCoordinates();
		int npts=roi.getNCoordinates();
		int[] xvals=new int[npts];
		int[] yvals=new int[npts];
		int nlines=npts-1;
		//int nlines=1;
		for(int i=0;i<npts;i++){
			xvals[i]=xvals1[i]+r.x;
			yvals[i]=yvals1[i]+r.y;
		}
		int length=0;
		for(int i=0;i<nlines;i++){
			length+=(int)Math.sqrt((xvals[i+1]-xvals[i])*(xvals[i+1]-xvals[i])+(yvals[i+1]-yvals[i])*(yvals[i+1]-yvals[i]));
		}
//RLA added
		if(enclosed == true){		//this includes the last line segment if the selection is enclosed
			length+=(int)Math.sqrt((xvals[0]-xvals[nlines])*(xvals[0]-xvals[nlines])+(yvals[0]-yvals[nlines])*(yvals[0]-yvals[nlines]));
		}
////////
		float[] straightened=new float[length*linewidth];
		float[] pixelval=new float[length];
		float[] profile=new float[length];
		int counter=0;
		for(int i=0;i<nlines;i++){
			float[] coords={xvals[i],yvals[i],xvals[i+1],yvals[i+1]};
			int templength=0;
			for(int j=0;j<linewidth;j++){
				float distance=(float)j-((float)linewidth)/2.0f;
				float[] newcoords=getParallelLine(coords,distance);
				if(outsidebounds(newcoords,width,height)){
					IJ.showMessage("Outside of Image");
					return;
				}
				float[] tempfloat=getLineProfile(newcoords,pixels,width,height);
				templength=tempfloat.length;
				for(int k=0;k<templength;k++){
					straightened[(counter+k)*linewidth+j]=tempfloat[k];
				}
				for(int k=0;k<templength;k++){
					profile[counter+k]+=tempfloat[k]/(float)linewidth;
				}
			}

			for(int j=0;j<templength;j++){
				pixelval[counter+j]=(float)(counter+j+1);
			}
			counter+=templength;
		}


			if(enclosed == true){		//this includes the last line segment if the selection is enclosed
				float[] lastcoords={xvals[nlines],yvals[nlines],xvals[0],yvals[0]};
				int templength=0;
				for(int j=0;j<linewidth;j++){
					float distance=(float)j-((float)linewidth)/2.0f;
					float[] newcoords=getParallelLine(lastcoords,distance);
					if(outsidebounds(newcoords,width,height)){
						IJ.showMessage("Outside of Image");
						return;
					}
					float[] tempfloat=getLineProfile(newcoords,pixels,width,height);
					templength=tempfloat.length;
					for(int k=0;k<templength;k++){
						straightened[(counter+k)*linewidth+j]=tempfloat[k];
					}
					for(int k=0;k<templength;k++){
						profile[counter+k]+=tempfloat[k]/(float)linewidth;
					}
				}
				for(int j=0;j<templength;j++){
					pixelval[counter+j]=(float)(counter+j+1);
				}
			}
		String title1 = "Polyline Profile - Ch " + chan;
		String title2 = "Straightened - Ch " + chan;
		(new PlotWindow(title1,"Pixel","Intensity",pixelval,profile)).draw();
		FloatProcessor fp2=new FloatProcessor(linewidth,length,straightened,null);
		if(straightimg==true){
			(new ImagePlus(title2,fp2)).show();     //shows straightened data
		}

	}
			if(crosscorr==true){
				IJ.run("traj pearson crosscorr jru v1");
				return;
			}


}




	private boolean outsidebounds(float[] coords,int width,int height){
		for(int i=0;i<4;i++){
			if(coords[i]<0){return true;}
		}
		if((coords[0]>=width || coords[1]>=height) || (coords[2]>=width || coords[3]>=height)){return true;}
		return false;
	}

	private float[] getParallelLine(float[] coords,float distance){
		float xinc,yinc;
		if(coords[2]!=coords[0] && coords[3]!=coords[1]){
			float slope=(coords[3]-coords[1])/(coords[2]-coords[0]);
			float newslope=-1.0f/slope;
			xinc=(float)Math.sqrt(1.0/(1.0+(double)(newslope*newslope)));
			yinc=newslope*xinc;
		} else {
			if(coords[2]==coords[0]){
				xinc=1.0f;
				yinc=0.0f;
			} else {
				xinc=0.0f;
				yinc=1.0f;
			}
		}
		float[] oc=new float[4];
		oc[0]+=distance*xinc+coords[0];
		oc[1]+=distance*yinc+coords[1];
		oc[2]+=distance*xinc+coords[2];
		oc[3]+=distance*yinc+coords[3];
		return oc;
	}

	private float[] getLineProfile(float[] coords,float[] image,int width,int height){
		int length=(int)Math.sqrt((coords[2]-coords[0])*(coords[2]-coords[0])+(coords[3]-coords[1])*(coords[3]-coords[1]));
		float[] line=new float[length];
		float xinc,yinc;
		if(coords[2]!=coords[0]){
			float slope=(coords[3]-coords[1])/(coords[2]-coords[0]);
			xinc=(float)Math.sqrt(1.0/(1.0+(double)(slope*slope)));
			if(coords[2]<coords[0] && xinc>0.0f){xinc=-xinc;}
			yinc=slope*xinc;
		} else {
			xinc=0.0f;
			yinc=1.0f;
		}
		float x=coords[0];
		float y=coords[1];
		for(int i=0;i<length;i++){
			line[i]=interp(image,width,height,x,y);
			x+=xinc;
			y+=yinc;
		}
		return line;
	}

	private float interp(float[] image,int width,int height,float x,float y){
		int intx=(int)x;
		int inty=(int)y;
		float remx=x-(float)intx;
		float remy=y-(float)inty;
		float interpx1=image[intx+width*inty]+remx*(image[intx+width*inty+1]-image[intx+width*inty]);
		float interpx2=image[intx+width*(inty+1)]+remx*(image[intx+width*(inty+1)+1]-image[intx+width*(inty+1)]);
		return interpx1+remy*(interpx2-interpx1);
	}

}
