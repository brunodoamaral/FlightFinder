package br.com.flightfinder.model

class Flight {

	Airport from
	Airport to	
	def departTime
	def arrivalTime
	
	@Override
	public String toString() {
		return this.dump();
	}

}
