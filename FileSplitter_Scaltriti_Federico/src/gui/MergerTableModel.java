package gui;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import merge.Merger;

public class MergerTableModel  extends DefaultTableModel{

	/**
	 * Vettore di dati da aggiungere alla tabella
	 * */
	private Vector<Merger> v = null;
	
	/**
	 * Oggetto contenente i dati per la tabella
	 * */
	private Merger m;
	
	/**
	 * Array contenente le intestazioni delle colonne
	 * */ 
	private String[] ColName = {"Nome", "Percorso"};

	/**
	 * Costruttore del TableModel
	 * @param v vettore di dati da aggiungere
	 * */
	public MergerTableModel(Vector<Merger> v) {
	this.v = v;
	}
	
	/**
	 * Metodo che restituisce il numero di colonne
	 * */
	@Override
	public int getColumnCount() {
		return ColName.length;
	}

	/**
	 * Metodo per la restituzione dei valori appropriati all'interno della tabella
	 * */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// seleziona il file		
		m = (Merger) v.elementAt(rowIndex);
		
		// la stringa corrispondente alla colonna
		switch (columnIndex){

			case 0: return m.getFileName();
			case 1: return m.getFilePath();
			default: return null;
		}
	}

	/**
	 * Metodo che specifica se le celle sono editabili
	 * */
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
	
	/** Metodo che imposta i valori all'interno della tabella. */
	public void setValueAt(Object value, int row, int col){
		fireTableDataChanged();
	}
}
