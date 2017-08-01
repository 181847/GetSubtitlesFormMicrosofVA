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
 * 写这个程序的初衷是为了提取微软虚拟学院中的字幕，
 * 使用这个程序之前，需要手动的提取网页中的html信息，
 * 选择复制字幕元素，然后新建一个以“.srt”格式结尾的文本文件，
 * 将复制到的html信息放进这个文本文件中，
 * 你需要将这个新创建的“*.srt”文件放到指定的文件夹中
 * （本程序的默认文件夹在sourceFolderPath中定义了），
 * 运行程序，
 * 然后这个文件夹中所有以".srt"结尾的文件都会被处理，
 * 并且自动在sourceFolderPath文件夹中创建一个名为“output”的子文件夹，
 * 所有被处理过的文件都会在这个“output”文件夹中，
 * 名字不会改变。
 */
public class GetSubtitleFromSimpleHtml {
	/**
	 * 处理文件的路径。
	 */
	protected static String sourceFolderPath = "D:\\tempFile\\新手入门  Windows 10 开发\\titles";
	/**
	 * 用于输出处理结果文件的文件夹名称。
	 */
	protected static String outputFolderName = "Subtitles";
	/**
	 * 输出文件夹，这个路径由sourceFolderPath决定。
	 */
	protected static String outputFolderPath;
	
	/**
	 * 设置输出文件夹名，如果不输出到同一个文件夹下的话，
	 * 所有输出的文件名会放在被处理的文件夹之下，
	 * 并在文件名前面加上这个设置的文件夹名。
	 * @param outputFolderName
	 * 		被设置的文件名中不能包含以下字符：<br>
	 *		\/:*?"<>|
	 * @return 
	 * 		设置成功返回true，否则返回false。
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
	 * 配置是否将处理的文件放在被处理文件夹的一个统一的文件夹之下。
	 */
	public static void setOutputToOneFolder(boolean outputToOneFolder) {
		GetSubtitleFromSimpleHtml.outputToOneFolder = outputToOneFolder;
	}

	/**
	 * 是否将所有的处理文件输出到同一个文件夹中，
	 * 如果输出到同一个文件夹，
	 * 将会自动在sourceFolderPath文件夹中创建一个outputFolderName的文件夹，
	 * 如果不输出到同一个文件夹，
	 * 则会在每一个输出文件的开头添加上outputFolderName这个成员变量代表的字符串。
	 */
	protected static boolean outputToOneFolder = true;
	/**
	 * 时间匹配正则表达式。
	 */
	public static Pattern pTime = Pattern.compile(" *(\\<.+?startTime.+?\\>)(.*)(\\<.+?\\>)");
	/**
	 * 翻译匹配正则表达式。
	 */
	public static Pattern pTransition = Pattern.compile(" *(\\<.+?textContent.+?\\>)(.*)(\\<.+?\\>)");
	
	/**
	 * 调用的入口。
	 * @param folderPath
	 * 		想要处理的文件路径。
	 */
	public static void DealWithTheFolder(String folderPath){
		try{
			doTheWork(folderPath);
		} catch (Exception e){
			throw new RuntimeException("处理文件夹中的字幕文件出错。");
		}
	}
	
	protected static void doTheWork(String folderPath) throws IOException {
		sourceFolderPath = folderPath;
		File sourceFolder = new File(sourceFolderPath);
		
		if ( ! sourceFolder.exists()){
			System.out.println("处理文件夹不存在，请创建文件夹：" + sourceFolderPath);
			throw new RuntimeException("源路径不存在。");
		}
		
		if ( sourceFolder.isFile()){
			System.out.println("目标路径不是一个文件夹，请确保目标路径是一个文件夹，当前的目标路径：" + sourceFolderPath);
			throw new RuntimeException("源路径不是文件夹。");
		}
		
		if (outputToOneFolder){
			//创建输出文件夹。
			outputFolderPath = sourceFolderPath + "\\" + outputFolderName;
			File outputFolder = new File(outputFolderPath);
			outputFolder.mkdirs();
		}
		
		//获取所有的子文件。
		File[] unSolvedSubtitles = sourceFolder.listFiles();
		
		//对每一个子文件进行操作。
		for (File subFile : unSolvedSubtitles){
			//要求子文件必须是文件类型，
			//而且文件名必须以".srt"结尾。
			if (subFile.isFile() && subFile.getName().endsWith(".srt")){
				OutputSubtitle(subFile);
			}
		}
		
		System.out.println("处理结束。");
	}
	
	/**
	 * 从目标文件读取信息，输出一个符合srt格式的字幕文件。
	 * @param unSolvedSubtitle
	 * 		从html中提取的字幕格式。
	 * @throws IOException
	 */
	protected static void OutputSubtitle(File unSolvedSubtitle) throws IOException{
		//创建输出文件。
		File newSubtitle;
		if (outputToOneFolder){
			//如果输出到统一的文件夹下，
			//需要在名字前加上父文件夹路径。
			newSubtitle = new File(outputFolderPath + "\\" + unSolvedSubtitle.getName());
		} else {
			//如果不输出到统一的文件夹下，
			//只在输出文件夹的名字前添加一个前缀。
			newSubtitle = new File(
					unSolvedSubtitle.getParent() + "\\" 	//父路径。
					+ outputFolderName + 				//新文件名前缀。
					unSolvedSubtitle.getName());		//旧文件名。
		}
		newSubtitle.createNewFile();
		
		//用于存储字幕，偶数行存储时间，奇数行存储文本。
		ArrayList<String> pureSubtitles = new ArrayList<String>();
		//用于限制行数的计数器，防止pureSubtitles中顺序出现混乱，
		//要求添加时间信息时，保证lineRestrict 为 偶数，
		//添加文本信息时，lineRestrict为奇数。
		int lineRestrict = 0;
		
		boolean occurredError = false;
				
		try {
			BufferedReader usubReader = 
					new BufferedReader(new InputStreamReader(new FileInputStream(unSolvedSubtitle)));
			
			String line = "";
			
			//读取每一行。
			while((line = usubReader.readLine() ) != null){
				//用时间正则表达式提取时间信息。
				Matcher matcher = pTime.matcher(line);
				if (matcher.find()){
					if (lineRestrict % 2 != 0){
						System.out.println("文件格式错误，时间信息不能正常添加。");
						//标志错误，防止写入文件。
						occurredError = true;
						break;
					}
					//存储时间信息。
					pureSubtitles.add(matcher.group(2));
					//行计数限制加一。
					++lineRestrict;
					//执行下一次循环。
					continue;
				}

				//用文本正则表达式提取文本信息。
				matcher = pTransition.matcher(line);
				if (matcher.find()){
					if (lineRestrict % 2 != 1){
						System.out.println("文件格式错误，字幕文本信息不能正常添加。");
						//标志错误，防止写入文件。
						occurredError = true;
						break;
					}
					pureSubtitles.add(matcher.group(2));
					//行计数限制加一。
					++lineRestrict;
				}
			}
			
			//检查是否发生了错误。
			if ( !occurredError){
				//没有发生错误，向目标文件写入字幕信息。
				WriteOutputFile(pureSubtitles, newSubtitle);
			}
			
			usubReader.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("提取字幕文件出错，请检查这个的文件：" + unSolvedSubtitle.getPath());
			e.printStackTrace();
		}
	}

	/**
	 * 向目标文件写入字幕信息。
	 * @param pureSubtitles
	 * 		偶数行为时间信息没有末尾的毫秒信息，
	 * 		奇数行为字幕信息，字幕信息没有换行。
	 * @param newSubtitle
	 * 		目标写入文件，要求文件必须存在。
	 * @throws IOException 
	 */
	private static void WriteOutputFile(ArrayList<String> pureSubtitles, File newSubtitle) throws IOException {
		FileWriter outputFile = new FileWriter(newSubtitle);
		//一个字幕的总信息，
		//包括一个序号，一个时间段，一个字幕文本。
		String oneUnit;
		
		//到指定时间字幕的偏移量。
		int offset;
		
		String startTime;
		String endTime;
		String text;
		
		//size必须为一个偶数。
		int size = pureSubtitles.size();
		
		//i每次增加二，因为一行是时间，一行是字幕。
		for (int i = 1; i * 2 <= size; ++i){
			offset = (i - 1) * 2;
			
			startTime = pureSubtitles.get(offset);
			text = pureSubtitles.get(offset + 1);
			if (i * 2 == size){
				//如果到达了最后一个单元字幕。
				char[] time = startTime.toCharArray();
				//将第一个时间设为9，尝试将最后一个字幕的时间无限延后。
				time[0] = '9';
				endTime = String.valueOf(time);
			} else {
				//单元字幕结束时间为下一个字幕开始时间。
				endTime = pureSubtitles.get(offset + 2);
			}
			
			oneUnit = i + "\n"		//序号。
					//开始时间		 开始的微秒时间	箭头		结束时间	结束的微秒时间	
					+ startTime + ",000" + " --> " + endTime + ",000" + "\n"		//时间间隔。
					+ text			//字幕文字。
					+ "\n\n";		//换两行。
			
			outputFile.write(oneUnit);
		}
		
		outputFile.close();
	}

}
