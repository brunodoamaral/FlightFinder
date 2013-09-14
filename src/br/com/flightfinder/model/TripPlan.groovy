package br.com.flightfinder.model

class TripPlan {
	
	Calendar startDate
	Calendar endDate
	Integer minDays
	Integer maxDays
	Airport from
	Airport to
	List<Flight> flights

}
