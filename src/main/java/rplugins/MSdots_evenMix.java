package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class MSdots_evenMix implements PlugIn {
	//constants for random number generator:
	final int IA = 16807;
	final int IM = 2147483647;
	final double AM = (1.0/(double)IM);
	final int IQ = 127773;
	final int IR = 2836;
	final int MASK = 123459876;
	int seed;

	public void run(String arg) {
		GenericDialog gd = new GenericDialog("Options");
		seed=1;
		gd.addNumericField("Seed",seed,0);
		boolean randomize=false;
		gd.addCheckbox("randomize seed",randomize);
		int dwidth=12;
		gd.addNumericField("Dot width",dwidth,0);
		int dheight=12;
		gd.addNumericField("Dot height",dheight,0);
		int ndots=500;
		gd.addNumericField("Number of Dots",ndots,0);
		int width=512;
		gd.addNumericField("Image width",width,0);
		int height=512;
		gd.addNumericField("Image height",height,0);
		gd.showDialog();
		if(gd.wasCanceled()){return;}
		seed=(int)gd.getNextNumber();
		randomize=gd.getNextBoolean();
		dwidth=(int)gd.getNextNumber();
		dheight=(int)gd.getNextNumber();
		ndots=(int)gd.getNextNumber();
		width=(int)gd.getNextNumber();
		height=(int)gd.getNextNumber();
		if(randomize){
			seed=(int)((double)System.currentTimeMillis()/1000.0);
		}
		float[] pixels1=new float[width*height];
		float[] pixels2=new float[width*height];
		float[] pixels3=new float[width*height];
		for(int i=0;i<ndots;i++){
			int x=(int)((double)(width-1)*ran0());
			int y=(int)((double)(height-1)*ran0());
			int num = (int)(7*ran0());
				//IJ.log(""+num);
			for(int j=0;j<dwidth;j++){
				for(int k=0;k<dheight;k++){
					
					int dumval=x+y*width+j+k*width;
					if(dumval<width*height){
						/*
						if(num == 0){pixels1[dumval]+=1.0f;}
						if(num == 1){pixels2[dumval]+=1.0f;}
						if(num == 2){pixels3[dumval]+=1.0f;}
						if(num == 3){pixels1[dumval]+=1.0f;  pixels2[dumval]+=1.0f;}
						if(num == 4){pixels1[dumval]+=1.0f;  pixels3[dumval]+=1.0f;}
						if(num == 5){pixels2[dumval]+=1.0f;  pixels3[dumval]+=1.0f;}
						if(num == 6){pixels1[dumval]+=1.0f;  pixels2[dumval]+=1.0f;  pixels3[dumval]+=1.0f;}
						*/
						if(num == 0||num == 3||num == 4||num == 6){
							pixels1[dumval]+=1.0f;
						}
						if(num == 1||num == 3||num == 5||num == 6){
							pixels2[dumval]+=1.0f;
						}
						if(num == 2||num == 4||num == 5||num == 6){
							pixels3[dumval]+=1.0f;
						}
						
					}
				}
			}
		}
		FloatProcessor fp1 = new FloatProcessor(width,height,pixels1,null);
		ImagePlus imp1 = new ImagePlus("Random Dots 1",fp1);
		imp1.show();

		FloatProcessor fp2 = new FloatProcessor(width,height,pixels2,null);
		ImagePlus imp2 = new ImagePlus("Random Dots 2",fp2);
		imp2.show();

		FloatProcessor fp3 = new FloatProcessor(width,height,pixels3,null);
		ImagePlus imp3 = new ImagePlus("Random Dots 3",fp3);
		imp3.show();

	}

	public double ran0()   //Gives a random number between 0 and 1
	{
	//minimal linear congruential random number generator from numerical recipes in c
		int k;
		double ans;
		seed ^= MASK;
		k=seed/IQ;
		seed=IA*(seed-k*IQ)-IR*k;
		if(seed < 0){seed += IM;}
		ans=AM*seed;
		seed ^= MASK;
		return ans;
	}

}
