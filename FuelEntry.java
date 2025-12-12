import java.time.LocalDate;

public class FuelEntry extends LogEntry {
	protected float cost, volume;
	private static final EntryType TYPE = EntryType.FUEL;
	protected LogDestination destination;
	public FuelEntry(LocalDate entryDate, LogDestination destination, float cost, float volume) {
		super(TYPE,entryDate);
		this.cost = cost;
		this.volume = volume;
		this.destination = destination;
	}
}
