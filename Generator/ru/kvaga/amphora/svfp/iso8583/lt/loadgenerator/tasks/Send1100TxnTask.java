package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.tasks;

import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.TransactionMap;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.SystemConfigurator;
import ru.kvaga.amphora.svfp.loadGenerator.log.LogInit;
//import org.jpos.iso;
import ru.kvaga.amphora.svfp.iso8583.lt.client.ISO8583Client;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.CardData;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.CountryEnum;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.isoutil.RandomISOFields;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.TrnTypeEnum;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.isoutil.UniqueISOFields;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.statistic.LoadStatistic;

import static ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util.FilePrinter.printFileRecord;

public class Send1100TxnTask implements Runnable {
	private final Logger logCommon = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
	private final Logger logSender = LogInit.getLogger(LogInit.LOGGER_NAME_SENDER);

	private final ISO8583Client isoClient;
	private final UniqueISOFields uniqueISOFields = UniqueISOFields.getInstance();
	private final RandomISOFields randomISOFields = RandomISOFields.getInstance();

	private final LoadStatistic loadStatistic = LoadStatistic.getInstance();
	private final String cardID, amount, procCode, terminalType;
	private final TrnTypeEnum trnType;
	private       CountryEnum countryCode;
	private final Random rnd = new Random();

	public Send1100TxnTask(ISO8583Client isoClient, String cardID, String amount, String procCode, TrnTypeEnum trnType,
	                       String terminalType, CountryEnum countryCode) {
		this.isoClient = isoClient;
		this.cardID = cardID;
		this.amount = amount;
		this.procCode = procCode;
		this.trnType = trnType;
		this.terminalType = terminalType;
		this.countryCode = countryCode;
		Thread t = new Thread(this);
		t.setName("Send1100TxnTask-" + Long.toString(t.getId()));

		// checking current threads count
		t.start();
	}

	/**
	 * Запуск потока отправки сообщения 1100
	 */
	@Override
	public void run() {
		try {
			long currentTimeMillis = new Date().getTime();
			//call main function
			send1100();

			long threadLiveTime = new Date().getTime() - currentTimeMillis;
			LoadStatistic.send1100ThreadFinished(threadLiveTime);
		} catch (Exception ex) {
			LoadGenerator.logCommon.log(Level.WARNING, "[Send1100TxnTask] Exception: " + ex.toString());
		}
	}

	/**
	 * Основная функция отправки сообщения 1100
	 *
	 * @return int
	 */
	public int send1100() {
		logCommon
				.log(Level.INFO, "[Send1100TxnTask] Задача Send1100TxnTask запущена с параметрами: " + ";cardID=" + cardID + ";amount=" + amount
						+ ";procCode=" + procCode + ";trnType=" + trnType.getValue() + ";terminalType=" + terminalType + ";country ="
						+ countryCode);
		// Проверки уровня клиента ISO
		if (!isoClient.isConnected()) {
			logCommon.log(Level.WARNING, "[isoClient] There is no connection between ISO8583 client and server");
			loadStatistic.setIncrementConnectTotalFailed();
			return -8;
		}

		// Проверки уровня ISO сообщения
		if (procCode.length() != 6) {
			logCommon.log(Level.WARNING, "[Send1100TxnTask] Incorrect processing code length. Expected: 6, Actual: " + procCode.length());
			return -2;
		}
		if (trnType.getValue().length() != 3) {
			logCommon.log(Level.WARNING, "[Send1100TxnTask] Incorrect transaction type length. Expected: 3, Actual: "
					+ trnType.toString().length());
			return -3;
		}

		ISOMsg msg = new ISOMsg();

		String FETransactionId = "";
		String field12 = "";
		String field15 = "";
		// String field3 = "";
		synchronized (uniqueISOFields) {
			FETransactionId = uniqueISOFields.get37Field();
			field12 = uniqueISOFields.get12Field();
			field15 = uniqueISOFields.get15Field();
		}

		String terminalID = randomISOFields.getTerminalEnum(trnType);
		String cardID = this.cardID;
		CardData cardData = randomISOFields.getCardData(cardID);
		String mccID = Integer.toString(randomISOFields.getMCC(trnType));
		String posDataCode = randomISOFields.getPOSDateCode(trnType);
		String currency = rnd.nextBoolean() ? "810" : "840";
		String field11Value = LoadGenerator.getTestTxnID();

		//если предыдущая транзакция для выбранной карты из другой страны была менее, чем секунду назад,
		//а также по профилю фродов не должно быть фрода (транзакции из разных стран),
		//то взять страну из предыдущей транзакции для данной карты
		if (LoadGenerator.bConf.FRAUD_CHANCE != 0) {
			long prevCardTimestamp = cardData.getTimestamp();
			if ((System.currentTimeMillis() - prevCardTimestamp <= 1) && (cardData.getCountry() != countryCode)) {
				logCommon.log(Level.INFO, "[Send1100TxnTask] System.currentTimeMillis() - cardData.getTimestamp() <=1");
				LoadStatistic loadStatistic = LoadStatistic.getInstance();
				if (loadStatistic.getSendTxnsTotal() % Math.round((100 / LoadGenerator.bConf.FRAUD_CHANCE)) != 0)
					countryCode = cardData.getCountry();
			}
		}
		try {
			randomISOFields.setCardData(cardID, countryCode, System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			msg.setMTI("1100"); // MTI, тип сообщения
			// --> 1100 (платежное сообщение, отправляется со стороны SV)
			// <-- 1110

			// --> 1804 (проверка сети, отправляется со стороны SVFP)
			// <-- 1814
			msg.set(2, cardID);// "4154286515291236");
			// Поле содержит номер карты, по которому производится операция.
			msg.set(3, procCode); // field3 //msg.set(3, "002000");
			// Код обработки состоит из 3 подполей. Первое подполе (позиции 1-2)
			// указывает на код проводимой операции, второе (позиции 3-4)
			// и третье (позиции 5-6) – тип счета источника и счета получателя соответственно.
			msg.set(4, amount); // Сумма, запрошенная держателем карты в валюте транзакции без комиссии.
			// В 1100 запросе баланса поле должно содержать нулевое значение. В 1110 ответе на запрос баланса
			// в поле будет передан баланс счета или нули. Если баланс отрицательный.
			msg.set(5, amount); // Сумма операции в валюте счета, включая сумму комиссии.
			// В запросе в поле передается сумма транзакции, в ответе в поле передается баланс счета.
			msg.set(6, amount); // В поле передается сумма, содержащаяся в поле 4,
			// сконвертированная в валюту расчета (51 поле). Курс конвертации передается 9 поле.

			msg.set(9, "70300000"); // Курс конвертации из валюты транзакции (49 поле) в валюту расчетов (51 поле).
			// Левая цифра указывает на количество знаков поле запятой в курсе конвертации, последующие 7 цифр
			// указывают курс, для которого устанавливается «запятая».
			// Пример: значение 69985022 обозначает курс 9.985022.
			msg.set(10, "53333333"); // Курс конвертации из валюты расчетов (51 поле) в валюту счета (50 поле).
			// Левая цифра указывает на количество знаков поле запятой в курсе конвертации, последующие 7 цифр
			// указывают курс, для которого устанавливается «запятая».
			// Пример: значение 69985022 обозначает курс 9.985022.
			// Поле используется для сопоставления ответного сообщения с оригинальным запросом.
			msg.set(11, field11Value);
			// YYMMD(/Dhhmmss Локальная дата и время совершения транзакции в точке приема карты
			msg.set(12, field12);

			// msg.set(12, "130522122511");
			msg.set(14, cardID.substring(6, 10)); // YYMM когда истекает период использования карты
			msg.set(15, field15); // YYMMDD Банковская дата эквайера в SVFE.
			msg.set(18, mccID); // Поле 18: Merchant Type
			msg.set(22, posDataCode); // Поле 22: Point of Service Code;
			// msg.set(22, "000000000000"); // Поле 22: Point of Service Code;
			// msg.set(22, new byte[]{30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30});
			msg.set(32, "00"); // Код института эквайера идентифицирует финансовый институт, в котором была инициирована
			// транзакция.
			msg.set(37, FETransactionId); // Ссылочный номер транзакции. Используется для сопоставления операций между собой.
			// //msg.set(37, "500002612352");
			msg.set(41, terminalID); // "122141  ");
			// Идентификатор терминала. Код, уникально идентифицирующий терминал в пределах точки обслуживания.
			// Наименование и расположение точки обслуживания.
			msg.set(43, "TSPTESTOVOEPOS>" + countryCode.getCapital() + "             " + countryCode);
			// [ИМЯ]>[ГОРОД][\s] 38 символов + 2 симв код страны
			// msg.set(48, "0010012002003" + trnType.getValue() + "013002B" + terminalType); //
			msg.set(48, "0010011002003" + trnType.getValue() + "00400100250011026001" + terminalType + "027003" + "121"
					+ "028007       029001 030001 031001 03200200033001003400200035001003600200037003000");
			// msg.set(48, "0010012002003680");
			// TODO 49,50,51 - "810" "840" random ; Done
			msg.set(49, currency); // Код валюты, в которой транзакция была запрошена держателем карты.
			msg.set(50, currency); // Код валюты счета, по которому производится операция.
			currency = rnd.nextBoolean() ? "810" : "840";
			msg.set(51, currency); // Валюта расчета.

			msg.set(100, "00"); // Номер института эмитента карты в SVFE.


			// logCommon.log(Level.INFO, "***** SENDING ISO MESSAGE: *******");
			// // Get and print the output result
			// // byte[] data = msg.pack();
			// // System.out.println("RESULT : " + new String(data));
			// String msgPartStr = "";
			// for (int i = 0; i < 199; i++) {
			// try {
			// for (int j = 0; j < msg.getBytes(i).length; j++) {
			// msgPartStr += (char) msg.getBytes(i)[j];
			// }
			// logCommon.log(Level.INFO, String.valueOf(i) + " : " + msgPartStr);
			// msgPartStr = "";
			// } catch (Exception e) {
			//
			// }
			// }
			// logCommon.log(Level.INFO, "************");
		} catch (ISOException e) {
			logCommon.log(Level.WARNING, e.getMessage());
			return -6;
		}

		int statusSendTxn = isoClient.sendMsg(msg);

		if (statusSendTxn < 0) {

			switch (statusSendTxn) {
				case -1:
					logCommon.log(Level.WARNING,
					              "[isoClient] Не установлен packager для упаковки сообщений. Это необходимо делать в конструкторе");
					break;
				case -2:
					logCommon.log(Level.WARNING, "[isoClient] Socket Error");
					break;
				case -3:
					logCommon.log(Level.WARNING, "[isoClient] I/O ERROR");
					break;
				case -4:
					logCommon.log(Level.WARNING, "[isoClient] Ошибка во время подготовки к отправке сообщения");
					break;
				case -5:
					logCommon.log(Level.WARNING, "[isoClient] Connection lost");
					break;
				case -6:
					logCommon.log(Level.WARNING, "[isoClient] Error of calculating BitMap");
					break;
			}
			return -11;
		}

		logCommon.log(Level.INFO, "[SEND] Message <1100> was send with parameters: cardID=<" + cardID + ">, FE_TRN_ID=<"
				+ FETransactionId + ">, PROCESSING_CODE=<" + procCode + ">, AMOUNT=<" + amount + ">.");

		//logSender.log(Level.ALL, "1100;" + FETransactionId + ";" + cardID);

		TransactionMap.getInstance().setTxnMap(Long.parseLong(FETransactionId), new Date().getTime());
		loadStatistic.setIncrementSendTxnsTotal();
		printFileRecord(SystemConfigurator.getInstance().SendLogFullPath ,LoadGenerator.getFormattedDate(new Date(), SystemConfigurator.getInstance().timeFormat)+";1100;" + FETransactionId + ";" + cardID);



		// System.out.println("1100;"+FETransactionId+";"+idCustomer);

		return 0;
	}

}
