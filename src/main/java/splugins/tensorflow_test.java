package splugins;

import com.google.common.io.ByteStreams;
import ij.process.ByteProcessor;
import org.tensorflow.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Random;
import ij.io.OpenDialog;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;


public class tensorflow_test implements PlugIn {

	ImagePlus imp, newimg;

    String dir = "/fast/smc/Fiji.app/tensorflow";
    String pbfile = "frozen_RNA.pb";
    int output_channels=1;
    float input_scaler=(float) 4096.0;

    float[][][] pixels;
    
    int width, height, channels, frames, slices;

    @Override
    public void run(String arg0)  {

        //TestTensorFlow t = new TestTensorFlow();
    	OpenDialog dlg=new OpenDialog("Select tensorflow pb");
        imp=WindowManager.getCurrentImage();
        
        GenericDialog gd=new GenericDialog("Output size");
        gd.addNumericField("Number of channels for output image: ", 1, 0);
        gd.addNumericField("Scale input by: ", 4096, 1);
        gd.showDialog();
        output_channels=(int)gd.getNextNumber();
        input_scaler=(float)gd.getNextNumber();
        
        width=imp.getWidth();
        height=imp.getHeight();
        slices=imp.getNSlices();
        frames=imp.getNFrames();
        channels=imp.getNChannels();
        newimg=NewImage.createFloatImage("Img", width, height, frames*output_channels, NewImage.FILL_BLACK);
        
        
        
        int batch_size=200;
        int current_position=0;
        
        for (int ff=0; ff<frames; ff+=batch_size)
        {
        	IJ.showProgress((float)ff/(float)frames);
        	int initial_frame=ff;
        	int number_in_batch=Math.min(frames-initial_frame, batch_size);

        	float[][][][] image = new float[number_in_batch][width][height][channels];
        	float [][][][] dec=new float[number_in_batch][width][height][output_channels];
        	
            for (int f=0; f<number_in_batch; f++)
            {
            	for (int c=0; c<channels; c++)
            	{
            		float [] current_slice=(float [])imp.getStack().getProcessor((ff+f)*channels*slices+c+1).getPixels();
            		for (int y=0; y<height; y++)
            		{
            			for (int x=0; x<width; x++)
            			{
            				image[f][x][y][c]=(float) ((float)current_slice[x+y*width]/input_scaler);
            			}
            		}
            	}
            }
            
            Tensor<Float> tx = Tensors.create(image);
            Tensor<Float> ty;

            byte[] graphBytes = readGraph(dlg.getPath());
            System.out.println("Bytes " + graphBytes.length);
            Graph g = new Graph();
            Session sess = new Session(g);

            g.importGraphDef(graphBytes);

            ty = sess.runner()
                    .feed("x", tx)
                    .fetch("probabilities")
                    .run()
                    .get(0)
                    .expect(Float.class);

            ty.copyTo(dec);
            sess.close();
            
            for (int f=0; f<number_in_batch; f++)
            {
            	for (int c=0; c<output_channels; c++)
            	{
	            	float [] current_slice=(float [])newimg.getStack().getProcessor((ff+f)*output_channels+c+1).getPixels();
	        		for (int y=0; y<height; y++)
	        		{
	        			for (int x=0; x<width; x++)
	        			{
	        				current_slice[x+y*width]=dec[f][x][y][c];
	        			}
	        		}
            	}
            }
        }
        newimg.setDimensions(output_channels, 1, frames);
        newimg.setOpenAsHyperStack(true);
        newimg.show();
    	newimg.updateAndDraw();
    }

    private byte[] readGraph(String path) {

        try (InputStream is = new FileInputStream(path)) {
            return ByteStreams.toByteArray(is);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*private void showDecImage(float[][][][] dec) {

    	
    	ImagePlus 
    	
    	for (int f=0; f<frames; f++)
        {
    		float [] current_slice=(float [])newimg.getStack().getProcessor(f+1).getPixels();
    		for (int y=0; y<height; y++)
    		{
    			for (int x=0; x<width; x++)
    			{
    				current_slice[x+y*width]=dec[f][x][y][0];
    			}
    		}
        }
    	
    }*/

	
	
}
