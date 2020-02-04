package gui;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import merge.Merger;

public class MergerTableModel  extends DefaultTableModel{
	private static final long serialVersionUID = 1L;
	private Vector<Merger> v = null;
	Merger m;
	int offset = 0;
	// intestazioni delle colonne
	private String[] ColName = {"Nome", "Percorso"};

	public MergerTableModel(Vector<Merger> v) {
	this.v = v; // inizializzato con il vettore
	}
	
	@Override
	public int getColumnCount() {
		return ColName.length;
	}

	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// seleziona il file		
		m = (Merger) v.elementAt(rowIndex);
		
		// la stringa corrispondente alla colonna
		switch (columnIndex){

			case 0: return m.getFileName();
			case 1: return m.getFilePath();
			case 2: return m.getSize();
			default: return null;
		}
	}

	// specifica se le celle sono editabili
	@Override
	public boolean isCellEditable(int row, int col) {
		// nessuna cella editabile
		return false;
	}
	
	/** Metodo per restituire il nome della colonna
	 *  */
	public String getColumnName(int col) {
		return ColName[col];
	}

	/** restituisce il tipo dei valori
	* serve per allineare correttamente i numeri */
//	public Class getColumnClass(int col) {
//		return getValueAt(0, col).getClass();
//	}
	
	public void setValueAt(Object value, int row, int col){
		
		fireTableDataChanged();
	}
}
