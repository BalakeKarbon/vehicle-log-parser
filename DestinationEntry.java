import java.time.LocalDate;

class DestinationEntry extends LogEntry {
	private static final EntryType TYPE = EntryType.DESTINATION;
	protected LogDestination destination;
	public DestinationEntry(LocalDate entryDate) {
		super(TYPE,entryDate);
	}
}
