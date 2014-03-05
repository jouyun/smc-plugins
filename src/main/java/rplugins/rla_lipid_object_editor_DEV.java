import ij.*;
import ij.plugin.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import java.awt.Frame;
import java.awt.Component;
import java.awt.Container;
import java.awt.Canvas;
import java.awt.Panel;
import java.awt.event.*;
import java.awt.Rectangle;
import java.util.*;
import java.lang.reflect.*;
import java.util.Arrays;
import ij.text.*;
import java.util.*;
import javax.swing.*;

public class rla_lipid_object_editor_DEV implements PlugInFilter, MouseListener, ActionListener, AdjustmentListener, KeyListener{
	JButton buttonAdd;
	JButton buttonDelete;
	MouseListener mouselisten;
	List<List<String>> llTableArray;
	TextPanel tpTable;
	ImagePlus imp;
	ImageWindow win;
	ImageCanvas canvas;
	String imageName;
	ImageStack origStack;
	int width;
	int height;
	int stackSize;
	int numChan;
	int numZ;

	int x;
	int y;
	int z;

	int[] X;
	int[] Y;
	int[] Z;
	int[] R;
	boolean change = true;
	
	public int setup(String arg, ImagePlus img) {
		IJ.register(RLAinterfaceTESTstandalone.class);
		return DOES_ALL+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		imp=WindowManager.getCurrentImage();
		int bitD = imp.getBitDepth();
		if(bitD<32){
			WaitForUserDialog wfud2 =new WaitForUserDialog("ERROR", "Image must be 32bit");
			wfud2.show();
		}else{
			IJ.run("32-bit");
			//ImageWindow win2 = img.getWindow();
			//doSetupFakeObjects();
			doGetTableData();
			getImageInfo();
	
			IJ.run("Make Composite", "display=Composite");
			createAddDeleteFrame();
			doListen();	//method inherited from RLAMouseListenerObject.java
			doUnhighlight();
			canvas.removeMouseListener(this);
			tpTable.removeMouseListener(this);
		}
		return;
	}
	
	public void createAddDeleteFrame(){
		//Create frame (top level container)
		JFrame frame = new JFrame("Object Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//create components
		JLabel label1 = new JLabel("Add or Delete ROIs here:");
		buttonAdd = new JButton("Add (F1)");
		buttonAdd.addActionListener(this);
		buttonAdd.addKeyListener(this);
		buttonDelete = new JButton("Delete (F2)");
		buttonDelete.addActionListener(this);
		buttonDelete.addKeyListener(this);
		
		//create panel to go inside frame, and add components
		JPanel contentPane = new JPanel();
		GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addComponent(label1)
			.addGroup(layout.createSequentialGroup()
				.addComponent(buttonAdd)
				.addComponent(buttonDelete))
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(label1)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(buttonAdd)
				.addComponent(buttonDelete))
		);
		
		frame.add(contentPane);
		frame.pack();
		frame.addKeyListener(this);
		frame.setVisible(true);
	}
	
	public void adjustmentValueChanged(AdjustmentEvent e){
		//this event only fires on ImageJ's "ScrollbarWithLabel" objects
		//if the slice changes, doUnhighlight()
		doUnhighlight();
		doUnselectRow();
		// Object obj = e.getSource();
		// IJ.log("clicked scroll:   "+obj.toString());
	}
	
	public void actionPerformed(ActionEvent e) {
		//ImagePlus imp=WindowManager.getCurrentImage();
		Roi currentROI = imp.getRoi();
		//IJ.log(e.getSource().toString());
		if(e.getSource() == buttonAdd){
			if(currentROI == null){
				IJ.log("need ROI first");
			}else{
				doAddROI(currentROI);
			}
		}
		if(e.getSource() == buttonDelete){
			//IJ.setForegroundColor(0, 0, 0);
			int index = tpTable.getSelectionStart();
			if(index<0){
				IJ.log("need table selection");
			}else{
				doDeleteROI(index);
			}
		}
    }
	
	public void keyTyped(KeyEvent e) {
		//
	}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		Roi currentROI = imp.getRoi();
		switch (keyCode){
			case KeyEvent.VK_F1:
				IJ.log("add key pressed");
				if(currentROI == null){
					IJ.log("need ROI first");
				}else{
					doAddROI(currentROI);
				}
				break;
			case KeyEvent.VK_F2:
				IJ.log("delete key pressed");
				int index = tpTable.getSelectionStart();
				if(index<0){
					IJ.log("need table selection");
				}else{
					doDeleteROI(index);
				}
				break;
		}
	}

	public void keyReleased(KeyEvent e) {
		//
	}
	
	public void mousePressed(MouseEvent e) {
		Component comp = e.getComponent();
		if(comp instanceof ImageCanvas){ //ImageCanvas was clicked
			int posX=e.getX();
			int posY=e.getY();
			// int offscreenX = canvas.offScreenX(x);
			// int offscreenY = canvas.offScreenY(y);
			//IJ.log("x= "+posX+"    y= "+posY);
			doReportMouse(posX, posY);
		}else{	//tpTable was clicked
			int index = tpTable.getSelectionStart();
			if(index<0){
				doUnhighlight();
			}else{
				doHighlightFromTableRow(index);
			}
		}
	}
	
	OvalRoi doHighlightFromTableRow(int index){
		List<String> row = llTableArray.get(index);
		int xx = Integer.parseInt(row.get(0));
		int yy = Integer.parseInt(row.get(1));
		int zz = Integer.parseInt(row.get(2));
		double rr = Double.parseDouble(row.get(3));
			// IJ.log("From llTable:   x = "+xx);
			// IJ.log("From llTable:   y = "+yy);
			// IJ.log("From llTable:   z = "+zz);
			// IJ.log("From llTable:   r = "+rr);
		int currentChan = imp.getChannel();
		int currentFrame = imp.getFrame();
		imp.setPosition(currentChan,zz,currentFrame);
		OvalRoi circle = doHighlight(xx,yy,rr);
		return circle;
	}
	
	void doDeleteROI(int index){
		//delete pixels in Ch2 image
		int posZ = imp.getSlice();
		int currentFrame = imp.getFrame();
		imp.setPosition(2,posZ,currentFrame);	//make sure pixels are deleted on channel 2, not actual image
		OvalRoi circle = doHighlightFromTableRow(index);
		imp.setRoi(circle);
		IJ.setForegroundColor(0, 0, 0);
		IJ.run("Reset...", "reset=[Locked Image]");
		IJ.run("Fill", "slice");
		doUnhighlight();
		imp.killRoi();
		//remove from tpTable
		tpTable.setSelection(index,index);
		tpTable.clearSelection();
		//remove from llTableArray
		llTableArray.remove(index);
	}
	
	void doAddROI(Roi currentROI){
		//get x, y, z, r for new object
		Rectangle bounds = currentROI.getBounds();
		int width = bounds.width;
		int height = bounds.height;
		int posX = bounds.x+(width/2);
		int posY = bounds.y+(height/2);
		int posZ = imp.getSlice();
		double minD;
		if(width<height){
			minD = width;
		}else{
			minD = height;
		}
		double R = minD/2;
		//create new object in tpTable and lltableArray
		String newLine = Integer.toString(posX)+"\t"+Integer.toString(posY)+"\t"+Integer.toString(posZ)+"\t"+Double.toString(R)+"\t0\t0\t0";
		tpTable.appendLine(newLine);
		String[] splitLine = newLine.split("\t");
		List<String> listLine = stringarray2list(splitLine);
		llTableArray.add(listLine);
		
		//create new object in image
		IJ.setForegroundColor(255, 255, 255);
		imp.setActivated();
		int currentFrame = imp.getFrame();
		imp.setPosition(2,posZ,currentFrame);	//make sure ROI is added on channel 2, not actual image
		IJ.run("Reset...", "reset=[Locked Image]");
		IJ.run("Fill", "slice");
		imp.killRoi();
		//IJ.log("Add");
		
		////must remove listener and re-add to catch new rows in table
		// removeTableListeners();
		// setTableListeners();
	}
		
	public void doListen(){
		win = imp.getWindow();
		win.addKeyListener(this);
		//win.addMouseListener(this);
		
		canvas = win.getCanvas();
			canvas.addKeyListener(this);
			//IJ.log("canvas: "+canvas.toString());
			Container cont= canvas.getParent();
			//IJ.log("parent1: "+cont.toString());
			Component[] comps = cont.getComponents();
			for(Component c : comps){
				if(c instanceof ScrollbarWithLabel){
					ScrollbarWithLabel swl= (ScrollbarWithLabel)c;
					swl.addAdjustmentListener(this);
				}
			}
			// Container testing2 = testing.getParent();
			// IJ.log("parent2: "+testing2.toString());
			// Container testing3 = testing2.getParent();
			// IJ.log("parent3: "+testing3.toString());
		canvas.addMouseListener(this);

		WaitForUserDialog wfud =new WaitForUserDialog("Object Editor", "Changes between the table and image will be synchronized.\n \nTo add:  Create a circular ROI, then click 'Add'\nTo delete:  Select object from image or table, then click 'Delete'\n \nClick 'OK' when you are finished and have saved your data.");
		wfud.show();
		canvas.removeMouseListener(this);
		win.removeMouseListener(this);
		tpTable.removeMouseListener(this);
		//doCloseTasks();
		return;
	}

	public void doReportMouse(int x, int y){
		this.x = canvas.offScreenX(x);
		this.y = canvas.offScreenY(y);
		z = imp.getSlice();
		// IJ.log("NEW  " + this.x + "   NEW  "+this.y + "current z = "+z);
		// IJ.log("x = "+x+"     y = "+y);
		doFindNearestObject();
	}
	
	public void doCloseTasks(){
		Container cont= canvas.getParent();
		Component[] comps = cont.getComponents();
		for(Component c : comps){
			if(c instanceof ScrollbarWithLabel){
				ScrollbarWithLabel swl= (ScrollbarWithLabel)c;
				swl.removeAdjustmentListener(this);
			}
		}
		win.removeKeyListener(this);
		canvas.removeKeyListener(this);
		canvas.removeMouseListener(this);
		removeTableListeners();
	}
	
	void doFindNearestObject(){
		int length = llTableArray.size();
		double r = -1;
		double distance=-1;
		int index=-1;
		for(int i = 0; i<length; i++){
			List<String> row = llTableArray.get(i);
			
			if(Integer.parseInt(row.get(2))==z){
				//IJ.log("Found at index:  " +i);
				int xdiff= x - Integer.parseInt(row.get(0));
				int ydiff= y - Integer.parseInt(row.get(1));
				r=Math.sqrt(ydiff*ydiff+xdiff*xdiff);	//---distance between the two points
				if(r<distance || distance==-1){	//---minimize distance to find nearest object to click
					distance = r;
					//IJ.log(""+distance);
					index = i;
				}
			}
		}
		if(distance >=0){
			List<String> row = llTableArray.get(index);
			double rr = Double.parseDouble(row.get(3));
			//IJ.log("rr = "+rr+"    dist = "+distance);
			if(rr>=distance){
				int xx = Integer.parseInt(row.get(0));
				int yy = Integer.parseInt(row.get(1));
				//int zz = Integer.parseInt(row.get(2));
				doHighlight(xx,yy,rr);
				doSelectRow(index);
			}else{
				doUnhighlight();
				doUnselectRow();
			}
		}else{
			doUnhighlight();
			doUnselectRow();
		}
	}
	
	void setTableListeners(){
		tpTable.addMouseListener(this);
		tpTable.addKeyListener(this);
	}
	void removeTableListeners(){
		tpTable.removeMouseListener(this);
		tpTable.removeKeyListener(this);
	}
	
	void doGetTableData(){
		//first get the table window
		Frame[] niframes=WindowManager.getNonImageWindows();
		String[] titles=new String[niframes.length];
		for(int i=0;i<niframes.length;i++){
			titles[i]=niframes[i].getTitle();
		}
		GenericDialog gd=new GenericDialog("Select Data Table");
		//gd.addTextAreas("",null,10,20);
		gd.addChoice("Table Window",titles,titles[0]);
		gd.showDialog(); if(gd.wasCanceled()){return;}
		//String input=gd.getNextText();
		int index=gd.getNextChoiceIndex();
		if(niframes[index] instanceof TextWindow){
			TextWindow tw=(TextWindow)niframes[index];
			tpTable=tw.getTextPanel();
			llTableArray = table2listtable(tpTable);
			setTableListeners();
		}else {
			IJ.showMessage("wrong window type");
		}
	}
	
	public static List<List<String>> table2listtable(TextPanel tp){	//from Jay's table_tools
        int nlines=tp.getLineCount();
        List<List<String>> retvals=new ArrayList<List<String>>();
        int longest=0;
        for(int i=0;i<nlines;i++){
            String line=tp.getLine(i);
            String[] temp2=split_string_tab(line);
            List<String> temp=stringarray2list(temp2);
            if(i==0){longest=temp.size();}
            for(int j=temp.size();j<longest;j++){
                temp.add("");
            }
            retvals.add(temp);
        }
        return retvals;
    }
	
	public static String[] split(String line,String delim){	//from Jay's table_tools
        return line.split(delim);
    }
	
	public static String[] split_string_tab(String line){	//from Jay's table_tools
        String temp;
        if(line.endsWith("\t")){
            temp=line.substring(0,line.length()-1);
        } else {
            temp=line.substring(0);
        }
        return split(temp,"\t");
    }
	
	public static List<String> stringarray2list(String[] arr){	//from Jay's table_tools
        List<String> temp=new ArrayList<String>();
        for(int i=0;i<arr.length;i++){
            temp.add(arr[i]);
        }
        return temp;
    }

	void getImageInfo(){
		imp=WindowManager.getCurrentImage();
		imageName = imp.getTitle();
		width=imp.getWidth(); height=imp.getHeight();
		origStack=imp.getStack();
		stackSize = origStack.getSize();
		numChan = imp.getNChannels();
		numZ = imp.getNSlices();
		imp.setActivated();
		IJ.run("32-bit");
		//IJ.log(imageName+"    size: "+stackSize+"    chan: "+numChan+"    z: "+numZ);
	}
	
	void doSelectRow(int index){
		tpTable.setSelection(index,index);
	}
	
	void doUnselectRow(){
		tpTable.resetSelection();
	}

	OvalRoi doHighlight(int x, int y, double r){
		OvalRoi circle = new OvalRoi((double)x-r,(double)y-r,(double)r*2, (double)r*2);
		imp.setOverlay(circle ,java.awt.Color.yellow,3,java.awt.Color.yellow);
		imp.killRoi();
		return circle;
	}

	void doUnhighlight(){
		//OvalRoi circle = new OvalRoi(0,0,0,0);
		imp.setHideOverlay(true);
		//imp.killRoi();
	}

	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}	
	public void mouseMoved(MouseEvent e) {}

}
