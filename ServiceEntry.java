import java.util.HashSet;
import java.time.LocalDate;

public class ServiceEntry extends LogEntry {
	protected String description;
	public enum ServiceType {
		OIL,
		COOLANT,
		GREASE,
		REPAIR;
	}
	protected HashSet<ServiceType> services;
	private static final EntryType TYPE = EntryType.SERVICE;
	public ServiceEntry(LocalDate entryDate, HashSet<ServiceType> services,String description) {
		super(TYPE, entryDate);
		this.services = services;
		this.description = description;
	}
	public HashSet<ServiceType> getServices() {
		return this.services;
	}
	public String getDescription() {
		return this.description;
	}
}
