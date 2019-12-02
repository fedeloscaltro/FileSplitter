package principal;

import java.util.Vector;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class MyTableModel extends AbstractTableModel{

	private Vector v = null;
	// intestazioni delle colonne
	private String[] ColName = {"Name", "Path", "Dimensions", "Action"};

	public MyTableModel(Vector v) {

	this.v = v; // inizializzato con il vettore

	}
	
	@Override
	public int getColumnCount() {
		return ColName.length;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
