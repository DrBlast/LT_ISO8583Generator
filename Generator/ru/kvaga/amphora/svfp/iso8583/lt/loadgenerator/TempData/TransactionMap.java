package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.LoadGenerator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 13.01.14
 * Time: 16:22
 */
public class TransactionMap {
	private  ConcurrentHashMap<Long, Long> txnMap = new ConcurrentHashMap<Long, Long>();
	private static TransactionMap transactionMap = null;
	/**
	 * Получение Конфигурации Стран
	 *
	 * @return CountryConfigurator
	 */
	static public TransactionMap getInstance() {
		if (transactionMap == null) {
			transactionMap = new TransactionMap();
		}
		return transactionMap;
	}

	/**
	 * Добавление транзакции с времени создания в список
	 * @param txnId
	 * @param time_creation
	 */
	public  void setTxnMap(long txnId, long time_creation){
		try{
			if (!txnMap.containsKey(txnId)){
				txnMap.put(txnId, time_creation);
			} else {
				LoadGenerator.logCommon.log(Level.WARNING,"[TransactionMap] Transaction ("+txnId+") already exists.");
			}
		} catch(Exception ex){
			LoadGenerator.logCommon.log(Level.WARNING,"[TransactionMap] setTxnMap Exception: "+ex);
		}
	}

	/**
	 * Получение времени создания транзакции и удаление транзакции из списка
	 * @param txnId
	 * @return long
	 */
	public  long getTxnMap(long txnId){
		long time_creation = 0;
		try{
			if (txnMap.get(txnId)!=null){
				time_creation = txnMap.get(txnId);
				txnMap.remove(txnId);
			}
			return time_creation;
		} catch(Exception ex){
			ex.printStackTrace();
			LoadGenerator.logCommon.log(Level.WARNING,"[TransactionMap] getTxnMap Exception: "+ex);
		}
		return time_creation;
	}
}
