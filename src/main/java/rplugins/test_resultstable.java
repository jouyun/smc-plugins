import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.text.*;
import ij.io.*;

public class test_resultstable implements PlugIn {

	public void run(String arg) {
		ImagePlus imp=WindowManager.getCurrentImage();
		FileInfo FI = imp.getFileInfo();
		String dir = FI.directory;
		IJ.log(""+dir);
		FileSaver FS = new FileSaver(imp);
		IJ.log(""+FS.getDescriptionString());
		
		TextWindow myResultsWin = new TextWindow("My Results","ONE\tTWO\tTHREE",null,900,700);
		TextPanel myResultsPanel = myResultsWin.getTextPanel();
		myResultsPanel.updateColumnHeadings("ONE\tTWO\tTHREE");
		myResultsPanel.appendLine("1\t2\t3");
		myResultsPanel.appendLine("4\t5\t6");
		myResultsPanel.appendLine("7\t8\t9");
		// IJ.log(myResultsPanel.getLine(0));
		// IJ.log(myResultsPanel.getLine(1));
		// IJ.log(myResultsPanel.getLine(2));
		////IJ.log(myResultsPanel.getLine(3));
		int numLines = myResultsPanel.getLineCount();
		for(int i = 0; i<6;i++){
			if(i<numLines){
				String temp = myResultsPanel.getLine(i);
				myResultsPanel.setLine(i,temp+"\t"+i);
			}else{
				String temp = "\t\t\t"+i;
				myResultsPanel.appendLine(temp+"\t"+i);
			}
		}
		myResultsPanel.updateColumnHeadings("ONE\tTWO\tTHREE\tParams");
		myResultsPanel.updateDisplay();
	}
}