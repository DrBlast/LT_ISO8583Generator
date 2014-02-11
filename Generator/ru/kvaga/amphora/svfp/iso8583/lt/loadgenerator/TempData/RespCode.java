package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 09.12.13
 * Time: 12:59
 * To change this template use File | Settings | File Templates.
 */
public enum RespCode {
	OK("000"),
	POSSIBLE_FRAUD("952"),
	SCOR_SUSP_FRAUD_AND_BLOCK("245"), // Транзакция отклонена модулем скорингового анализа как мошенническая. Блокировать карту.
	SCOR_SUSPECT_FRAUD("246"), // Транзакция отклонена модулем скорингового анализа как мошенническая.
	SCOR_ERROR("248"),  // Ошибка при обработке транзакции. Данный код отказа служит для информирования SVFE или внешней системы
	// фрод-мониторинга невозможности проверки транзакции с использованием только одного скоринг-монитора.
	FRAUD_ERROR("247"), // Ошибка при обработке транзакции внешней системы фрод-мониторинга.
	// Данный код является основанием для отказа в авторизации.
	SUSP_FRAUD_AND_BLOCK("280"),
	CARD_BLOCKED("953"); //незадокументированный код ответа, выявлен 11.12.2013

	private String value = "XXX"; //default UNKNOWN_CODE

	RespCode(String s) {
		this.value = s;
	}

	public String getCodeValue() {
		return value;
	}

	private static final Map<String, RespCode> lookup = new HashMap<String, RespCode>();

	static {
		//Create reverse lookup hash map
		for (RespCode d : RespCode.values())
			lookup.put(d.getCodeValue(), d);
	}

	/**
	 * Получение значения результата транзакции
	 * @return
	 */
	public static RespCode get(String value) {
		//the reverse lookup by simply getting
		//the value from the lookup HsahMap.
		return (RespCode) lookup.get(value);
	}

	/**
	 * Проверка содержания результата в справочнике
	 * @param value
	 * @return  true - результат присутствует в справочнике <br>
	 *         false - результат отсутствует в справочнике
	 */
	public static boolean containsCode(String value) {
		return lookup.containsKey(value);
	}

	/**
	 * Проверка, является ли результат фродовым
	 * @param value
	 * @return  true - фрод <br>
	 *         false - не фрод
	 */
	public static boolean containsFraudCode(String value) {
		if (value.equals("000")) {
			return false;
		} else {
			return lookup.containsKey(value);
		}
	}
}
