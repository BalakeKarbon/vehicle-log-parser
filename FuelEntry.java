import java.time.LocalDate;

class FuelEntry extends LogEntry {
	protected float cost, volume;
	private static final EntryType TYPE = EntryType.FUEL;
	protected LogDestination destination;
	public FuelEntry(LocalDate entryDate, float cost, float volume) {
		super(TYPE,entryDate);
		this.cost = cost;
		this.volume = volume;
	}
}
