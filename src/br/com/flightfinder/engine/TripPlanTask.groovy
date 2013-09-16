package br.com.flightfinder.engine

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.flightfinder.model.Airline;
import br.com.flightfinder.model.Flight;
import br.com.flightfinder.model.RoundTrip;
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
			for( def duration = tripPlan.minDays; duration <= tripPlan.maxDays; duration++ ) {
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
	def airlineTaskFinished(AirlineTask task, List<RoundTrip> foundTrips, Exception error) {
		if( error ) {
			error.printStackTrace();
		} else {
			tripPlan.trips.addAll( foundTrips )
		}
		if ( taskCount.decrementAndGet() == 0 ) {
			println 'Found flights:'
			
			tripPlan.trips.each{ trip ->
				println "Flight from ${trip.from.code} at ${trip.departingFlights.first().departTime} to ${trip.to.code} at ${trip.departingFlights.last().arrivalTime} for ${trip.value}"
			}
			
			pool.shutdown()
		}
	}
}
