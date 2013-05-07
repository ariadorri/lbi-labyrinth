package uk.co.lbi.labyrinth.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Location")
public class Location {

	@XmlElementWrapper(name = "Exits")
	@XmlElement(name = "string")
	private List<String> exits;

	@XmlElement(name = "LocationId")
	private String locationId;

	@XmlElement(name = "LocationType")
	private String locationType;

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public String getLocationType() {
		return locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	public List<String> getExits() {
		return exits;
	}

	public void setExits(List<String> exits) {
		this.exits = exits;
	}

	@Override
	public String toString() {
		return "Location [exits=" + exits + ", locationId=" + locationId + ", locationType="
				+ locationType + "]";
	}

}
