package br.com.flightfinder.engine.airlines

import br.com.flightfinder.engine.AirlineTask
import br.com.flightfinder.model.Flight

class AmericanAirlinesTask extends AirlineTask {
	
	@Override
	public void run() {
		println "Starting a task for American Airlines from ${from.code} to ${to.code} between ${start} and ${end}"
		
		this.onDoneDelegate.airlineTaskFinished(this, [new Flight([
			from: from,
			to: to,
			departTime: start,
			arrivalTime: end,
			value: 1000,
			taxIncluded: true,
			airline: airline
			])]);
	}

}
