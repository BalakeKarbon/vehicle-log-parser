/*
Just some notes regarding this program. With the variablility in spelling and all sorts of other things I think the application of a simple feed forward neural network might be better and easier. That is not used here due to program requirements.
*/

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
//import java.io.PrintWriter;
import java.util.Collections;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class VehicleLogParser {
	// Implement some sort of custom exception
	//public static void parseLog(String path) throws LogParseError {
	//	// here
	//}
	//public static void loadKey(String path) { // Custom Error? Load or parse? Idk

	//}
	// Perhaps a mode to create a key file by asking the user line by line what something means?
	// Allow user intervention when a line cannot be parsed! In such case add to key file perhaps?
	// Do we support complex patterns???? wb regex? (yes)
	public static Map<String, LogEntry.EntryType> typeMap;
	public static Map<String, LogDestination> locationMap;
	public static List<LogEntry> VehicleLog = new ArrayList<>();
	public static boolean validateKeyFile(String path) {
		try (Stream<String> stream = Files.lines(Paths.get(path))) {
			List<String> lines = stream.collect(Collectors.toList());
			String words[];
			typeMap = new HashMap<>();
			locationMap = new HashMap<>();
			if(lines.size() >= 4) {
				for(int lineIndex = 0;lineIndex<lines.size();lineIndex++) {
					words = lines.get(lineIndex).split(",");
					switch(lineIndex) {
						case 0:
							// Destination
							for(String word : words) {
								typeMap.put(word, LogEntry.EntryType.DESTINATION);
							}
							break;
						case 1:
							// Fuel
							for(String word : words) {
								typeMap.put(word, LogEntry.EntryType.FUEL);
							}
							break;
						case 2:
							// Service
							for(String word : words) {
								typeMap.put(word, LogEntry.EntryType.SERVICE);
							}
							break;
						case 3:
							// Odometer
							for(String word : words) {
								typeMap.put(word, LogEntry.EntryType.ODOMETER);
							}
							break;
						default:
							if(words.length >= 2) {
								for(int wordIndex = 0;wordIndex<words.length;wordIndex++) {
									String title = null;
									String description = null;
									List<String> keys = new ArrayList<>();
									switch(wordIndex) {
										case 0:
											title = words[wordIndex];
											break;
										case 1:
											description = words[wordIndex];
											break;
										default:
											keys.add(words[wordIndex]);
											break;
									}
									LogDestination newDestination = new LogDestination(title,description);
									for(String key : keys) {
										locationMap.put(key, newDestination);
									}
								}
							} else {
								// Error not enough descriptors for location in csv!
								System.err.println("Error parsing location descriptor for keyfile: " + lines.get(lineIndex));
								return false;
							}
							break;
					}
				}
			} else {
				// Error CSV not enough lines
				System.err.println("Error csv file not enough information or wrong structure.");
				return false;
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
			return false;
		}
		return true;
	}
	public static boolean parseLog(String path) {
		Pattern datePattern = Pattern.compile("^(\\d{1,2}[./-]\\d{1,2}[./-](?:\\d{4})).*");
		Matcher m;
		String dateParts[];
		int month,day,year;
		Map<LogEntry.EntryType, Integer> scores = new HashMap<>();
		LocalDate currentDate = null;
		boolean lineParsed;
		try (Stream<String> stream = Files.lines(Paths.get(path))) {
			for(String line : (Iterable<String>) stream::iterator) {
				//String data[] = line.split(',');
				//String 
				m = datePattern.matcher(line);
				if(m.find()) {
					dateParts = m.group(1).split("-");
					try {
						month = Integer.parseInt(dateParts[0]);
						day = Integer.parseInt(dateParts[1]);
						year = Integer.parseInt(dateParts[2]);
						currentDate = LocalDate.of(year, month, day);
						// We have decided to go back and fix dates later.
						System.out.println("Parsing "+currentDate+"...");

					} catch (NumberFormatException e) {
						System.out.println("Cannot parse date for: " + line);
						// Now what? we enter date or continue i suppose.
					}
					//if(currentDate != null) {
					//	if(currentDate > LocalDate.of(year,month,day)) {
					//		// Here we have the next day being before the previous day so time travel
					//	} else if() {

					//	} else {
					//		currentDate = LocalDate.of(year, month, day);
					//	}
					//} else {
					//	currentDate = LocalDate.of(year, month, day);
					//}
				} else {
					if(currentDate != null) {
						for(LogEntry.EntryType type : LogEntry.EntryType.values()) { // Advanced loop through an enum wow
							if(type == LogEntry.EntryType.DESTINATION) {
								scores.put(type, 1); // Destination gets a bias as we want that to be default
							} else {
								scores.put(type, 0); // Zero out all of our types. Oo automoxing
							}
						}
						lineParsed = false;
						// I use 4 integers for the scores instead of a HashMap because it makes me sleep better. I know we have all the ram in the world but still it tastes bad to allocate HashMap space for 4 integers just to make key-value ease.
						//Actually I think I change my mind.
						// Go throuhg each word in the line and if it has a majority character match with anything in our key file then tally it for that category, majority catigory wins.
						for(String word : line.split(" ")) {
							if(typeMap.containsKey(word)) {
								scores.merge(typeMap.get(word), 1, Integer::sum);
							}
						}
						//for(LogEntry.EntryType type : LogEntry.EntryType.values()) {
						//	System.out.println(scores.get(type));
						//}
						int maxValue = Collections.max(scores.values());
						List<LogEntry.EntryType> bestKeys = scores.entrySet().stream()
							.filter(e -> e.getValue() == maxValue)
							.map(Map.Entry::getKey)
							.collect(Collectors.toList());
						System.out.println(bestKeys); // All keys with the highest value
						if(bestKeys.size() > 1) {
							//Tie! Remove loosers!
						}

						if(lineParsed == false) {
							System.err.println("Unable to parse line: " + line);
						}
					}
				}
				
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
			return false;
		}
		return true;
	}
	public static void main(String args[]) {
		String logFilePath, keyFilePath, outputFilePath;
		boolean keyValidated = false;
		boolean logParsed = false;
		Scanner scnr = new Scanner(System.in);
		if(args.length >= 1) {
			keyFilePath = args[0];
			keyValidated = validateKeyFile(keyFilePath);
		}
		while(!keyValidated) {
			System.out.print("Enter Key File Path: ");
			keyFilePath = scnr.nextLine();
			keyValidated = validateKeyFile(keyFilePath);
		}
		if(args.length >= 2) {
			logFilePath = args[1];
			logParsed = parseLog(logFilePath);
		}
		if(args.length >= 3) {
			outputFilePath = args[2];
		}
		while(!logParsed) {
			System.out.print("Enter Log File Path: ");
			logFilePath = scnr.nextLine();
			logParsed = parseLog(logFilePath);
		}
		// Here we need to first get our key loaded properly, then we can attempt to parse the log file. If this succeeds we can store it in some fashion.
		// What sort of structure do we want for our key file? CSV?
	}
}
