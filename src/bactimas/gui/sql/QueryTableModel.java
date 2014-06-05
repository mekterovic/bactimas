package bactimas.gui.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import bactimas.db.ConnectionManager;

public class QueryTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1661574317605316512L;

	Vector<String[]> cache; // will hold String[] objects . . .

	int colCount;

	String[] headers;

	Connection db;

	// Statement statement;

	String currentURL;

	public QueryTableModel() {
		cache = new Vector<String[]>();
	}

	public String getColumnName(int i) {
		return headers[i];
	}

	public int getColumnCount() {
		return colCount;
	}

	public int getRowCount() {
		return cache.size();
	}

	public Object getValueAt(int row, int col) {
		return ((String[]) cache.elementAt(row))[col];
	}

	// All the real work happens here; in a real application,
	// we'd probably perform the query in a separate thread.
	public void runQuery(String q) throws Exception{
		cache = new Vector<String[]>();
		try {
			Connection conn = ConnectionManager.getConnection();
			Statement stat =  conn.createStatement();
			// Execute the query and store the result set and its metadata
			ResultSet rs = stat.executeQuery(q);
			ResultSetMetaData meta = rs.getMetaData();
			colCount = meta.getColumnCount();

			// Now we must rebuild the headers array with the new column names
			headers = new String[colCount];
			for (int h = 1; h <= colCount; h++) {
				headers[h - 1] = meta.getColumnName(h);
			}

			// and file the cache with the records from our query. This would
			// not be
			// practical if we were expecting a few million records in response
			// to our
			// query, but we aren't, so we can do this.
			while (rs.next()) {
				String[] record = new String[colCount];
				for (int i = 0; i < colCount; i++) {
					record[i] = rs.getString(i + 1);
				}
				cache.addElement(record);
			}
			fireTableChanged(null); // notify everyone that we have a new table.
		} catch (Exception e) {
			cache = new Vector<String[]>(); // blank it out and keep going.
			e.printStackTrace();
			throw e;
		}
	}

}