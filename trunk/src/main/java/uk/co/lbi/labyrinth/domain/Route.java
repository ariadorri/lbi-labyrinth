package uk.co.lbi.labyrinth.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.lbi.labyrinth.jaxb.Location;

public class Route {
	public enum LocationTypes {
		Start, Exit, Normal, PowerPill;
	}

	private List<String> path = new ArrayList<String>();
	private Set<String> powerPills = new HashSet<String>();
	private LocationTypes result;

	public void next(Location location) {
		this.getPath().add(location.getLocationId());
		this.result = LocationTypes.valueOf(location.getLocationType());
	}

	public String lastLocation() {
		if (getPath().isEmpty()) {
			return null;
		}
		return getPath().get(getPath().size() - 1);
	}

	public void continueTo(Route route) {
		path.addAll(route.getPath());
		powerPills.addAll(route.getPowerPills());
		result = route.result;
	}

	public void addPowerPills(String location) {
		powerPills.add(location);
	}

	public LocationTypes getResult() {
		return result;
	}

	public List<String> getPath() {
		return path;
	}

	public Set<String> getPowerPills() {
		return powerPills;
	}

	public int length() {
		return path.size();
	}

	public int numberOfPowerPills() {
		return powerPills.size();
	}

	public boolean hasPowerPill(String location) {
		return powerPills.contains(location);
	}

}
