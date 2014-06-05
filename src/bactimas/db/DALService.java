package bactimas.db;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import bactimas.datamodel.CurrentExperiment;
import bactimas.db.beans.BTreeElement;
import bactimas.db.beans.Bacteria;
import bactimas.db.beans.BacteriaMeasurement;
import bactimas.db.beans.BacteriaSplit;
import bactimas.db.beans.BacteriaState;
import bactimas.db.beans.BacteriaStateChange;
import bactimas.db.beans.Experiment;
import bactimas.db.beans.ExperimentEvent;
import bactimas.db.beans.ExperimentMeasurements;
import bactimas.db.beans.Frame;
import bactimas.db.beans.Roi;
import bactimas.gui.ControlPanel;
import signalprocesser.voronoi.VPoint;
import au.com.bytecode.opencsv.CSVWriter;


/**
 * @author igor
 *
 */
public class DALService {
	
	static Logger log = Logger.getLogger("bactimas.db.DALService" );
	
	public static boolean deleteExperiment(int idExperiment) {

		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			
			conn.setAutoCommit(false);
			
			stat = conn.prepareStatement("DELETE FROM BacteriaMeasurement WHERE idBacteria IN (SELECT idBacteria FROM Bacteria WHERE idExperiment = ?)");			
			stat.setInt(1,idExperiment);			
			stat.executeUpdate();

			
			stat = conn.prepareStatement("DELETE FROM BacteriaSplit WHERE idBacteriaParent IN (SELECT idBacteria FROM Bacteria WHERE idExperiment = ?)");			
			stat.setInt(1,idExperiment);			
			stat.executeUpdate();			

			
			stat = conn.prepareStatement("DELETE FROM Roi WHERE idBacteria IN (SELECT idBacteria FROM Bacteria WHERE idExperiment = ?)");			
			stat.setInt(1,idExperiment);			
			stat.executeUpdate();				
			
			
			stat = conn.prepareStatement("DELETE FROM Bacteria WHERE idExperiment = ?");			
			stat.setInt(1,idExperiment);			
			stat.executeUpdate();				
			
			
			stat = conn.prepareStatement("DELETE FROM Frame WHERE idExperiment = ?");			
			stat.setInt(1,idExperiment);			
			stat.executeUpdate();	
			
			stat = conn.prepareStatement("DELETE FROM ExperimentMeasurements WHERE idExperiment = ?");			
			stat.setInt(1,idExperiment);			
			stat.executeUpdate();
			
			
			stat = conn.prepareStatement("DELETE FROM Experiment WHERE idExperiment = ?");			
			stat.setInt(1,idExperiment);			
			stat.executeUpdate();
			
			conn.commit();
			
			return true;
			
			
		} catch (SQLException e) {		
			log.error("Error in splitBacteria", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}		
	}	
	
	/**
	 * Loads the experiment with the given name or creates a new one.
	 * @param expName
	 * @param redPath
	 * @param greenPath
	 * @param bluePath
	 * @param spf
	 * @param pixelWidth
	 * @param pixelHeight
	 * @param pictureScale
	 * @return Experiment object
	 */
	public static Experiment beginExperiment (String expName, String redPath, String greenPath, String bluePath, int spf, BigDecimal pixelWidth, BigDecimal pixelHeight, BigDecimal pictureScale) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		Experiment experiment = null;
		try {
			stat = conn.prepareStatement("SELECT idExperiment FROM Experiment WHERE expName = ?");
			stat.setString(1, expName);
			ResultSet rs = stat.executeQuery();
			if (rs.next()) {
				experiment = loadExperiment(rs.getInt("idExperiment"));
			} else {
				stat = conn.prepareStatement("INSERT INTO Experiment (expName, redMovieFileName, greenMovieFileName, blueMovieFileName,  movieSpf, pixelWidthMicron, pixelHeightMicron, pictureScale, dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
				stat.setString(1, expName);
				stat.setString(2, redPath);
				stat.setString(3, greenPath);
				stat.setString(4, bluePath);
				stat.setInt(5, spf);
				// ugly workaround for:
				// java.sql.SQLException: not implemented by SQLite JDBC driver
				//at org.sqlite.Unused.unused(Unused.java:29)
				//at org.sqlite.Unused.setBigDecimal(Unused.java:88)				
				stat.setString(6, pixelWidth.toString());
				stat.setString(7, pixelHeight.toString());
				stat.setString(8, pictureScale.toString());
				stat.setLong(9, (new java.util.Date()).getTime());				
				stat.executeUpdate();
				ResultSet rs2 = stat.getGeneratedKeys();
				rs2.next();
				experiment =  loadExperiment(rs2.getInt(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in beginExperiment", e);
			e.printStackTrace();
		}
		return experiment;
		
	}
	
	public static Experiment updateExperiment (String expName, String redPath, String greenPath, String bluePath, int spf, BigDecimal pixelWidth, BigDecimal pixelHeight, BigDecimal pictureScale, int idExperiment) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement("UPDATE Experiment SET expName = ? , redMovieFileName = ?, greenMovieFileName = ?, blueMovieFileName = ?, movieSpf = ?, pixelWidthMicron = ?, pixelHeightMicron = ?, pictureScale = ?  WHERE idExperiment = ? ");
			
			stat.setString(1, expName);
			stat.setString(2, redPath);
			stat.setString(3, greenPath);
			stat.setString(4, bluePath);
			stat.setInt(5, spf);
			// ugly workaround for:
			// java.sql.SQLException: not implemented by SQLite JDBC driver
			//at org.sqlite.Unused.unused(Unused.java:29)
			//at org.sqlite.Unused.setBigDecimal(Unused.java:88)				
			stat.setString(6, pixelWidth.toString());
			stat.setString(7, pixelHeight.toString());
			stat.setString(8, pictureScale.toString());
			stat.setInt(9, idExperiment);
			stat.executeUpdate();
						
			return loadExperiment(idExperiment);

		} catch (SQLException e) {		
			log.error("Error in updateFrameTranslation", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return null;
		}			
	}	
	
	
	
	
	public static Experiment[] getAllExperiments() {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		Experiment experiment = null;
		ArrayList<Experiment> exps;
		try {
			
			stat = conn.prepareStatement("SELECT idExperiment, expName, redMovieFileName, greenMovieFileName, blueMovieFileName, movieSpf, pixelWidthMicron, pixelHeightMicron, pictureScale, dateCreated FROM Experiment Order by dateCreated DESC");
			
			ResultSet rs = stat.executeQuery();
			exps = new ArrayList<Experiment>();
			while (rs.next()) {
				
				experiment = new Experiment();
				experiment.setIdExperiment(rs.getInt(1));
				experiment.setExperimentName(rs.getString(2));
				experiment.setRedMovieFileName(rs.getString(3));
				experiment.setGreenMovieFileName(rs.getString(4));
				experiment.setBlueMovieFileName(rs.getString(5));
				experiment.setMovieSpf(rs.getInt(6));
				experiment.setPixelWidthMicron(new BigDecimal(rs.getString("pixelWidthMicron")));
				experiment.setPixelHeightMicron(new BigDecimal(rs.getString("pixelHeightMicron")));
				experiment.setPictureScale(new BigDecimal(rs.getString("pictureScale")));					
				experiment.setDateCreated(rs.getLong("dateCreated"));
				
				exps.add(experiment);				
				
			}
			if (exps.size() == 0) {
				
				JOptionPane.showConfirmDialog(null, "There are no experiments in the current database.", "Info", JOptionPane.INFORMATION_MESSAGE );
				return null;
			}
			log.debug("getAllExperiments returning " + exps.size() + " experiments.");
			return exps.toArray(new Experiment[exps.size()]);
						
			
		} catch (Exception e) {
			log.error("Error loading experiment" , e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static Experiment loadExperiment(int idExperiment) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		Experiment experiment = null;
		try {
			
			stat = conn.prepareStatement("SELECT idExperiment, expName, redMovieFileName, greenMovieFileName, blueMovieFileName, movieSpf, pixelWidthMicron, pixelHeightMicron, pictureScale, dateCreated  FROM Experiment WHERE idExperiment = ?");
			stat.setInt(1, idExperiment);
			ResultSet rs = stat.executeQuery();
			if (rs.next()) {
				experiment = new Experiment();
				experiment.setIdExperiment(rs.getInt(1));
				experiment.setExperimentName(rs.getString(2));
				experiment.setRedMovieFileName(rs.getString(3));
				experiment.setGreenMovieFileName(rs.getString(4));
				experiment.setBlueMovieFileName(rs.getString(5));
				experiment.setMovieSpf(rs.getInt(6));
				experiment.setPixelWidthMicron(new BigDecimal(rs.getString("pixelWidthMicron")));
				experiment.setPixelHeightMicron(new BigDecimal(rs.getString("pixelHeightMicron")));
				experiment.setPictureScale(new BigDecimal(rs.getString("pictureScale")));				
				experiment.setDateCreated(rs.getLong("dateCreated"));;
				
			} else {
				log.error("No experiment with id=" + idExperiment);
				JOptionPane.showConfirmDialog(null, "You must select a frame node or bacteria node.", "Info", JOptionPane.INFORMATION_MESSAGE ); 
			}
			log.debug ("Succesfully loaded Experiment id = "+ idExperiment);
						
			
		} catch (Exception e) {
			log.error("Error loading experiment" , e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return experiment;		
	}
	
	
	@SuppressWarnings("unchecked")
	public static void dumpCSV(final int idExperiment, final File csvFile){


		final LinkedList<ExpMeasurement> columns = (LinkedList<ExpMeasurement>) ExpMeasurement.getBuiltInMeasures().clone();
//		for (ExpMeasurement em : ExpMeasurement.getBuiltInMeasures()) {
//			columns.addLast(em.getName(), em.getSql());
//		}
		
		try {
			PreparedStatement stat = null;
			Connection conn = ConnectionManager.getConnection();
			stat = conn.prepareStatement(
					"SELECT DISTINCT measurementName, channelName, Channel.idChannel              " +
							"  FROM BacteriaMeasurement                                           " +
							"  JOIN Measurement                                                   " +
							"    ON BacteriaMeasurement.idMeasurement = measurement.idMeasurement " +  
							"  JOIN Bacteria                                                      " +
							"    ON BacteriaMeasurement.idBacteria = bacteria.idBacteria          " +
							"  JOIN Channel                                                       " +
							"    ON BacteriaMeasurement.idChannel = Channel.idChannel             " +							
							" WHERE idExperiment = ?                                              " +
						    "   AND lower(measurementName) NOT IN  ('area', 'intden', 'mean')" +							
							" ORDER BY measurementName, channelName                           " 
					);
			stat.setInt(1, idExperiment);			
			ResultSet rs = stat.executeQuery();			
			// Add the measured ones:
			while (rs.next()){		
				columns.addLast( new ExpMeasurement(
						rs.getString(1) + "_" + rs.getString("channelName"), 
				        "              (SELECT value FROM BacteriaMeasurement bm1                " +
						"                 JOIN Measurement                                       " +
						"                   ON bm1.idMeasurement = measurement.idMeasurement     " +                  
						"            WHERE bm1.idBacteria = vBacteriaMeasurement.id_bacteria  " +
						"              AND bm1.frameNo    = vBacteriaMeasurement.frame_no  " +
						"              AND bm1.idChannel = " + rs.getInt("idChannel") + 
						"              AND measurementName = '" + rs.getString("measurementName") + "') as " + rs.getString(1) + "_" + rs.getString("channelName")
						, ExpMeasurement.EXP_VAR_GROUP_USER
						,false
						)
								
				);
			} 	

			
        	JPanel panel = new JPanel(new GridLayout(0,5));
        	final HashMap<String, JCheckBox> cboxes = new HashMap<String, JCheckBox> ();
    		for (ExpMeasurement col : columns) {
    			JCheckBox cb = new JCheckBox(col.getName());    	        
    	        cb.setSelected(true);
    			panel.add(cb);    
    			cboxes.put(col.getName(), cb);
    		}	
			
			
    		final JDialog dialog = new JDialog(null, "Select columns to export:", Dialog.ModalityType.APPLICATION_MODAL);
    		
    		JButton dump = new JButton("Dump CSV");

    		dump.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                    String sql = "";
                    for (ExpMeasurement col : columns) {
            			if (cboxes.get(col.getName()).isSelected()) {
                    		if (sql == "") {
//                    			String col = columns.get(c.getText());
//                    			col = col.substring(col.indexOf(",") + 1);
                    			sql = "SELECT " + col.getSql(); //columns.get(c.getText());
                    		} else {
                    			sql += "\n,\t" + col.getSql();
                    		}            				
            			}
            		}
                    
        			sql += "\n\tFROM vBacteriaMeasurement" +                                                                              					
        					"\tWHERE id_experiment = ?"
        					+"\nORDER BY frame_no, bact_name";
        			log.debug("Dumping CSV for sql: \n" + sql);
        			

        			
        			
        			String delimiter;		
        			delimiter = JOptionPane.showInputDialog(null, "Delimiter",  ";"); 		
        			CSVWriter  writer;
        			try {
            			
        				Connection conn = ConnectionManager.getConnection();
            			PreparedStatement stat = null;        			
            			stat = conn.prepareStatement(sql);
            			stat.setInt(1, idExperiment);			
            			ResultSet rs = stat.executeQuery();
            			
        				writer = new CSVWriter(new FileWriter(csvFile), delimiter.charAt(0));
        			    writer.writeAll(rs, true);
        			    writer.close();
        			    ControlPanel.addStatusMessage("Dumped to CSV:" + csvFile.getAbsolutePath());
        			} catch (IOException ex) {
        				// TODO Auto-generated catch block
        				ControlPanel.addStatusMessage("Error writing to file " + csvFile.getAbsolutePath() + " err msg:" + ex.getMessage());
        				ex.printStackTrace();
        			} catch (SQLException ex) {
        				// TODO Auto-generated catch block
        				ControlPanel.addStatusMessage("SQL Error writing to file " + csvFile.getAbsolutePath() + "\n sql err msg:" + ex.getMessage());
        				ex.printStackTrace();
        			}        			
                    
                    
                }
            });			
			
	        Container cp3 = dialog.getContentPane();
	        cp3.setLayout(new BorderLayout());
//	        cp3.add(new JLabel("Note that here you only define to what channel a measurement maps to." 
//	        		+ "\n In order for measure to be taken you also HAVE TO CHECK it via: ImageJ|Analyze|Set Measurements"), BorderLayout.NORTH);
	        cp3.add(panel, BorderLayout.CENTER);    	        
	        cp3.add(dump, BorderLayout.SOUTH);
	        
    	    dialog.setSize(800, (columns.size()+1)/5*30);
    	    dialog.setLocationRelativeTo(null);
        	dialog.setVisible(true);  
			
			
			

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in getBacteriasForFrame", e);
			e.printStackTrace();
		}
					
	}
	

	public static LinkedList<ExpMeasurement> getUserMeasures(int idExperiment){


		LinkedList<ExpMeasurement> measures = new LinkedList<ExpMeasurement>();
		try {
			PreparedStatement stat = null;
			Connection conn = ConnectionManager.getConnection();
			stat = conn.prepareStatement(
					"SELECT DISTINCT measurementName, channelName, Channel.idChannel              " +
							"  FROM BacteriaMeasurement                                           " +
							"  JOIN Measurement                                                   " +
							"    ON BacteriaMeasurement.idMeasurement = measurement.idMeasurement " +  
							"  JOIN Bacteria                                                      " +
							"    ON BacteriaMeasurement.idBacteria = bacteria.idBacteria          " +
							"  JOIN Channel                                                       " +
							"    ON BacteriaMeasurement.idChannel = Channel.idChannel             " +							
							" WHERE idExperiment = ?                                              " +
						    "   AND lower(measurementName) NOT IN  ('area', 'intden', 'mean')" +							
							" ORDER BY measurementName, channelName                           " 
					);
			stat.setInt(1, idExperiment);			
			ResultSet rs = stat.executeQuery();			
			// Add the measured ones:
			while (rs.next()){		
				measures.addLast(new ExpMeasurement(
										rs.getString("measurementName") + " on " + rs.getString("channelName"), 										
								        "              (SELECT value FROM BacteriaMeasurement bm1                " +
										"                 JOIN Measurement                                       " +
										"                   ON bm1.idMeasurement = measurement.idMeasurement     " +                  
										"            WHERE bm1.idBacteria = vBacteriaMeasurement.idBacteria  " +
										"              AND bm1.frameNo    = vBacteriaMeasurement.frameNo  " +
										"              AND bm1.idChannel = " + rs.getInt("idChannel") + 
										"              AND measurementName = '" + rs.getString("measurementName") + "') as " + rs.getString(1) + "_" + rs.getString("channelName"),
										ExpMeasurement.EXP_VAR_GROUP_USER,
										true
								)
				);
			} 	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in getUserMeasures", e);
			e.printStackTrace();
		}
		return measures;
					
	}	
	
	
	/**
	 * Loads the frames for the active experiment or initializes (inserts) new ones. 
	 * @param redFolder
	 * @param greenFolder
	 * @param blueFolder
	 * @return an ArrayList of Frames
	 * @throws Exception
	 */
	public static ArrayList<Frame> initFrames(String redFolder, String greenFolder, String blueFolder) throws Exception {
		if (!CurrentExperiment.hasData()) {
			throw new Exception ("Experiment not loaded, you must initialze the experiment before frames.");			
		}
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		ArrayList<Frame> frames = null;
		try {
			stat = conn.prepareStatement("SELECT * FROM Frame WHERE idExperiment = ? ORDER BY frameNo");
			stat.setInt(1, CurrentExperiment.getIdExperiment());
			ResultSet rs = stat.executeQuery();
			frames = new ArrayList<Frame>();
			Frame f;			
			if (rs.next()) {
//				int i = 0;
				do {					
					f = new Frame ();
					f.setIdFrame(rs.getInt("idFrame"));
					f.setIdExperiment(rs.getInt("idExperiment"));
					f.setFrameNo(rs.getInt("frameNo"));
					f.setFrameRedFileName(rs.getString("frameRedFileName").trim());	
					f.setFrameGreenFileName(rs.getString("frameGreenFileName").trim());
					f.setFrameBlueFileName(rs.getString("frameBlueFileName").trim());
					f.setTransX(rs.getInt("transX"));
					f.setTransY(rs.getInt("transY"));
					f.setBgGreenMean(rs.getDouble("bgGreenMean"));
					f.setBgBlueMean(rs.getDouble("bgBlueMean"));
					f.setBgRedMean(rs.getDouble("bgRedMean"));
					f.setIgnoreFrame(rs.getString("ignoreFrame"));
					f.setAlgorithm(rs.getString("algorithm"));
					
					frames.add(f);	
					log.debug("initFrames2 # Adding frame " + f.getFrameNo()
							+ " Id frame = " + f.getIdFrame()
							+  " Blue file name = "  + f.getFrameBlueFileName()
							+ " frames.size = " + frames.size()
							);
				} while (rs.next());
							
			} else {
				
				ControlPanel.addStatusMessage("Inserting image descriptions into the DB.") ;
				
				FilenameFilter pngFilter = new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				        return name.toLowerCase().endsWith(".png");
				    }
				};
				File[] redFiles 	= new File(redFolder).listFiles(pngFilter);
				File[] greenFiles 	= new File(greenFolder).listFiles(pngFilter);
				File[] blueFiles 	= new File(blueFolder).listFiles(pngFilter);
				try {
										
					if (redFiles != null && redFiles.length > 0) {

						Arrays.sort(redFiles, new Comparator<File>() {
							public int compare(File f1, File f2) {
								return f1.getName().compareTo(f2.getName());
							}
						});
						log.debug(redFiles.length + " files sorted (in: " + redFolder  + ").");
						ControlPanel.addStatusMessage(redFiles.length + " files sorted (in: " + redFolder  + ").") ;
						
					} else {
						JOptionPane.showMessageDialog(null, 
								"There are " + ((redFiles == null) ? "null" : redFiles.length) + " files in the folder:" + redFolder,
								"Error", 
								JOptionPane.ERROR_MESSAGE);
					}
					if (greenFiles != null && greenFiles.length > 0) {

						Arrays.sort(greenFiles, new Comparator<File>() {
							public int compare(File f1, File f2) {
								return f1.getName().compareTo(f2.getName());
							}
						});
						log.debug(greenFiles.length + " files sorted (in: " + greenFolder  + ").");									
						ControlPanel.addStatusMessage(greenFiles.length + " files sorted (in: " + greenFolder  + ").") ;
					} else {
						JOptionPane.showMessageDialog(null, 
								"There are " + ((greenFiles == null) ? "null" : greenFiles.length) + " files in the folder:" + greenFolder,
								"Error", 
								JOptionPane.ERROR_MESSAGE);
					}
					if (blueFiles != null && blueFiles.length > 0) {

						Arrays.sort(blueFiles, new Comparator<File>() {
							public int compare(File f1, File f2) {
								return f1.getName().compareTo(f2.getName());
							}
						});
						log.debug(blueFiles.length + " files sorted (in: " + blueFolder  + ").");									
						ControlPanel.addStatusMessage(blueFiles.length + " files sorted (in: " + blueFolder  + ").");
					} else {
						JOptionPane.showMessageDialog(null, 
								"There are " + ((blueFiles == null) ? "null" : blueFiles.length) + " files in the folder:" + blueFolder,
								"Error", 
								JOptionPane.ERROR_MESSAGE);
					}
					
				} catch (Exception e) {
					System.out.println("Exc" + e.getMessage());
					e.printStackTrace();
				}		
								
				
		    	for (int i=0; blueFiles != null && i < blueFiles.length; ++i) {
		    		f = new Frame ();
					
					f.setIdExperiment(CurrentExperiment.getIdExperiment());
					f.setFrameNo(i+1);
					f.setFrameRedFileName(redFiles[i].getName());		    		
					f.setFrameGreenFileName(greenFiles[i].getName());
					f.setFrameBlueFileName(blueFiles[i].getName());
					
					stat = conn.prepareStatement("INSERT INTO Frame (idExperiment, frameNo, frameRedFileName, frameGreenFileName, frameBlueFileName, transX, transY, bgGreenMean, bgRedMean, bgBlueMean) VALUES (?, ?, ?, ?, ?, 0, 0, 100, 100, 100)", Statement.RETURN_GENERATED_KEYS);
					stat.setInt(1, f.getIdExperiment());
					stat.setInt(2, f.getFrameNo());
					stat.setString(3, f.getFrameRedFileName());
					stat.setString(4, f.getFrameGreenFileName());
					stat.setString(5, f.getFrameBlueFileName());
					stat.executeUpdate();
					
					ResultSet rs2 = stat.getGeneratedKeys();
					rs2.next();			
					f.setIdFrame(rs2.getInt(1));
					
					frames.add(f);	
		    	}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in initFrames", e);
			e.printStackTrace();
		}
		return frames;			
	}
	
	
	/**
	 * Returns measurements for the given frame (from the DB).
	 * @param idBacteria
	 * @param frameNo
	 * @param idRoiType
	 * @return a list of BacteriaMeasurement objects
	 */
	public static LinkedList<BacteriaMeasurement> getBacteriaMeasurementsForFrame(int idBacteria, int frameNo, int idRoiType) {

		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		LinkedList<BacteriaMeasurement> measurements = null;
		try {
			stat = conn.prepareStatement(
					"SELECT BacteriaMeasurement.*, bactName, measurementName                            " +
							"  FROM BacteriaMeasurement                                                         " +
							"  JOIN Bacteria ON BacteriaMeasurement.idBacteria = Bacteria.idBacteria            " +
							"  JOIN Measurement ON BacteriaMeasurement.idMeasurement = Measurement.idMeasurement" +
							" WHERE idExperiment = ?                                                            " +
							"   AND Bacteria.idBacteria = ?                                                            " +
							"   AND frameNo = ?                                                            " +
							"   AND idRoiType = ?                                                            " +
							" ORDER BY measurementName "					
					);
			

			stat.setInt(1,CurrentExperiment.getIdExperiment());
			stat.setInt(2, idBacteria);
			stat.setInt(3, frameNo);
			stat.setInt(4, idRoiType);
			
			ResultSet rs = stat.executeQuery();
			
			BacteriaMeasurement bm;			
			measurements = new LinkedList<BacteriaMeasurement>();
			while (rs.next()){
				bm = new BacteriaMeasurement(
						rs.getInt("idBacteria"),
						rs.getInt("frameNo"),
						rs.getInt("idMeasurement"),
						rs.getDouble("value"),
						rs.getString("bactName"),
						rs.getString("measurementName"), 
						idRoiType,
						rs.getInt("idChannel")
						);
				measurements.add(bm);		
			} 								 
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in initFrames", e);
			e.printStackTrace();
		}
		return measurements;			
	}
	
	
	public static LinkedList<Bacteria> getBacteriasForFrame(Frame f) {

		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		LinkedList<Bacteria> bacterias = null;
		try {
			stat = conn.prepareStatement(
					"  SELECT DISTINCT Bacteria.*                                                     " +              
							"    FROM Bacteria JOIN Experiment                                       " +     
							"                    ON bacteria.idExperiment = Experiment.idExperiment  " +     
							"                  JOIN Roi                                              " +    
							"                    ON Bacteria.idBacteria = Roi.idBacteria             " +    
							"   WHERE Experiment.idExperiment = ?                                    " + 
							"     AND frameNo = ?                                                    " +  
							"   ORDER BY bactname                                                    "
					);
			stat.setInt(1, f.getIdExperiment());
			stat.setInt(2, f.getFrameNo());
			ResultSet rs = stat.executeQuery();
			
						
			bacterias = new LinkedList<Bacteria>();
			while (rs.next()){				
				bacterias.add(
					new Bacteria(
						rs.getInt("idBacteria"),
						f.getIdExperiment(),					
						rs.getString("bactName")
						//,						rs.getBytes("initialRoiBlob")
						)
					);		
			} 								 
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in getBacteriasForFrame", e);
			e.printStackTrace();
		}
		return bacterias;			
	}
	public static LinkedList<Bacteria> getSplitBacteriasForFrame(Frame f) {

		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		LinkedList<Bacteria> bacterias = null;
		try {
			stat = conn.prepareStatement(
							" SELECT Bacteria.*                                                        " +
							"   FROM Bacteria JOIN BacteriaSplit                                       " +
							"                   ON Bacteria.idBacteria = BacteriaSplit.idBacteriaParent " +
							"   WHERE idExperiment = ?                                                      " +
							"    AND  frameNo = ?                                                  " 
					);
			stat.setInt(1, f.getIdExperiment());
			stat.setInt(2, f.getFrameNo());
			ResultSet rs = stat.executeQuery();
			
						
			bacterias = new LinkedList<Bacteria>();
			while (rs.next()){				
				bacterias.add(
					new Bacteria(
						rs.getInt("idBacteria"),
						f.getIdExperiment(),					
						rs.getString("bactName")
						//,						rs.getBytes("initialRoiBlob")
						)
					);		
			} 								 
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in getBacteriasForFrame", e);
			e.printStackTrace();
		}
		return bacterias;			
	}	
	
	public static LinkedList<BacteriaSplit> getBacteriaFamilySplits (int idExperiment, Bacteria root) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		LinkedList<BacteriaSplit> splits = null;
		try {
			stat = conn.prepareStatement(
					" SELECT frameNo, parent.bactName, childA.bactName as childA, childB.bactName as childB " +
							"   FROM BacteriaSplit                                                                  " +
							" 	JOIN Bacteria as parent                                                              " +
							"     ON BacteriaSplit.idBacteriaParent = parent.idBacteria                             " +
							"   JOIN Bacteria as childA                                                             " +
							"     ON BacteriaSplit.idBacteriaChildA = childA.idBacteria                             " +
							"   JOIN Bacteria as childB                                                             " +
							"     ON BacteriaSplit.idBacteriaChildB = childB.idBacteria                             " +
							"  WHERE parent.idExperiment = ?                                                        " +
							"    AND parent.bactName LIKE '" + root.getBactName().charAt(0) + "%' " +	
							"  ORDER BY frameNo "

					);
			stat.setInt(1, idExperiment);			
			ResultSet rs = stat.executeQuery();
			
						
			splits = new LinkedList<BacteriaSplit>();
			while (rs.next()){		
//				if (rs.getString("bactName").startsWith(root.getBactName())) {
					splits.add(
							new BacteriaSplit(
								rs.getInt("frameNo"),							
								rs.getString("bactName") ,
								rs.getString("childA") ,
								rs.getString("childB") 						
								)
							);							
//				}
			} 								 
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in getBacteriasForFrame", e);
			e.printStackTrace();
		}
		return splits;			
		
	}	
	public static int getSecondsPerFrame(int idExperiment) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		
		try {
			stat = conn.prepareStatement(
					" SELECT movieSpf " +
							"   FROM Experiment      " +							
							"  WHERE idExperiment = ?" 
							
					);
			stat.setInt(1, idExperiment);			
			ResultSet rs = stat.executeQuery();
						
			
			if (rs.next()){		
				return rs.getInt("movieSpf");						
			} 								 
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in getBacteriasForFrame", e);
			e.printStackTrace();
		}
		return 0;	
						
	}		
	public static LinkedList<Color> getPalette(int idPalette) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		LinkedList<Color> palette = null;
		try {
			stat = conn.prepareStatement(
					" SELECT  red, green, blue " +
					"   FROM PaletteColor      " +							
					"  WHERE idPalette = ?     " +												
					"  ORDER BY ordinal "

					);
			stat.setInt(1, idPalette);			
			ResultSet rs = stat.executeQuery();
			
						
			palette = new LinkedList<Color>();
			while (rs.next()){					
				palette.add(
						new Color(													
							rs.getInt("red"),							
							rs.getInt("green"),
							rs.getInt("blue")
							)
						);											
			} 								 
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in getBacteriaFamilyIntensities", e);
			e.printStackTrace();
		}
		return palette;			
		
	}	
//	public static boolean materializeVMeasurements (int idExperiment) {
//		Connection conn = ConnectionManager.getConnection();
//		PreparedStatement stat = null;
//		try {
//
//			
//			conn.setAutoCommit(false);
//			
//			stat = conn.prepareStatement("DELETE FROM m WHERE 1=1");								
//			stat.executeUpdate();
//
//			stat = conn.prepareStatement("insert into m select * from vBacteriaMeasurement where idExperiment = ?");
//			stat.setInt(1, idExperiment);	
//			stat.executeUpdate();
//			
//			conn.commit();
//			
//			return true;
//			
//			
//		} catch (SQLException e) {		
//			log.error("Error in materializeVMeasurements", e);
//			e.printStackTrace();
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}			
//			return false;
//		}		
//	
//	}
	
	public static LinkedList<BTreeElement> getBacteriaBTreeElements (int idExperiment, Bacteria root, String onWidthSQL, String onColorSQL) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		LinkedList<BTreeElement> items = null;
		try {
			stat = conn.prepareStatement(
					" SELECT  bact_name, frame_no, " +
							onWidthSQL + ", " + 
							onColorSQL +  // CAREFUL HERE - these indexes must match - see bellow
					"   FROM vBacteriaMeasurement " +							
					"  WHERE id_experiment =  ?" +							
					"    AND bact_name LIKE '" + root.getBactName().charAt(0) + "%' " +	
					"  ORDER BY frame_no "
					);
						
			stat.setInt(1, idExperiment);
					
			ResultSet rs = stat.executeQuery();
			
			items = new LinkedList<BTreeElement>();
			
			while (rs.next()){		
				
				items.add(
						new BTreeElement(
							idExperiment,							
							rs.getInt("frame_no"),							
							rs.getString("bact_name") ,
							rs.getFloat(3) ,
							rs.getFloat(4)    // CAREFUL HERE - these indexes must match 
							)
						);
				log.debug("Added btreeelement:" + items.get(0));
				
			} 								 
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in getBacteriaBTreeElements", e);
			e.printStackTrace();
		}
		return items;			
		
	}		
	
	public static boolean truncateFromFrame(int frameNo) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {
			
			
			conn.setAutoCommit(false);
			stat = conn.prepareStatement("DELETE FROM Roi WHERE idBacteria IN (SELECT idBacteria from Bacteria where idExperiment = ?) AND frameNo >= ? ");
			
			stat.setInt(1, CurrentExperiment.getIdExperiment());
			stat.setInt(2, frameNo);
			
			stat.executeUpdate();
			
			stat = conn.prepareStatement("DELETE FROM BacteriaSplit WHERE idBacteriaParent IN (SELECT idBacteria from Bacteria where idExperiment = ?) AND frameNo >= ? ");
			
			stat.setInt(1, CurrentExperiment.getIdExperiment());
			stat.setInt(2, frameNo);
			
			stat.executeUpdate();
			
			conn.commit();
			
			
			
			return true;
			
			
		} catch (SQLException e) {		
			log.error("Error in truncateFromFrame", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}
	}
	
	public static Bacteria[] splitBacteria(Bacteria b, int frameNo) {
		
		Bacteria bactA = getOrInsertBacteria(b.getBactName() + "A");
		Bacteria bactB = getOrInsertBacteria(b.getBactName() + "B");
		
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			
			conn.setAutoCommit(false);
			stat = conn.prepareStatement("DELETE FROM BacteriaSplit WHERE idBacteriaParent = ? AND frameNo = ? ");
			
			stat.setInt(1, b.getIdBacteria());
			stat.setInt(2, frameNo);
			
			stat.executeUpdate();
			

			stat = conn.prepareStatement("INSERT INTO BacteriaSplit(idBacteriaParent, idBacteriaChildA, idBacteriaChildB, frameNo) VALUES (?, ?, ?, ?)");
			stat.setInt(1, b.getIdBacteria());
			stat.setInt(2, bactA.getIdBacteria());
			stat.setInt(3, bactB.getIdBacteria());
			stat.setInt(4, frameNo);
			
			stat.executeUpdate();

			stat = conn.prepareStatement("DELETE FROM Roi WHERE idBacteria = ? AND frameNo = ? AND idRoiType = 1");
			
			stat.setInt(1, b.getIdBacteria());
			stat.setInt(2, frameNo);			
			stat.executeUpdate();			
			
			stat = conn.prepareStatement("INSERT INTO Roi(idBacteria, frameNo, idRoiType, roiBlob, roiName) " 
 					+ " SELECT ?, frameNo, 1, roiBlob, ? "  
 					+ " FROM Roi "
 					+ " WHERE idBacteria = ?" 				 					
 					+ "   AND frameNo = ?");
			
			stat.setInt(1, bactA.getIdBacteria());
			stat.setString(2, bactA.getBactName() + frameNo);
			stat.setInt(3, b.getIdBacteria());			
			stat.setInt(4, frameNo);
			
			stat.executeUpdate();
			
			stat = conn.prepareStatement("INSERT INTO Roi(idBacteria, frameNo, idRoiType, roiBlob, roiName) " 
 					+ " SELECT ?, frameNo, 1, roiBlob, ? "  
 					+ " FROM Roi "
 					+ " WHERE idBacteria = ?" 				 					
 					+ "   AND frameNo = ?");
			
			stat.setInt(1, bactB.getIdBacteria());
			stat.setString(2, bactB.getBactName() + frameNo);
			stat.setInt(3, b.getIdBacteria());			
			stat.setInt(4, frameNo);
			stat.executeUpdate();	
			
			
			stat = conn.prepareStatement("DELETE FROM Roi WHERE idBacteria = ? AND frameNo >= ? ");
			
			stat.setInt(1, b.getIdBacteria());
			stat.setInt(2, frameNo);			
			stat.executeUpdate();
			
			conn.commit();
			
			
			
			return new Bacteria[] {bactA, bactB};
			
			
		} catch (SQLException e) {		
			log.error("Error in splitBacteria", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return null;
		}			
		
	}
	@SuppressWarnings("resource")
	public static String moveBacteriaSplit(Bacteria someChild, int delta) {
						
		String desc = "";
		
		int frameNo;
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {
			stat = conn.prepareStatement("SELECT frameNo, idBacteriaParent, idBacteriaChildA, idBacteriaChildB, bactName as parentName " +
										"  FROM BacteriaSplit" +	
										"  JOIN Bacteria ON idBacteriaParent = idBacteria " +
										" WHERE (idBacteriaChildA = ? OR idBacteriaChildB = ?)");
			stat.setInt(1, someChild.getIdBacteria());
			stat.setInt(2, someChild.getIdBacteria());
			ResultSet rs = stat.executeQuery();
			rs.next();		
			frameNo = rs.getInt("frameNo");
			String bacteriaParentName = rs.getString("parentName").trim(); 
			int idBacteriaParent = rs.getInt("idBacteriaParent");
			int idBacteriaChildA = rs.getInt("idBacteriaChildA");
			int idBacteriaChildB = rs.getInt("idBacteriaChildB");
			
			conn.setAutoCommit(false);  // begin trans
			
			
			// DELETE OLD Split
			stat = conn.prepareStatement("DELETE FROM BacteriaSplit WHERE idBacteriaParent = ?");			
			stat.setInt(1, idBacteriaParent);			
			
			stat.executeUpdate();
			desc += "\nDeleted split at frame " + frameNo;
			
			// Move the split ROIs by:
			// (a) delete the dest:
			stat = conn.prepareStatement("DELETE FROM Roi WHERE idBacteria IN (?, ?, ?) AND frameNo = ?");
			stat.setInt(1, idBacteriaParent);
			stat.setInt(2, idBacteriaChildA);
			stat.setInt(3, idBacteriaChildB);			
			stat.setInt(4, frameNo + delta);			
			
			stat.executeUpdate();
			
			if (delta != 0) {
			
				// move src -> dest
				stat = conn.prepareStatement("UPDATE Roi SET frameNo = ? WHERE frameNo = ? AND idBacteria IN (?, ?, ?)");
				stat.setInt(1, frameNo + delta);		
				stat.setInt(2, frameNo);		
				stat.setInt(3, idBacteriaParent);
				stat.setInt(4, idBacteriaChildA);
				stat.setInt(5, idBacteriaChildB);
				
								
				stat.executeUpdate();
				
				desc += "\nMoved split Rois to frame " + (frameNo + delta);
			}
			
			int minFrame = (delta > 0) ? frameNo: frameNo + delta;
			int maxFrame = (delta > 0) ? frameNo + delta: frameNo;
			
			
			if (delta == 0) {
				// Delete all ROIs (human and computer, all parties involved AND ALL THEIR CHILDREN) after the split
				stat = conn.prepareStatement("DELETE FROM Roi WHERE idBacteria IN (SELECT idBacteria FROM Bacteria WHERE idExperiment = ? AND bactName LIKE '" + bacteriaParentName +  "%' ) AND frameNo >= ?");
				
				stat.setInt(1, CurrentExperiment.getIdExperiment());						
				stat.setInt(2, frameNo);				
				
				stat.executeUpdate();			
				desc += "\nDeleted all Rois (whose name starts with '" + bacteriaParentName + "') from frame " + minFrame + " to the end frame. ";
				
				// Delete all Measurements (all parties involved) between OLD and NEW frame
				stat = conn.prepareStatement("DELETE FROM BacteriaMeasurement WHERE idBacteria IN (SELECT idBacteria FROM Bacteria WHERE idExperiment = ? AND bactName LIKE '" + bacteriaParentName +  "%' ) AND frameNo >= ?");
				
				stat.setInt(1, CurrentExperiment.getIdExperiment());						
				stat.setInt(2, frameNo);	
				
				stat.executeUpdate();			
				desc += "\nDeleted all measurements (whose name starts with '" + bacteriaParentName + "') from frame " + minFrame + " to the end frame. ";
				
			} else {
				// Delete all ROIs (human and computer, all parties involved) between OLD and NEW frame
				stat = conn.prepareStatement("DELETE FROM Roi WHERE idBacteria IN (?, ?, ?) AND frameNo BETWEEN ? AND ?");
				
				stat.setInt(1, idBacteriaParent);
				stat.setInt(2, idBacteriaChildA);
				stat.setInt(3, idBacteriaChildB);			
				stat.setInt(4, minFrame+1);
				stat.setInt(5, maxFrame-1);
				
				stat.executeUpdate();			
				desc += "\nDeleted all Rois (except the-new-split ones) from frame " + minFrame + " to frame " + maxFrame;
				
				// Delete all Measurements (all parties involved) between OLD and NEW frame
				stat = conn.prepareStatement("DELETE FROM BacteriaMeasurement WHERE idBacteria IN (?, ?, ?) AND frameNo BETWEEN ? AND ?");
				
				stat.setInt(1, idBacteriaParent);
				stat.setInt(2, idBacteriaChildA);
				stat.setInt(3, idBacteriaChildB);			
				stat.setInt(4, minFrame);
				stat.setInt(5, maxFrame);
				
				stat.executeUpdate();			
				desc += "\nDeleted all measurements from frame " + minFrame + " to frame " + maxFrame;
			
			
				// Insert new split (Rois are already there :)):
				stat = conn.prepareStatement("INSERT INTO BacteriaSplit(idBacteriaParent, idBacteriaChildA, idBacteriaChildB, frameNo) VALUES (?, ?, ?, ?)");
				stat.setInt(1, idBacteriaParent);
				stat.setInt(2, idBacteriaChildA);
				stat.setInt(3, idBacteriaChildB);
				stat.setInt(4, frameNo + delta);
				
				stat.executeUpdate();
				
				desc += "\nDefined a new split at frame " + (frameNo + delta);
			}
			conn.commit();
			
			
			
			return desc;
			
			
		} catch (SQLException e) {		
			log.error("Error in moveBacteriaSplit", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return "Error occured. Check the log (and/or console).";
		}			
		
	}
	public static Bacteria getOrInsertBacteria(String bactName) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;

		try {
			
			stat = conn.prepareStatement("SELECT idBacteria FROM Bacteria WHERE idExperiment = ? AND bactName = ?");
			stat.setInt(1, CurrentExperiment.getIdExperiment());
			stat.setString(2, bactName);		
			ResultSet rs = stat.executeQuery();
			if (rs.next()) {
				return new Bacteria(rs.getInt(1), CurrentExperiment.getIdExperiment(), bactName); // , rs.getBytes("initialRoiBlob")
			} else {
			
				stat = conn.prepareStatement("INSERT INTO Bacteria (idExperiment, bactName) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
				stat.setInt(1, CurrentExperiment.getIdExperiment());
				stat.setString(2, bactName);					
				stat.executeUpdate();
				ResultSet rs2 = stat.getGeneratedKeys();
				rs2.next();
				return new Bacteria(rs2.getInt(1), CurrentExperiment.getIdExperiment(), bactName);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Error in insertBacteria", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns a computer/humean generated Roi that is stored in a DB.
	 * @param bacteriaId
	 * @param frameNo
	 * @param idRoiType
	 * @return ImageJ Roi object
	 */
	public static Roi getRoiForBacteria(int bacteriaId, int frameNo, int idRoiType) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement("SELECT roiBlob as roi, roiName, bactName FROM Bacteria join Roi on Bacteria.idBacteria = Roi.idBacteria WHERE idRoiType = ? AND Bacteria.idBacteria = ? and frameNo = ?");
			
						
			stat.setInt(1, idRoiType);
			stat.setInt(2, bacteriaId);
			stat.setInt(3, frameNo);
			ResultSet rs = stat.executeQuery();
			if (rs.next()) {
				return new Roi(
						bacteriaId, 
						frameNo, 
						rs.getBytes("roi"), 
						rs.getString("roiName"),
						idRoiType
						);
			} else {
				return null;
			}
			
		} catch (SQLException e) {		
			log.error("Error in getRoiForBacteria", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return null;
		}		
		
	}
	
	public static boolean hasHumanRoi(int bacteriaId, int frameNo) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement("SELECT COUNT(*) as cnt FROM Bacteria join Roi on Bacteria.idBacteria = Roi.idBacteria WHERE idRoiType = 1 AND Bacteria.idBacteria = ? and frameNo = ?");
			
						
			
			stat.setInt(1, bacteriaId);
			stat.setInt(2, frameNo);
			ResultSet rs = stat.executeQuery();
			if (rs.next()) {
				
				return (rs.getInt("cnt") == 1);
				
			} else {
				return false;
			}
			
		} catch (SQLException e) {		
			log.error("Error in hasHumanRoi", e);
			e.printStackTrace();
			
		}
		return false;		
		
	}	
	
	
	
	public static boolean updateRoiForBacteria(Roi dbRoi) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {
			conn.setAutoCommit(false);
			stat = conn.prepareStatement("DELETE FROM Roi WHERE idBacteria = ? AND frameNo = ? AND idRoiType = ?");
			
			stat.setInt(1, dbRoi.getIdBacteria());
			stat.setInt(2, dbRoi.getFrameNo());
			stat.setInt(3, dbRoi.getIdRoiType());
			stat.executeUpdate();
			

			stat = conn.prepareStatement("INSERT INTO Roi(idBacteria, frameNo, roiBlob, roiName, idRoiType) VALUES (?, ?, ?, ?, ?)");
			stat.setInt(1, dbRoi.getIdBacteria());
			stat.setInt(2, dbRoi.getFrameNo());
			stat.setBytes(3, dbRoi.getRoiBlob());
			stat.setString(4, dbRoi.getRoiName());
			stat.setInt(5, dbRoi.getIdRoiType());
			stat.executeUpdate();
			
			conn.commit();
			
			return true;
		} catch (SQLException e) {		
			log.error("Error in updateRoiForBacteria", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}			
	}

	public static boolean deleteAllMeasurementsToBeMeasured(int idExperiment) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement(
					"DELETE FROM BacteriaMeasurement " +
					" WHERE idBacteria IN (SELECT idBacteria FROM Bacteria WHERE idExperiment = ?)" +
					"   AND EXISTS (SELECT * FROM ExperimentMeasurements " +
					"                       WHERE idExperiment = ? " +
					"                         AND ExperimentMeasurements.idMeasurement = BacteriaMeasurement.idMeasurement" +
					"                         AND ExperimentMeasurements.idChannel     = BacteriaMeasurement.idChannel )");
			
			stat.setInt(1, idExperiment);
			stat.setInt(2, idExperiment);
			stat.executeUpdate();

			
			return true;
		} catch (SQLException e) {		
			log.error("Error in deleteAllMeasurementsToBeMeasured", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}			
	}	
	

	
	
	public static BacteriaStateChange[] getAllBacteriaStateChanges (int idExperiment) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;		
		ArrayList<BacteriaStateChange> items;
		try {
			
			stat = conn.prepareStatement(
					"SELECT BacteriaStateChange.idBacteria, frameNo, BacteriaStateChange.idState, stateName, stateTag, bactName " + 
							"FROM BacteriaStateChange " + 
							"JOIN BacteriaState ON BacteriaStateChange.idState = BacteriaState.idState " +
							"JOIN Bacteria ON BacteriaStateChange.idBacteria = Bacteria.idBacteria " + 
							"WHERE BacteriaStateChange.idBacteria IN " + 
							"        (SELECT idBacteria " + 
							"           FROM Bacteria " + 
							"          WHERE idExperiment = ?) " + 
							"ORDER BY frameNo, " + 
							"         BacteriaStateChange.idBacteria; "					
					);
			stat.setInt(1, idExperiment);
			ResultSet rs = stat.executeQuery();
			items = new ArrayList<BacteriaStateChange>();
			while (rs.next()) {					
				
				//     	public BacteriaStateChange(int idBacteria, int frameNo, int idState, String stateName, String stateTag, String bName) {
				items.add(new BacteriaStateChange(rs.getInt("idBacteria"), rs.getInt("frameNo"), rs.getInt("idState"), rs.getString("stateName"), rs.getString("stateTag"), rs.getString("bactName")));								
			}						
			return items.toArray(new BacteriaStateChange[items.size()]);	
						
			
		} catch (Exception e) {
			log.error("Error in getAllBacteriaStateChanges()" , e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
		
	}	
	
	public static String getBacteriaStateAt (Bacteria b, int frameNo) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;				
		try {
			
			stat = conn.prepareStatement(
							"SELECT stateName" +
							"  FROM bacteriastatechange" +
							"  JOIN bacteriastate ON bacteriastatechange.idState = bacteriastate.idState" +
							" WHERE frameNo =" +
							"    (SELECT max(frameNo)" +
							"     FROM bacteriastatechange" +
							"     WHERE idBacteria = ?" +
							"       AND frameNo <= ?)" +
							"   AND idBacteria = ?"					
					);
			stat.setInt(1, b.getIdBacteria());
			stat.setInt(2, frameNo);
			stat.setInt(3, b.getIdBacteria());
			
			ResultSet rs = stat.executeQuery();
			
			if (rs.next()) {									
				return rs.getString("stateName");								
			} else {						
				return null;
			}
						
			
		} catch (Exception e) {
			log.error("Error in getBacteriaStateAt()" , e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
		
	}		
	
	public static ExperimentEvent[] getAllEvents (int idExperiment) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;		
		ArrayList<ExperimentEvent> events;
		try {
			
			stat = conn.prepareStatement("SELECT frameNo, eventDesc, eventAbbr FROM ExperimentEvent WHERE idExperiment = ? Order by frameNo");
			stat.setInt(1, idExperiment);
			ResultSet rs = stat.executeQuery();
			events = new ArrayList<ExperimentEvent>();
			while (rs.next()) {							
				events.add(new ExperimentEvent(idExperiment, rs.getInt("frameNo"), rs.getString("eventDesc"), rs.getString("eventAbbr")));								
			}						
			return events.toArray(new ExperimentEvent[events.size()]);	
						
			
		} catch (Exception e) {
			log.error("Error in getAllEvents()" , e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
		
	}
	public static BacteriaState[] getAllBacteriaStates() {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;		
		ArrayList<BacteriaState> states;
		try {
			
			stat = conn.prepareStatement("SELECT idState, stateName, stateTag FROM BacteriaState ORDER BY idState");
			
			ResultSet rs = stat.executeQuery();
			states = new ArrayList<BacteriaState>();
			while (rs.next()) {							
				states.add(new BacteriaState( rs.getInt("idState"), rs.getString("stateName"), rs.getString("stateTag")));								
			}						
			return states.toArray(new BacteriaState[states.size()]);	
						
			
		} catch (Exception e) {
			log.error("Error in getAllBacteriaStates()" , e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public static boolean setStateChange(int idBacteria, int frameNo, int idState) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {
			stat = conn.prepareStatement("DELETE FROM BacteriaStateChange WHERE idBacteria = ? ANd frameNo = ?");			
			stat.setInt(1,idBacteria);
			stat.setInt(2,frameNo);			
			stat.executeUpdate();
			
			if (idState != -1) {
			
				stat = conn.prepareStatement("INSERT INTO BacteriaStateChange(idBacteria, frameNo, idState) VALUES (?, ?, ?)");
				stat.setInt(1, idBacteria);
				stat.setInt(2, frameNo);
				stat.setInt(3, idState);			
				stat.executeUpdate();			
			}
			return true;
			
		} catch (SQLException e) {		
			log.error("Error in setStateChange", e);
			e.printStackTrace();				
			return false;
		}			
	}	
	public static boolean setExperimentEvent(int idExperiment, int frameNo, String  eventAbbr, String eventDesc) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {
			stat = conn.prepareStatement("DELETE FROM ExperimentEvent WHERE idExperiment = ? ANd frameNo = ?");			
			stat.setInt(1,idExperiment);
			stat.setInt(2,frameNo);			
			stat.executeUpdate();
			
			if (!eventAbbr.equals("") && !eventDesc.equals("")) {
			
				stat = conn.prepareStatement("INSERT INTO ExperimentEvent(idExperiment, frameNo, eventDesc, eventAbbr) VALUES (?, ?, ?, ?)");
				stat.setInt(1, idExperiment);
				stat.setInt(2, frameNo);
				stat.setString(3, eventDesc);
				stat.setString(4, eventAbbr);
				stat.executeUpdate();			
			}
			return true;
			
		} catch (SQLException e) {		
			log.error("Error in insertExperimentEvent", e);
			e.printStackTrace();				
			return false;
		}			
	}
	
	public static boolean insertBacteriaMeasurements(LinkedList<BacteriaMeasurement> measurements) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {
			conn.setAutoCommit(false);

			
			for (BacteriaMeasurement bm : measurements) {
//				stat = conn.prepareStatement("DELETE FROM BacteriaMeasurement WHERE idBacteria = ? AND frameNo = ? AND idRoiType = ? AND idChannel = ?");
//				
//				stat.setInt(1, bm.gebacteriaId);
//				stat.setInt(2, frameNo);
//				stat.setInt(3, idRoiType);
//				stat.setInt(4, dfgdf);
//				stat.executeUpdate();
				
				stat = conn.prepareStatement("INSERT INTO BacteriaMeasurement(idBacteria, frameNo, idMeasurement, idRoiType,  value, idChannel) VALUES (?, ?, ?, ?, ?, ?)");
				stat.setInt(1, bm.getIdBacteria());
				stat.setInt(2, bm.getFrameNo());
				stat.setInt(3, bm.getIdMeasurement());
				stat.setInt(4,  bm.getIdRoiType());
				stat.setDouble(5, bm.getValue());
				stat.setDouble(6, bm.getIdChannel());
				stat.executeUpdate();
			}
			
			conn.commit();
			
			return true;
		} catch (SQLException e) {		
			log.error("Error in updateBacteriaMeasurements", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}			
	}
	
	
	public static boolean updateFrameTranslation(int frameNo, VPoint trans) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement("UPDATE Frame SET transX = ? , transY = ? WHERE idExperiment = ? AND frameNo = ? ");
			
			stat.setInt(1, trans.x);
			stat.setInt(2, trans.y);
			stat.setInt(3,  CurrentExperiment.getIdExperiment());
			stat.setInt(4,  frameNo);
			stat.executeUpdate();
			
			
			return true;

		} catch (SQLException e) {		
			log.error("Error in updateFrameTranslation", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}			
	}	
	
	
	public static boolean updateFrameAlgorithm(int idExperiment, int  frameFrom, int frameTo, String algorithmName) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement("UPDATE Frame SET algorithm = ? WHERE idExperiment = ? AND frameNo BETWEEN ? AND ? ");
			
			stat.setString(1, algorithmName);			
			stat.setInt(2,  idExperiment);
			stat.setInt(3,  frameFrom);
			stat.setInt(4,  frameTo);
			stat.executeUpdate();
			
			
			return true;

		} catch (SQLException e) {		
			log.error("Error in updateFrameAlgorithm", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}	
		
	}
	public static boolean updateFrameBackgroundGreenMean(int idExperiment, int frameNo, double mean) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement("UPDATE Frame SET bgGreenMean = ? WHERE idExperiment = ? AND frameNo = ? ");
			
			stat.setDouble(1, mean);			
			stat.setInt(2,  idExperiment);
			stat.setInt(3,  frameNo);
			stat.executeUpdate();
			
			
			return true;

		} catch (SQLException e) {		
			log.error("Error in updateFrameBackgroundMean", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}			
	}	
	public static boolean updateFrameBackgroundRedMean(int idExperiment, int frameNo, double mean) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement("UPDATE Frame SET bgRedMean = ? WHERE idExperiment = ? AND frameNo = ? ");
			
			stat.setDouble(1, mean);			
			stat.setInt(2,  idExperiment);
			stat.setInt(3,  frameNo);
			stat.executeUpdate();
			
			
			return true;

		} catch (SQLException e) {		
			log.error("Error in updateFrameBackgroundRedMean", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}			
	}		
	public static boolean updateFrameBackgroundRGBMean(int idExperiment, int frameNo, double redMean, double greenMean, double blueMean) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement("UPDATE Frame SET bgRedMean = ?, bgGreenMean = ?, bgBlueMean = ? WHERE idExperiment = ? AND frameNo = ? ");
			
			stat.setDouble(1, redMean);			
			stat.setDouble(2, greenMean);
			stat.setDouble(3, blueMean);
			stat.setInt(4,  idExperiment);
			stat.setInt(5,  frameNo);
			stat.executeUpdate();
			
			
			return true;

		} catch (SQLException e) {		
			log.error("Error in updateFrameBackgroundRGBMean", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}			
	}		
	
	
	public static boolean toggleIgnoreFrame(int idExperiment, int frameNo) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			conn.setAutoCommit(false);
			stat = conn.prepareStatement( " update Frame  " +
					"  set ignoreFrame = (CASE WHEN ignoreFrame = 'n' then 'y' ELSE 'n' END) " +
					"where idExperiment = ? " +
					"  and frameNo = ? ");
		
			stat.setInt(1,  idExperiment);
			stat.setInt(2,  frameNo);
			stat.executeUpdate();
			
			
			
			stat = conn.prepareStatement("delete from BacteriaMeasurement " +
										 " where frameNo = (select frameNo from Frame where ignoreFrame = 'y' and idExperiment = ? and frameNo = ?) " +
										 "   and idBacteria in (select idBacteria from Bacteria where idExperiment = ?) ");
			
			stat.setInt(1, CurrentExperiment.getIdExperiment());
			stat.setInt(2, frameNo);
			stat.setInt(3, CurrentExperiment.getIdExperiment());
			
			stat.executeUpdate();
			
			conn.commit();			
						
			return true;

		} catch (SQLException e) {		
			log.error("Error in toggleIgnoreFrame", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return false;
		}			
	}	
	
	public static VPoint getFrameTranslation(int frameNo) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {

			stat = conn.prepareStatement("SELECT transX, transY FROM Frame WHERE idExperiment = ? and frameNo = ?");
			
						
			stat.setInt(1,  CurrentExperiment.getIdExperiment());
			stat.setInt(2, frameNo);
			ResultSet rs = stat.executeQuery();
			if (rs.next()) {
				return new VPoint(						 
						rs.getInt("transX"), 
						rs.getInt("transY")
						);
			} else {
				return null;
			}
			
		} catch (SQLException e) {		
			log.error("Error in getFrameTranslation", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			return null;
		}
					
	}	
	public static int getChannelId(String channelName) {
		if (channelName.trim().toLowerCase().equals("red")) {
			return 1;
		} else if (channelName.trim().toLowerCase().equals("green")) {
			return 2;
		} else if (channelName.trim().toLowerCase().equals("blue")) {
			return 3;
		} else if (channelName.trim().toLowerCase().equals("ignore")) {
			return 0;
		}
		assert (false) : "wtf channel?" + channelName;
		return -1;
	}
	
	public static Hashtable<String, ExperimentMeasurements> getExperimentMeasures(int idExperiment) {
		
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		Hashtable<String, ExperimentMeasurements> measurements = new Hashtable<String, ExperimentMeasurements>();
		try {

			stat = conn.prepareStatement(
							"SELECT measurementName, channelName, collarSize                          " + 
							" FROM ExperimentMeasurements                                             " +
							"    JOIN Measurement                                                     " +
							"      ON ExperimentMeasurements.idMeasurement = Measurement.idMeasurement" +
							"    JOIN Channel                                                         " +
							"      ON ExperimentMeasurements.idChannel = Channel.idChannel            " +
							" WHERE idExperiment = ?                                                 "					
					);
						
			stat.setInt(1, idExperiment);
			ResultSet rs = stat.executeQuery();
			while (rs.next()) {
				measurements.put(
						rs.getString("measurementName"),
						new ExperimentMeasurements(
								rs.getInt("collarSize"),
								rs.getString("measurementName"),
								rs.getString("channelName")
								)
				);
			} 
			
		} catch (SQLException e) {		
			log.error("Error in getExperimentMeasures", e);
			e.printStackTrace();
			return null;
		}
		
		return measurements;
				
		
	}		
	public static void updateExperimentMeasures(Hashtable<String, ExperimentMeasurements> measures, int idExperiment) {
		
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		try {
			conn.setAutoCommit(false);
			stat = conn.prepareStatement("DELETE FROM ExperimentMeasurements WHERE idExperiment = ?");
			stat.setInt(1,idExperiment);
			stat.executeUpdate();
			
			for (ExperimentMeasurements m : measures.values()) {
				
				stat = conn.prepareStatement("INSERT INTO ExperimentMeasurements(idExperiment, idMeasurement, idChannel, collarSize) VALUES (?, ?, ?, ?)");
				stat.setInt(1, idExperiment);
				stat.setInt(2, getMeasurementId(m.getMeasurementName()));
				stat.setInt(3, getChannelId(m.getChannelName()));
				stat.setInt(4, m.getCollarSize());
				stat.executeUpdate();
			}
						
			conn.commit();
			
			
		} catch (SQLException e) {		
			log.error("Error in updateRoiForBacteria", e);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			
		}			
		
	}	
	
	public static int getMeasurementId(String measurementName) {
		Integer value = cachedMeasurements.get(measurementName.trim().toLowerCase());
		if (value == null) {
			cacheMeasurementsCatalog();
			value = cachedMeasurements.get(measurementName.trim().toLowerCase());
		}
		if (value == null) {
			Connection conn = ConnectionManager.getConnection();
			PreparedStatement stat = null;
			try {
				
				stat = conn.prepareStatement("INSERT INTO Measurement (measurementName) VALUES (?)");				
				stat.setString(1, measurementName.trim().toLowerCase());		
				stat.executeUpdate();
								
			} catch (SQLException e) {				
				log.error("Error in getMeasurementId", e);
				e.printStackTrace();
			}
			cacheMeasurementsCatalog();
			value = cachedMeasurements.get(measurementName.trim().toLowerCase());
		}
		return cachedMeasurements.get(measurementName.trim().toLowerCase());		
	}
	private static HashMap<String, Integer> cachedMeasurements = new HashMap<String, Integer>();
	private static void cacheMeasurementsCatalog() {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stat = null;
		 
		try {
			conn.setAutoCommit(false);
			stat = conn.prepareStatement("SELECT * FROM Measurement");
			
			ResultSet rs = stat.executeQuery();
			while (rs.next()) {
				cachedMeasurements.put(rs.getString("measurementName").trim(), rs.getInt("idMeasurement"));
			}			
			
		} catch (SQLException e) {		
			log.error("Error in cacheMeasurementsCatalog", e);
			e.printStackTrace();			
		}			
	}
}
