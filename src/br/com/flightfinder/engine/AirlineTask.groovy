package br.com.flightfinder.engine

import br.com.flightfinder.model.*;

public interface AirlineTaskFinished {
	def airlineTaskFinished(AirlineTask task, List<Flight> foundFlights);
}
	
abstract class AirlineTask implements Runnable {
	
	AirlineTaskFinished onDoneDelegate
	Date start
	Date end
	Airport from
	Airport to
	Airline airline
	
}
