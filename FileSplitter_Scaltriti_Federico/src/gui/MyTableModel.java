package gui;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import split.Split;

public class MyTableModel extends DefaultTableModel{
	
	/**
	 * Vettore di dati da aggiungere alla tabella
	 * */
	private Vector<Split> v = null;
	
	/**
	 * Oggetto contenente i dati per la tabella
	 * */
	private Split sp;
	
	/**
	 * Array contenente le intestazioni delle colonne
	 * */ 
	private String[] ColName = {"Nome", "Percorso", "Dimensione", "Azione"};

	/**
	 * Costruttore del TableModel
	 * @param v vettore di dati da aggiungere
	 * */
	public MyTableModel(Vector<Split>v) {
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
		sp = (Split) v.elementAt(rowIndex);
		
		// la stringa corrispondente alla colonna
		switch (columnIndex){

			case 0: return sp.getFullFileName();
			case 1: return sp.getFullPath();
			case 2: return sp.getSourceSize();
			case 3: 
				switch(sp.getDecision()){
					case 1:
						return "Split";
					case 2:
						return "Crypto";
					case 3:
						return "Zip";
					case 4:
						return "Split Times";
				}
				return sp.getDecision();
							
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
	
	/** Metodo che restituisce il nome della colonna */
	public String getColumnName(int col) {
		return ColName[col];
	}

	/** Metodo che restituisce il tipo dei valori.
	* Serve per allineare correttamente i numeri */
	public Class getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}
	
	/** Metodo che imposta i valori all'interno della tabella. */
	public void setValueAt(Object value, int row, int col){
		
		fireTableDataChanged();
	}
}
