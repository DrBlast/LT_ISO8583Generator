/**
 * Клиент сервера по протоколу ISO8583
 * @version 1.0 15.03.2011 17:11
 * @author kvaga
 */
package ru.kvaga.amphora.svfp.iso8583.lt.client;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

//import org.jpos.iso.packager.GenericPackager;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.ISO87PackagerExt;
import ru.kvaga.amphora.svfp.loadGenerator.log.LogInit;
//import org.jpos.iso.channel.RawChannel;
import com.sybase365.iso8583.channel.RawChannelExt;

public class ISO8583Client {
	//private RawChannel channel = null;
	private RawChannelExt    channel  = null;
	private ISO87PackagerExt packager = null;
	//private GenericPackager packager = null;
	private int connectionTimeOut;
	private String hostName   = null;
	private int    portNumber = 0;
	private Logger logCommon  = LogInit.getLogger(LogInit.LOGGER_NAME_COMMON);

	/**
	 * Конструктор клиента ISO8583
	 *
	 * @param hostName          - хост сервера
	 * @param portNumber        - порт сервера
	 * @param connectionTimeOut - таймаут коннекта в миллисекундах
	 * @throws IOException - exception при неудачной попытке коннекта к серверу
	 */
	public ISO8583Client(String hostName, int portNumber, int connectionTimeOut) {

		packager = new ISO87PackagerExt();
		channel = new RawChannelExt(hostName, portNumber, packager, null);
		this.connectionTimeOut = connectionTimeOut;
		this.hostName = hostName;
		this.portNumber = portNumber;
	}

	/**
	 * Уставление коннекта
	 *
	 * @param
	 * @return 0 - успех<br>
	 *         -1 - ошибка во время установления коннекта<br>
	 *         1 - коннект уже установлен
	 *         -2 - Объект ISO8583Client'а предвариетльно не создавался
	 */
	public synchronized int connect() {
		if (channel == null) {
			if (packager == null)
				packager = new ISO87PackagerExt();
			if (hostName == null)
				return -2;
			channel = new RawChannelExt(hostName, portNumber, packager, null);
		}
		if (channel.isConnected()) {

			return 1;
		}
		try {
			channel.connect();

		} catch (IOException e) {
			logCommon.log(Level.WARNING, "[ISOClient][Connect] Exception: " + e.getMessage());
			return -1;
		}
		return 0;
	}

	/**
	 * Метод отправки единичного сообщения на сервер
	 *
	 * @param msg - сообщение
	 * @return 0 - успех<br>
	 *         -1 - Не установлен packager для упаковки сообщений. Это необходимо делать в конструкторе<br>
	 *         -2 - Ошибка сокета<br>
	 *         -3 - Ошибка ввода\вывода<br>
	 *         -4 - Ошибка во время подготовки к отправке сообщения<br>
	 *         -5 - Невозможно отправить сообщение из-за отсутствия коннекта<br>
	 *         -6 - Ошибка при вычислении BitMap
	 */
	public synchronized int sendMsg(ISOMsg msg) {
		if (packager == null)
			return -1;

		msg.setPackager(packager);
		try {
			msg.recalcBitMap();
			if (channel.isConnected()) {
				try {
					channel.setTimeout(connectionTimeOut);
					channel.send(msg);
				} catch (SocketException e) {
					logCommon.log(Level.WARNING, "[ISOClient][sendMsg] SocketException: " + e.getMessage());
					return -2;
				} catch (IOException e) {
					logCommon.log(Level.WARNING, "[ISOClient][sendMsg] IOException: " + e.getMessage());
					return -3;
				} catch (ISOException e) {
					logCommon.log(Level.WARNING, "[ISOClient][sendMsg] ISOException: " + e.getMessage());
					e.printStackTrace();

					return -4;
				}
			} else {
				return -5;
			}
		} catch (ISOException e) {
			logCommon.log(Level.WARNING, "[ISOClient][sendMsg] ISOException: " + e.getMessage());
			return -6;
		}
		return 0;
	}

	/**
	 * Метод для получения сообщений из канала
	 *
	 * @return ISOMsg - Возвращает сообщение типа ISOMsg.
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws ISOException
	 */
	public ISOMsg receive() throws ISOException, NullPointerException, IOException {
		// TODO: Решить вопрос с инхронизацией по методу и синхронизацией по объектам.
		// Влияние синхронизации по методу на синхронизацию по объектам.
		ISOMsg responseMsg = null;
		if (!channel.isConnected()) throw new SocketException("No connection.");
		try {
			responseMsg = channel.receive();
		} catch (IOException e) {
			throw new IOException("Error during get message. Connection lost.");
		}
		if (responseMsg == null)
			throw new NullPointerException("Null value received from channel.");
		responseMsg.setPackager(packager);
		return responseMsg;
	}

	/**
	 * Метод для отправки сообщения и обязательного получения ответа на него.
	 * Используется для приема\отправки 1804\1814
	 *
	 * @param msg - отправляемое ISO сообщение
	 * @return ISOMsg - ISO8583 сообщение
	 * @throws ISOException - ошибка при работе с ISO сообщением
	 * @throws IOException  - ошибка при работе с каналом.
	 */
	public synchronized ISOMsg sendAndReceiveMsg(ISOMsg msg) throws ISOException, IOException {
		msg.setPackager(packager);

		ISOMsg responseMsg = null;
		try {
			msg.recalcBitMap();
			if (channel.isConnected()) {
				channel.setTimeout(connectionTimeOut);
				channel.send(msg);
				responseMsg = channel.receive();
				if (responseMsg == null)
					throw new ISOException("Received NULL ISOMsg from SMP.");
				responseMsg.setPackager(packager);
			}
		} catch (ISOException e) {
			throw new ISOException("Error during work with ISO8583 message. INFO: "
					                       + e.getMessage());
		} catch (SocketException e) {
			throw new SocketException("INFO: " + e.getMessage());
		} catch (IOException e) {
			throw new IOException("INFO: " + e.getMessage());
		}
		return responseMsg;
	}


	/**
	 * Закрытие коннекта
	 *
	 * @return 1 Канал уже закрыт<br>
	 *         0 Успешное уничтожение канала ISO<br>
	 *         -1 Ошибка ввода\вывода при уничтожении канала ISO<br>
	 */
	public synchronized int disconnect() {
		if (channel.isConnected()) {
			try {
				channel.disconnect();
			} catch (IOException e) {
				logCommon.log(Level.WARNING, "[ISOClient][disconnect] IOException: " + e.getMessage());
				return -1;
			}
		} else {
			return 1;
		}
		return 0;
	}


	/**
	 * Метод проверки коннекта клиента ISO к серверу
	 *
	 * @return true - коннект есть<br>
	 *         false - коннекта нет
	 */
	public boolean isConnected() {
		return channel.isConnected();
	}
}
