package splugins;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import imagescience.feature.Differentiator;
import imagescience.image.FloatImage;
import imagescience.image.Image;

public class Create_Tensorflow_Stacks implements PlugIn {

	ImagePlus imp;
	int width;
	int height;
	int slices;
	int frames;
	int channels;
	int cur_slice;
	int cur_frame;
	int cur_channel;
	ImagePlus source_img, new_imp;
	int sub_sample=1000;
	int window_size;
	static final int z_window_size=2;
	
	class MyPoint {
		int x;
		int y;
		int z;
		
		MyPoint(int xx, int yy, int zz)
		{
			x=xx;
			y=yy;
			z=zz;
		}
	}
	
	@Override
	public void run(String arg0) {
		imp=WindowManager.getCurrentImage();
		width=imp.getWidth(); 
		height=imp.getHeight();
		slices=imp.getNSlices();
		frames=imp.getNFrames();
		channels=imp.getNChannels();
		cur_slice=imp.getSlice()-1;
		cur_frame=imp.getFrame()-1;
		cur_channel=imp.getChannel()-1;
		
		final ImagePlus[] admissibleImageList = createAdmissibleImageList();
		final String[] sourceNames = new String[admissibleImageList.length];	
		//IJ.log("Good ones:  "+admissibleImageList.length+"\n");		
		if (admissibleImageList.length == 0) return;
		//IJ.log(""+admissibleImageList.length);
		for (int k = 0; (k < admissibleImageList.length); k++) 
		{
			sourceNames[k]=admissibleImageList[k].getTitle();
		}
		GenericDialog gd = new GenericDialog("Choose point masked image");
		gd.addChoice("Source image:", sourceNames, admissibleImageList[0].getTitle());
		gd.addNumericField("Sub-sample rate", 1000, 0);
		gd.addNumericField("Window size: ", 32, 0);
		gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		int sourceIndex=gd.getNextChoiceIndex();		
		source_img=admissibleImageList[sourceIndex];
		sub_sample=(int)gd.getNextNumber();
		window_size=(int)gd.getNextNumber();
		
		ArrayList <MyPoint> point_list=get_point_list(source_img);
		IJ.log("Number of points:  "+point_list.size());
		
		new_imp=NewImage.createFloatImage("Img", window_size*2+1, window_size*2+1, channels*point_list.size()/sub_sample, NewImage.FILL_BLACK);
		
		fill_zoomed_image(new_imp, point_list, imp);
		
		new_imp.setDimensions(channels, point_list.size(), 1);
		new_imp.setOpenAsHyperStack(true);
		new_imp.show();
		new_imp.updateAndDraw();
		

	}
	
	public void fill_zoomed_image(final ImagePlus new_imp, final ArrayList <MyPoint> point_list, final ImagePlus source_img)
	{
		final AtomicInteger ai = new AtomicInteger(0);
		final Thread[] threads = newThreadArray(); 
		
		
		for (int ithread = 0; ithread < threads.length; ithread++) 
		{  

			// Concurrently run in as many threads as CPUs  

			threads[ithread] = new Thread() 
			{  
                      
				{ setPriority(Thread.NORM_PRIORITY); }  

				public void run() 
				{  

					// Each thread processes a few items in the total list  
					// Each loop iteration within the run method  
					// has a unique 'i' number to work with  
					// and to use as index in the results array:  

					for (int i = ai.getAndIncrement(); i < point_list.size()/sub_sample;  
							i = ai.getAndIncrement()) 
					{  
						Random rand = new Random();

						int  n = rand.nextInt(point_list.size());
						if (sub_sample==1) n=i;
						MyPoint pt=point_list.get(n);
						int xx=-1;
						for (int x=pt.x-window_size; x<pt.x+window_size+1; x++)
						{
							xx++;
							int yy=-1;
							for (int y=pt.y-window_size; y<pt.y+window_size+1; y++)
							{
								yy++;
								if (x<0||x>=width||y<0||y>=height) continue;
								for (int c=0; c<channels; c++)
								{
									float [] dest=(float [])new_imp.getStack().getProcessor(c+i*channels+1).getPixels();
									int n_cur_slices=0;
									for (int z=pt.z-z_window_size; z<=pt.z+z_window_size+1; z++)
									{
										if (z<0||z>=slices) continue;
										n_cur_slices++;
										float [] pix=(float [])source_img.getStack().getProcessor(1+c+z*channels).getPixels();
										dest[xx+yy*(2*window_size+1)]+=pix[x+y*width];
									}
									dest[xx+yy*(2*window_size+1)]=dest[xx+yy*(2*window_size+1)]/n_cur_slices;
								}
								
							}
						}
						IJ.showProgress((double)i/(double)point_list.size()*sub_sample);
					}  
				}
			};  
		}  

    	startAndJoin(threads);  
	}
	
	private ArrayList <MyPoint> get_point_list(ImagePlus img)
	{
		ArrayList <MyPoint> rtnval=new ArrayList <MyPoint> ();
		for (int z=0; z<img.getStackSize(); z++)
		{
			byte [] pix=(byte [])img.getStack().getProcessor(z+1).getPixels();
			for (int x=0; x<width; x++)
			{
				for (int y=0; y<height; y++)
				{
					if (pix[x+y*width]!=0) 
					{
						rtnval.add(new MyPoint(x,y,z));
					}
				}
			}
		}
		return rtnval;
	}
	
	private ImagePlus[] createAdmissibleImageList () 
	{
		final int[] windowList = WindowManager.getIDList();
		//IJ.log(""+windowList[0]+"\n");
		final Stack stack = new Stack();
		for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) 
		{
			final ImagePlus imp = WindowManager.getImage(windowList[k]);
			//IJ.log(imp.getTitle()+"\n");
			//if ((imp != null) && (imp.getType() == imp.GRAY32)) 
			{
				//IJ.log("got one");
				stack.push(imp);
			}
		}
		//IJ.log("Stack size:  " + stack.size() + "\n");
		final ImagePlus[] admissibleImageList = new ImagePlus[stack.size()];
		int k = 0;
		while (!stack.isEmpty()) {
			admissibleImageList[k++] = (ImagePlus)stack.pop();
		}
		if (k==0 && (windowList != null && windowList.length > 0 )){
			IJ.error("No float images, convert to float and try again");
		}
		return(admissibleImageList);
	} /* end createAdmissibleImageList */

	private static Thread[] newThreadArray() {  
		int n_cpus = Runtime.getRuntime().availableProcessors();  
		IJ.log("CPUs:  "+n_cpus);
		return new Thread[n_cpus];  
	}  
	public static void startAndJoin(Thread[] threads)  
	{  
		for (int ithread = 0; ithread < threads.length; ++ithread)  
		{  
			threads[ithread].setPriority(Thread.NORM_PRIORITY);  
			threads[ithread].start();  
		}  

		try  
		{     
			for (int ithread = 0; ithread < threads.length; ++ithread)  
				threads[ithread].join();  
		} 
		catch (InterruptedException ie)  
		{  
			throw new RuntimeException(ie);  
		}  
	}  
}  

