package ru.kvaga.amphora.svfp.loadGenerator.log;

import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 25.12.13
 * Time: 11:22
 */
public class TestRules {

	@Test
	public void rulesTest() throws IOException {
		File fileName = new File("D:\\Amphora\\rules_list.txt");
		String line = "";
		InputStream fis = new FileInputStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		// Deal with the line
		HashMap<Integer, Integer> rules = new HashMap<Integer, Integer>();
		while ((line = br.readLine()) != null) {
			if (!line.contains("NULL")) {
				String[] rulesInLine = line.split("\t")[1].split(",");
				for (String rule : rulesInLine) {
					int iRule = Integer.parseInt(rule);
					if (rules.containsKey(iRule)) {
						int value = rules.get(iRule);
						value++;
						rules.put(iRule, value);
					} else
						rules.put(iRule, 1);
				}
			}
		}

		for (Map.Entry<Integer,Integer> entry : rules.entrySet()){
			System.out.println(entry.getKey() +"  "+ entry.getValue());
		}
	}
}
