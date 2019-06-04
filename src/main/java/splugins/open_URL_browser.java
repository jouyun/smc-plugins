package splugins;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class open_URL_browser implements PlugIn {

	@Override
	public void run(String arg0) {
		GenericDialog gd =new GenericDialog("Choose URL");
		gd.addStringField("URL" , "");
		gd.showDialog();
		String urls=gd.getNextString();
		Desktop desktop=Desktop.getDesktop();
		try {
			desktop.browse(new URL(urls).toURI());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
