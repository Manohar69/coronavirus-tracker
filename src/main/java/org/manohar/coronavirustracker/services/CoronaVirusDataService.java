package org.manohar.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.manohar.coronavirustracker.models.LocationStats;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CoronaVirusDataService {

	private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	private RestTemplate restTemplate = new RestTemplate();
	private List<LocationStats> allStats = new ArrayList();

	public List<LocationStats> getAllStats() {
		return allStats;
	}

	@PostConstruct
	@Scheduled(cron = "* * 1 * * *")
	public void fetchVirusData() throws IOException, InterruptedException {
		List<LocationStats> newStats = new ArrayList();

		ResponseEntity<String> response = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			HttpEntity<String> request = new HttpEntity<>(headers);
			response = restTemplate.exchange(VIRUS_DATA_URL, HttpMethod.GET, request, String.class);
		} catch (Exception ex) {
			System.out.println("Error while fetching data from URL :-" + ex.getMessage());
		}

		StringReader csvBodyReader = new StringReader(response.getBody());
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
		for (CSVRecord record : records) {
			LocationStats locationStat = new LocationStats();
			locationStat.setState(record.get("Province/State"));
			locationStat.setCountry(record.get("Country/Region"));
			int latestCases = Integer.parseInt(record.get(record.size() - 1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			locationStat.setLatestTotalCases(latestCases);
			locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
			newStats.add(locationStat);
		}
		this.allStats = newStats;
	}
	/*
	 * private String processHTTPUrl(String url) throws IOException { StringBuilder
	 * sb = new StringBuilder(); String response = ""; URL uri = new URL(url);
	 * 
	 * HttpsURLConnection connection = null; BufferedReader br = null;
	 * connection.setRequestMethod("GET"); connection.setRequestProperty("Accept",
	 * "application/xml,text/xml,application/xhtml+xml");
	 * connection.setRequestProperty("Content-Type", "application/xml");
	 * connection.setUseCaches(false); connection.setDoInput(true);
	 * connection.setDoOutput(true);
	 * 
	 * if ((connection).getResponseCode() != 200) {
	 * logger.logDebug("connection failed in rss feed:" +
	 * connection.getResponseCode()); } br = new BufferedReader(new
	 * InputStreamReader(connection.getInputStream()));
	 * 
	 * while ((response = br.readLine()) != null) { sb.append(response); }
	 * br.close(); if (StringUtils.isNotEmpty(br.toString())) { br.close(); } if
	 * (StringUtils.isNotEmpty(connection.toString())) { connection.disconnect(); }
	 * return sb.toString(); }
	 */

}
