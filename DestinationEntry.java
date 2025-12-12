import java.time.LocalDate;

public class DestinationEntry extends LogEntry {
	private static final EntryType TYPE = EntryType.DESTINATION;
	protected LogDestination destination;
	public DestinationEntry(LocalDate entryDate, LogDestination destination) {
		super(TYPE,entryDate);
		this.destination = destination;
	}
	public LogDestination getDestination() {
		return this.destination;
	}
}
