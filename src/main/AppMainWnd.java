package main;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListModel;
import javax.swing.TransferHandler;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class AppMainWnd extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton startProcBtn;
	private JLabel outputMessage;
	private JLabel helpMessage;
	private JScrollPane folderScrollPane;
	private JList<String> folderList;

	/**
	 * Create the frame.
	 */
	public AppMainWnd() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		
		outputMessage = new JLabel("\u4FE1\u606F\u663E\u793A\uFF1A");
		outputMessage.setVerticalTextPosition(SwingConstants.BOTTOM);
		outputMessage.setVerticalAlignment(SwingConstants.TOP);
		
		helpMessage = new JLabel("\u8BF7\u5C06\u8981\u5904\u7406\u7684\u6587\u4EF6\u5939\u62D6\u62FD\u5230\u4E0B\u9762\u7684\u5217\u8868\u4E2D\uFF1A");
		
		startProcBtn = new JButton("\u5F00\u59CB\u5904\u7406");
		startProcBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				ListModel<String> folderListModel = folderList.getModel();
				int fileCount = folderListModel.getSize();
				String targetFolderPath;
				
				//��¼�����˴�����ļ�����š�
				String finalResult = "";
				for (int i = 0; i < fileCount; ++i){
					targetFolderPath = folderListModel.getElementAt(i);
					try {
						//�����ļ���֮�µ��ļ���
						GetSubtitleFromSimpleHtml.doTheWork(targetFolderPath);
					} catch (IOException e) {
						//��¼����������ļ�����š�
						finalResult += 
								((i + 1) + " ");
					}
				}
				
				//��������Ϣ��
				if (finalResult.isEmpty()){
					outputMessage.setText("����ɹ���");
				} else {
					outputMessage.setText("��" + finalResult + "���ļ��д�������IO����");
				}
			}
		});
		
		folderScrollPane = new JScrollPane();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addComponent(startProcBtn, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
						.addComponent(folderScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
						.addComponent(helpMessage, Alignment.LEADING)
						.addComponent(outputMessage, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(helpMessage)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(folderScrollPane, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(outputMessage, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(startProcBtn, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
					.addGap(15))
		);
		
		folderList = new JList<String>();
		folderScrollPane.setViewportView(folderList);
		panel.setLayout(gl_panel);
		
		folderScrollPane.setTransferHandler(new TransferHandler(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean importData(JComponent comp, Transferable t){
				try{
					//����Listģ�塣
					DefaultListModel<String> folderListModel = new DefaultListModel<String>();
					
					//���Խ���ק������ת��Ϊ�ļ��б�
					Object objs = t.getTransferData(DataFlavor.javaFileListFlavor);
					
					//ת��Ϊ�ļ��б�����ȡÿһ���ļ���
					//ֻ���ļ��и��ŵ�folderList�С�
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)objs;
					for (File file : files){
						if (file.isDirectory() && file.exists()){
							folderListModel.addElement(file.getAbsolutePath());
							//��ʾ������Ϣ��
							outputMessage.setText("����ť�����������ļ���");
						} else {
							//��ʾ������Ϣ��Ҫ�������קһ�����߶���ļ��С�
							outputMessage.setText("��ק��������Ŀ�д����ļ�����ֻ��ק�ļ��С�");
						}
					}
					
					folderList.setModel(folderListModel);
					return true;
					
				}catch (UnsupportedFlavorException ue){
					outputMessage.setText("���ڲ�֧�ֵ���ק���ݣ�����קһ�����߶���ļ��С�");
				}catch (Exception e){
					outputMessage.setText("�����쳣��");
				}
				
				return false;
			}
			
			@Override
			public boolean canImport(JComponent c, DataFlavor[] flavors){
				for (DataFlavor df : flavors){
					//��֤������ק���������ݶ����ļ���
					if ( ! df.isFlavorJavaFileListType()){
						//��ʾ��ʾ��Ϣ��
						outputMessage.setText("���ڲ�֧����ק�����ݡ�");
						return false;
					}
				}
				
				//��ʾ������Ϣ��
				outputMessage.setText("������ļ���");
				return true;
			}
		});
	}
	public JButton getStartProcBtn() {
		return startProcBtn;
	}
	public JLabel getOutputMessage() {
		return outputMessage;
	}
	public JLabel getHelpMessage() {
		return helpMessage;
	}
	public JScrollPane getFolderScrollPane() {
		return folderScrollPane;
	}
	public JList getFolderList() {
		return folderList;
	}
}
