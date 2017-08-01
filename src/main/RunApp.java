package main;

import java.awt.EventQueue;

public class RunApp {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		GetSubtitleFromSimpleHtml.setOutputFolderName("Subtitles");
		GetSubtitleFromSimpleHtml.setOutputToOneFolder(false);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppMainWnd frame = new AppMainWnd();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
