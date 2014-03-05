import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class BatchDeleteSlice implements PlugIn {

	public void run(files) {
		for(File f : files){
			String current =  f.getPath();
			if (!f.getPath().endsWith("/") & f.getPath().endsWith(".tif")){
				ij.IJ.open(Path);
				String kernel = "first="+logListBegin+" last="+logListEnd+" increment=1";
				IJ.run("Slice Remover", kernel);
			}
		close();
		}
	}

}
