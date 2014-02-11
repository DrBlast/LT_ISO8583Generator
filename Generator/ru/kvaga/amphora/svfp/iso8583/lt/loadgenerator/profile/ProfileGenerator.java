package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.profile;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.CountryEnum;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.isoutil.RandomISOFields;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.TrnTypeEnum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.kvaga.amphora.svfp.iso8583.lt.client.ISO8583Client;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.*;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.tasks.Send1100TxnTask;
import ru.kvaga.amphora.svfp.loadGenerator.log.LogInit;


public class ProfileGenerator {
	private double[] data = new double[6];
	//private int      txnAmount = 100;
	private ISO8583Client isoClient;

	// Создание профиля стран
	private CountryGenerator                        countryGenerator        = CountryGenerator.getInstance();
	private RandomISOFields                         randomISOFields         = RandomISOFields.getInstance();
	private int                                     currentIteration        = 0;
	private ConcurrentHashMap<TrnTypeEnum, Integer> transactionGeneratorMap = new ConcurrentHashMap<TrnTypeEnum, Integer>();

	/**
	 * Инициализация профиля генератора
	 *
	 * @param isoClient
	 */
	private ProfileGenerator(ISO8583Client isoClient) {
		this.isoClient = isoClient;


		for (TrnTypeEnum trnType : TrnTypeEnum.values()) {
			transactionGeneratorMap.put(trnType, 0);
		}
	}


	private ProfileConfigurator pConf = null;
	private SystemConfigurator  sConf = null;
	private int                 j     = -1;
	CountryEnum countryCode = CountryEnum.RU;

    /**
     * Получение типа транзакции по профилю
     * @return
     */
	public TrnTypeEnum getNextPercentProfile() {
		TrnTypeEnum resultTrnTypeEnum = TrnTypeEnum.CASHOUT_ATM;
		if (sConf == null)
			sConf = SystemConfigurator.getInstance();
		if (pConf == null) {
			pConf = ProfileConfigurator.getInstance();
		}
		if (checkIsPercentProfileCorrect() < 0) {
			return null;
		}


		currentIteration++;

		int transactionGeneratorMapSize = transactionGeneratorMap.size();
		for (int i = 0; i < transactionGeneratorMapSize; i++) {
			j = ((j + 1) % transactionGeneratorMapSize);
			int iter = 0;
			for (Map.Entry<TrnTypeEnum, Integer> entry : transactionGeneratorMap.entrySet()) {
				int transactionTypePercent = pConf.getTrnTypeMap().get(entry.getKey());
				int currentValue = entry.getValue();
				if (((currentValue * 100 / currentIteration) < transactionTypePercent)
						&& (transactionTypePercent != 0) && (j == iter)) {
					currentValue++;
					entry.setValue(currentValue);
					return entry.getKey();
				}
				iter++;
			}

		}
		return resultTrnTypeEnum;
	}

    /**
     * Отправка следующей транзакции в сответствии с профилем
     * @return
     */
	public int sendNextPercentProfile() {
        try{
            countryCode = countryGenerator.getNextCountryProfile();
            if (countryCode == null) {
                return -1;
            }
            TrnTypeEnum transactionType = getNextPercentProfile();
            if (transactionType == null) {
                return -2;
            }
            new Send1100TxnTask(isoClient, randomISOFields.getCard(),
                                randomISOFields.generateRandomNumberWithPrefix(10, 12, "0", true, 7),
                                randomISOFields.getProcCode(transactionType),
                                transactionType,
                                randomISOFields.getTerminalType(transactionType),
		                    countryCode);
		    return 0;
        }catch(Exception ex){
            LoadGenerator.logCommon.warning("[ProfileGenerator] Error during prepare and send transaction:"+ex);
            ex.printStackTrace();
            return -3;
        }

	}


	private static ProfileGenerator profileGenerator = null;

	/**
	 * Получение инстанса профиля генератора
	 *
	 * @param isoClient
	 * @return ProfileGenerator
	 */
	public static ProfileGenerator getInstance(ISO8583Client isoClient) {
		if (profileGenerator == null) {
			profileGenerator = new ProfileGenerator(isoClient);
		}
		return profileGenerator;
	}


	Logger logCommon = null;

	/**
	 * Проверка процентных параметров
	 *
	 * @return 0 - без ошибок <br>
	 *         -1 - ошибка в значении параметров
	 */
	private int checkIsPercentProfileCorrect() {

		if (logCommon == null) logCommon = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);
		double sumOfProfilePercents = 0;
		for (Map.Entry<TrnTypeEnum, Integer> entry : pConf.getTrnTypeMap().entrySet()) {
			sumOfProfilePercents += entry.getValue();
		}
		if (sumOfProfilePercents != 100) {
			logCommon.log(Level.WARNING, "[ProfileGenerator] Incorrect sum of profile percents. Requirement: 100%, Actual: "
					+ sumOfProfilePercents + "%");
			return -1;
		}
		return 0;
	}

}
