package br.com.flightfinder.model

import br.com.flightfinder.engine.AirlineTask;
import br.com.flightfinder.engine.airlines.AmericanAirlinesTask;

class Airline {
	
	String name
	Class<? extends AirlineTask> taskClass
	
	public static final List<Airline> ALL_AIRLINES = [
		new Airline([name: 'American Airlines', taskClass: AmericanAirlinesTask.class])]

}
