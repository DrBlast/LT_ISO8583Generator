package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 13.01.14
 * Time: 16:46
 */
public class FilePrinter {

	/**
	 * Пример добавления строки в файл
	 *
	 * @param fileName
	 * @param outputText
	 */
	public static void printFileRecordOld(String fileName, String outputText) {
		try {
			writeBuffered(outputText, fileName);
		} catch (IOException e) {
			LoadGenerator.logCommon.log(Level.WARNING, "[FilePrinter] Exception: " + e.toString());
		}
	}

	/**
	 * Пример добавления строки в файл
	 *
	 * @param fileName
	 * @param outputText
	 */
	public static void printFileRecord(String fileName, String outputText) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
			out.println(outputText);
		} catch (IOException e) {
			LoadGenerator.logCommon.log(Level.WARNING, "[FilePrinter] Exception: " + e.toString());
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Пример добавления строки в файл
	 *
	 * @param obj
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeBuffered(Object obj, String fileName) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		try {
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			FileOutputStream fos = new FileOutputStream(raf.getFD());
			objectOutputStream = new ObjectOutputStream(fos);
			objectOutputStream.writeObject(obj);
		} finally {
			if (objectOutputStream != null) {
				objectOutputStream.close();
			}
		}
	}

	/**
	 * Java7 std File write method
	 *
	 * @param strLines
	 * @param fileName
	 * @throws IOException
	 */
	static void writeTextFile(List<String> strLines, String fileName) throws IOException {
		Path path = Paths.get(fileName);
		Files.write(path, strLines, StandardCharsets.UTF_8);
	}

	/**
	 * Тестирование скорости записи файла
	 */
	public static void testFileWriteSpeed() {
		try {
			char[] chars = new char[20 * 1024 * 1024];
			Arrays.fill(chars, 'A');
			String text = new String(chars);

			//check HDD write speed. method 1:
			long start = System.nanoTime();
			BufferedWriter bw = new BufferedWriter(new FileWriter("a.txt"));
			bw.write(text);
			bw.close();
			long time = System.nanoTime() - start;
			System.out.println("check HDD write speed("+chars.length/1024/1024+" Mb). method 1:  " + chars.length * 1000L / time + " MB/s.");

			//check HDD write speed. method 2:
			start = System.nanoTime();
			printFileRecord("a.txt", text);
			time = System.nanoTime() - start;
			System.out.println("check HDD write speed("+chars.length/1024/1024+" Mb). method 2:  " + chars.length * 1000L / time + " MB/s.");

			//check HDD write speed. method 3:
			start = System.nanoTime();
			printFileRecordOld("a.txt", text);
			time = System.nanoTime() - start;
			System.out.println("check HDD write speed("+chars.length/1024/1024+" Mb). method 3:  " + chars.length * 1000L / time + " MB/s.");

			//check HDD write speed. method 4:
			List<String> strLines = new ArrayList();
			strLines.add(text);
			start = System.nanoTime();
			writeTextFile(strLines, "a.txt");
			time = System.nanoTime() - start;
			System.out.println("check HDD write speed("+chars.length/1024/1024+" Mb). method 4:  " + chars.length * 1000L / time + " MB/s.");


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}