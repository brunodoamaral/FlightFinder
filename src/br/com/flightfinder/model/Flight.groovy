package br.com.flightfinder.model

class Flight {

	Airport from
	Airport to	
	def departTime
	def arrivalTime
	def value
	Boolean taxIncluded
	Airline airline
	Map extra
	
	@Override
	public String toString() {
		return this.dump();
	}

}
