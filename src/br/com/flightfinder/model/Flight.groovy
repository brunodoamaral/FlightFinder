package br.com.flightfinder.model

class Flight {

	Airport from
	Airport to	
	Calendar departTime
	Calendar arrivalTime
	
	@Override
	public String toString() {
		return this.dump();
	}

}
