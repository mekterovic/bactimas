package bactimas.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import bactimas.gui.ControlPanel;
import bactimas.util.S;

public class ConnectionManager {
	static Logger log = Logger.getLogger("bactimas.db.ConnectionManager");
	static Connection _conn = null;
    public static void init() {
    	Properties properties = new Properties();
    	try {    					
			properties.load(new FileInputStream(S.getDbPropertiesAbsFileName()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			ControlPanel.addStatusMessage("Couldn't load the 'db.properties' file:" + S.getDbPropertiesAbsFileName());
			e1.printStackTrace();
			return;
		}
    	
        try {
            // load the database driver (make sure this is in your classpath!)
        	// TODO parameterize:
            Class.forName(properties.getProperty("jdbc.driver"));
        } catch (Exception e) {
        	ControlPanel.addStatusMessage("Couldn't load db driver:" + e.getMessage());
        	log.error("Couldn't load db driver:" + e.getMessage());
            e.printStackTrace();
            return;
        }
        String url = properties.getProperty("jdbc.url").trim();
        if (url.startsWith("jdbc:sqlite:")) {
	        url = url.replaceAll("jdbc:sqlite:", "").trim();        
	        url = S.getAbsFromRelFolder(url);
	        url = "jdbc:sqlite:" + url;
        }
        try {
        	
            _conn = DriverManager.getConnection(url);
            ControlPanel.addStatusMessage("Connection to DB " + url + " succesfully opened.");
         } catch (SQLException exception) {
        	ControlPanel.addStatusMessage("Couldn't open the conneciton to database: " + url + ". See log for details.");
            log.error("Couldn't open the conneciton:" + exception.getMessage());
            exception.printStackTrace();
            return;
         }              
    }
    
    public static Connection getConnection() {
    	if (_conn == null) init();
    	return _conn;
    }

       
}
