package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration;

public class DefaultConfigurator extends Configurator{
	public static final String DEFAULT_FILE_NAME_FOR_LOAD_PROPERTIES = "";
	public static String PARAM_DEFAULT_DATA_POOL_PATH_VC;
	public static String PARAM_DEFAULT_DATA_POOL_PATH_PC;

	/**
	 * Загрузка параметров из файлов
 	 * @param fileName
	 * @return 0
	 */
	public int loadProperties(String fileName) {
		PARAM_DEFAULT_DATA_POOL_PATH_VC = "dataPool/VCCustomerPool.txt";
		PARAM_DEFAULT_DATA_POOL_PATH_PC = "dataPool/PCCustomerPool.txt";
		return 0;
	}

	/**
	 * Получение Конфигурации по умолчанию
	 * @return DefaultConfigurator
	 */
	public static DefaultConfigurator getInstance(){
		if(configurator == null) configurator = new DefaultConfigurator();
		return configurator;
	}
	private DefaultConfigurator(){}
	private static DefaultConfigurator configurator = null;
}
