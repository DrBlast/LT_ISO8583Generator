package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration;

public abstract class Configurator {
	// Класс должен содержать список семантически необходимых параметров.

	/**
	 * Метод позволяет считать и заполнить все требуемые параметры
	 *
	 * @param - файл с параметрами
	 * @return <0 - один или более параметров не были найдены в файле
	 */
	abstract int loadProperties(String fileName);
}
