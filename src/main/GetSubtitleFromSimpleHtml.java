package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * д�������ĳ�����Ϊ����ȡ΢������ѧԺ�е���Ļ��
 * ʹ���������֮ǰ����Ҫ�ֶ�����ȡ��ҳ�е�html��Ϣ��
 * ѡ������ĻԪ�أ�Ȼ���½�һ���ԡ�.srt����ʽ��β���ı��ļ���
 * �����Ƶ���html��Ϣ�Ž�����ı��ļ��У�
 * ����Ҫ������´����ġ�*.srt���ļ��ŵ�ָ�����ļ�����
 * ���������Ĭ���ļ�����sourceFolderPath�ж����ˣ���
 * ���г���
 * Ȼ������ļ�����������".srt"��β���ļ����ᱻ����
 * �����Զ���sourceFolderPath�ļ����д���һ����Ϊ��output�������ļ��У�
 * ���б���������ļ������������output���ļ����У�
 * ���ֲ���ı䡣
 */
public class GetSubtitleFromSimpleHtml {
	/**
	 * �����ļ���·����
	 */
	protected static String sourceFolderPath = "D:\\tempFile\\��������  Windows 10 ����\\titles";
	/**
	 * ��������������ļ����ļ������ơ�
	 */
	protected static String outputFolderName = "Subtitles";
	/**
	 * ����ļ��У����·����sourceFolderPath������
	 */
	protected static String outputFolderPath;
	
	/**
	 * ��������ļ�����������������ͬһ���ļ����µĻ���
	 * ����������ļ�������ڱ�������ļ���֮�£�
	 * �����ļ���ǰ�����������õ��ļ�������
	 * @param outputFolderName
	 * 		�����õ��ļ����в��ܰ��������ַ���<br>
	 *		\/:*?"<>|
	 * @return 
	 * 		���óɹ�����true�����򷵻�false��
	 */
	public static boolean setOutputFolderName(String outputFolderName) {
		if (outputFolderName.contains("\\")
				|| outputFolderName.contains("/")
				|| outputFolderName.contains(":")
				|| outputFolderName.contains("?")
				|| outputFolderName.contains("\"")
				|| outputFolderName.contains("<")
				|| outputFolderName.contains(">")
				|| outputFolderName.contains("|")){
			return false;
		}
		GetSubtitleFromSimpleHtml.outputFolderName = outputFolderName;
		return true;
	}

	/**
	 * �����Ƿ񽫴�����ļ����ڱ������ļ��е�һ��ͳһ���ļ���֮�¡�
	 */
	public static void setOutputToOneFolder(boolean outputToOneFolder) {
		GetSubtitleFromSimpleHtml.outputToOneFolder = outputToOneFolder;
	}

	/**
	 * �Ƿ����еĴ����ļ������ͬһ���ļ����У�
	 * ��������ͬһ���ļ��У�
	 * �����Զ���sourceFolderPath�ļ����д���һ��outputFolderName���ļ��У�
	 * ����������ͬһ���ļ��У�
	 * �����ÿһ������ļ��Ŀ�ͷ�����outputFolderName�����Ա����������ַ�����
	 */
	protected static boolean outputToOneFolder = true;
	/**
	 * ʱ��ƥ��������ʽ��
	 */
	public static Pattern pTime = Pattern.compile(" *(\\<.+?startTime.+?\\>)(.*)(\\<.+?\\>)");
	/**
	 * ����ƥ��������ʽ��
	 */
	public static Pattern pTransition = Pattern.compile(" *(\\<.+?textContent.+?\\>)(.*)(\\<.+?\\>)");
	
	/**
	 * ���õ���ڡ�
	 * @param folderPath
	 * 		��Ҫ������ļ�·����
	 */
	public static void DealWithTheFolder(String folderPath){
		try{
			doTheWork(folderPath);
		} catch (Exception e){
			throw new RuntimeException("�����ļ����е���Ļ�ļ�����");
		}
	}
	
	protected static void doTheWork(String folderPath) throws IOException {
		sourceFolderPath = folderPath;
		File sourceFolder = new File(sourceFolderPath);
		
		if ( ! sourceFolder.exists()){
			System.out.println("�����ļ��в����ڣ��봴���ļ��У�" + sourceFolderPath);
			throw new RuntimeException("Դ·�������ڡ�");
		}
		
		if ( sourceFolder.isFile()){
			System.out.println("Ŀ��·������һ���ļ��У���ȷ��Ŀ��·����һ���ļ��У���ǰ��Ŀ��·����" + sourceFolderPath);
			throw new RuntimeException("Դ·�������ļ��С�");
		}
		
		if (outputToOneFolder){
			//��������ļ��С�
			outputFolderPath = sourceFolderPath + "\\" + outputFolderName;
			File outputFolder = new File(outputFolderPath);
			outputFolder.mkdirs();
		}
		
		//��ȡ���е����ļ���
		File[] unSolvedSubtitles = sourceFolder.listFiles();
		
		//��ÿһ�����ļ����в�����
		for (File subFile : unSolvedSubtitles){
			//Ҫ�����ļ��������ļ����ͣ�
			//�����ļ���������".srt"��β��
			if (subFile.isFile() && subFile.getName().endsWith(".srt")){
				OutputSubtitle(subFile);
			}
		}
		
		System.out.println("���������");
	}
	
	/**
	 * ��Ŀ���ļ���ȡ��Ϣ�����һ������srt��ʽ����Ļ�ļ���
	 * @param unSolvedSubtitle
	 * 		��html����ȡ����Ļ��ʽ��
	 * @throws IOException
	 */
	protected static void OutputSubtitle(File unSolvedSubtitle) throws IOException{
		//��������ļ���
		File newSubtitle;
		if (outputToOneFolder){
			//��������ͳһ���ļ����£�
			//��Ҫ������ǰ���ϸ��ļ���·����
			newSubtitle = new File(outputFolderPath + "\\" + unSolvedSubtitle.getName());
		} else {
			//����������ͳһ���ļ����£�
			//ֻ������ļ��е�����ǰ���һ��ǰ׺��
			newSubtitle = new File(
					unSolvedSubtitle.getParent() + "\\" 	//��·����
					+ outputFolderName + 				//���ļ���ǰ׺��
					unSolvedSubtitle.getName());		//���ļ�����
		}
		newSubtitle.createNewFile();
		
		//���ڴ洢��Ļ��ż���д洢ʱ�䣬�����д洢�ı���
		ArrayList<String> pureSubtitles = new ArrayList<String>();
		//�������������ļ���������ֹpureSubtitles��˳����ֻ��ң�
		//Ҫ�����ʱ����Ϣʱ����֤lineRestrict Ϊ ż����
		//����ı���Ϣʱ��lineRestrictΪ������
		int lineRestrict = 0;
		
		boolean occurredError = false;
				
		try {
			BufferedReader usubReader = 
					new BufferedReader(new InputStreamReader(new FileInputStream(unSolvedSubtitle)));
			
			String line = "";
			
			//��ȡÿһ�С�
			while((line = usubReader.readLine() ) != null){
				//��ʱ��������ʽ��ȡʱ����Ϣ��
				Matcher matcher = pTime.matcher(line);
				if (matcher.find()){
					if (lineRestrict % 2 != 0){
						System.out.println("�ļ���ʽ����ʱ����Ϣ����������ӡ�");
						//��־���󣬷�ֹд���ļ���
						occurredError = true;
						break;
					}
					//�洢ʱ����Ϣ��
					pureSubtitles.add(matcher.group(2));
					//�м������Ƽ�һ��
					++lineRestrict;
					//ִ����һ��ѭ����
					continue;
				}

				//���ı�������ʽ��ȡ�ı���Ϣ��
				matcher = pTransition.matcher(line);
				if (matcher.find()){
					if (lineRestrict % 2 != 1){
						System.out.println("�ļ���ʽ������Ļ�ı���Ϣ����������ӡ�");
						//��־���󣬷�ֹд���ļ���
						occurredError = true;
						break;
					}
					pureSubtitles.add(matcher.group(2));
					//�м������Ƽ�һ��
					++lineRestrict;
				}
			}
			
			//����Ƿ����˴���
			if ( !occurredError){
				//û�з���������Ŀ���ļ�д����Ļ��Ϣ��
				WriteOutputFile(pureSubtitles, newSubtitle);
			}
			
			usubReader.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("��ȡ��Ļ�ļ���������������ļ���" + unSolvedSubtitle.getPath());
			e.printStackTrace();
		}
	}

	/**
	 * ��Ŀ���ļ�д����Ļ��Ϣ��
	 * @param pureSubtitles
	 * 		ż����Ϊʱ����Ϣû��ĩβ�ĺ�����Ϣ��
	 * 		������Ϊ��Ļ��Ϣ����Ļ��Ϣû�л��С�
	 * @param newSubtitle
	 * 		Ŀ��д���ļ���Ҫ���ļ�������ڡ�
	 * @throws IOException 
	 */
	private static void WriteOutputFile(ArrayList<String> pureSubtitles, File newSubtitle) throws IOException {
		FileWriter outputFile = new FileWriter(newSubtitle);
		//һ����Ļ������Ϣ��
		//����һ����ţ�һ��ʱ��Σ�һ����Ļ�ı���
		String oneUnit;
		
		//��ָ��ʱ����Ļ��ƫ������
		int offset;
		
		String startTime;
		String endTime;
		String text;
		
		//size����Ϊһ��ż����
		int size = pureSubtitles.size();
		
		//iÿ�����Ӷ�����Ϊһ����ʱ�䣬һ������Ļ��
		for (int i = 1; i * 2 <= size; ++i){
			offset = (i - 1) * 2;
			
			startTime = pureSubtitles.get(offset);
			text = pureSubtitles.get(offset + 1);
			if (i * 2 == size){
				//������������һ����Ԫ��Ļ��
				char[] time = startTime.toCharArray();
				//����һ��ʱ����Ϊ9�����Խ����һ����Ļ��ʱ�������Ӻ�
				time[0] = '9';
				endTime = String.valueOf(time);
			} else {
				//��Ԫ��Ļ����ʱ��Ϊ��һ����Ļ��ʼʱ�䡣
				endTime = pureSubtitles.get(offset + 2);
			}
			
			oneUnit = i + "\n"		//��š�
					//��ʼʱ��		 ��ʼ��΢��ʱ��	��ͷ		����ʱ��	������΢��ʱ��	
					+ startTime + ",000" + " --> " + endTime + ",000" + "\n"		//ʱ������
					+ text			//��Ļ���֡�
					+ "\n\n";		//�����С�
			
			outputFile.write(oneUnit);
		}
		
		outputFile.close();
	}

}
