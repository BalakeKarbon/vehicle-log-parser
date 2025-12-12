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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
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
	public static List<LogEntry> vehicleLog = new ArrayList<>();
	/*
	Keyfile Notess:
	So the top 4 lines go:
	destination words
	fuel words
	service words
	odometer words
	
	then the following lines are location word pairs and such. These are prioritiezed from top to bottom to my understanding with .values() from the hash map but I am not entirely sure, testing required.
	*/
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
								String title = null;
								String description = null;
								List<String> keys = new ArrayList<>();
								for(int wordIndex = 0;wordIndex<words.length;wordIndex++) {
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
										//System.out.println(key+": "+title);
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
	//public static logEntry parseLine(LogEntry.EntryType type, String line) throws CustomException? {

	//}
	public static <T extends LogEntry> String entryToCSV(T entry) {
		String csvString = entry.getDate().toString()+","+entry.getType().toString();
		if(entry instanceof DestinationEntry d) {
			csvString+=","+d.getDestination().title+","+d.getDestination().description;
		} else if (entry instanceof FuelEntry f) {
			csvString+=","+f.getDestination().title+","+f.getDestination().description+","+Float.toString(f.getCost())+","+Float.toString(f.getVolume());
		} else if (entry instanceof ServiceEntry s) {
			csvString+=",";
			for(ServiceEntry.ServiceType type : s.getServices()) {
				csvString+=type.toString()+",";
			}
			csvString+=s.getDescription();
		} else if (entry instanceof OdometerEntry o) {
			csvString+=","+Float.toString(o.getMiles());
		}
		return csvString;
	}
	public static boolean writeToCSV(String path) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
			for(LogEntry entry : vehicleLog) {
				writer.println(entryToCSV(entry));
			}
		return true;

		} catch (IOException e) {
			System.err.println("Error writing CSV: " + e);
			return false;
		}
	}
	public static boolean parseLog(String path) {
		Pattern datePattern = Pattern.compile("^(\\d{1,2}[./-]\\d{1,2}[./-](?:\\d{4})).*");
		Pattern fuelPattern = Pattern.compile("(\\$\\d+(?:\\.\\d+)?)|(\\d+(?:\\.\\d+)?\\s*gal)");
		Pattern odometerPattern = Pattern.compile("\\b\\d{4,}(\\.\\d+)?\\b");
		Matcher m;
		String dateParts[];
		int month,day,year;
		Map<LogEntry.EntryType, Integer> typeScores = new HashMap<>();
		Map<LogDestination, Integer> destinationScores = new HashMap<>();
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
						//System.out.println("Parsing "+currentDate+"...");
						//System.out.println(line);

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
							//if(type == LogEntry.EntryType.DESTINATION) {
							//	typeScores.put(type, 1); // Destination gets a bias as we want that to be default
							//} else {
							//	typeScores.put(type, 0); // Zero out all of our types. Oo automoxing
							//}
							typeScores.put(type, 0); // Zero out all of our types. Oo automoxing
						}
						lineParsed = false;
						// I use 4 integers for the typeScores instead of a HashMap because it makes me sleep better. I know we have all the ram in the world but still it tastes bad to allocate HashMap space for 4 integers just to make key-value ease.
						//Actually I think I change my mind.
						// Go throuhg each word in the line and if it has a majority character match with anything in our key file then tally it for that category, majority catigory wins.
						for(String word : line.split(" ")) {
							if(typeMap.containsKey(word)) {
								typeScores.merge(typeMap.get(word), 1, Integer::sum);
							}
						}
						while(typeScores.size()>0) {
							int maxValue = Collections.max(typeScores.values());
							List<LogEntry.EntryType> bestKeys = typeScores.entrySet().stream()
								.filter(e -> e.getValue() == maxValue)
								.map(Map.Entry::getKey)
								.collect(Collectors.toList());
							for(LogEntry.EntryType key : bestKeys) { // Here we remove the best keys in case we must loop again to next best options
								typeScores.remove(key); // Remove for next loop to go to next best options if still no parse
							}
							//HERE WE MUST LOOP THROUGH BESTKEYS IN PRIORITY ORDER ATTEMPTINT TO PARSE!!!!
							// Attempt to parse!
								// If parsed correctly we break!
							if (bestKeys.contains(LogEntry.EntryType.FUEL)) {
								// Attempt to parse FUEL
								// Find price and volume numbers plus station location?
								m = fuelPattern.matcher(line);
								float cost = 0;
								float volume = 0;
								LogDestination destination = null;
								while(m.find()) {
									//System.out.println(m.group());
									if(m.group().contains("gal")) {
										volume = Float.parseFloat(m.group().replace(" gal", ""));
										//System.out.println(volume);
									} else {
										cost = Float.parseFloat(m.group().replace("$", ""));
										//System.out.println(cost);
									}
									//System.out.println(m.group());
								}
								if(volume != 0 && cost != 0) {
									for(String word : line.split(" ")) {
										if((destination = locationMap.get(word))!=null) {
											break;	
										}	
									}
									if(destination != null) {
										vehicleLog.add(new FuelEntry(currentDate, destination, cost, volume));
										// Print output here maybe.
										lineParsed = true;
										break;
									}
								}
							}
							if(bestKeys.contains(LogEntry.EntryType.DESTINATION)) { // We dont need !lineParsed && here because this is just for priority checking time save
								// Attempt to parse DESTINATION
								// Potentially another scoring system for locations???
								LogDestination destination = null;
								for(String word : line.split(" ")) {
									if((destination = locationMap.get(word))!=null) {
										break;	
									}	
								}
								if(destination != null) {
									vehicleLog.add(new DestinationEntry(currentDate, destination));
									// Print output here maybe.
									lineParsed = true;
									break;
								}
							}
							if (bestKeys.contains(LogEntry.EntryType.SERVICE)) {
								// Attempt to parse SERVICE
								// Determine what services were perfomed
								// HERE
								HashSet<ServiceEntry.ServiceType> services = new HashSet<>();
								if(line.toLowerCase().contains("oil")) {
									services.add(ServiceEntry.ServiceType.OIL);
									lineParsed=true;
								}
								if(line.toLowerCase().contains("coolant")) {
									services.add(ServiceEntry.ServiceType.COOLANT);
									lineParsed=true;
								}
								if(line.toLowerCase().contains("grease")) {
									services.add(ServiceEntry.ServiceType.GREASE);
									lineParsed=true;
								}
								if(line.toLowerCase().contains("repair") || line.toLowerCase().contains("replace")) {
									services.add(ServiceEntry.ServiceType.REPAIR);
									lineParsed=true;
								}
								if(lineParsed) {
									m = odometerPattern.matcher(line);
									if(m.find()) {
										vehicleLog.add(new OdometerEntry(currentDate,Float.parseFloat(m.group())));
									}
									vehicleLog.add(new ServiceEntry(currentDate, services, line));
									break;
								}
							}
							if (bestKeys.contains(LogEntry.EntryType.ODOMETER)) {
								// Attempt to parse ODOMETER
								// Find long number
								m = odometerPattern.matcher(line);
								if(m.find()) {
									//System.out.println(Float.toString(Float.parseFloat(m.group())));
									vehicleLog.add(new OdometerEntry(currentDate,Float.parseFloat(m.group())));
									lineParsed = true;
									break;
								}
							}
							//for(LogEntry.EntryType type : LogEntry.TypePriority) {
							//	if(bestKeys.contains(type)) {
							//		switch(type) {
							//			case LogEntry.EntryType.DESTINATION:
							//				break;
							//			case LogEntry.EntryType.FUEL:
							//				break;
							//			case LogEntry.EntryType.SERVICE:
							//				break;
							//			case LogEntry.EntryType.ODOMETER:
							//				break;
							//		}
							//		//try {
							//		//	parseLine(type,line) // Use generic here
							//		//	typeScores.clear();
							//		//	break;
							//		//} catch ( Custom Exception? ) {
							//		//	//
							//		//}
							//	}
							//}
							// IF PARSING WORKS THEN BREAK HERE OTHERWISE CONTINUE AND LOOP!
							// IE: parse, if works set lineParsed = true, then set parseOption to false or break to exit loop.
							
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
		boolean outputWrote = false;
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
		while(!logParsed) {
			System.out.print("Enter Log File Path: ");
			logFilePath = scnr.nextLine();
			logParsed = parseLog(logFilePath);
		}
		if(args.length >= 3) {
			outputFilePath = args[2];
			outputWrote = writeToCSV(outputFilePath);
		}
		if(!outputWrote) {
			System.out.print("Enter Output File Path: ");
			outputFilePath = scnr.nextLine();
			outputWrote = writeToCSV(outputFilePath);
		}
		// Here we need to first get our key loaded properly, then we can attempt to parse the log file. If this succeeds we can store it in some fashion.
		// What sort of structure do we want for our key file? CSV?
	}
}
