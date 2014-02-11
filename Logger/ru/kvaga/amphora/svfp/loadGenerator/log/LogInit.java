package ru.kvaga.amphora.svfp.loadGenerator.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogInit {
	/**
	 * LOGGER_NAME_RECEIVER - название логгера получателя
	 */
	public static final String LOGGER_NAME_RECEIVER="RECEIVER";
	/**
	 * LOGGER_NAME_SENDER - название логгера отправителяs
	 */
	public static final String LOGGER_NAME_SENDER="SENDER";
	/**
	 * LOGGER_NAME_COMMON - название общего логгера
	 */
	public static final String LOGGER_NAME_COMMON="COMMON";
	/**
	 * LOGGER_NAME_TPS - название логгера TPS
	 */
	public static final String LOGGER_NAME_TPS="TPS";

	/**
	 *	Указание выходного файла для логгера.
	 * @param loggerName - название логгера. Константы: LOGGER_NAME_RECEIVER, LOGGER_NAME_SENDER, LOGGER_NAME_COMMON
	 * @param fileName - имя файла
	 * @return true - succes<br>
	 * false - fail
	 */
	public static boolean setLoggerFile(String loggerName, String fileName){
		
		if(!new File(fileName).isFile()){
			try {
				if(!new File(fileName).createNewFile()){
					printErr("[LogInit] The <"+fileName+"> is not real file.");
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		if(loggerName.equals(LOGGER_NAME_COMMON)) loggerCommonFileName=fileName;
		else if (loggerName.equals(LOGGER_NAME_RECEIVER)) loggerReceiverFileName=fileName;
		else if(loggerName.equals(LOGGER_NAME_SENDER)) loggerSenderFileName=fileName;
		else if(loggerName.equals(LOGGER_NAME_TPS)) loggerTPSFileName=fileName;
		else{
			printErr("Unknown logger: <"+loggerName+">");
			return false;
		}
		return true;
	}
	/**
	 *	Указание выходного файла для логгера.
	 * @param loggerName - название логгера. Константы: LOGGER_NAME_RECEIVER, LOGGER_NAME_SENDER, LOGGER_NAME_COMMON
	 * @param fileName - имя файла
	 * @param filePath - путь к файлу
	 * @return true - succes<br>
	 * false - fail
	 */
	public static boolean setLoggerFile(String loggerName, String filePath, String fileName){
		
		if(!new File(filePath).isDirectory()){
			new File(filePath).mkdirs();
		}
		if(!filePath.endsWith("//")){
			filePath+="/";
		}
		if(loggerName.equals(LOGGER_NAME_COMMON))
			loggerCommonFileName=filePath + fileName;
		else if (loggerName.equals(LOGGER_NAME_RECEIVER)) loggerReceiverFileName=filePath + fileName;
		else if(loggerName.equals(LOGGER_NAME_SENDER)) loggerSenderFileName=filePath + fileName;
		else if(loggerName.equals(LOGGER_NAME_TPS)) loggerTPSFileName=filePath + fileName;
		else{
			printErr("Unknown logger: <"+loggerName+">");
			return false;
		}
		return true;
	}
	/**
	 * Получение требуемого логгера
	 * @param loggerName - название логгера. Константы: LOGGER_NAME_RECEIVER, LOGGER_NAME_SENDER, LOGGER_NAME_COMMON
	 */
	public static Logger getLogger(String loggerName){
		if(loggerName.equals(LOGGER_NAME_COMMON)){
			if(loggerCommonFileName==null){
				printErr("File didn't set for COMMON logger.");
				return null;
			}
			if(loggerCommon==null){
				if(logInitCommon()<0)
					return null;
			}
			return loggerCommon;
		}else if(loggerName.equals(LOGGER_NAME_RECEIVER)){
			if(loggerReceiverFileName==null){
				printErr("File didn't set for RECEIVER logger.");
				return null;
			}
			if(loggerReceiver==null){
				if(logInitReceiver()<0)
					return null;
			}
			return loggerReceiver;
		}else if(loggerName.equals(LOGGER_NAME_SENDER)){
			if(loggerSenderFileName==null){
				printErr("File didn't set for SENDER logger.");
				return null;
			}
			if(loggerSender==null){
				if(logInitSender()<0)
					return null;
			}
			return loggerSender;
		}else if(loggerName.equals(LOGGER_NAME_TPS)){
				if(loggerTPSFileName==null){
					printErr("File didn't set for TPS logger.");
					return null;
				}
				if(loggerTPS==null){
					if(logInitTPS()<0)
						return null;
				}
				return loggerTPS;
		}else{
			return null;
		}
	}
	
	
	private static int logInitCommon(){
		try {
			loggerCommon = Logger.getLogger(LOGGER_NAME_COMMON);
			FileHandler fh = new FileHandler(loggerCommonFileName, true);
			loggerCommon.addHandler(fh);
			//fh.setFormatter(new SimpleFormatter());
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
			
			fh.setFormatter(new Formatter(){
				public String format(LogRecord arg0) {
					return sdf.format(new Date()) + "\t" + arg0.getLevel() + "\t[Thread-" + arg0.getThreadID() +"\t]\t " + arg0.getMessage() + "\n";
				}
				
			});
			loggerCommon.setUseParentHandlers(false);
			ConsoleHandler ch = new ConsoleHandler();
			ch.setFormatter(new Formatter(){
				public String format(LogRecord arg0){
					return sdf.format(new Date()) + "\t" + arg0.getLevel() + "\t[Thread-" + arg0.getThreadID() +"\t] " + arg0.getMessage() + "\n";
				}
			});
			loggerCommon.addHandler(ch);
			
			
			loggerCommon.setLevel(Level.ALL);
			
		} catch (SecurityException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e1) {
			e1.printStackTrace();
			return -2;
		}	
		return 0;
	}
	private static int logInitReceiver(){
		try{
			loggerReceiver = Logger.getLogger(LOGGER_NAME_RECEIVER);
			FileHandler fh = new FileHandler(loggerReceiverFileName,true);
			loggerReceiver.addHandler(fh);
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
			fh.setFormatter(new Formatter(){
				public String format(LogRecord arg0){
					return sdf.format(new Date())+";" + arg0.getMessage() + "\n";
				}
			});
			loggerReceiver.setLevel(Level.ALL);
			loggerReceiver.setUseParentHandlers(false);
		}catch(SecurityException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e1) {
			e1.printStackTrace();
			return -2;
		}	
		return 0;
	}
	private static int logInitSender(){
		try{
			loggerSender = Logger.getLogger(LOGGER_NAME_SENDER);
			FileHandler fh = new FileHandler(loggerSenderFileName,true);
			loggerSender.addHandler(fh);
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
			fh.setFormatter(new Formatter(){
				public String format(LogRecord arg0){
					return sdf.format(new Date())+";"+arg0.getMessage() + "\n";
				}
			});
			loggerSender.setLevel(Level.ALL);
			loggerSender.setUseParentHandlers(false);
		}catch(SecurityException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e1) {
			e1.printStackTrace();
			return -2;
		}	
		return 0;
	}
	
	private static int logInitTPS(){
		try{
			loggerTPS = Logger.getLogger(LOGGER_NAME_TPS);
			FileHandler fh = new FileHandler(loggerTPSFileName,true);
			loggerTPS.addHandler(fh);
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
			fh.setFormatter(new Formatter(){
				public String format(LogRecord arg0){
					return sdf.format(new Date())+";"+arg0.getMessage() + "\n";
				}
			});
			loggerTPS.setLevel(Level.ALL);
			loggerTPS.setUseParentHandlers(false);
		}catch(SecurityException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e1) {
			e1.printStackTrace();
			return -2;
		}	
		return 0;
	}
	private static Logger loggerReceiver=null,
					loggerSender=null,
					loggerCommon=null,
					loggerTPS=null;

	private static String loggerReceiverFileName=null,
							loggerSenderFileName=null,
							loggerCommonFileName=null,
							loggerTPSFileName=null;

	private LogInit(){}
	
	private static void printErr(Object text){
		System.err.println("[ERROR][LOGGER] " + text);
	}
	
//	private logInfo(Object text){
//		log.lo
//	}
	
//	//	------------- Logger ---------------//
//	private static boolean bolAlreadyInit = false;
//	private static Logger log;
//	private static int logInit(){
//		if(bolAlreadyInit) return 1;
//		
//		try {
//			//log = Logger.getLogger("OBS Logger");
//			log = Logger.getLogger();
//			FileHandler fh = new FileHandler(LOG_FILE, true);
//			log.addHandler(fh);
//			//fh.setFormatter(new SimpleFormatter());
//			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
//			
//			fh.setFormatter(new Formatter(){
//				public String format(LogRecord arg0) {
//					return sdf.format(new Date()) + "\t" + arg0.getLevel() + "\t[Thread-" + arg0.getThreadID() +"\t]\t " + arg0.getMessage() + "\n";
//				}
//				
//			});
//			log.setLevel(Level.ALL);
//			
//		} catch (SecurityException e) {
//			e.printStackTrace();
//			return -1;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return -2;
//		}	
//		bolAlreadyInit = true;
//		return 0;
//	}
	
	public static void main(String args[]){
		LogInit.setLoggerFile(LogInit.LOGGER_NAME_COMMON, "c://qqq.Common.log");
		LogInit.setLoggerFile(LogInit.LOGGER_NAME_RECEIVER, "c://qqq.Receiver.log");
		LogInit.setLoggerFile(LogInit.LOGGER_NAME_SENDER, "c://qqq.Sender.log");
		LogInit.setLoggerFile(LogInit.LOGGER_NAME_TPS, "c://qqq.TPS.log");
		
		Logger sender = LogInit.getLogger(LogInit.LOGGER_NAME_SENDER);
		Logger common = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
		Logger receiver = LogInit.getLogger(LogInit.LOGGER_NAME_RECEIVER);
		Logger tps = LogInit.getLogger(LogInit.LOGGER_NAME_TPS);
		if(sender==null)
			printErr("Sender1  =null");
		if(receiver==null)
			printErr("Receiver1  =null");
		if(common==null)
			printErr("Common1  =null");
		if(tps==null)
			printErr("tps1  =null");
		for(int i = 0; i<10; i++){
			sender.log(Level.INFO,""+i);
			receiver.log(Level.INFO,""+i);
			common.log(Level.INFO,""+i);	
			tps.log(Level.INFO,""+i);
		}
		sender = LogInit.getLogger(LogInit.LOGGER_NAME_SENDER);
		common = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
		receiver = LogInit.getLogger(LogInit.LOGGER_NAME_RECEIVER);
		tps= LogInit.getLogger(LogInit.LOGGER_NAME_TPS);
		if(sender==null)
			printErr("Sender2  =null");
		if(receiver==null)
			printErr("Receiver2  =null");
		if(common==null)
			printErr("Common2  =null");
		if(tps==null)
			printErr("tps2  =null");
		for(int i = 20; i<30; i++){
			sender.log(Level.INFO,""+i);
			receiver.log(Level.INFO,""+i);
			common.log(Level.INFO,""+i);	
			tps.log(Level.INFO,""+i);
		}
	}
}
