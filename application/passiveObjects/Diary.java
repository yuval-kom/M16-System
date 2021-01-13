package bgu.spl.mics.application.passiveObjects;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.*;

import java.util.Map;
import java.util.concurrent.atomic.*;

/**
 * Passive object representing the diary where all reports are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Diary {

	private List<Report> reports = new LinkedList<>(); //only executed missions
	private AtomicInteger total = new AtomicInteger(); //total number of received missions (executed & aborted)
	private static Diary instance = new Diary();

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Diary getInstance() {
		return instance;
	}

	public List<Report> getReports() {
		return reports;
	}

	/**
	 * adds a report to the diary
	 * @param reportToAdd - the report to add
	 */
	public synchronized void addReport(Report reportToAdd){
		reports.add(reportToAdd);
	}

	/**
	 *
	 * <p>
	 * Prints to a file name @filename a serialized object List<Report> which is a
	 * List of all the reports in the diary.
	 * This method is called by the main method in order to generate the output.
	 */
	public void printToFile(String filename) {
		Map<String,Object> toString = new HashMap<>(1);
		toString.put("reports",getReports());
		toString.put("total",getTotal());
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String theJsonDiary = gson.toJson(toString);
		try(Writer writer = new FileWriter(filename)) {
			{
				writer.write(theJsonDiary);
			}
		}
		catch (Exception e){}
	}

	/**
	 * Gets the total number of received missions (executed / aborted) be all the M-instances.
	 * @return the total number of received missions (executed / aborted) be all the M-instances.
	 */
	public int getTotal(){
		return total.get();
	}

	public void increment(){
		int localTotal = getTotal();
		while (!total.compareAndSet(localTotal, localTotal+1)){
			localTotal = getTotal();
		}
	}
}