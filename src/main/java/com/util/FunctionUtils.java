package com.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.app.VaccineAvailibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pojo.Centre;
import com.pojo.Session;

public class FunctionUtils {

	ObjectMapper mapper = new ObjectMapper();

	public FunctionUtils() {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	public Map<String, List<Centre>> callAPIToGetResponse(String parameter, String value, String dateString)
			throws JsonProcessingException, UnsupportedOperationException, IOException {
		Map<String, List<Centre>> response = null;
		String call = "https://cowin.gov.in/api/v2/appointment/sessions/public/" + parameter + value
				+ "&date=" + dateString;

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(call.toString());
		CloseableHttpResponse httpResponse = httpclient.execute(httpGet);

		try {
			HttpEntity entity = httpResponse.getEntity();
			TypeReference<HashMap<String, List<Centre>>> typeRef = new TypeReference<HashMap<String, List<Centre>>>() {
			};
			response = mapper.readValue(entity.getContent(), typeRef);
			EntityUtils.consume(entity);
		} catch (Exception e) {
			System.out.println(httpResponse.getStatusLine());
			System.out.println(EntityUtils.toString(httpResponse.getEntity()));
		} finally {
			httpResponse.close();
		}
		return response;
	}
	
	public boolean getAvailability(Map<String, List<Centre>> response) {
		int age = 18;
		boolean found = false;
		List<Centre> centres = response.get("centers");
		Set<String> elgibleCentres = new HashSet<String>();
		Set<String> availableCentres = new HashSet<String>();
		for (Centre centre : centres) {
			List<Session> sessions = centre.getSessions();
			for (Session session : sessions) {
				int minAge = session.getMin_age_limit();
				if (minAge == age) { // 18+ adults
					elgibleCentres.add(centre.getName());
					int availability = session.getAvailable_capacity();
					if (availability > 0) {
						availableCentres.add(centre.getName());
						System.out.println("Slot found!");
						System.out.println(availability + " slots avaiable at " + centre.getName() + " on date "
								+ session.getDate());
						System.out.println();
						found = true;
					}
				}
			}
		}
		System.out.println(centres.size() + " total centres found.");
		System.out.println(elgibleCentres.size() + " centers are allowing for 18+ adults");
		System.out.println(availableCentres.size() + " centers have slots available");

		if (!found)
			System.out.print(" :(");
		return found;
	}
	
	public void playTheme() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		AudioInputStream audioInputStream = AudioSystem
				.getAudioInputStream(VaccineAvailibility.class.getResource("/Theme_Undertaker_Entry.wav"));
		Clip clip = AudioSystem.getClip();
		clip.open(audioInputStream);
		clip.start();
	}
}
