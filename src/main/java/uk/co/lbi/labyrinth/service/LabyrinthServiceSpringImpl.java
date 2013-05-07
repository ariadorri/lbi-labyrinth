package uk.co.lbi.labyrinth.service;

import java.util.HashMap;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import uk.co.lbi.labyrinth.jaxb.Location;

public class LabyrinthServiceSpringImpl implements LabyrinthService {

	private static Log log = LogFactory.getLog(LabyrinthServiceSpringImpl.class);

	private Map<String, Location> simpleCache = new HashMap<String, Location>();

	private String endpoint;

	private RestTemplate restTemplate = new RestTemplate();

	public LabyrinthServiceSpringImpl(String endpoint) {
		this.endpoint = endpoint;
	}

	public Location checkPath(String location, boolean start) {
		if (simpleCache.containsKey(location)) {
			log.debug("RETURNING CACHED VALUE");
			return simpleCache.get(location);
		}
		HttpMethod method = HttpMethod.POST;
		if (start) {
			method = HttpMethod.GET;
		}
		try {
			ResponseEntity<Location> response = restTemplate.exchange(endpoint + location, method,
					null, Location.class);
			Location locationResult = response.getBody();
			log.debug(locationResult.toString());
			simpleCache.put(location, locationResult);
			return locationResult;
		} catch (Exception e) {
			return null;
		}
	}

}
