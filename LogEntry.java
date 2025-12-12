import java.time.LocalDate;

public class LogEntry {
	public enum EntryType {
		DESTINATION,
		FUEL,
		SERVICE,
		ODOMETER;
	}
	protected LocalDate eventDate;
	public final EntryType type;
	public LogEntry(EntryType type, LocalDate eventDate) { // If we dont ever use this make it protected.
		this.type = type;
		this.eventDate = eventDate;
	}
	public EntryType getType() {
		return this.type;
	}
	public LocalDate getDate() {
		return this.eventDate;
	}
	public void setDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}
}
