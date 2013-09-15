package br.com.flightfinder.engine

import java.util.List;

import br.com.flightfinder.model.Airline;
import br.com.flightfinder.model.Flight;
import br.com.flightfinder.model.TripPlan;

class TripPlanTask implements Runnable, AirlineTaskFinished {
	
	TripPlan tripPlan

	@Override
	public void run() {
		// Creates the taks for each valid date in the trip plan
		def currentDate = tripPlan.startDate
		while ( currentDate < tripPlan.endDate ) {
			for( def duration = tripPlan.minDays; duration < tripPlan.maxDays; duration++ ) {
				if( currentDate + duration <= tripPlan.endDate ) {
					Airline.ALL_AIRLINES.each { airline ->
						AirlineTask task = airline.taskClass.newInstance([
							airline: airline,
							from: tripPlan.from,
							to: tripPlan.to,
							start: currentDate,
							end: currentDate + duration])
						
						task.onDoneDelegate = this
						
						task.run()
					}
				} else {
					// Breaks the loop
					break ;
				}
			}
			
			// Next day
			currentDate++ 
		}
		
		println 'Found flights:'
		
		tripPlan.flights.each{ flight ->
			println flight
		}
	}
	
	@Override
	def airlineTaskFinished(AirlineTask task, List<Flight> foundFlights) {
		tripPlan.flights << foundFlights
	}
}
