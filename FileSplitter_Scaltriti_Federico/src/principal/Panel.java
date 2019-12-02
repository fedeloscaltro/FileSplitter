package principal;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.TableModel;

public class Panel extends JPanel implements ActionListener{

	@Override
	public void paint(Graphics g){
		super.paint(g);
		Vector<?> f = null;
		// crea il modello di dati
		TableModel dataModel = new MyTableModel(f);
		// crea la tabella
		JTable t = new JTable(dataModel);
		// aggiunge la tabella al pannello
		add(t);
	}
	
	public Panel(){
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
