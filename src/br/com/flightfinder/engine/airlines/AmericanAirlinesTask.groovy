package br.com.flightfinder.engine.airlines

import java.text.SimpleDateFormat;

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import br.com.flightfinder.model.*
import br.com.flightfinder.engine.AirlineTask
import br.com.flightfinder.model.Flight

class AmericanAirlinesTask extends AirlineTask {
	
	@Override
	public void run() {
		try {
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
			
//			println "Partidas..."
			def tableValues = html."**".find( { it.name() == "TABLE" && it.@id == "js-matrix-departure-lowest" } )
			
			SimpleDateFormat hourFormat = new SimpleDateFormat("KK:mm a")
			
			def roundTrips = []
			if ( tableValues ) {
//				println "Option trip:"
				def currTrip = new RoundTrip([airline: airline])
				tableValues.TBODY.TR.each { trValue ->
					def matchValue = trValue.@id =~ /flight-lowest-departure-(\d+)-(\d+)/
					if ( matchValue ) {
						def currFlight = new Flight()
						if ( matchValue[0][2] == "0") {
							// Value
							def tdEconomy = trValue.TD.find({ it.@id =~ /.*Economy.*/ })
							def departingInput = tdEconomy.LABEL.INPUT.find({ it.@name == "departing" })
							currTrip.value = departingInput.@value
						}
						
						// Flight details
						def tdsTime = trValue.TD.findAll({ it.@class == "aa-flight-time" })
						
						// Reference date: the last arrival date OR the start date
						def startDate = currTrip.departingFlights.size() ? currTrip.departingFlights.last().arrivalTime.time : start 
						
						// -> From
						currFlight.from = Airport.getByCode(tdsTime[0].SPAN.text().trim())
						def departTime = hourFormat.parse(tdsTime[0].STRONG.text())
						currFlight.departTime = new GregorianCalendar(startDate[Calendar.YEAR], startDate[Calendar.MONTH], startDate[Calendar.DAY_OF_MONTH], departTime[Calendar.HOUR_OF_DAY], departTime[Calendar.MINUTE])
						currFlight.departTime.setTimeZone(currFlight.from.timeZone)
						
						// -> To
						currFlight.to = Airport.getByCode(tdsTime[1].SPAN.text().trim())
						def arrivalTime = hourFormat.parse(tdsTime[1].STRONG.text())
						currFlight.arrivalTime = new GregorianCalendar(startDate[Calendar.YEAR], startDate[Calendar.MONTH], startDate[Calendar.DAY_OF_MONTH], arrivalTime[Calendar.HOUR_OF_DAY], arrivalTime[Calendar.MINUTE])
						currFlight.arrivalTime.setTimeZone(currFlight.to.timeZone)
						
						// Fix next-day arrival
						if ( currFlight.arrivalTime.before(currFlight.departTime) ) {
							currFlight.arrivalTime.add(Calendar.DAY_OF_MONTH, 1)
						}
						
//						println "${currFlight.from.code}->${currFlight.to.code} ${formatCalendar(currFlight.departTime)} ${formatCalendar(currFlight.arrivalTime)}"
						
						// Add current flight to trip
						currTrip.departingFlights.add( currFlight )
					} else if ( trValue.@id =~ /flight-notes-*/  ) {
						currTrip.extra['notes'] = trValue.text()
						roundTrips.add(currTrip)
//						println "Option trip:"
						currTrip = new RoundTrip([airline: airline])
					}
				}
			}
			
			println "Found ${roundTrips.size()} trips"
			
			this.onDoneDelegate.airlineTaskFinished(this, roundTrips, null);
		} catch(Exception e) {
			this.onDoneDelegate.airlineTaskFinished(this, null, e);
		}
	}

	def formatCalendar(Calendar date) {
		date.format("dd/MM/yyyy HH:mm Z")
//		SimpleDateFormat format = new SimpleDateFormat()
//		format.setCalendar(date)
//		return format.format(date.time)
	}
}
