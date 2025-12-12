import java.util.HashSet;

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
	public ServiceEntry(localDate entryDate) {
		super(TYPE, entryDate);
	}
}
