// A derived service entry type

import java.util.HashSet;
import java.time.LocalDate;

public class ServiceEntry extends LogEntry {
	protected String description; // A description of the service
	public enum ServiceType { // Categories of service that can be performed
		OIL,
		COOLANT,
		GREASE,
		REPAIR;
	}
	protected HashSet<ServiceType> services; // A set to store any categories included in this service
	private static final EntryType TYPE = EntryType.SERVICE; // Type for use in our constructor
	public ServiceEntry(LocalDate entryDate, HashSet<ServiceType> services,String description) { // Constructor
		super(TYPE, entryDate); // Base constructor
		this.services = services; // Additional information
		this.description = description;
	}
	public HashSet<ServiceType> getServices() { // Getters
		return this.services;
	}
	public String getDescription() {
		return this.description;
	}
}
