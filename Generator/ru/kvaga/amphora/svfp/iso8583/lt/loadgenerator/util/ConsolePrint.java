package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util;

public class ConsolePrint {
	/**
	 * Вывод текста
	 * @param text
	 */
	public static void print(Object text){
		System.out.println(text);
	}

	/**
	 * Вывод ошибки
	 * @param text
	 */
	public static void printErr(Object text){
		System.err.println("[ERROR] "+text);
	}
}
