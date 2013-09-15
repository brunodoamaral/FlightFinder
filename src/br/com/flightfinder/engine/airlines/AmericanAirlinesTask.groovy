package br.com.flightfinder.engine.airlines

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import br.com.flightfinder.model.*

import br.com.flightfinder.engine.AirlineTask
import br.com.flightfinder.model.Flight

class AmericanAirlinesTask extends AirlineTask {
	
	@Override
	public void run() {
		println "Starting a task for American Airlines from ${from.code} to ${to.code} between ${start} and ${end}"
		
		
		def http = new HTTPBuilder('https://www.aa.com', HTML)
		
		def params = [
			'currentCalForm': 'dep',
			'currentCodeForm': '',
			'tripType': 'roundTrip',
			'originAirport': from.code,
			'flightParams.flightDateParams.travelMonth': start.format("M"),
			'flightParams.flightDateParams.travelDay': start.format("d"),
//			'flightParams.flightDateParams.searchTime': '040001',
			'destinationAirport': to.code,
			'returnDate.travelMonth': end.format("M"),
			'returnDate.travelDay': end.format("d"),
//			'returnDate.searchTime': '040001',
			'adultPassengerCount': '1',
			'adultPassengerCount': '1',
			'hotelRoomCount': '1',
			'serviceclass': 'coach',
			'searchTypeMode': 'matrix',
			'awardDatesFlexible': 'true',
			'originAlternateAirportDistance': '0',
			'destinationAlternateAirportDistance': '0',
			'discountCode': '',
			'flightSearch': 'revenue',
			'dateChanged': 'false',
			'fromSearchPage': 'true',
			'advancedSearchOpened': 'false',
			'numberOfFlightsToDisplay': '10',
			'searchCategory': '',
			'aairpassSearchType': 'false',
			'moreOptionsIndicator': '',
			'seniorPassengerCount': '0',
			'youngAdultPassengerCount': '0',
			'childPassengerCount': '0',
			'infantPassengerCount': '0',
			'passengerCount': '1',
			'locale': 'pt_BR' ];
		
		// perform a GET request, expecting JSON response data
		def html = http.post( [ path: '/reservation/searchFlightsSubmit.do', body: params ])
		
		def refreshMeta = html.HEAD.META.find({ it['@http-equiv'] == "refresh" })
		def urlMatch = refreshMeta.@content =~ /.*URL=\'([^\?]+)\?([^\']+)\'/
		
		def redirectUrl = urlMatch[0][1]
		def redirectQueryString = urlMatch[0][2]
		
		println "Redirecting to ${redirectUrl}?${redirectQueryString}"
		
		// Faz o segundo request...
		html = http.get([path: redirectUrl, queryString: redirectQueryString])
		
		println "Partidas..."
		def tableValues = html."**".find( { it.name() == "TABLE" && it.@id == "js-matrix-departure-lowest" } )
		
		def flights = []
		if ( tableValues ) {
			def currFlight = new Flight([from: from, to: to, airline: airline])
			tableValues.TBODY.TR.each { trValue ->
				def matchValue = trValue.@id =~ /flight-lowest-departure-(\d+)-(\d+)/
				if ( matchValue ) {
					if ( matchValue[0][2] == "0") {
						// Pega o valor
						def tdEconomy = trValue.TD.find({ it.@id =~ /.*Economy.*/ })
						def departingInput = tdEconomy.LABEL.INPUT.find({ it.@name == "departing" })
						currFlight.value = departingInput.@value
						
						// Pega a hora de início
						def tdsTime = trValue.TD.findAll({ it.@class == "aa-flight-time" })
						currFlight.departTime = tdsTime[0].STRONG.text()
					} else {
						// Pega a hora final
						def tdsTime = trValue.TD.findAll({ it.@class == "aa-flight-time" })
						currFlight.arrivalTime = tdsTime[1].STRONG.text()
					}
				} else if ( trValue.@id =~ /flight-notes-*/  ) {
					flights.add(currFlight)
					currFlight = new Flight([from: from, to: to, airline: airline])
				}
			}
		}
		
		println "Found ${flights.size()} flights"
		
		this.onDoneDelegate.airlineTaskFinished(this, flights);
	}

}
