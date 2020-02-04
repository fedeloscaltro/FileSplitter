package gui;

import java.awt.CardLayout;
import java.io.File;
import java.io.RandomAccessFile;

import javax.swing.*;

import merge.Merger;
import split.Crypto;
import split.Split;
import split.SplitTimes;
import split.Zip;

public class Main {

	/**
     * Metodo main() in cui inizia l'esecuzione del programma.
     * Viene creata la finestra principale e inizializzati i componenti.
     * @param args Argomenti inseriti da riga di comando.
     */
	public static void main(String[] args) throws Exception{
	
		//imposto il frame contenitore
		JFrame frame = new JFrame("File Splitter");
		//imposto l'azione in caso di chiusura frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		//elemento contenitore per i 2 Tab
		JTabbedPane tabPane = new JTabbedPane();
		
		//imposto i 2 pannelli
		PanelSplit splitPanel = new PanelSplit();
		PanelMerge mergePanel = new PanelMerge();
		
		tabPane.addTab("Split", splitPanel);
		tabPane.addTab("Merge", mergePanel);
		
		frame.setBounds(0, 0, 700, 550);
		frame.setResizable(false);

		frame.add(tabPane);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
    }	 
}
