package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator;

import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.TempData.CountryEnum;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.configuration.CountryConfigurator;
import ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.profile.CountryGenerator;

import java.util.*;

import static ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util.FilePrinter.testFileWriteSpeed;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 02.12.13
 * Time: 14:23
 * Тестовый класс проверки функций
 */

public class TestClass {

	public static void main(String[] args) {
		try {
		/*CountryConfigurator cConf = CountryConfigurator.getInstance();
		cConf.loadProperties("conf/countryConfiguration.cfg");
		CountryGenerator cGen = CountryGenerator.getInstance();
		CountryEnum current = CountryEnum.BG;
		for (int i = 0; i < 100; i++)
			System.out.println(cGen.getNextCountryProfile());
         */
			//	testFileWriteSpeed();

			//sleep method with high CPU usage
			long starttime = System.nanoTime();
			long reqTPS = 100;
			double sleepTime= 1000/reqTPS;
			long goalTime=starttime+(long)(sleepTime*1000000);
			long currentSleepTime=starttime;
			long sleepTimerCount = 0;
			while(currentSleepTime<goalTime){
				HashMap<Integer,Integer> hash = new HashMap();
				hash.put(1, 10);
				hash.remove(1);
				hash.clear();
				sleepTimerCount++;
				currentSleepTime=System.nanoTime();
			}
			long endtime = System.nanoTime();
			System.out.println("Improved sleep Time("+sleepTime+") real sleepTime="+(endtime-starttime)+" nanos; sleep initiated "+ sleepTimerCount+ " times(CPU ticks).");

			//try new sleep method
			starttime = System.nanoTime();
			Queue<Long> q = new LinkedList<Long>();
			for (int i = 0; i < 100; i++) {
				long currentTime = System.nanoTime();
				q.add(currentTime  - starttime);
				starttime = currentTime;
			}
			for (int i = 0; i < 100; i++) {
				System.out.println(q.remove()+" nanos");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
