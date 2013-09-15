import java.util.Calendar;
import java.util.List;

import br.com.flightfinder.engine.TripPlanTask;
import br.com.flightfinder.model.*

TripPlan aPlan = new TripPlan([
	startDate: new GregorianCalendar(2013, Calendar.OCTOBER, 1).time,
	endDate: new GregorianCalendar(2013, Calendar.OCTOBER, 30).time,
	minDays: 5,
	maxDays: 10,
	from: Airport.getByCode('GIG'),
	to: Airport.getByCode('WAS')
	])

new TripPlanTask([tripPlan: aPlan]).run()