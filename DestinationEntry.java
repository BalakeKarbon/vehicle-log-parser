// Our derived DestinationEntry object

import java.time.LocalDate;

public class DestinationEntry extends LogEntry {
	private static final EntryType TYPE = EntryType.DESTINATION; // Our type set here for use in constructor
	protected LogDestination destination; // Our destination type
	public DestinationEntry(LocalDate entryDate, LogDestination destination) { // Constructor
		super(TYPE,entryDate); // Base constructor
		this.destination = destination; // additional information for this type
	}
	public LogDestination getDestination() { // Getter
		return this.destination;
	}
}
