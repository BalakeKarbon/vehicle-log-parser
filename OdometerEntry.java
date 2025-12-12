class OdometerEntry extends LogEntry {
	protected float miles;
	private static final EntryType TYPE = EntryType.ODOMETER;
	public OdometerEntry(LocalDate entryDate, float miles) {
		super(TYPE,entryDate);
	}
}
