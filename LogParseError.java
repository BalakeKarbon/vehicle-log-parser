class LogParseError extends Exception {
	private String line; // This might change as there could be an inter-line error probably?
	public LogParseError(String message) {
		super(message);
	}
}
