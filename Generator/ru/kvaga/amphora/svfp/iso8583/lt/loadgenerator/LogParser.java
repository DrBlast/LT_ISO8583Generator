package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util.CollectionsPlus;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 02.12.13
 * Time: 14:23
 * Тестовый класс парсинга логов
 */
public class LogParser {

	/**
	 * Преобразование строки в дату
	 *
	 * @param strDate
	 * @param dateFormat
	 * @return Date
	 * @throws Exception
	 */
	private static Date stringToDate(String strDate, String dateFormat) throws Exception {
		if (strDate == null) {
			throw new Exception("String has null value");
		} else {
			DateFormat df = new SimpleDateFormat(dateFormat);
			Date result = df.parse(strDate);
			return result;
		}
	}

	/**
	 * Проверка на возможность парсинга long из строки
	 *
	 * @param str
	 * @return boolean
	 */
	public static boolean isLong(String str) {
		try {
			Long.parseLong(str);
		} catch (NumberFormatException nfe) {
			return false;
		} catch (NullPointerException npe) {
			return false;
		} catch (Exception e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	/**
	 * Проверка на возможность парсинга integer из строки
	 *
	 * @param str
	 * @return boolean
	 */
	public static boolean isInt(String str) {
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		} catch (NullPointerException npe) {
			return false;
		} catch (Exception e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	/**
	 * Парсер логов
	 *
	 * @param fileName
	 * @param map
	 * @return HashMap
	 */
	private static HashMap<Long, String> parseLog(String fileName, HashMap<Long, String> map) {
		try {
			String line = "";
			InputStream fis = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			// Deal with the line
			while ((line = br.readLine()) != null) {
				if (isLong(line.split(";")[2]))
					map.put(Long.parseLong(line.split(";")[2]), line.split(";")[0]);  //0- DateTime
				// 1-TXN_type
				// 2-FE_TXN_ID
				// 3-ID_CUSTOMER
				// 4-STATUS
			}
			br.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	/**
	 * Группировка записей по времени
	 *
	 * @param map
	 * @param GROUP_TIME
	 * @return
	 */
	private static HashMap<String, Long> groupLog(HashMap<String, Long> map, int GROUP_TIME) {
		try {
			//TODO сделать группировку по полю TIME

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	/**
	 * Сортировка HashMap
	 *
	 * @param unsortHashMap
	 * @return HashMap
	 */
	private static HashMap<Long, String> sortByComparator(HashMap<Long, String> unsortHashMap) {
		List<Map.Entry<Long, String>> list = new LinkedList<Map.Entry<Long, String>>(unsortHashMap.entrySet());
		// sort list based on comparator
		Collections.sort(list, new Comparator<Map.Entry<Long, String>>() {
			public int compare(Map.Entry<Long, String> o1, Map.Entry<Long, String> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		// put sorted list into map again
		//LinkedHashMap make sure order in which keys were inserted
		HashMap<Long, String> sortedMap = new LinkedHashMap<Long, String>();
		for (Iterator it = list.iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put((Long) entry.getKey(), (String) entry.getValue());
		}
		return sortedMap;
	}



	/**
	 * Сведение логов отправки и получения времени отклика.
	 */
	private static HashMap<Long, String> sendLogMap = new HashMap<Long, String>();
	private static HashMap<Long, String> recLogMap  = new HashMap<Long, String>();
	private static HashMap<String, Long> resultMap  = new HashMap<String, Long>();

	public static void parseLT_ISO8583Generator_logs(final String LOG_FILE_SENDER, final String LOG_FILE_RECEIVER,
	                                                 String LOG_RESPONCE_TIMES, int GROUP_TIME) throws
	                                                                                            IOException {
		String dateFormat = "yyyy-MM-dd HH:mm:ss,SSS";
		try {
			Thread sendLogThread = new Thread("sendLogParcer") {
				public void run() {
					sendLogMap = parseLog(LOG_FILE_SENDER, sendLogMap);
				}
			};
			sendLogThread.start();
			Thread recLogThread = new Thread("recLogParcer") {
				public void run() {
					recLogMap = parseLog(LOG_FILE_RECEIVER, recLogMap);
				}
			};
			recLogThread.start();
			//wait threads
			sendLogThread.join();
			recLogThread.join();

			OutputStream fos = new FileOutputStream(new File(LOG_RESPONCE_TIMES));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, Charset.forName("UTF-8")));
			HashMap<Long, String> sortedSendLogMap = sortByComparator(sendLogMap);
			bw.write("TEST_TIME;RERSPONCE_TIME\r\n");
			for (Map.Entry<Long, String> sendEntry : sortedSendLogMap.entrySet()) {
				String SendTimeStr = sendEntry.getValue();
				String RecTimeStr = recLogMap.get(sendEntry.getKey());
				/*System.out.println(SendTimeStr + ";" +
						                   (stringToDate(RecTimeStr, dateFormat).getTime() -
								                   stringToDate(SendTimeStr, dateFormat).getTime())
				); //TIME;RESPONCE_TIME
				*/
				//TODO сохранить таблицу результатов для дальнейшей группировки по полю TIME
				Long responseTime = (Long) (stringToDate(RecTimeStr, dateFormat).getTime() -
						stringToDate(SendTimeStr, dateFormat).getTime());
				resultMap.put(SendTimeStr, responseTime);
			}

			Map<String, Long> sortedResultMap = CollectionsPlus.sortMapByValue(resultMap);
			for (Map.Entry<String, Long> resultEntry : resultMap.entrySet()) {
				bw.write(resultEntry.getKey() + ";" + resultEntry.getValue() + "\r\n");
			}
			bw.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String LOG_FILE_SENDER = "log/2013.12.16_13-23-16/ISOServerLogSender.log";      //default
		String LOG_FILE_RECEIVER = "log/2013.12.16_13-23-16/ISOServerLogReceiver.log";  //default
		String LOG_RESPONCE_TIMES = "log/2013.12.16_13-23-16/ResponceTimes.csv";  //default
		int GROUP_TIME = 0;  //default
		if (args.length != 4) {
			System.out
			      .println("[WARN] Incorrect arguments! Usage: \"LOG_FILE_SENDER\"  \"LOG_FILE_RECEIVER\"  \"LOG_RESPONCE_TIMES\"");
		} else {
			if (isInt(args[3])) {
				LOG_FILE_SENDER = args[0];
				LOG_FILE_RECEIVER = args[1];
				LOG_RESPONCE_TIMES = args[2];
				GROUP_TIME = Integer.parseInt(args[3]);
			}
		}
		System.out.println("[INFO] Try to use args:\r\n" +
				                   "  LOG_FILE_SENDER:\"" + LOG_FILE_SENDER + "\";\r\n" +
				                   "  LOG_FILE_RECEIVER:\"" + LOG_FILE_RECEIVER + "\";\r\n" +
				                   "  LOG_RESPONCE_TIMES:\"" + LOG_RESPONCE_TIMES + "\";\r\n" +
				                   "  GROUP_TIME:" + GROUP_TIME + ".");
		if (!new File(LOG_FILE_SENDER).exists() || !new File(LOG_FILE_RECEIVER).exists()) {
			System.out.println("[ERROR] Files not exists!");
			System.exit(-1);
		} else if (GROUP_TIME < 0) {
			System.out.println("[ERROR] Incorrect GROUP time arg! Please select GROUP_TIME >= 0.");
			System.exit(-2);
		}
		try {
			parseLT_ISO8583Generator_logs(LOG_FILE_SENDER, LOG_FILE_RECEIVER, LOG_RESPONCE_TIMES, GROUP_TIME);
			System.out.println("[INFO] Operation completed. Result file:\"" + LOG_RESPONCE_TIMES + "\";");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-3);
		}

	}
}
