package splugins;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Manual_Alignment implements PlugIn, KeyListener, ImageListener {
	float [] original_pix;
	float [] last_pix;
	float [] base_pix;
	ImageWindow win;
    ImageCanvas canvas;
    ImagePlus myimg;
    int width, height;
    double rotation_step, total_rotation, xy_step, total_x, total_y;
    boolean adjust_mode;

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
        adjust_mode=false;
	}

	public void init_frame()
	{
		ImageProcessor ipf=myimg.getProcessor();
		ImageProcessor ipi=myimg.getStack().getProcessor(myimg.getCurrentSlice()-1);
		float [] pixf=(float []) ipf.getPixels();
		base_pix=(float []) ipi.getPixels();
		original_pix=new float [width*height];
		last_pix=new float [width*height];
		total_x=0;
		total_y=0;
		total_rotation=0;
		for (int i=0; i<width*height; i++)
		{
			original_pix[i]=pixf[i];
			last_pix[i]=pixf[i];
			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
		}
		
	}
	public void init_reverse_frame()
	{
		ImageProcessor ipf=myimg.getProcessor();
		ImageProcessor ipi=myimg.getStack().getProcessor(myimg.getCurrentSlice()+1);
		float [] pixf=(float []) ipf.getPixels();
		base_pix=(float []) ipi.getPixels();
		original_pix=new float [width*height];
		last_pix=new float [width*height];
		total_x=0;
		total_y=0;
		total_rotation=0;
		for (int i=0; i<width*height; i++)
		{
			original_pix[i]=pixf[i];
			last_pix[i]=pixf[i];
			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
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
        boolean shift_down=e.isShiftDown();
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
    		adjust_mode=true;
    		return;
        }
        if (keyChar=='g') 
        {
        	init_reverse_frame();
    		myimg.updateAndDraw();
    		adjust_mode=true;
    		return;
        }

        if (!adjust_mode) return;
        
		ImageProcessor ipf=myimg.getProcessor();
		float [] pixf=(float []) ipf.getPixels();
        for (int i=0; i<width*height; i++)
        {
        	pixf[i]=last_pix[i];
        }

        if (keyChar=='z'||keyChar=='Z') 
        {
        	myimg.getProcessor().setInterpolationMethod(2);
        	if (shift_down) total_rotation+=(-rotation_step*.1);
        	else total_rotation+=-rotation_step;
        	for (int i=0; i<width*height; i++)
            {
            	pixf[i]=original_pix[i];
            }
        	myimg.getProcessor().rotate(total_rotation);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='v'||keyChar=='V') 
        {
        	myimg.getProcessor().setInterpolationMethod(2);
        	if (shift_down) total_rotation+=(.1*rotation_step);
        	else total_rotation+=rotation_step;
        	for (int i=0; i<width*height; i++)
            {
            	pixf[i]=original_pix[i];
            }
        	myimg.getProcessor().rotate(total_rotation);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='c') 
        {
        	myimg.getProcessor().setInterpolationMethod(2);
        	total_rotation+=90;
        	for (int i=0; i<width*height; i++)
            {
            	pixf[i]=original_pix[i];
            }
        	myimg.getProcessor().rotate(total_rotation);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='x') 
        {
        	myimg.getProcessor().setInterpolationMethod(2);
        	total_rotation+=180;
        	for (int i=0; i<width*height; i++)
            {
            	pixf[i]=original_pix[i];
            }
        	myimg.getProcessor().rotate(total_rotation);
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }

        if (keyChar=='s'||keyChar=='S') 
        {
        	if (shift_down) myimg.getProcessor().translate(0,10*xy_step);
        	else myimg.getProcessor().translate(0,xy_step);
        	if (shift_down) total_y+=10*xy_step;
        	else total_y+=xy_step;
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='w'||keyChar=='W') 
        {
        	if (shift_down) myimg.getProcessor().translate(0,-xy_step*10); 
        	else myimg.getProcessor().translate(0,-xy_step);
        	if (shift_down) total_y-=10*xy_step;
        	else total_y-=xy_step;
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='d'||keyChar=='D') 
        {
        	if (shift_down) myimg.getProcessor().translate(10*xy_step, 0);
        	else myimg.getProcessor().translate(xy_step, 0);
        	if (shift_down) total_x+=(10*xy_step);
        	else total_x+=xy_step;
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='a'||keyChar=='A') 
        {
        	if (shift_down) myimg.getProcessor().translate(-xy_step*10,0);
        	else myimg.getProcessor().translate(-xy_step,0);
        	if (shift_down) total_x=total_x-(10*xy_step);
        	else total_x=total_x-xy_step;
        	for (int i=0; i<width*height; i++)
    		{
    			last_pix[i]=pixf[i];
    			pixf[i]=(pixf[i]+base_pix[i])/2.0f;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='q') 
        {
        	for (int i=0; i<width*height; i++)
    		{
    			pixf[i]=last_pix[i];
    		}
        	adjust_mode=false;
    		myimg.updateAndDraw();
    		IJ.log("Slice "+myimg.getCurrentSlice()+" was rotated "+(-total_rotation)+" shifted "+ total_x+"pixels in x and "+(-total_y)+"pixels in y");
    		total_rotation=0;
    		total_x=0;
    		total_y=0;
        }
        if (keyChar=='r') 
        {
        	total_rotation=0;
        	total_x=0;
        	total_y=0;
        	for (int i=0; i<width*height; i++)
    		{
        		last_pix[i]=original_pix[i];
    			pixf[i]=(original_pix[i]+base_pix[i])/2;
    		}
    		myimg.updateAndDraw();
        }
        if (keyChar=='h') 
        {
        	GenericDialog dlg=new GenericDialog("Help Menu");
        	dlg.addHelp("r does reset");
        	//dlg.show();
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
