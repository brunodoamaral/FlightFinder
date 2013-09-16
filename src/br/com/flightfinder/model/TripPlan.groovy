package br.com.flightfinder.model

class TripPlan {
	
	Date startDate
	Date endDate
	Integer minDays
	Integer maxDays
	Airport from
	Airport to
	List<RoundTrip> trips = []

}
