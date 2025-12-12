// Our base LogEntry object.

import java.time.LocalDate;

public class LogEntry {
	public enum EntryType { // A enumerator for its type we can store within the object
		DESTINATION,
		FUEL,
		SERVICE,
		ODOMETER;
	}
	protected LocalDate entryDate; // Every entry has a date
	public final EntryType type; // Our type that is only set on construction
	public LogEntry(EntryType type, LocalDate entryDate) { // Constructor
		this.type = type;
		this.entryDate = entryDate;
	}
	public EntryType getType() { // Getters
		return this.type;
	}
	public LocalDate getDate() {
		return this.entryDate;
	}
	public void setDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}
}
