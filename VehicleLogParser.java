/*
Just some notes regarding this program. With the variablility in spelling and all sorts of other things I think the application of a simple feed forward neural network might be better and easier. That is not used here due to program requirements.
*/

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
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
	// Variables for parsing our Key File
	public static Map<String, LogEntry.EntryType> typeMap; // optional type strings get paired to actual type values here
	public static Map<String, LogDestination> locationMap; // optional location strings get paired to actual destinations
	// Our parsed log entry list
	public static List<LogEntry> vehicleLog = new ArrayList<>();
	/*
	Keyfile Notes:
	So the top 4 lines go:
	destination words
	fuel words
	service words
	odometer words
	
	then the following lines are location word pairs and such. These are prioritiezed from top to bottom to my understanding with .values() from the hash map but I am not entirely sure, testing required.
	*/
	public static boolean validateKeyFile(String path) { // A method for parsing/validating our keyfile. Returns true for success.
		try (Stream<String> stream = Files.lines(Paths.get(path))) { // Attempt to open the file
			List<String> lines = stream.collect(Collectors.toList()); // Get lines from file
			String words[]; // Array for our split csv line
			typeMap = new HashMap<>(); // Initialize our typeMap declared above
			locationMap = new HashMap<>(); // Initialize our locationMap declared above
			if(lines.size() >= 4) { // Verify we at least have our 4 recquired lines
				for(int lineIndex = 0;lineIndex<lines.size();lineIndex++) { // Iterate through lines keeping index
					words = lines.get(lineIndex).split(","); // Split each line by commas since CSV type
					switch(lineIndex) { // Switch for line index
						case 0: // If were at line 0, it is for:
							// Destination
							for(String word : words) {
								typeMap.put(word, LogEntry.EntryType.DESTINATION);
							}
							break;
						case 1: // Line 1:
							// Fuel
							for(String word : words) {
								typeMap.put(word, LogEntry.EntryType.FUEL);
							}
							break;
						case 2: // Line 2:
							// Service
							for(String word : words) {
								typeMap.put(word, LogEntry.EntryType.SERVICE);
							}
							break;
						case 3: // Line 3:
							// Odometer
							for(String word : words) {
								typeMap.put(word, LogEntry.EntryType.ODOMETER);
							}
							break;
						default: // At this point any following lines are location descriptors.
							if(words.length >= 2) { // Each descriptor at least requires a title and description
								String title = null;
								String description = null;
								List<String> keys = new ArrayList<>(); // A list of all the optional keys we find for referencing our descriptor
								for(int wordIndex = 0;wordIndex<words.length;wordIndex++) {
									switch(wordIndex) {
										case 0: // First word is title
											title = words[wordIndex];
											break;
										case 1: // Second word is description
											description = words[wordIndex];
											break;
										default: // All other words are keys
											keys.add(words[wordIndex]);
											break;
									}
									LogDestination newDestination = new LogDestination(title,description); // Create a new destination object based on our descriptor
									for(String key : keys) { // Iterate through all keys so we can pair optional keys to Destination values.
										locationMap.put(key, newDestination); // pair in the HashMap
									}
								}
							} else { // Not enough words for descriptor
								System.err.println("Error parsing location descriptor for keyfile: " + lines.get(lineIndex));
								return false;
							}
							break;
					}
				}
			} else { // Error parsing key file, Return false.
				System.err.println("Error csv file not enough information.");
				return false;
			}
		} catch (IOException e) { // Error opening key file, Return false.
			System.err.println("Error reading file: " + e.getMessage());
			return false;
		}
		return true; // If we make it this far we have success so return true.
	}
	public static <T extends LogEntry> String entryToCSV(T entry) { // Generic method for converting an entry to a CSV type string
		String csvString = entry.getDate().toString()+","+entry.getType().toString(); // All entries at least have a date and type, put those into string with commas
		if(entry instanceof DestinationEntry d) { // Here we begin our checks for specific types and add additional information to our string based on that
			csvString+=","+d.getDestination().title+","+d.getDestination().description; // Destination is destination title and description
		} else if (entry instanceof FuelEntry f) {
			csvString+=","+f.getDestination().title+","+f.getDestination().description+","+Float.toString(f.getCost())+","+Float.toString(f.getVolume()); // Fuel is destination title and description, then cost and volume
		} else if (entry instanceof ServiceEntry s) {
			csvString+=","; // Prep for following loop
			for(ServiceEntry.ServiceType type : s.getServices()) { // For the service type entry we have optional types of service performed stored in a set. For this we iterate through all types in our set and add them to our csv string
				csvString+=type.toString()+",";
			}
			csvString+=s.getDescription(); // Conclude with the description
		} else if (entry instanceof OdometerEntry o) {
			csvString+=","+Float.toString(o.getMiles()); // Odometer is just miles additional
		}
		return csvString; // Return our formatted string
	}
	public static boolean writeToCSV(String path) { // A method for writing our parsed log to a formatted CSV
		try (PrintWriter writer = new PrintWriter(new FileWriter(path))) { // Attempt opening our new file for writing
			for(LogEntry entry : vehicleLog) { // For each entry in our parsed log
				writer.println(entryToCSV(entry)); // Write a new formatted CSV line!
			}
			return true; // Return true to signify successful write
		} catch (IOException e) { // Error
			System.err.println("Error writing CSV: " + e);
			return false;
		}
	}
	public static boolean parseLog(String path) { // Our method for parsing our sparatic log to our data types
		// A set of regex patterns for matching purposes below
		Pattern datePattern = Pattern.compile("^(\\d{1,2}[./-]\\d{1,2}[./-](?:\\d{4})).*");
		Pattern fuelPattern = Pattern.compile("(\\$\\d+(?:\\.\\d+)?)|(\\d+(?:\\.\\d+)?\\s*gal)");
		Pattern odometerPattern = Pattern.compile("\\b\\d{4,}(\\.\\d+)?\\b");
		Matcher m; // A matcher object to preform regex operations
		String dateParts[]; // Buffer array to hold parts of a date line
		int month,day,year; // Integers for parsed date components
		Map<LogEntry.EntryType, Integer> typeScores = new HashMap<>(); // This is a HashMap we use to store ranking information for our Entry Types based on keys found in our line
		LocalDate currentDate = null; // A buffer for the current date of our entry we are parsing
		boolean lineParsed; // A check for each line to verify parsing and close loops etc.
		try (Stream<String> stream = Files.lines(Paths.get(path))) { // Attempt to open and read our log file
			for(String line : (Iterable<String>) stream::iterator) { // Advanced loop through each line in our file
				m = datePattern.matcher(line); // For each line, we start by checking if it seems like a date
				if(m.find()) { // Pattern found?
					dateParts = m.group(1).split("-"); // If we find a pattern, we grab it with group and split it by "-" since its our only consistent
					try { // Parse numbers the pattern recognized and store them in proper locations
						month = Integer.parseInt(dateParts[0]);
						day = Integer.parseInt(dateParts[1]);
						year = Integer.parseInt(dateParts[2]);
						currentDate = LocalDate.of(year, month, day); // If we got here we have all the data to set our current date

					} catch (NumberFormatException e) { // Some error parsing an int
						System.out.println("Cannot parse date for: " + line);
					}
				} else { // No date pattern found, this will be any other line in the file
					if(currentDate != null) { // Verify we have a current date, if not we are in the gibberish header section.
						for(LogEntry.EntryType type : LogEntry.EntryType.values()) { // Advanced loop through an enum of entryTypes to initialize all scores to zero.
							typeScores.put(type, 0); // Zero out all of our types. automoxing
						}
						lineParsed = false; // initialize lineParsed
						// Go throuhg each word in the line and if it has a majority character match with anything in our key file then tally it for that category, majority catigory wins.
						for(String word : line.split(" ")) {
							if(typeMap.containsKey(word)) { // Check if our word provides a valid key in our type map
								typeScores.merge(typeMap.get(word), 1, Integer::sum); // if so Increment score for that specific type key
							}
						}
						while(typeScores.size()>0) { // While we have all of our scores OR have not broken from this loop continue to parse based on ranking starting with top set of ties and going down until there are no scores left
							int maxValue = Collections.max(typeScores.values()); // Get max value of our typeScores
							List<LogEntry.EntryType> bestKeys = typeScores.entrySet().stream() // Aggregate any keys in our typeScores that have this max value, allows for ties.
								.filter(e -> e.getValue() == maxValue)
								.map(Map.Entry::getKey)
								.collect(Collectors.toList());
							for(LogEntry.EntryType key : bestKeys) { // Here we remove the best keys in case we must loop again to next best options
								typeScores.remove(key); // Remove for next loop to go to next best options if still no parse
							}
							// Attempt to parse!
							// If parsed correctly we break!
							// Bellow we have if statments checking the type of our max keys in tie-breaking priority order, if a succesful parse is made then we set linePrased to true and break out of our loop ignoring the following lines.
							if (bestKeys.contains(LogEntry.EntryType.FUEL)) {
								// Attempt to parse FUEL
								m = fuelPattern.matcher(line); // Get pattern setup for fuel type line. Basically matches any integers or decimals preceded by $ OR followed by gal
								float cost = 0; // Initialize our cost and volume and destination
								float volume = 0;
								LogDestination destination = null;
								while(m.find()) { // Iterate through any found patterns
									if(m.group().contains("gal")) { // If it contains the word gal
										volume = Float.parseFloat(m.group().replace(" gal", "")); // Convert to float while removing " gal"
									} else { // Otherwsie assume its a price
										cost = Float.parseFloat(m.group().replace("$", "")); // Convert to float while removing "$"
									}
								}
								if(volume != 0 && cost != 0) { // If these values have been set outside of their initialization it means we have succesfully found our cost and volume signifying a fuel line
									for(String word : line.split(" ")) { // Iterate through all words in the line checking for location keys
										if((destination = locationMap.get(word))!=null) { // If we find a valid key then set our destination to the paired value at that key in our locationMap
											break; // Location found, break from word iteration loop
										}	
									}
									if(destination != null) { // Verify destination was found
										vehicleLog.add(new FuelEntry(currentDate, destination, cost, volume)); // Add our new valid parsed fuel entry
										lineParsed = true; // line is parsed
										break; // break from main loop
									}
								}
							}
							if(bestKeys.contains(LogEntry.EntryType.DESTINATION)) { // If we made it here then the line is still not parsed so next priority is Destination
								// Attempt to parse DESTINATION
								LogDestination destination = null; // Initialize our destination
								for(String word : line.split(" ")) { // Iterate through each word in line until we find a destination like above
									if((destination = locationMap.get(word))!=null) { // if key valid then set destination and break like above
										break;	
									}	
								}
								if(destination != null) { // Like above we verify a destination found
									vehicleLog.add(new DestinationEntry(currentDate, destination)); // In this case we store our new parsed destination entry
									lineParsed = true; // parsed
									break; // break main loop
								}
							}
							if (bestKeys.contains(LogEntry.EntryType.SERVICE)) { // still no break out so next priority service
								// Attempt to parse SERVICE
								// Determine what services were perfomed
								HashSet<ServiceEntry.ServiceType> services = new HashSet<>(); // A set to store service types performed
								if(line.toLowerCase().contains("oil")) { // These if statements check for their keyword or keywords and if found they add the type to the set
									services.add(ServiceEntry.ServiceType.OIL);
									lineParsed=true; // If we have at least one type found then its a valid service line so we set lineParsed to true
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
								} // All service types found added to set
								if(lineParsed) { // we found at least one service type validating line type:
									m = odometerPattern.matcher(line); // Check for odometer number in line
									if(m.find()) { // If found
										vehicleLog.add(new OdometerEntry(currentDate,Float.parseFloat(m.group()))); // Add a new odometer entry before the service entry
									}
									vehicleLog.add(new ServiceEntry(currentDate, services, line)); // add our new parsed ServiceEntry
									break; // like above
								}
							}
							if (bestKeys.contains(LogEntry.EntryType.ODOMETER)) { // Finally if we still are in this loop odometer is the lowest priority of tie breaking
								// Attempt to parse ODOMETER
								m = odometerPattern.matcher(line); // Check for odometer number pattern
								if(m.find()) {
									vehicleLog.add(new OdometerEntry(currentDate,Float.parseFloat(m.group()))); // Add our new parsed odometer entry
									lineParsed = true; // parsed
									break; // break
								}
							}
						}
						if(lineParsed == false) { // If we get through all levels of scoring and still have not parsed the line then it does not meet any patterns.
							System.err.println("Unable to parse line: " + line); // Notify user of error but continue parsing file.
						}
					}
				}
				
			}
		} catch (IOException e) { // Error eading our log file
			System.err.println("Error reading file: " + e.getMessage());
			return false;
		}
		return true; // If we got this far the file is parsed properly.
	}
	public static void main(String args[]) {
		String logFilePath, keyFilePath, outputFilePath; // Paths
		boolean keyValidated = false; // veritifcation booleans
		boolean logParsed = false;
		boolean outputWrote = false;
		Scanner scnr = new Scanner(System.in);
		if(args.length >= 1) { // first command line arg exists
			keyFilePath = args[0]; // set to keyFilePath
			keyValidated = validateKeyFile(keyFilePath); // check valid key file
		}
		while(!keyValidated) { // If first arg is not set or it wasnt a valid key file then we reach here where we repeatedly prompt the user until succesfull allowing ctrl-C breakout
			System.out.print("Enter Key File Path: ");
			keyFilePath = scnr.nextLine();
			keyValidated = validateKeyFile(keyFilePath);
		}
		// The following structures are the same as above for logFile and outputFile
		if(args.length >= 2) {
			logFilePath = args[1];
			logParsed = parseLog(logFilePath); // validate parse log
		}
		while(!logParsed) {
			System.out.print("Enter Log File Path: ");
			logFilePath = scnr.nextLine();
			logParsed = parseLog(logFilePath);
		}
		if(args.length >= 3) {
			outputFilePath = args[2];
			outputWrote = writeToCSV(outputFilePath); // validate write formatted csv
		}
		if(!outputWrote) {
			System.out.print("Enter Output File Path: ");
			outputFilePath = scnr.nextLine();
			outputWrote = writeToCSV(outputFilePath);
		}
	}
}
