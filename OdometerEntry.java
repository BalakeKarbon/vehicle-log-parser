// Derived odometer entry

import java.time.LocalDate;

public class OdometerEntry extends LogEntry {
	protected float miles; // Float for miles on odometer
	private static final EntryType TYPE = EntryType.ODOMETER; // Type for constructor
	public OdometerEntry(LocalDate entryDate, float miles) { // Constructor
		super(TYPE,entryDate); // Base constructor
		this.miles = miles; // Additional info
	}
	public float getMiles() { // getter
		return this.miles;
	}
}
