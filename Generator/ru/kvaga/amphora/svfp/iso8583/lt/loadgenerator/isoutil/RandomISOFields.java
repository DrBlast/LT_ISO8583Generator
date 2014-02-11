package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.isoutil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.CardData;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.BusinessConfigurator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.TrnTypeEnum;
import ru.kvaga.amphora.svfp.lt.persistence.Storage;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.CountryEnum;


public class RandomISOFields {

	private RandomISOFields() {
		//заполнение пула карточек
		loadCardIDs(BusinessConfigurator.getInstance().DATAPOOL);
		loadCurrency("conf/ISO8583.LT.LoadGenerator.Currency.cfg");
	}

	private static RandomISOFields randomISOFields = null;
	private        Storage         storage         = Storage.getInstance();

	private static String terminalPool[][] =
			{
					{"10008611",
					 "S1DP9509",
					 "54310308",
					 "70029995",
					 "19270001"},
					// type 1: 508, 700
					{"00501538",
					 "00400117",
					 "00152175",
					 "00155386",
					 "55012780"},
					// type 2: 774
					{"00400874"}
					// type 8: 680
			};

	private static List<Integer> mccPool = Arrays.asList(6011, 7011, 5309, 4814, 4812, 5722, 5944, 5732, 5072, 5734,
	                                                     5932, 5812, 5541, 5993, 4814, 5812, 8999, 7011, 5699, 8398, 5993, 9402, 5542, 5983, 5542);

	private static List<Character> pdcFieldValues = Arrays.asList('T', 'U', 'V', 'S');

	/**
	 * Создание карты терминалов
	 *
	 * @return Collections.unmodifiableMap(terminalMap);
	 */
	private static Map<TrnTypeEnum, List<String>> create_map() {
		Map<TrnTypeEnum, List<String>> terminalMap = new HashMap<TrnTypeEnum, List<String>>();
		List<String> tmp = Arrays.asList("10008611", "S1DP9509", "54310308", "70029995");
		terminalMap.put(TrnTypeEnum.CASHOUT_ATM, tmp);
		// aMap.put(TrnTypeEnum.PURCHASE_ATM, tmp);
		tmp = Arrays.asList("501538", "400117", "152175", "155386", "55012780");
		terminalMap.put(TrnTypeEnum.POS, tmp);
		tmp = Arrays.asList("400874");
		terminalMap.put(TrnTypeEnum.EPOS, tmp);
		return Collections.unmodifiableMap(terminalMap);
	}

	private       ArrayList<String>                   cardIDPool   = new ArrayList<String>();
	private       ArrayList<String>                   CurrencyPool = new ArrayList<String>();
	public static ConcurrentHashMap<String, CardData> CardMap      = new ConcurrentHashMap<String, CardData>();

	private Random rnd = new Random();

	/**
	 * Получение Instance рандомизатора значений
	 *
	 * @return RandomISOFields
	 */
	public static RandomISOFields getInstance() {
		if (randomISOFields == null) randomISOFields = new RandomISOFields();

		return randomISOFields;
	}

	/**
	 * Получение ID терминала
	 *
	 * @param trnType
	 * @return terminalID
	 */
	public /*synchronized*/ String getTerminalEnum(TrnTypeEnum trnType) {
		String eightSpaces = "        ";
		String terminalID = "XXXXXXXX";

		if ((trnType.equals(TrnTypeEnum.CASHOUT_ATM)) || (trnType.equals(TrnTypeEnum.PURCHASE_ATM)) || (trnType
				.equals(TrnTypeEnum.BALANCE)))
			terminalID = terminalPool[0][rnd.nextInt(terminalPool[0].length)];    // 1
		else if (trnType.equals(TrnTypeEnum.POS))
			terminalID = terminalPool[1][rnd.nextInt(terminalPool[1].length)];    // 2
		else if (trnType.equals(TrnTypeEnum.EPOS))
			terminalID = terminalPool[2][rnd.nextInt(terminalPool[2].length)];    // 8
		// дополняем пробелами до 8-ми символов
		return terminalID;//.concat(eightSpaces.substring(0, 8 - terminalID.length()));
	}

	/**
	 * get isofield #22 Point of Service Data Code
	 *
	 * @param trnType
	 * @return String(pdcChars)
	 */
	public /*synchronized*/ String getPOSDateCode(TrnTypeEnum trnType) {
		char[] pdcChars = new char[12];
		pdcChars[11] = '1';
		pdcChars[10] = '0';
		pdcChars[9] = '0';
		pdcChars[3] = '9';

		if (trnType.equals(TrnTypeEnum.EPOS)) {
			pdcChars[0] = '0';
			pdcChars[1] = '6';
			pdcChars[2] = (rnd.nextInt(2) == 0) ? '0' : '1';
			pdcChars[4] = '5';
			pdcChars[5] = '0';
			pdcChars[8] = '5';
			pdcChars[6] = pdcFieldValues.get(rnd.nextInt(pdcFieldValues.size()));
			pdcChars[7] = pdcFieldValues.get(rnd.nextInt(pdcFieldValues.size() - 1));
		} else {
			pdcChars[0] = '8';
			pdcChars[5] = '1';
			pdcChars[7] = pdcChars[1] = (rnd.nextInt(2) == 0) ? '1' : '6';
			pdcChars[4] = pdcChars[2] = (rnd.nextInt(2) == 0) ? '0' : '1';
			pdcChars[6] = (rnd.nextInt(2) == 0) ? '2' : '5';
			pdcChars[8] = (rnd.nextInt(2) == 0) ? '1' : '5';
		}
		return new String(pdcChars);
	}

	/**
	 * Получение mcc по типу транзакции
	 *
	 * @param trnType
	 * @return mcc
	 */
	public /*synchronized*/ int getMCC(TrnTypeEnum trnType) {
		int mcc = mccPool.get(0);
		if (!trnType.equals(TrnTypeEnum.POS)) {
			mcc = mccPool.get(rnd.nextInt((mccPool.size() - 1)) + 1);
		}
		return mcc;
	}

	/**
	 * Получение случайного номера карты из пула
	 *
	 * @return cardID
	 */
	public /*synchronized*/ String getCard() {
		String cardID = "";
		try {
			cardID = (String) cardIDPool.get(rnd.nextInt(cardIDPool.size()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cardID;
	}

	/**
	 * Получение данных по карте на основе ID
	 *
	 * @param cardID
	 * @return CardData
	 */
	public /*synchronized*/ CardData getCardData(String cardID) {
		CardData cardData = null;
		try {
			cardData = CardMap.get(cardData);
		} catch (Exception e) {
			cardData = new CardData(cardID, CountryEnum.RU, 0);
		}
		return cardData;
	}

	/**
	 * Присвоение данных по карте
	 *
	 * @param cardID
	 * @param country
	 * @param timestamp
	 * @throws Exception
	 */
	public /*synchronized*/ void setCardData(String cardID, CountryEnum country, long timestamp) throws Exception {
		CardData cardData = CardMap.get(cardID);
		cardData = CardMap.replace(cardID, cardData);
	}

	/**
	 * Получение случайного номера
	 *
	 * @param generateNumbersCount
	 * @param fieldLength
	 * @param prefix
	 * @param rate10
	 * @param maxNumber
	 * @return randomNumber
	 */
	public String generateRandomNumberWithPrefix(int generateNumbersCount, int fieldLength, String prefix, boolean rate10,
	                                             int maxNumber) {
		if (rate10) {
			int maxInt = 1, rateLength = 1;
			Random randLength = new Random();
			rateLength = randLength.nextInt(maxNumber) + 1;
			for (int i = 0; i < rateLength; i++) maxInt = maxInt * 10;
			String randomNumber = Integer.toString(maxInt);
			while (randomNumber.length() < fieldLength) {
				randomNumber = prefix + randomNumber;
			}
			return randomNumber;
		} else {
			return generateRandomNumberWithPrefix(generateNumbersCount, fieldLength, prefix);
		}
	}

	/**
	 * Получение случайного номера
	 *
	 * @param generateNumbersCount
	 * @param Fieldlength
	 * @param prefix
	 * @return randomNumber
	 */
	public String generateRandomNumberWithPrefix(int generateNumbersCount, int Fieldlength, String prefix) {
		if (prefix.isEmpty()) {
			return generateRandomNumberWithPrefix(generateNumbersCount, Fieldlength);
		}
		String randomNumber = generateRandomNumber(generateNumbersCount);
		while (randomNumber.length() < Fieldlength) {
			randomNumber = randomNumber + prefix;
		}
		return randomNumber;
	}

	/**
	 * Получение случайного номера
	 *
	 * @param numbersCount
	 * @param Fieldlength
	 * @return randomNumber
	 */
	public String generateRandomNumberWithPrefix(int numbersCount, int Fieldlength) {
		String randomNumber = generateRandomNumber(numbersCount);
		while (randomNumber.length() < Fieldlength) {
			randomNumber = randomNumber + "0";
		}
		return randomNumber;
	}

	/**
	 * Получение случайного номера
	 *
	 * @param count
	 * @return randomNumber
	 */
	public String generateRandomNumber(int count) {
		Random rand = new Random();
		int maxInt = 1;
		for (int i = 0; i < count; i++) maxInt = maxInt * 10;
		return Integer.toString(rand.nextInt(maxInt - maxInt / 10 - 1) + maxInt / 10);
	}

	/**
	 * Получение пула карт
	 *
	 * @param fileName
	 * @return 0 - успех <br
	 *         -1 - файл не найден <br>
	 *         -2 - ошибка ввода\вывода при чтении файла параметров<br>
	 *         -3 - другая ошибка<br>
	 */
	private int loadCardIDs(String fileName) {
		try {
			InputStream fis = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			String line;
			while ((line = br.readLine()) != null) {
				// Deal with the line
				cardIDPool.add(line);
				CardMap.put(line, new CardData(line, CountryEnum.RU, 0));
			}
			br.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		} catch (Exception e) {
			e.printStackTrace();
			return -3;
		}
		return 0;
	}

	/**
	 * Получение справочника валют (не используется)
	 *
	 * @param fileName
	 * @return 0 - успех <br
	 *         -1 - файл не найден <br>
	 *         -2 - ошибка ввода\вывода при чтении файла параметров<br>
	 *         -3 - другая ошибка<br>
	 */
	private int loadCurrency(String fileName) {
		try {
			InputStream fis = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			String line;
			// Deal with the line
			while ((line = br.readLine()) != null) {
				CurrencyPool.add(line.split(";")[1]);  //0- ShortName 1-CurrencyCode 2-Valure full russian name
			}
			br.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		} catch (Exception e) {
			e.printStackTrace();
			return -3;
		}
		return 0;
	}

	public String getProcCode(TrnTypeEnum transactionType) {
		String procCode = "000000";
		switch (transactionType) {
			case EPOS:
				procCode = "000000";
				break;
			case CASHOUT_ATM:
				procCode = "010000";
				break;
			case BALANCE:
				procCode = "310000";
				break;
			case POS:
				procCode = "000000";
				break;
			case PURCHASE_ATM:
				procCode = "500000";
				break;
		}
		return procCode;
	}

	public String getTerminalType(TrnTypeEnum transactionType) {
		String terminalType = "1";
		switch (transactionType) {
			case EPOS:
				terminalType = "8";
				break;
			case CASHOUT_ATM:
				terminalType = "1";
				break;
			case BALANCE:
				terminalType = "1";
				break;
			case POS:
				terminalType = "2";
				break;
			case PURCHASE_ATM:
				terminalType = "1";
				break;
		}
		return terminalType;
	}
}
