package splugins;

import ij.*;  
import ij.plugin.PlugIn;  
import ij.process.*;  
import ij.io.Opener;  
import imagescience.feature.Differentiator;
import imagescience.image.Aspects;
import imagescience.image.Dimensions;
import imagescience.image.FloatImage;
import imagescience.image.Image;

import java.util.concurrent.atomic.AtomicInteger;

public class MultiThread_Test implements PlugIn {

	@Override

	public void run(String arg) {  
	  
		//final ImagePlus dot_blot = new Opener().openURL("http://rsb.info.nih.gov/ij/images/blobs.gif");
		final ImagePlus dot_blot=WindowManager.getCurrentImage();
		
		
		

		final int starting_threshold = 1;  
		final int ending_threshold = 35;  
		final int n_tests = ending_threshold - starting_threshold + 1;  
		final AtomicInteger ai = new AtomicInteger(starting_threshold);  

		// store all result images here  
		final ImageProcessor[] results = new ImageProcessor[n_tests];  

		final Thread[] threads = newThreadArray(); 
		
		final int [] scale_array={1, 2, 4, 8, 16,1, 2, 4, 8, 16,1, 2, 4, 8, 16,1, 2, 4, 8, 16,1, 2, 4, 8, 16,1, 2, 4, 8, 16,1, 2, 4, 8, 16,};
		final int [] order_array={2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8};
		
		final int [] sigma_scale_array={1, 2, 4, 8, 16, 32, 1, 2, 4, 8, 16, 32};
		final int [] int_scale_array={1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3};
		
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

					for (int i = ai.getAndIncrement(); i <= ending_threshold;  
							i = ai.getAndIncrement()) 
					{  
						// 'i' is the lower bound of the threshold window  
						ImageProcessor ip = dot_blot.getProcessor().duplicate();

						ImagePlus imp = new ImagePlus("Threshold " + i, ip);
						
						Image img = Image.wrap(imp);
						Image newimg = new FloatImage(img);
						Differentiator diff = new Differentiator();
						diff.run(newimg,scale_array[i-starting_threshold],order_array[i-starting_threshold],order_array[i-starting_threshold],0);
						//newimg.imageplus().show();
						results[i-starting_threshold] = newimg.imageplus().getProcessor();  
						//results[i-starting_threshold] = imp.getProcessor();
						IJ.log("Thread " + (i-starting_threshold));
					}  
				}
			};  
		}  

    	startAndJoin(threads);  

    	// 	now the results array is full. Just show them in a stack:  
    	final ImageStack stack = new ImageStack(dot_blot.getWidth(),  
                                            	dot_blot.getHeight());  
    	for (int i=0; i< results.length; i++) { 
    		IJ.log(""+i+" "+results[i].getWidth());
    		stack.addSlice(Integer.toString(i), results[i]);  
    	}  

    	new ImagePlus("Results", stack).show();  
	}  

	/** Create a Thread[] array as large as the number of processors available. 
	 * 	From Stephan Preibisch's Multithreading.java class. See: 
	 * 	http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
	*/  
	private static Thread[] newThreadArray() {  
		int n_cpus = Runtime.getRuntime().availableProcessors();  
		IJ.log("CPUs:  "+n_cpus);
		return new Thread[n_cpus];  
	}  

	/** Start all given threads and wait on each of them until all are done. 
	 * From Stephan Preibisch's Multithreading.java class. See: 
	 * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
	 */  
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