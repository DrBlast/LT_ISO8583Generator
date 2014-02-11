package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.isoutil;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.kvaga.amphora.svfp.lt.persistence.Storage;

public class UniqueISOFields {
	private long numberIterationFor37Field=0;
	private SimpleDateFormat dateFormat12Field = new SimpleDateFormat("yyMMddHHmmss");
	private SimpleDateFormat dateFormat15Field = new SimpleDateFormat("yyMMdd");
	//private SimpleDateFormat dateFormat37Field = new SimpleDateFormat("MMddHHmmss");
	private UniqueISOFields(){}
	private static UniqueISOFields uniqueISOFields=null;
	private Storage storage = Storage.getInstance();
    private static long   TestTxnID = 0;

	/**
	 * Получение уникальных значений
	 * @return UniqueISOFields
	 */
	public static UniqueISOFields getInstance(){
		if(uniqueISOFields==null) uniqueISOFields = new UniqueISOFields();
		return uniqueISOFields;
	}

	/**
	 * Получение значения 12 поля ISO8583 сообщения
	 * @return String
	 */
	public synchronized String get12Field(){
		return dateFormat12Field.format(new Date());
	}

	/**
	 * Получение значения 15 поля ISO8583 сообщения
	 * @return String
	 */
	public synchronized String get15Field(){
		return dateFormat15Field.format(new Date());
	}

	/**
	 * Получение значения 37 поля ISO8583 сообщения
	 * @return String
	 */
	public synchronized String get37Field(){
		// Данная версия отменяется, после токого как была обнаружена проблема с задвоением authResponseCode 
		// return dateFormat37Field.format(new Date()) + getPostfix37Field();
		
		// C 2011.01.13 используется версия ниже. После поставки и применения CR authResponseCode (Clearing)
		// требуется перейти к прежней версии генерации чисел.
		return format37FieldToCorrectLength(""+storage.getNextTraceNumber());
	}

    /**
     * Увеличение номера TestTxnID
     */
    public static void incrementTestTxnID() {
        TestTxnID++;
    }

    /**
     * Очистка номера TestTxnID
     */
    public static void resetTestTxnID() {
        TestTxnID = 0;
    }

    /**
     * Получение номера TestTxnIDtoString
     * @return String
     */
    public static String getTestTxnID() {
        String TestTxnIDtoString = "";
        for (int i = 0; i < Long.toString(TestTxnID).length(); i++) {
            TestTxnIDtoString = "0" + Long.toString(TestTxnID);
        }
        return TestTxnIDtoString;
    }

	/**
	 * Получение откорректированного по длинне значения 37 поля ISO8583 сообщения
	 * @param traceNumber
	 * @return traceNumber
	 */
	private String format37FieldToCorrectLength(String traceNumber){
		while(traceNumber.length()<12){
            if (traceNumber.length() == 7)
			    traceNumber="00000"+traceNumber;
		}
		return traceNumber;
	}
}
