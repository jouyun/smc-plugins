package splugins;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import fiji.plugins.FJ_Options;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import imagescience.feature.Differentiator;
import imagescience.feature.Laplacian;
import imagescience.feature.Structure;
import imagescience.image.FloatImage;
import imagescience.image.Image;

public class Threaded_Derivatives implements PlugIn {

	@Override
	public void run(String arg0) {
		final ImagePlus img=WindowManager.getCurrentImage();
		final int [] x={1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4};
		final double [] scales={1, 2, 4, 8, 1, 2, 4, 8, 1, 2, 4, 8, 1, 2, 4, 8};
		//final int [] x={1, 1};
		//final double [] scales={1, 2};
		
		final double [] sigma_scale_array={1, 2, 4, 8, 16, 32, 64, 1, 2, 4, 8, 16, 32,64};
		final double [] int_scale_array={1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3,3};
		//final double [] sigma_scale_array={1, 2};
		//final double [] int_scale_array={1, 1};
		
		final double [] laplacian_scales={1, 2, 4, 8, 16, 32, 64};
		//final double [] laplacian_scales={1};
		
		final int [] z=new int[x.length];
		final AtomicInteger myi=new AtomicInteger(0);
		final int number_criteria=3;
		final ImagePlus[] my_results=new ImagePlus[number_criteria];
		final Thread[] threads=newThreadArray();
		//my_results[0]=execute_structures(img, sigma_scale_array, int_scale_array);
		//my_results[1]=execute_derivatives(img, x, x, z, scales);
		//my_results[2]=execute_laplacians(img, laplacian_scales);
		//38
		for (int ithread = 0; ithread < threads.length; ithread++) 
		{  
			

			// Concurrently run in as many threads as CPUs  

			threads[ithread] = new Thread() 
			{  
                      
				{ setPriority(Thread.NORM_PRIORITY); }  

				public void run() 
				{  
					for (int i = myi.getAndIncrement(); i <= number_criteria-1;  
							i = myi.getAndIncrement()) 
					{
						IJ.log("Am in counter: "+i);
						if (i==0) my_results[0]=execute_structures(img, sigma_scale_array, int_scale_array);
						if (i==1) my_results[1]=execute_derivatives(img, x, x, z, scales);
						if (i==2) my_results[2]=execute_laplacians(img, laplacian_scales);
					}
				}
			};
		}
		startAndJoin(threads);  
		//ImagePlus newimg=execute_derivatives(img, x, x, z, scales);
		//ImagePlus newimg=execute_structures(img, sigma_scale_array, int_scale_array);
		ImageStack final_stack = new ImageStack(my_results[0].getWidth(), my_results[0].getHeight());
		for (int i=0; i<number_criteria; i++)
		{
			for (int j=0; j<my_results[i].getStackSize(); j++)
			{
				final_stack.addSlice(my_results[i].getStack().getSliceLabel(j+1), my_results[i].getStack().getProcessor(j+1));
			}
		}
		new ImagePlus("Features", final_stack).show();
		/*my_results[1].show();
		my_results[2].show();
		my_results[0].show();*/

	}
	
	public static ImagePlus execute_derivatives(final ImagePlus input, final int [] xs,final int [] ys, final int [] zs, final double [] scales)
	{
		final AtomicInteger ai = new AtomicInteger(0);  
		final int num_entries=xs.length;

		// store all result images here  
		final ImageProcessor[] results = new ImageProcessor[num_entries];  

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

					for (int i = ai.getAndIncrement(); i <= num_entries;  
							i = ai.getAndIncrement()) 
					{  
						// 'i' is the lower bound of the threshold window  
						ImageProcessor ip = input.getProcessor().duplicate();

						ImagePlus imp = new ImagePlus("Derivatives " + xs[i]+","+ys[i]+","+zs[i]+","+scales[i], ip);
						
						Image img = Image.wrap(imp);
						Image newimg = new FloatImage(img);
						Differentiator diff = new Differentiator();
						diff.run(newimg,scales[i],xs[i],ys[i],zs[i]);
						results[i] = newimg.imageplus().getProcessor();  
					}  
				}
			};  
		}  

    	startAndJoin(threads);  

    	// 	now the results array is full. Just show them in a stack:  
    	final ImageStack stack = new ImageStack(input.getWidth(),input.getHeight());  
    	for (int i=0; i< results.length; i++) { 
    		stack.addSlice("Derivative_"+xs[i]+"_"+ys[i]+"_"+zs[i]+"_"+scales[i], results[i]);  
    	}  
    	return (new ImagePlus("Derivatives", stack));  
	}  

	public static ImagePlus execute_structures(final ImagePlus input, final double [] sscales, final double [] iscales)
	{
		final AtomicInteger ai = new AtomicInteger(0);  
		final int num_entries=sscales.length;

		// store all result images here  
		final ImageProcessor[] results = new ImageProcessor[num_entries*2];  

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

					for (int i = ai.getAndIncrement(); i <= num_entries;  
							i = ai.getAndIncrement()) 
					{  
						// 'i' is the lower bound of the threshold window  
						ImageProcessor ip = input.getProcessor().duplicate();
						ImagePlus imp = new ImagePlus("Structures " + sscales[i]+","+iscales[i], ip);
						
						Image img = Image.wrap(imp);
						Image newimg = new FloatImage(img);
						
						final Structure structure = new Structure();
						
						final Vector<Image> eigenimages = structure.run(new FloatImage(newimg),sscales[i],iscales[i]);
						
						results[i*2]=eigenimages.get(0).imageplus().getProcessor();
						if (eigenimages.size()>2) results[i*2+1]=eigenimages.get(2).imageplus().getProcessor();
						else results[i*2+1]=eigenimages.get(1).imageplus().getProcessor();

					}  
				}
			};  
		}  

    	startAndJoin(threads);  

    	// 	now the results array is full. Just show them in a stack:  
    	final ImageStack stack = new ImageStack(input.getWidth(),input.getHeight());  
    	for (int i=0; i< results.length; i++) { 
    		stack.addSlice(""+i+"Structure_"+sscales[i/2]+"_"+iscales[i/2], results[i]); 
    		//stack.addSlice(""+i, results[i]);
    		IJ.log(""+results.length);
    	}  

    	return (new ImagePlus("Structures", stack));  
	}  
	
	public static ImagePlus execute_laplacians(final ImagePlus input, final double [] scales)
	{
		final AtomicInteger ai = new AtomicInteger(0);  
		final int num_entries=scales.length;

		// store all result images here  
		final ImageProcessor[] results = new ImageProcessor[num_entries];  

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

					for (int i = ai.getAndIncrement(); i <= num_entries;  
							i = ai.getAndIncrement()) 
					{  
						// 'i' is the lower bound of the threshold window  
						ImageProcessor ip = input.getProcessor().duplicate();

						ImagePlus imp = new ImagePlus("Laplcaians " + scales[i], ip);
						
						Image img = Image.wrap(imp);
						Image newimg = new FloatImage(img);
						Laplacian lap=new Laplacian();
						lap.run(newimg,  scales[i]);
						results[i] = newimg.imageplus().getProcessor();  
					}  
				}
			};  
		}  

    	startAndJoin(threads);  

    	// 	now the results array is full. Just show them in a stack:  
    	final ImageStack stack = new ImageStack(input.getWidth(),input.getHeight());  
    	for (int i=0; i< results.length; i++) { 
    		stack.addSlice("Laplacian_"+scales[i], results[i]);  
    	}  
    	return (new ImagePlus("Laplacians", stack));  
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
