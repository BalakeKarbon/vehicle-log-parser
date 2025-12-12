class DestinationEntry extends LogEntry {
	protected double latitude, longitude;
	protected String title, description;
	private static final EntryType TYPE = EntryType.DESTINATION;
	public DestinationEntry(LocalDate entryDate) {
		super(TYPE,entryDate);
	}
}
