package com.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DistrictsDownloader {

	ObjectMapper mapper = new ObjectMapper();
	private Map<String, String> stateMap = new HashMap<String, String>();
	private Map<String, String> districtMap = new HashMap<String, String>();

	public void downloadData() {
		try {
			JsonNode statesNode = mapper.readTree(DistrictsDownloader.class.getResourceAsStream("/states.json"));
			Iterator<JsonNode> stateIt = statesNode.get("states").iterator();
			while (stateIt.hasNext()) {
				JsonNode stateNode = stateIt.next();

				stateMap.put(stateNode.get("state_id").asText(), stateNode.get("state_name").asText());
				this.getDistrictsForState(stateNode.get("state_id").asText());
			}
		} catch (IOException e) {
			System.out.println("Failed to get states. Exiting..");
			System.exit(0);
		}
	}

	private void getDistrictsForState(String stateId) {
		String call = "https://cdn-api.co-vin.in/api/v2/admin/location/districts/" + stateId;

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(call.toString());
		httpGet.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
		try {
			CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			JsonNode response = mapper.readTree(entity.getContent());
			EntityUtils.consume(entity);
			Iterator<JsonNode> districtIt = response.get("districts").iterator();
			while (districtIt.hasNext()) {
				JsonNode districtNode = districtIt.next();
				districtMap.put(districtNode.get("district_name").asText(), districtNode.get("district_id").asText());
			}
		} catch (Exception e) {
			System.out.println("Couldnt get districts data for " + stateId);
			e.printStackTrace();
		}
	}

	public Map<String, String> getDistrictMap() {
		return districtMap;
	}
}
