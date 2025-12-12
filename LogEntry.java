import java.time.LocalDate;

public class LogEntry {
	public enum EntryType {
		DESTINATION,
		FUEL,
		SERVICE,
		ODOMETER;
	}
	//public static final EntryType TypePriority[] = {EntryType.DESTINATION,EntryType.FUEL,EntryType.SERVICE,EntryType.ODOMETER};
	protected LocalDate entryDate;
	public final EntryType type;
	public LogEntry(EntryType type, LocalDate entryDate) { // If we dont ever use this make it protected.
		this.type = type;
		this.entryDate = entryDate;
	}
	public EntryType getType() {
		return this.type;
	}
	public LocalDate getDate() {
		return this.entryDate;
	}
	public void setDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}
}
