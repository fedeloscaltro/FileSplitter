package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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

import merge.Merger;
import split.Crypto;
import split.Split;
import split.SplitTimes;
import split.Zip;

/**
 * Classe per inizializzare il pannello per la gestione grafica dell'unione dei file
 * @see JPanel
 * @see ActionListener
 * */
public class PanelMerge extends JPanel implements ActionListener{
	
	/**
	 * Componenti grafici:
	 * TableModel che specifica i metodi per gestire la JTable;
	 * JTable per presentare graficamente i file selezionati;
	 * JTextField per indicare la cartella di destinazione selezionata
	 * Serie di JButton per: attivare la ricerca per la selezione del file da cui far partire l'unione;
	 * permettere in un JDialog di scegliere la cartella di destinazione dei file divisi; 
	 * confermare le impostazioni di divisione scelte per il singolo file; per eliminare tutti i file selezionati;
	 * avviare l'unione dei file; 
	 * JLabel indicante il nome del file selezionato;
	 * JLabel contenente un messaggio d'errore;
	 * JDialog per la scelta delle impostazioni di unione;
	 * JDialog per visualizzare eventuali messaggi di errore.
	 * */
	private MergerTableModel dataModel;
	private JTable t;
	private JTextField destinationFolderField;
	private JButton browse, browseDestination, settingsOk, delete, merge;
	private JLabel fileNameLabel, errorLabel;
	private JDialog choiceDialog, messageDialog;
	
	/**
	 * Vettori di: oggetti Merger da aggiungere alla tabella; Stringhe per memorizzare le cartelle di destinazione scelte;
	 * file selezionati.
	 * File indicante il singolo file selezionato;
	 * Stringa rappresentante la password immessa da utente.
	 * */ 
	private Vector<Merger> v;
	private Vector<String> destinations;
	private Vector<File> totalFiles;
	private File selectedFile;
	private String password;
	
	
	/**
	 * Costruttore del pannello contenente i componenti grafici per l'unione
	 * */
	public PanelMerge(){
		super();
		v = new Vector<Merger>();
		destinations = new Vector<String>();
		totalFiles = new Vector<File>();
		
		//pannelli figli che contengono la tabella e e bottoni
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
		
		JScrollPane jsp = new JScrollPane(t); //pannello per contenere la tabella
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
	
	/**
	 * Metodo per impostare il layout del JDialog per le impostazioni di unione
	 * */
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
	
	/**
	 * Metodo per impostare il layout del JDialog per le impostazioni di unione
	 * @param currentFile Stringa contenente il nome del file selezionato
	 * @param flag parametro per abilitare o no il campo per l'inserimento della password
	 * */
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
			public void windowClosed(WindowEvent e){ //popolo il vettore e la password con le impostazioni da salvare
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
	
	/**
	 * Metodo per impostare il Dialog per i messaggi d'errore
	 * @param label JLabel contenente il messaggio da visualizzare
	 * */
	public void setMessageDialog(JLabel label){
		messageDialog = new JDialog();
		messageDialog.setVisible(true);
		messageDialog.setTitle("Errore");
		messageDialog.setSize(350, 70);
		messageDialog.add(label);
		messageDialog.setLocationRelativeTo(null);
	}
	
	/**
	 * Metodo per l'aggiunta di un file alla tabella
	 * */
	private void addToTable(){
		
		File f = new File(selectedFile.getAbsolutePath()); //...\Nome1Lettera.Formato
		System.out.println("f.getAbsolutePath(): "+f.getAbsolutePath());
		totalFiles.add(f);
		
		String fileName = f.getAbsolutePath()
				.substring(f.getAbsolutePath().lastIndexOf(File.separator)+1, 
						f.getAbsolutePath().lastIndexOf(".")-2); 								//Nome1Lettera
		String fileFormat = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf("."), 
				f.getAbsolutePath().length());												//.Formato
		
		Merger m = new Merger(fileName+fileFormat, destinationFolderField.getText()+fileName+fileFormat);
		v.add(m);
		dataModel.addRow(v); //aggiunta effettiva alla tabella
	}
	
	/**
	 * Metodo per pulire la tabella:
	 * rimuovo tutti gli elementi dal vettore, dalla tabella graficamente e comunico il cambiamento
	 * */
	private void clearTable(){
		v.removeAllElements();
		dataModel.getDataVector().removeAllElements();
		dataModel.fireTableDataChanged();
	}
	
	/**
	 * Metodo per abilitare o disabilitare i JButton presenti nel pannello di unione
	 * */
	public void enableButtons(boolean flag){
		browse.setEnabled(flag);
		merge.setEnabled(flag);
		delete.setEnabled(flag);
	}
	
	/**
	 * Metodo sovrascritto per la gestione degli eventi scaturiti dal click sui JButton
	 * */
	@Override
	public void actionPerformed(ActionEvent e) {
		//se si è scelto di cercare nel file system i file da dividere
		if (e.getActionCommand().equals("Naviga")){
			
			if(totalFiles.size() == 1){ //controllo che non si vada ad avere più di un file selezionato
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
					
					//se ho effettivamente selezionato un file
					if(selectedFile != null)
						setChoiceDialog(selectedFile.getAbsolutePath(), true);
				}			
			}
			
			//se si clicca sul bottone per selezionare la cartella di destinazione
		}else if (e.getActionCommand().equals("Cerca")){
			JFileChooser folderChooser = new JFileChooser();
			folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			folderChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			
			//disabilitata l'opzione 'Tutti i file'
			folderChooser.setAcceptAllFileFilterUsed(false);
			
			if (folderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				destinationFolderField.setText(folderChooser.getSelectedFile()+File.separator);	
			
			//se si clicca sul bottone per confermare le impostazioni di unione
		}else if (e.getActionCommand().equals("OK")){
			String noDestination = "Cartella di destinazione non valida";
				
			//se non si ha scelto una destinazione valida
			if (destinationFolderField.getText().equals("Destinazione")){
				errorLabel = new JLabel(noDestination, SwingConstants.CENTER);
				setMessageDialog(errorLabel);
			}else{
				choiceDialog.dispose();
				addToTable();
			}
			
		//in caso si voglia eliminare il file scelto
		}else if (e.getActionCommand().equals("Elimina")){
			destinations.clear();
			totalFiles.clear();
			clearTable();
			
		}else{ //l'unica azione rimanente è "Unisci"
			if (totalFiles.size() == 0){
				errorLabel = new JLabel("Non hai selezionato alcun file", SwingConstants.CENTER);
				setMessageDialog(errorLabel);
			}else{
				//attuo l'unione effettiva
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
