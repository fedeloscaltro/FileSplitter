package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import merge.Merger;
import split.Crypto;
import split.Split;
import split.SplitTimes;
import split.Zip;

public class PanelMerge extends JPanel implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MergerTableModel dataModel;
	private Vector<Merger> v;
	private JTable t;
	private JTextField destinationFolderField;
	private JButton browse, browseDestination, settingsOk, delete, merge, okPasswd;
	private JLabel fileNameLabel, errorLabel;
	private JDialog choiceDialog, messageDialog;
	private Vector<String> destinations;
	private Vector<File> totalFiles;
	private File selectedFile;
	private String password;
	
	public PanelMerge(){
		super();
		v = new Vector<Merger>();
		destinations = new Vector<String>();
		totalFiles = new Vector<File>();
		
		JPanel up = new JPanel();
		JPanel down = new JPanel();
		
		// crea il modello di dati
		dataModel = new MergerTableModel(v);
		// crea la tabella
		t = new JTable(dataModel);
		t.getColumnModel().getColumn(0).setPreferredWidth(150);
		t.getColumnModel().getColumn(1).setPreferredWidth(250);
		
		// aggiunge la tabella al pannello
		add(t);
		
		JScrollPane jsp = new JScrollPane(t);
		up.add(jsp);
		
		/*--------------------------------------------------------------------------------*/
		
		//inizializzazione componenti
		destinationFolderField = new JTextField("Destinazione");
		destinationFolderField.setEditable(false);
		
		browseDestination = new JButton("Cerca");
		browseDestination.addActionListener(this);
		
		settingsOk = new JButton("OK");
		settingsOk.addActionListener(this);
		
		browse = new JButton("Naviga");
		browse.addActionListener(this);
		down.add(browse);
		
		delete = new JButton("Elimina");
		delete.addActionListener(this);
		down.add(delete);
		
		merge = new JButton("Unisci");
		merge.addActionListener(this);
		down.add(merge);
		
		add(up);
		add(down);
	}
	public void setDialogLayout(JLabel fileName, JPasswordField psswdField, JPanel contentPane){
		JLabel passwdLabel = new JLabel("Password");
		choiceDialog.add(passwdLabel);
		
		GroupLayout groupLayout= new GroupLayout(contentPane);
		contentPane.setLayout(groupLayout);
		groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(fileName)
						.addComponent(destinationFolderField))
				.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(browseDestination)
						.addComponent(passwdLabel)
						.addComponent(psswdField))
				.addComponent(settingsOk));
		
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
			.addComponent(fileName)
			.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
					.addComponent(destinationFolderField)
					.addComponent(browseDestination))
			.addComponent(passwdLabel)
			.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
					.addComponent(psswdField)
					.addComponent(settingsOk)));
		
		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
	}
	
	public void setChoiceDialog(String currentFile, boolean flag){
		
		JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
		choiceDialog = new JDialog(topFrame);
		char letter = currentFile.charAt(currentFile.lastIndexOf(".")-1);
		
		JPasswordField passwdField = new JPasswordField();
		passwdField.setSize(70, 30);
		
		System.out.println("letter: "+letter);
		
		if(letter == 'C')
			passwdField.setEnabled(true);
		else
			passwdField.setEnabled(false);
		
		
		JPanel contentPane = new JPanel();
		contentPane.setOpaque(true);
		choiceDialog.setContentPane(contentPane);
		
		choiceDialog.addWindowListener(new WindowAdapter (){
			
			@Override
			public void windowClosed(WindowEvent e){ //popolo i vettori con le impostazioni da salvare
					destinations.add(destinationFolderField.getText());		
					password = String.copyValueOf(passwdField.getPassword());
			}
		});
		
		choiceDialog.setTitle("Impostazioni");
		choiceDialog.setVisible(true);
		choiceDialog.setLocationRelativeTo(null);
		choiceDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		fileNameLabel = new JLabel(currentFile);
		setDialogLayout(fileNameLabel, passwdField, contentPane);
		choiceDialog.pack();		
	}
	
	public void setMessageDialog(JLabel label){
		messageDialog = new JDialog();
		messageDialog.setVisible(true);
		messageDialog.setTitle("Errore");
		messageDialog.setSize(350, 70);
		messageDialog.add(label);
		messageDialog.setLocationRelativeTo(null);
	}
	
//	public void getPasswd(){
//		JPasswordField passwdField = new JPasswordField(); 
//		okPasswd = new JButton("Ok");
//		messageDialog = new JDialog();
//		messageDialog.setVisible(true);
//		messageDialog.setTitle("Inserimento password");
//		messageDialog.setSize(350, 70);
//		messageDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//		messageDialog.setLocationRelativeTo(null);
//		messageDialog.add(passwdField);
//		messageDialog.add(okPasswd);
//		
//		messageDialog.addWindowListener(new WindowAdapter(){
//			@Override
//			public void windowClosed(WindowEvent e){ //salvo la password inserita da utente
//				password = String.copyValueOf(passwdField.getPassword());
//			}
//		});
//	}
	
	private void addToTable(){
		
		File f = new File(selectedFile.getAbsolutePath()); //...\Muse1D.mp3
		System.out.println("f.getAbsolutePath(): "+f.getAbsolutePath());
		totalFiles.add(f);
		
		String fileName = f.getAbsolutePath()
				.substring(f.getAbsolutePath().lastIndexOf(File.separator)+1, 
						f.getAbsolutePath().lastIndexOf(".")-2); 								//Muse1D
		String fileFormat = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf("."), 
				f.getAbsolutePath().length());												//.mp3
		
		Merger m = new Merger(fileName+fileFormat, destinationFolderField.getText()+fileName+fileFormat);
		v.add(m);
		dataModel.addRow(v);
	}
	
	private void clearTable(){
		v.removeAllElements();
		dataModel.getDataVector().removeAllElements();
		dataModel.fireTableDataChanged();
	}
	
	public void enableButtons(boolean flag){
		browse.setEnabled(flag);
		merge.setEnabled(flag);
		delete.setEnabled(flag);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Naviga")){
			if(totalFiles.size() == 1){
				JOptionPane.showMessageDialog(this, 
						"Non puoi selezionare più file", "ERRORE", JOptionPane.ERROR_MESSAGE);
			}else{
				JFileChooser fileChooser = new JFileChooser();
				
				//per impostare la directory iniziale in cui cercare
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				
				int result = fileChooser.showOpenDialog(this);
				
				// se l'utente seleziona uno o più file
				if (result == JFileChooser.APPROVE_OPTION) {
					selectedFile = fileChooser.getSelectedFile();
					
					if(selectedFile != null)
						setChoiceDialog(selectedFile.getAbsolutePath(), true);
				}			
			}
			
		}else if (e.getActionCommand().equals("Cerca")){
			JFileChooser folderChooser = new JFileChooser();
			folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			folderChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			
			//disabilitata l'opzione 'Tutti i file'
			folderChooser.setAcceptAllFileFilterUsed(false);
			
			if (folderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				destinationFolderField.setText(folderChooser.getSelectedFile()+File.separator);	
			
			
		}else if (e.getActionCommand().equals("OK")){
			String noDestination = "Cartella di destinazione non valida";
				
			if (destinationFolderField.getText().equals("Destinazione")){
				errorLabel = new JLabel(noDestination, SwingConstants.CENTER);
				setMessageDialog(errorLabel);
			}else{
				choiceDialog.dispose();
				addToTable();
			}
			
			
		}else if (e.getActionCommand().equals("Elimina")){
			destinations.clear();
			totalFiles.clear();
			clearTable();
			
		}else{ //l'unica azione rimanente è "Unisci"
			if (totalFiles.size() == 0){
				errorLabel = new JLabel("Non hai selezionato alcun file", SwingConstants.CENTER);
				setMessageDialog(errorLabel);
			}else{
				//ATTUA LA DIVISIONE EFFETTIVA
				JOptionPane.showMessageDialog(this, 
						"Inizio del processo di unione", "Informazione", JOptionPane.INFORMATION_MESSAGE);
				
				try {
					Merger m = null;
					RandomAccessFile raf = new RandomAccessFile(selectedFile.getAbsolutePath(), "r");
					String fileFormat, fileName, originalDirectory = selectedFile.getAbsolutePath().substring(0, selectedFile.getAbsolutePath().indexOf(selectedFile.getName()));
					char letter =selectedFile.getAbsolutePath().charAt(selectedFile.getAbsolutePath().lastIndexOf(".")-1);
					
					System.out.println("originalDirectory: "+originalDirectory+"\nletter: "+letter);
					fileFormat = selectedFile.getAbsolutePath()
		                	.substring(selectedFile.getAbsolutePath().lastIndexOf("."), 
		                			selectedFile.getAbsolutePath().length());
		            fileName = selectedFile.getAbsolutePath()
		                	.substring(selectedFile.getAbsolutePath().lastIndexOf(File.separator)+1, 
		                			selectedFile.getAbsolutePath().lastIndexOf("."));
					//in base a che tipo di unione voglio fare, attuo determinate operazioni
		        	switch(letter){
		        		case 'D':
		        	       	System.out.println("\nfileFormat: "+fileFormat+'\n'+"fileName: "+fileName);
		        				                	
		        			m = new Merger(fileName, fileFormat, destinationFolderField.getText()); //directory+fileName+1+letter+fileFormat
		        			m.merge(originalDirectory);
		        			break;
		        		case 'C':
		        			m = new Merger(fileName, fileFormat, destinationFolderField.getText()); //directory+fileName+1+letter+fileFormat
		        			System.out.println("password: "+password);
		        			m.decrypt(originalDirectory, password); 
		        			break;
		        		case 'Z':
		        			m = new Merger(fileName, fileFormat, destinationFolderField.getText()); //directory+fileName+1+letter+fileFormat
		        			m.mergeZip(originalDirectory);
		        			break;
		        		case 'T':
		        			m = new Merger(fileName, fileFormat, destinationFolderField.getText()); //directory+fileName+1+letter+fileFormat
		        			m.merge(originalDirectory);
		        			break;
		        		default:
		        			JOptionPane.showMessageDialog(this, 
		    						"Formato del file errato", "ERRORE", JOptionPane.ERROR_MESSAGE);
					}
		        	totalFiles.clear();
		        	enableButtons(false);
		        	JOptionPane.showMessageDialog(this, 
    						"Unione terminata", "Terminazione", JOptionPane.INFORMATION_MESSAGE);
		        	enableButtons(true);
		        	clearTable();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		}
	}
}
