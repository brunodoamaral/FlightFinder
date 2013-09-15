import java.util.Calendar;
import java.util.List;

import br.com.flightfinder.engine.TripPlanTask;
import br.com.flightfinder.model.*

TripPlan aPlan = new TripPlan([
	startDate: new GregorianCalendar(2013, Calendar.OCTOBER, 1).time,
	endDate: new GregorianCalendar(2013, Calendar.OCTOBER, 11).time,
	minDays: 7,
	maxDays: 8,
	from: Airport.getByCode('GIG'),
	to: Airport.getByCode('WAS')
	])

new TripPlanTask([tripPlan: aPlan]).run()