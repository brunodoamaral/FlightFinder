package br.com.flightfinder.model

import au.com.bytecode.opencsv.CSVReader

class Airport {
	String name
	String city
	String country
	String code
	String code2
	Double latitude
	Double longitude
	Double altitude
	TimeZone timeZone
	
	private final static Map<String,Airport> allAirports;
	
	static {
		allAirports = new HashMap()
		CSVReader reader = new CSVReader(new FileReader("airports.dat"));
		String [] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			// nextLine[] is an array of values from the line
			def airport = new Airport([
				name: nextLine[1],
				city: nextLine[2],
				country: nextLine[3],
				code: nextLine[4],
				code2: nextLine[5],
				latitude: new Double( nextLine[6] ),
				longitude: new Double( nextLine[7] ),
				altitude: new Double( nextLine[8] ),
				timeZone: TimeZone.getTimeZone(nextLine[9][0] == '-' ? "GMT${nextLine[9]}:00" : "GMT+${nextLine[9]}:00")
				])
			allAirports.put(airport.code, airport );
//			System.out.println(nextLine[0] + nextLine[1] + "etc...");
		}
	}
	
	public static Airport getByCode(String code) {
		allAirports[code]
	}
}