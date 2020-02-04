package gui;

import java.lang.Object;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import split.Split;

public class Progress extends SwingWorker<Void, Integer>{
	private Thread th;
	private Split sp;
	private JLabel progressLabel;
	private long totalBytes;
	
	public Progress(Thread th, Split sp, JLabel progressLabel, long totalBytes){
		this.th = th;
		this.sp = sp;
		this.progressLabel = progressLabel;
		this.totalBytes = totalBytes;
	}
	
	public Thread getTh() {
		return th;
	}

	public void setTh(Thread th) {
		this.th = th;
	}

	public Split getSp() {
		return sp;
	}

	public void setSp(Split sp) {
		this.sp = sp;
	}	
	
	public JLabel getProgressLabel() {
		return progressLabel;
	}

	public void setProgressLabel(JLabel progressLabel) {
		this.progressLabel = progressLabel;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	@Override
	protected Void doInBackground() throws Exception {
		long readBytes = 0;
		while(getTh().getState() != Thread.State.TERMINATED){
			readBytes = getSp().getReadBytes();
			int val =  Math.toIntExact((readBytes*100)/getTotalBytes());
			publish(val);
		}
		return null;
	}
	
	@Override
	protected void process(List<Integer> chunks){
		int i = chunks.get(chunks.size()-1);
		getProgressLabel().setText(i+"%");
	}

	@Override
	protected void done() {
		try {
			get();
			if (getProgressLabel().getText().equals("98%") || 
					getProgressLabel().getText().equals("99%") || getProgressLabel().getText().equals("100%")){
				getProgressLabel().setText("100%");
				JOptionPane.showMessageDialog(getProgressLabel().getParent(), 
						"Divisione avvenuta con successo", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
				
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
