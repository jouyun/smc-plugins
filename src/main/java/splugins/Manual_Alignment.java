package splugins;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Manual_Alignment implements PlugIn, KeyListener, ImageListener {
	float [] original_pix;
	float [] last_pix;
	ImageWindow win;
    ImageCanvas canvas;
    ImagePlus myimg;
    int width, height;
    double rotation_step, total_rotation, xy_step, total_x, total_y;

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus img=WindowManager.getCurrentImage();
		width=img.getWidth();
		height=img.getHeight();
		/*ImageProcessor ipf=img.getProcessor();
		ImageProcessor ipi=img.getStack().getProcessor(img.getCurrentSlice()-1);
		float [] pixf=(float []) ipf.getPixels();
		float [] pixi=(float []) ipi.getPixels();
		original_pix=new float [width*height];
		last_pix=new float [width*height];
		for (int i=0; i<width*height; i++)
		{
			original_pix[i]=pixf[i];
			last_pix[i]=pixf[i];
			pixf[i]=(pixf[i]+pixi[i])/2.0f;
		}
		img.updateAndDraw();
		*/
		myimg = IJ.getImage();
        win = myimg.getWindow();
        canvas = win.getCanvas();
        win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        win.addKeyListener(this);
        canvas.addKeyListener(this);
        ImagePlus.addImageListener(this);
        rotation_step=1;
        xy_step=1;
	}

	public void init_frame()
	{
		ImageProcessor ipf=myimg.getProcessor();
		ImageProcessor ipi=myimg.getStack().getProcessor(myimg.getCurrentSlice()-1);
		float [] pixf=(float []) ipf.getPixels();
		float [] pixi=(float []) ipi.getPixels();
		original_pix=new float [width*height];
		last_pix=new float [width*height];
		
		for (int i=0; i<width*height; i++)
		{
			original_pix[i]=pixf[i];
			last_pix[i]=pixf[i];
			pixf[i]=(pixf[i]+pixi[i])/2.0f;
		}
		
	}
	public void cleanup(){
		if (win!=null)
		{
			win.removeKeyListener(this);
		}
		
        if (canvas!=null)
        {
        	canvas.removeKeyListener(this);
        }
        
        
        ImagePlus.removeImageListener(this);

	}
	@Override
	public void imageClosed(ImagePlus arg0) {
		// TODO Auto-generated method stub
		cleanup();
	}

	@Override
	public void imageOpened(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageUpdated(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        //IJ.log("char: "+keyChar);
        if (keyChar=='n') 
        {
        	myimg.setSlice(myimg.getCurrentSlice()+1);
        	myimg.updateAndDraw();
        	return;
        }
        if (keyChar=='p') 
        {
        	myimg.setSlice(myimg.getCurrentSlice()-1);
        	myimg.updateAndDraw();
        	return;
        }
        if (keyChar=='f') 
        {
        	init_frame();
    		myimg.updateAndDraw();
    		return;
        }

        
        
		ImageProcessor ipf=myimg.getProcessor();
		ImageProcessor ipi=myimg.getStack().getProcessor(myimg.getCurrentSlice()-1);
		float [] pixf=(float []) ipf.getPixels();
		float [] pixi=(float []) ipi.getPixels();
        for (int i=0; i<width*height; i++)
        {
        	pixf[i]=last_pix[i];
        }

        if (keyChar=='z') 
        {
        	myimg.getProcessor().rotate(-rotation_step);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+pixi[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='v') 
        {
        	myimg.getProcessor().rotate(rotation_step);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+pixi[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='c') 
        {
        	myimg.getProcessor().rotate(90);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+pixi[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='x') 
        {
        	myimg.getProcessor().rotate(180);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+pixi[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }

        if (keyChar=='s') 
        {
        	myimg.getProcessor().translate(0,xy_step);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+pixi[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='w') 
        {
        	myimg.getProcessor().translate(0,-xy_step);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+pixi[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='f') 
        {
        	myimg.getProcessor().translate(xy_step, 0);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+pixi[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='a') 
        {
        	myimg.getProcessor().translate(-xy_step,0);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+pixi[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='q') 
        {
        	for (int i=0; i<width*height; i++)
    		{
    			pixf[i]=last_pix[i];
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='r') 
        {
        	for (int i=0; i<width*height; i++)
    		{
    			pixf[i]=original_pix[i];
    		}
    		myimg.updateAndDraw();
        }

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
