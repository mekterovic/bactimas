package bactimas.gui.sql;

/*
 Java Swing, 2nd Edition
 By Marc Loy, Robert Eckstein, Dave Wood, James Elliott, Brian Cole
 ISBN: 0-596-00408-7
 Publisher: O'Reilly 
 */
// DatabaseTest.java
//Let's try to make one of these databases work with a JTable for ouptut.
//

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import bactimas.db.ConnectionManager;
import au.com.bytecode.opencsv.CSVWriter;

public class SQLQueryPane extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8615052258516395164L;

	JTextArea queryField;
	JLabel statusBar;

	QueryTableModel qtm;

	private void setStatus(String s) {
		statusBar.setText(s);
	}
	
	public SQLQueryPane() {
		super("Database Query Pane: enter your sql query and hit run.");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(350, 200);

		qtm = new QueryTableModel();
		JTable table = new JTable(qtm);
		JScrollPane scrollpane = new JScrollPane(table);
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());

		p1.add(queryField = new JTextArea(10, 0), BorderLayout.CENTER);
		//queryField.setText("\nSELECT *\n FROM Experiment\nORDER BY idExperiment\n\n\n");
		queryField.setText("\nSELECT bact_name,\n       generation,\n       1000 * growth_rate,\n       ROUND(min(green_mean),2) AS min_flo,\n       ROUND(max(green_mean),2) AS max_flo,\n       ROUND(avg(green_mean),2) AS avg_flo\n  FROM vBacteriaMeasurement\n WHERE id_experiment = 1038\nGROUP BY bact_name,\n         generation,\n         growth_rate\nORDER BY bact_name,\n         generation;\n");
		Font font = new Font("Courier New", Font.BOLD, 12);
		queryField.setFont(font);
		queryField.setForeground(Color.BLUE);

		JButton jbRun = new JButton("Run");
		jbRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					qtm.runQuery(queryField.getText().trim());
				} catch (Exception e1) {
					setStatus("Error executing SQL: " + e1.getMessage());
					
				}
			}
		});
		
		
		JButton jbDumpCSV = new JButton("Dump to CSV");
		jbDumpCSV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();

				int retval = fc.showSaveDialog(null);

				if (retval == JFileChooser.APPROVE_OPTION) {
					File csvFile = fc.getSelectedFile();

					String delimiter;
					delimiter = JOptionPane.showInputDialog(null, "Delimiter",
							";");
					CSVWriter writer;

					try {

						Connection conn = ConnectionManager.getConnection();
						PreparedStatement stat = null;
						stat = conn.prepareStatement(queryField.getText());
						ResultSet rs = stat.executeQuery();

						writer = new CSVWriter(new FileWriter(csvFile),
								delimiter.charAt(0));
						writer.writeAll(rs, true);
						writer.close();
						setStatus("Dumped to " + csvFile.getAbsolutePath());
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						setStatus("Error " + ex.getMessage());
						ex.printStackTrace();
					} catch (SQLException ex) {
						// TODO Auto-generated catch block
						setStatus("Error " + ex.getMessage());
						ex.printStackTrace();
					}
				}
			}

		});

		JPanel toolbar = new JPanel();
		toolbar.setLayout(new FlowLayout());		
		
		toolbar.add(jbRun);
		toolbar.add(jbDumpCSV);
		
		p1.add(toolbar, BorderLayout.SOUTH);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, p1,
				scrollpane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(.5);

		// getContentPane().add(p1, BorderLayout.NORTH);
		// getContentPane().add(scrollpane, BorderLayout.CENTER);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		getContentPane().add(statusBar = new JLabel("Ready."), BorderLayout.SOUTH);
		this.setExtendedState(Frame.MAXIMIZED_BOTH | this.getExtendedState());
	}

	public static void main(String args[]) {
		SQLQueryPane tt = new SQLQueryPane();
		tt.setVisible(true);
	}
}

// QueryTableModel.java
// A basic implementation of the TableModel interface that fills out a Vector of
// String[] structures from a query's result set.
//
