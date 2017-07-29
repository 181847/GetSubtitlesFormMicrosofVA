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
 * 会自动在sourceFolderPath文件夹中创建一个名为“output”的子文件夹，
 * 所有被处理过的文件都会在这个“output”文件夹中，
 * 名字不会改变。
 */
public class GetSubtitleFromSimpleHtml {
	/**
	 * 处理文件的路径。
	 */
	public static String sourceFolderPath = "D:\\titles";
	/**
	 * 输出文件夹，这个路径由sourceFolderPath决定。
	 */
	public static String outputFolderPath;
	/**
	 * 时间匹配正则表达式。
	 */
	public static Pattern pTime = Pattern.compile(" *(\\<.+?startTime.+?\\>)(.*)(\\<.+?\\>)");
	/**
	 * 翻译匹配正则表达式。
	 */
	public static Pattern pTransition = Pattern.compile(" *(\\<.+?textContent.+?\\>)(.*)(\\<.+?\\>)");
	
	public static void main(String[] args) throws IOException {
		File sourceFolder = new File(sourceFolderPath);
		
		if ( ! sourceFolder.exists()){
			System.out.println("处理文件夹不存在，请创建文件夹：" + sourceFolderPath);
			throw new RuntimeException("源路径不存在。");
		}
		
		if ( sourceFolder.isFile()){
			System.out.println("目标路径不是一个文件夹，请确保目标路径是一个文件夹，当前的目标路径：" + sourceFolderPath);
			throw new RuntimeException("源路径不是文件夹。");
		}
		
		//创建输出文件夹。
		outputFolderPath = sourceFolderPath + "\\output";
		File outputFolder = new File(outputFolderPath);
		outputFolder.mkdirs();
		
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
	}
	
	/**
	 * 从目标文件读取信息，输出一个符合srt格式的字幕文件。
	 * @param unSolvedSubtitle
	 * 		从html中提取的字幕格式。
	 * @throws IOException
	 */
	public static void OutputSubtitle(File unSolvedSubtitle) throws IOException{
		//创建输出文件。
		File newSubtitle = new File(outputFolderPath + "//" + unSolvedSubtitle.getName());
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
