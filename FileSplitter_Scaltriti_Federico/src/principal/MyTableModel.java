package principal;

import java.util.Vector;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import split.Split;

public class MyTableModel extends DefaultTableModel{
	
	private static final long serialVersionUID = 1L;
	private Vector v = null;
	Split sp;
	int offset = 0;
	// intestazioni delle colonne
	private String[] ColName = {"Nome", "Percorso", "Dimensione", "Azione"};

	public MyTableModel(Vector v) {
	this.v = v; // inizializzato con il vettore
	}
	
	@Override
	public int getColumnCount() {
		return ColName.length;
	}

	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// seleziona il file		
		sp = (Split) v.elementAt(rowIndex);
		// la stringa corrispondente alla colonna
		switch (columnIndex){

			case 0: return sp.getFileName();
			case 1: return sp.getFullPath();
			case 2: return sp.getSourceSize();
			case 3: 
				switch(sp.getClass().getCanonicalName()){
					case "split.Split":
						return "Split";
					case "split.Crypto":
						return "Crypto";
					case "split.Zip":
						return "Zip";
					case "split.SplitTimes":
						return "Split Times";
				}
				return sp.getClass().getCanonicalName();
							
			default: return null;
		}
	}

	// specifica se le celle sono editabili
	@Override
	public boolean isCellEditable(int row, int col) {
		// nessuna cella editabile
		return false;
	}
	
	/** restituisce il nome della colonna */
	public String getColumnName(int col) {
		return ColName[col];
	}

	/** restituisce il tipo dei valori
	* serve per allineare correttamente i numeri */
	public Class getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}
	
	public void setValueAt(Object value, int row, int col){
		
		fireTableDataChanged();
	}
}
