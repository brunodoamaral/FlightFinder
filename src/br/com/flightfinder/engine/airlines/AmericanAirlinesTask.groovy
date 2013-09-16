package br.com.flightfinder.engine.airlines

import java.text.SimpleDateFormat;

import groovy.util.slurpersupport.NodeChild;
import groovy.util.slurpersupport.NodeChildren;
import groovy.xml.XmlUtil;
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import org.apache.http.auth.*

import br.com.flightfinder.model.*
import br.com.flightfinder.engine.AirlineTask
import br.com.flightfinder.model.Flight

class AmericanAirlinesTask extends AirlineTask {
	
	@Override
	public void run() {
		try {
			println "Starting a task for American Airlines from ${from.code} to ${to.code} between ${start} and ${end}"
			
			def http = new HTTPBuilder("http://mobile.aa.com")
				
			http.setHeaders(['User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.65 Safari/537.36'])
			
			def params = [
				'dateChanged':'',
				'flightSearch':'revenue',
				'tripType':'roundTrip',
				'fromSearchPage':'true',
				'searchCategory':'false',
				'netSaaversTripType':'',
				'originAirport': from.code,
				'destinationAirport': to.code,
				'flightParams.flightDateParams.travelMonth': start.format("M"),
				'flightParams.flightDateParams.travelDay': start.format("d"),
				'returnDate.travelMonth': end.format("M"),
				'returnDate.travelDay': end.format("d"),
				'classOfServicePreference':'coach-restricted',
				'adultPassengerCount':'1',
				'seniorPassengerCount':'0',
				'youngAdultPassengerCount':'0',
				'childPassengerCount':'0',
				'infantPassengerCount':'0',
				'aairpassSearchType':'false',
				'currentCodeForm':'',
				'searchType':'fare',
				'currentCalForm':'dep',
				'adults':'1',
				'rooms':'1',
				'serviceclass':'coach',
				'cabinOfServicePreference':'matrix-lowest_fare',
				'cabinOfServicePreference':'matrix-show_all',
				'carrierPreference':'T',
				'countryPointOfSale':'BR',
				'discountCode':'',
				'passengerCount':'1',
				'_button_success':'Pesquisar',
				'un_form_encoding':'UTF-8',
				'locale': 'pt_BR' ];
			
			// perform a GET request, expecting JSON response data
			def html = http.post( [ path: '/mt/www.aa.com/reservation/tripSearchSubmit.do', body: params ])
			
			def divTrips = html.BODY.DIV[1].DIV[0]."**".findAll( { it.name() == "DIV" && (it.@class == "lmb02bl" || it.@class == "lmb12bl") } )
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy KK:mm a", new Locale('pt', 'BR'))
			
			def roundTrips = []
			divTrips.each { tripDiv -> 
				def currTrip = new RoundTrip([airline: airline])
				def infosDiv = tripDiv.DIV[0].DIV[1]
				def priceMatch = infosDiv.DIV[0].text() =~ /[^\d]+([\d\,]+).*/ 
				currTrip.value = priceMatch[0][1]
				
				def flightDivs = infosDiv.DIV[1]
				flightDivs.DIV.each { NodeChild flightDiv ->
					def matchFlight = flightDiv.text() =~ /(\w{3} \d+, \d+).*Partindo : (\w{3}) .* (\d\d:\d\d).?(\w\w) Chegando : (\w{3}) .* (\d\d:\d\d).?(\w\w)Hor.?rio.*/
					if ( matchFlight ) {
						Flight flight = new Flight()
						flight.from = Airport.getByCode(matchFlight[0][2])
						
						flight.departTime = new GregorianCalendar()
						dateFormat.setTimeZone(flight.from.timeZone)
						flight.departTime.setTime(dateFormat.parse( "${matchFlight[0][1]} ${matchFlight[0][3]} ${matchFlight[0][4]}" ) )
						flight.departTime.setTimeZone(flight.from.timeZone)
						
						flight.to = Airport.getByCode(matchFlight[0][5])
						flight.arrivalTime = new GregorianCalendar()
						dateFormat.setTimeZone(flight.to.timeZone)
						flight.arrivalTime.setTime(dateFormat.parse( "${matchFlight[0][1]} ${matchFlight[0][6]} ${matchFlight[0][7]}" ) )
						flight.arrivalTime.setTimeZone(flight.to.timeZone)
						
						// Fix next-day arrival
						if ( flight.arrivalTime.before(flight.departTime) ) {
							flight.arrivalTime.add(Calendar.DAY_OF_MONTH, 1)
						}
						
						// Check if it is after the departing time... so it is an returning flight
						if ( flight.departTime.time.after(end) ) {
							currTrip.arrivingFlights.add( flight )
						} else {
							currTrip.departingFlights.add( flight )
						}
					}
				}
				roundTrips.add(currTrip)
			}
			
			println "Found ${roundTrips.size()} trips"
			
			this.onDoneDelegate.airlineTaskFinished(this, roundTrips, null);
		} catch(Exception e) {
			this.onDoneDelegate.airlineTaskFinished(this, null, e);
		}
	}
}
