// A derived Fuel entry object

import java.time.LocalDate;

public class FuelEntry extends LogEntry {
	protected float cost, volume; // Floats for cost and volume of fuel
	private static final EntryType TYPE = EntryType.FUEL; // A type for use in our constructor
	protected LogDestination destination; // A destination where we got fuel
	public FuelEntry(LocalDate entryDate, LogDestination destination, float cost, float volume) { // Constructor
		super(TYPE,entryDate); // base constructor
		this.cost = cost; // Additional information
		this.volume = volume;
		this.destination = destination;
	}
	public float getCost() { // Getters
		return this.cost;
	}
	public float getVolume() {
		return this.volume;
	}
	public LogDestination getDestination() {
		return this.destination;
	}
}
