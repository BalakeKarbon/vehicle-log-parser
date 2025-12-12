import java.util.HashSet;
import java.time.LocalDate;

class ServiceEntry extends LogEntry {
	protected String description;
	protected enum ServiceType {
		OILCHANGE,
		COOLANTCHANGE,
		GREASE,
		REPAIR;
	}
	protected HashSet<ServiceType> services;
	private static final EntryType TYPE = EntryType.SERVICE;
	public ServiceEntry(LocalDate entryDate) {
		super(TYPE, entryDate);
	}
}
