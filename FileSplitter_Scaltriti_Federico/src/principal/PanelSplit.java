package principal;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import split.Split;

public class PanelSplit extends JPanel implements ActionListener{
	private JComboBox modValue;
	private Vector<Split> v;
	private MyTableModel dataModel;
	private JButton browse, split, browseDestination;
	private JTable t;
	private JTextField dimField, partsField, destinationFolder;
	private JLabel dimLabel;
	private JComboBox comboBox;
	
	public PanelSplit(){
		super();
		v = new Vector<Split>();
		
		comboBox = new JComboBox();
		comboBox.addItem("Splitter");
		comboBox.addItem("Crypter");
		comboBox.addItem("Zipper");
		comboBox.addItem("Parts");
		
		// crea il modello di dati a partire dal vettore
		dataModel = new MyTableModel(v);
		// crea la tabella
		t = new JTable(dataModel);
		t.getColumnModel().getColumn(1).setPreferredWidth(200);
		t.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBox));
		
		// aggiunge la tabella al pannello
		add(t);		
		
		browse = new JButton("Naviga");
		browse.addActionListener(this);
		add(browse);
		
		dimLabel = new JLabel("Dimensione");
		add(dimLabel);
		
		dimField = new JTextField("Dimensione");
		dimField.setSize(20, 50);
		add(dimField);
		
		partsField = new JTextField("N Parti");
		partsField.setSize(20, 20);
		add(partsField);
		
		split = new JButton("Dividi");
		split.addActionListener(this);
		add(split);
		
	}
	
	//ogni volta che seleziono dei file, ripulisco la tabella
	private void clearTable(){
		v.removeAllElements();
		dataModel.getDataVector().removeAllElements();
	}
	
	//azione del bottone "cerca"
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Naviga")){
			clearTable();
			JFileChooser fileChooser = new JFileChooser();
			//per abilitare la selezione multipla di file
			fileChooser.setMultiSelectionEnabled(true);
			//per impostare la directory iniziale in cui cercare
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			
			int result = fileChooser.showOpenDialog(this);
			
			// se l'utente seleziona un file
			if (result == JFileChooser.APPROVE_OPTION) {
				File[] selectedFiles = fileChooser.getSelectedFiles();
	
				for (File file : selectedFiles) {
					 Split sp = new Split(file.getName(), file.getAbsolutePath(), 4, 102400, file.length());
					 v.add(sp);
					 int i = v.size(); //mi salvo la dimensione perché addRow la aumenta di 4
					 dataModel.addRow(v);
					 v.setSize(i);
					 System.out.println("Selected file: " + file.getAbsolutePath());
				}						
			}
		}else{
			
		}
		
	}

}
