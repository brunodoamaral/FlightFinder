package br.com.flightfinder.model

import java.util.Map;

class RoundTrip {
	
	List<Flight> departingFlights = []
	List<Flight> arrivingFlights = []
	
	def getFrom() {
		departingFlights.first().from
	}
	
	def getTo() {
		departingFlights.last().to
	}

	def value
	Boolean taxIncluded
	Airline airline
	Map extra = [:]
}
