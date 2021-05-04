package com.app;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pojo.Centre;
import com.util.FunctionUtils;

public class VaccineAvailibility {

	public static void main(String[] args) throws JsonProcessingException, UnsupportedOperationException, IOException,
			InterruptedException, UnsupportedAudioFileException, LineUnavailableException {
		System.out.println("Application Started");
		
		System.out.println("Please select by which method you need to search slot.");
		System.out.println("Press 1 for District wise slot search, Press 2 for Pincode wise slot search and press Enter:");
		
		Scanner input = new Scanner(System.in);
		String selection = input.nextLine();
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
		DateTime today = DateTime.now();
		int weeksToCheck = 1;
		
		if ("1".equals(selection)) {
			System.out.println("Enter the exact name of your district as you see in Cowin/Aarogya Setu application and press Enter:");
			String districtName = input.nextLine();
			input.close();
			
			System.out.println("District Name Checking:");
			DistrictsDownloader data = new DistrictsDownloader();
			data.downloadData();
			Map<String, String> districts = data.getDistrictMap();
			
			if (districts.containsKey(districtName)) {
				System.out.println("District found! Lets go..");
				Thread.sleep(1000);
			} else {
				System.out.println("District not found. Exiting ..");
				System.exit(0);
			}
			String districtId = districts.get(districtName);

			while (true) {
				for (int i = 0; i < weeksToCheck; i++) {
					DateTime dateToPass = today.plusDays(7 * i);
					System.out.println("Checking week of " + dateToPass.toString(formatter) + ":");
					FunctionUtils checker = new FunctionUtils();
					Map<String, List<Centre>> response = checker.callAPIToGetResponse("calendarByDistrict?district_id=", districtId, dateToPass.toString(formatter));
					boolean found = checker.getAvailability(response);
					
					if (found) {
						System.out.println("Woohoo!");
						checker.playTheme();
						break;
					}
				}
				System.out.println("checking again in 30 seconds..");
				Thread.sleep(1000 * 30); // check every 30 seconds
			}
		}
		else if("2".equals(selection)) {
			System.out.println("Enter pincode and press Enter:");
			String pinCode = input.nextLine();
			input.close();
			
			while (true) {
				for (int i = 0; i < weeksToCheck; i++) {
					DateTime dateToPass = today.plusDays(7 * i);
					System.out.println("Checking week of " + dateToPass.toString(formatter) + ":");
					FunctionUtils checker = new FunctionUtils();
					Map<String, List<Centre>> response = checker.callAPIToGetResponse("calendarByPin?pincode=", pinCode, dateToPass.toString(formatter));
					boolean found = checker.getAvailability(response);
					
					if (found) {
						System.out.println("Woohoo!");
						checker.playTheme();
						break;
					}
				}
				System.out.println("checking again in 30 seconds..");
				Thread.sleep(1000 * 30); // check every 30 seconds
			}
		}
		else {
			System.out.println("Wrong selection. Exiting...");
			input.close();
			System.exit(0);
		}
	}
}
