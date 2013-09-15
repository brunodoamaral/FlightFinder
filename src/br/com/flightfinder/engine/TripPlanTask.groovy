package br.com.flightfinder.engine

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.flightfinder.model.Airline;
import br.com.flightfinder.model.Flight;
import br.com.flightfinder.model.TripPlan;

class TripPlanTask implements Runnable, AirlineTaskFinished {
	
	TripPlan tripPlan
	def pool = Executors.newFixedThreadPool(4)
	AtomicInteger taskCount = new AtomicInteger(0)

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
						
						taskCount.incrementAndGet()
						pool.submit(task)
					}
				} else {
					// Breaks the loop
					break ;
				}
			}
			
			// Next day
			currentDate++ 
		}
	}
	
	@Override
	def airlineTaskFinished(AirlineTask task, List<Flight> foundFlights) {
		tripPlan.flights.addAll( foundFlights )
		if ( taskCount.decrementAndGet() == 0 ) {
			println 'Found flights:'
			
			tripPlan.flights.each{ flight ->
				println "Flight from ${flight.from.code} at ${flight.departTime} to ${flight.to.code} at ${flight.arrivalTime} for ${flight.value}"
			}
			
			pool.shutdown()
		}
	}
}
