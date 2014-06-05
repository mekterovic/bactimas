package bactimas.util;

import ij.ImageJ;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import bactimas.gui.ControlPanel;

public class S {

	private static boolean DEBUG = true;

	static int PANEL_BACKGROUND_RED_TO = 210 // 190
			,
			PANEL_BACKGROUND_RED_FROM = 255, PANEL_BACKGROUND_GREEN_TO = 240 // 230
			, PANEL_BACKGROUND_GREEN_FROM = 255, PANEL_BACKGROUND_BLUE = 255;

	static int TOOLBAR_BACKGROUND_RED_TO = 150 // 190
			,
			TOOLBAR_BACKGROUND_RED_FROM = 200,
			TOOLBAR_BACKGROUND_GREEN_TO = 200 // 230
			,
			TOOLBAR_BACKGROUND_GREEN_FROM = 225,
			TOOLBAR_BACKGROUND_BLUE = 255;

	private static final String  DATACONF_URL_EMPTY = "http://homer.zpr.fer.hr/bactimas_releases/current/dataconf.zip";

	private static final String DATACONF_URL_DEMO = "http://homer.zpr.fer.hr/bactimas_releases/current/dataconfdemo.zip";

	public static void checkInitConfAndDataFolder () {
		File f = new File(getDbPropertiesAbsFileName());
		if(f.exists() && !f.isDirectory()) return;
		
		if (JOptionPane.showConfirmDialog(
			    null,
			    "We've detected that you don't have the conf and data folder."
			    +"\nYou're probably runnning Bactimas for the first time (as an Icy plugin)."
			    +"\nWould you like us to download and setup the conf and data folder?",
			    "Init config and data folder",
			    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			String url;
			if (JOptionPane.showConfirmDialog(
				    null,
				    "Would you like to also download the 'demo' project."
				    +"\n**Note: that's ~ 172MB download."
				    +"\nIf you choose 'No' we'll setup an empty database."
				    +"\nIf you choose 'Yes' be prepared to wait a minute or two until the file (172MB) downloads.",
				    "Init config and data folder",
				    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				url = DATACONF_URL_DEMO;
			} else {
				url = DATACONF_URL_EMPTY;
			}			
			
			URL downloadUrl;
			try {
				
				downloadUrl = new URL(url);
				unpackArchive(downloadUrl, new File(getBactimasFolder()));
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}	
	}
	
	/**
	 * Unpack an archive from a URL
	 * 
	 * @param url
	 * @param targetDir
	 * @return the file to the url
	 * @throws IOException
	 */
	public static File unpackArchive(URL url, File targetDir)
			throws IOException {
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		InputStream in = new BufferedInputStream(url.openStream(), 1024);
		// make sure we get the actual file
		File zip = File.createTempFile("arc", ".zip", targetDir);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(zip));
		copyInputStream(in, out);
		out.close();
		return unpackArchive(zip, targetDir);
	}

	/**
	 * Unpack a zip file
	 * 
	 * @param theFile
	 * @param targetDir
	 * @return the file
	 * @throws IOException
	 */
	@SuppressWarnings({ "resource", "rawtypes" })
	public static File unpackArchive(File theFile, File targetDir)
			throws IOException {
		if (!theFile.exists()) {
			throw new IOException(theFile.getAbsolutePath() + " does not exist");
		}
		if (!buildDirectory(targetDir)) {
			throw new IOException("Could not create directory: " + targetDir);
		}
		ZipFile zipFile = new ZipFile(theFile);
		for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			File file = new File(targetDir, File.separator + entry.getName());
			if (!buildDirectory(file.getParentFile())) {
				throw new IOException("Could not create directory: "
						+ file.getParentFile());
			}
			if (!entry.isDirectory()) {
				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(file)));
			} else {
				if (!buildDirectory(file)) {
					throw new IOException("Could not create directory: " + file);
				}
			}
		}
		zipFile.close();
		return theFile;
	}

	public static void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len >= 0) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		in.close();
		out.close();
	}

	public static boolean buildDirectory(File file) {
		return file.exists() || file.mkdirs();
	}
	
	
	public static String getAbsFromRelFolder(String relativePath) {
		String appFolder = getAppFolder();
		String rv;
		if (       appFolder.startsWith(relativePath) 
				|| relativePath.startsWith("/") 
				|| (relativePath.length() > 1 && relativePath.charAt(1) == ':'))
			rv = relativePath;  // already is absolute path
		else
			rv = getBactimasFolder() + File.separator + relativePath;
			
		return fixPath(rv);
	}
	
	public static String getAppFolder() {
		return new java.io.File("").getAbsolutePath();
	}
	public static String getBactimasFolder() {
		if (ControlPanel.isPlugin())
			return getAppFolder() 
					+ File.separator + "plugins" 
					+ File.separator + "mekterovic"
					+ File.separator + "bactimas";		
		else
			return getAppFolder();
	}	
	public static String getConfigFolder() {
		return getBactimasFolder() + File.separator + "conf";
	}	
	public static String getDbPropertiesAbsFileName() {
		return getConfigFolder() + File.separator + "db.properties";
	}	
	public static String getAllAlgorithmsAbsFileName() {
		return getConfigFolder() + File.separator + "all.algorithms";
	}
		
	public static String fixPath(String path) {
		if (File.separator != "\\") {
			path = path.replace("\\", File.separator);
		}
		if (File.separator != "/") {
			path = path.replace("/", File.separator);
		}
		return path;
	}
	
    public static String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }
    
    public static int parseInt (String s, String caption, int defaultFallback) {
    	int i;
    	try {
			i = Integer.parseInt(s);	
		} catch (Exception e1) {
			i = defaultFallback;
			ControlPanel.addStatusMessage("Couldn't parse " + caption + " = " + s + ", reverting to default = 5.");
		}
    	return i;
    }
    public static String[] concat(String[]... arrays) {
        int lengh = 0;
        for (String[] array : arrays) {
            lengh += array.length;
        }
        String[] result = new String[lengh];
        int pos = 0;
        for (String[] array : arrays) {
            for (String element : array) {
                result[pos] = element;
                pos++;
            }
        }
        return result;
    }    
	public static String hashtableToString(@SuppressWarnings("rawtypes") Hashtable ht) {
		StringBuffer sb = new StringBuffer("");
		for (Object key : ht.keySet()) {
			sb.append("[" + key.toString() + " = " + ht.get(key).toString()
					+ "] ");
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unused")
	private static String byteArrayToString(byte[] blob){
		StringBuffer sb = new StringBuffer("");
		for (int i=0; i< blob.length; ++i) {
			sb.append(blob[i]);
		}
		return sb.toString();
	}
	public static File getAppFolder(@SuppressWarnings("rawtypes") Class c) {
		String applicationDir = c.getProtectionDomain().getCodeSource()
				.getLocation().getPath();

		if (applicationDir.endsWith(".exe")) {
			applicationDir = new File(applicationDir).getParent();
		} else {
			// Add the path to the class files
			applicationDir += c.getName().replace('.', File.separatorChar);

			// Step one level up as we are only interested in the
			// directory containing the class files
			applicationDir = new File(applicationDir).getParent();
		}
		return new File(applicationDir);
	}

	public static String getMacroFullPath(String macroName) {
		return fixPath( 
				getAppFolder(ImageJ.class).getParent() + File.separator	+ "macros" + File.separator + macroName
				);
	}

	public static boolean saveTextFile(String path, String content) {
		try {

			File newTextFile = new File(path);

			FileWriter fw = new FileWriter(newTextFile);
			fw.write(content);
			fw.close();

		} catch (IOException iox) {
			// do stuff with exception
			iox.printStackTrace();
			return false;
		}
		return true;

	}

	public S() {
	}

	public static final void out(Object o) {
		out(o.toString());
	}

	public static final void out(String txtToPrintOut) {
		if (DEBUG)
			System.out.println(txtToPrintOut);
	}

	public static final void out2(String txtToPrintOut) {
		if (DEBUG)
			System.out.print(txtToPrintOut);
	}

	// public static final void assert(Class c, String txtToPrintOut){
	// System.out.println("!! ASSERT: " + txtToPrintOut + " IN CLASS " +
	// c.getName());
	// }
	public static String formatNumber(double num, int decPlaces) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(','); 
		String format = "0.";
		while (decPlaces-- > 0) format += "0";
		DecimalFormat df = new DecimalFormat(format, otherSymbols);
		return df.format(num);		
		
	}
	public static void reportError(Exception e) {
		JOptionPane.showMessageDialog(null, "Error:" + e.getMessage(),
				"Sorry, error occured", JOptionPane.ERROR_MESSAGE);
	}

	public final static String rep(char c, int count) {
		char[] s = new char[count];
		for (int i = 0; i < count; i++) {
			s[i] = c;
		}
		return new String(s).intern();
	}

	public static ImageIcon getAppIcon(String filename) {
		ImageIcon icon = null;
		URL url = ClassLoader.getSystemResource("images/" + filename);

		if (url != null) {
			icon = new ImageIcon(url);
		}
		return icon;

	}

	public static void paintBackground(Graphics g, Component panel) {
		// out("painting component= " + panel);
		double red, redStep, green, greenStep;
		red = 255;
		green = 255;
		redStep = (255. - PANEL_BACKGROUND_RED_TO) / panel.getHeight();
		greenStep = (255. - PANEL_BACKGROUND_GREEN_TO) / panel.getHeight();
		for (int y = 0; y < panel.getHeight(); y++) {
			g.setColor(new Color((int) red, (int) green, PANEL_BACKGROUND_BLUE));
			red -= redStep;
			green -= greenStep;
			g.drawLine(0, y, panel.getWidth(), y);
		}
	}

	public static void paintBackgroundGreen(Graphics g, Component panel) {
		// out("painting component= " + panel);
		double red, redStep, blue, blueStep;
		red = 255;
		blue = 255;
		redStep = (255. - PANEL_BACKGROUND_RED_TO) / panel.getHeight();
		blueStep = (255. - PANEL_BACKGROUND_RED_TO) / panel.getHeight();
		for (int y = 0; y < panel.getHeight(); y++) {
			g.setColor(new Color((int) red, PANEL_BACKGROUND_BLUE, (int) blue));
			red -= redStep;
			blue -= blueStep;
			g.drawLine(0, y, panel.getWidth(), y);
		}
	}
	public static String formatIntoHHMMSS(int secsIn) {

		int hours = secsIn / 3600, remainder = secsIn % 3600, minutes = remainder / 60, seconds = remainder % 60;

		return ((hours < 10 ? "0" : "") + hours + ":"
				+ (minutes < 10 ? "0" : "") + minutes + ":"
				+ (seconds < 10 ? "0" : "") + seconds);

	}
	public static void paintBackgroundHorizontal(Graphics g, Component panel) {
		double red, redStep, green, greenStep;
		red = 255;
		green = 255;
		
		redStep = (255. - TOOLBAR_BACKGROUND_RED_TO) / panel.getWidth() * 2;
		greenStep = (255. - TOOLBAR_BACKGROUND_GREEN_TO) / panel.getWidth() * 2;
		for (int x = 0; x < panel.getWidth(); x++) {
			g.setColor(new Color((int) red, (int) green, PANEL_BACKGROUND_BLUE));
			red -= redStep;
			green -= greenStep;
			g.drawLine(x, 0, x, panel.getHeight());
		}
	}
	public static void executeInEDTThread(Runnable job) {
			// TODO:
	//		Runnable cursorJob = new Runnable() {
	//			public void run() {
	//				ControlPanel.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	//				long start = System.currentTimeMillis();
	//				//job.run();
	//				try {
	//					Thread.sleep(2000);
	//				} catch (InterruptedException e) {
	//					// TODO Auto-generated catch block
	//					e.printStackTrace();
	//				}
	//				long millis = System.currentTimeMillis() - start;
	//				ControlPanel.addStatusMessage("Time taken:" + String.format("%d min, %d sec", 
	//					    TimeUnit.MILLISECONDS.toMinutes(millis),
	//					    TimeUnit.MILLISECONDS.toSeconds(millis) - 
	//					    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
	//					));
	//				ControlPanel.getInstance().setCursor(Cursor.getDefaultCursor());
	//			}
	//		};
			
			try {
		        SwingUtilities.invokeLater(job);
		    } catch (Exception exc) {
		        exc.printStackTrace();
		        ControlPanel.log.error(exc);
		    }
			
		}
	public static void executeInCThread(Runnable job) {
		Thread myThread = new Thread(job);
		myThread.start();
	}
	public static void executeInEDTAndWait(Runnable job) {
		
	    if (EventQueue.isDispatchThread()) {
	        job.run();
	    } else {
	    	try {
	    		SwingUtilities.invokeAndWait(job);
		    } catch (Exception exc) {
		        exc.printStackTrace();
		        ControlPanel.log.error(exc);
		    }	        
	    }	
		
	}
}
