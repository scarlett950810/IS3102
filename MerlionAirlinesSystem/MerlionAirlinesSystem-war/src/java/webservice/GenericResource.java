/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import imas.departure.sessionbean.SeatHelperClass;
import imas.departure.sessionbean.WebCheckInSessionBeanLocal;
import imas.distribution.entity.PassengerEntity;
import imas.distribution.entity.TicketEntity;
import imas.distribution.sessionbean.FlightLookupSessionBeanLocal;
import imas.distribution.sessionbean.MakeBookingSessionBeanLocal;
import imas.inventory.entity.BookingClassEntity;
import imas.planning.entity.FlightEntity;
import imas.planning.entity.AirportEntity;
import imas.planning.entity.RouteEntity;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author ruicai
 */
@Path("generic")
public class GenericResource {

    @EJB
    AppFlightLookupLocal appFlightLookup;

    @EJB
    FlightLookupSessionBeanLocal flightLookup;

    @EJB
    AppGenerateItineraryLocal appGenerateItinerary;
    @EJB
    MakeBookingSessionBeanLocal makeBooking;
    @EJB
    private WebCheckInSessionBeanLocal webCheckInSessionBean;
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of GenericResource
     */
    public GenericResource() {
    }

    @GET
    @Path(value = "getOneWayFlightsByRouteDate")
    @Produces("application/json")
    public List<TicketFlightEntity> getOneWayFlightsByRouteDate(@QueryParam("origin") String origin, @QueryParam("destination") String destination, @QueryParam("departureD") String departureD, @QueryParam("bcName") String bcName) throws ParseException {
        System.err.println("Server running: getOneWayflightsby route and date execute");
        System.err.println("destination" + destination + "origin" + origin + "departure" + departureD);
        List<FlightEntity> flights = new ArrayList<FlightEntity>();
        AirportEntity originA = appFlightLookup.getOneAirportFromCityName(origin);
        AirportEntity destinationA = appFlightLookup.getOneAirportFromCityName(destination);
        if (originA == null || destinationA == null) {
            System.err.println("cannot find origin or destination");
            return null;
        }
        System.err.println("origin" + originA);
        System.err.println("destination" + destinationA);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy");
        Date depD = simpleDateFormat.parse(departureD);
        RouteEntity route = flightLookup.getDirectRoute(originA, destinationA);

        Calendar cal = Calendar.getInstance();
        cal.setTime(depD);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date lowB = cal.getTime();
        System.err.println("Lower Bound in getOneWayFlightsByRouteDate: " + lowB);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date highB = cal.getTime();
        System.err.println("Higher Bound in getOneWayFlightsByRouteDate: " + highB);

        flights = flightLookup.getAvailableFlights(route, depD, highB);
        System.err.println("flights" + flights);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("d MMM yyyy HH:mm");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MMM d yyyy EEE");
        SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("HH:mm");

        List<TicketFlightEntity> test = new ArrayList<>();
        for (FlightEntity f : flights) {
            List<BookingClassEntity> bcs = new ArrayList<>();
            if (!bcName.equals("All Classes")) {
                bcs = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, bcName, 1);
            } else {
                List<BookingClassEntity> bcs1 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "First Class", 1);
                List<BookingClassEntity> bcs2 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "Business Class", 1);
                List<BookingClassEntity> bcs3 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "Premium Economy Class", 1);
                List<BookingClassEntity> bcs4 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "Economy Class", 1);
                bcs = bcs1;
                bcs.addAll(bcs2);
                bcs.addAll(bcs3);
                bcs.addAll(bcs4);
            }
            for (BookingClassEntity bc : bcs) {
                TicketFlightEntity f1 = new TicketFlightEntity();
                f1.setId(f.getId());
                f1.setFlightNo(f.getFlightNo());
                f1.setDepartureDate(simpleDateFormat1.format(f.getDepartureDate()));
                f1.setArrivalDate(simpleDateFormat1.format(f.getArrivalDate()));
                DecimalFormat df = new DecimalFormat("0.0");

                f1.setPrice(bc.getPrice());
                f1.setBookingClassName(bc.getSeatClass());
                f1.setOrigin(origin);
                f1.setDestination(destination);
                f1.setOriAirportName(originA.getAirportName());
                f1.setOriAirportCode(originA.getAirportCode());
                f1.setDesAirportName(destinationA.getAirportName());
                f1.setDesAirportCode(destinationA.getAirportCode());
                f1.setAircraftTailN(f.getAircraft().getAircraftType().getIATACode());
                f1.setDepDayWE(simpleDateFormat2.format(f.getDepartureDate()));
                f1.setDepTimeE(simpleDateFormat3.format(f.getDepartureDate()));
                f1.setAriDayWE(simpleDateFormat2.format(f.getArrivalDate()));
                f1.setAriTimeE(simpleDateFormat3.format(f.getArrivalDate()));
                long diff = f.getArrivalDate().getTime() - f.getDepartureDate().getTime();
                double diff1 = diff / (60 * 60 * 1000) % 24;

                String timeD = df.format(diff1);
                f1.setTimeDuration(timeD);

                test.add(f1);
            }
        }

        System.out.println("test" + test);

        return test;
    }

    @GET
    @Path(value = "generateItinerary")
    @Produces("application/json")
    public String generateItinerary(@QueryParam("flightId") long flightId,  @QueryParam("firstNP") String firstNameP,@QueryParam("lastNP") String lastNameP,@QueryParam("titleP") String titleP, @QueryParam("bc") String bcName,@QueryParam("nationNP") String nationNP, @QueryParam("title") String title, @QueryParam("firstN") String firstN, @QueryParam("lastN") String lastN,@QueryParam("address") String address, @QueryParam("city") String city, @QueryParam("country") String country, @QueryParam("zipCode") String zipCode,@QueryParam("contactN") String contactN, @QueryParam("passportP") String passportP, @QueryParam("email") String email, @QueryParam("price") Double price) throws ParseException {
        FlightEntity chosenFlight = appGenerateItinerary.findFlightById(flightId);
        List<FlightEntity> chosenFlights = new ArrayList<>();
        chosenFlights.add(chosenFlight);

        PassengerEntity p = new PassengerEntity();
        p.setFirstName(firstNameP);
        p.setLastName(lastNameP);
        p.setPassportNumber(passportP);
        p.setTitle(titleP);
        p.setNationality(nationNP);
        TicketEntity t = new TicketEntity(chosenFlight, bcName, price);
        t.setPassenger(p);
        List<TicketEntity> ts = new ArrayList<>();
        ts.add(t);
        p.setTickets(ts);
        
        List<PassengerEntity> ps = new ArrayList<>();
        ps.add(p);
        String referenceN = makeBooking.generateItinerary(chosenFlights, ps, title, firstN, lastN, address, city, country, email, contactN, zipCode,"paid", price, null);
        return referenceN;
    }
@GET
    @Path(value = "generateItineraryTwoWay")
    @Produces("application/json")
    public String generateItineraryTwoWay(@QueryParam("flightId") long flightId, @QueryParam("flightId2") long flightId2, @QueryParam("firstNP") String firstNameP,@QueryParam("lastNP") String lastNameP,@QueryParam("titleP") String titleP, @QueryParam("bc") String bcName, @QueryParam("bc1") String bcName1,@QueryParam("nationNP") String nationNP, @QueryParam("title") String title, @QueryParam("firstN") String firstN, @QueryParam("lastN") String lastN,@QueryParam("address") String address, @QueryParam("city") String city, @QueryParam("country") String country, @QueryParam("zipCode") String zipCode,@QueryParam("contactN") String contactN, @QueryParam("passportP") String passportP, @QueryParam("email") String email, @QueryParam("price") Double price ,@QueryParam("price1") Double price1) throws ParseException {
        FlightEntity chosenFlight = appGenerateItinerary.findFlightById(flightId);
        FlightEntity chosenFlightR = appGenerateItinerary.findFlightById(flightId2);
        List<FlightEntity> chosenFlights = new ArrayList<>();
        chosenFlights.add(chosenFlight);
        chosenFlights.add(chosenFlightR);

        PassengerEntity p = new PassengerEntity();
        p.setFirstName(firstNameP);
        p.setLastName(lastNameP);
        p.setPassportNumber(passportP);
        p.setTitle(titleP);
        p.setNationality(nationNP);
        TicketEntity t = new TicketEntity(chosenFlight, bcName, price);
        TicketEntity t1 = new TicketEntity(chosenFlightR,bcName1,price1);
        t.setPassenger(p);
        t1.setPassenger(p);
        List<TicketEntity> ts = new ArrayList<>();
        ts.add(t);
        ts.add(t1);
        p.setTickets(ts);
        
        List<PassengerEntity> ps = new ArrayList<>();
        ps.add(p);
        String referenceN = makeBooking.generateItinerary(chosenFlights, ps, title, firstN, lastN, address, city, country, email, contactN, zipCode,"paid", price, null);
        return referenceN;
    }

     @GET
    @Path(value = "webCheckInObj")
    @Produces("application/json")
    public List<TicketCheckInEntity> webCheckInObj (@QueryParam("referenceN") String referenceN,  @QueryParam("passport") String passport){
        List<TicketEntity> tickets = webCheckInSessionBean.getCheckInTicket(passport, referenceN);
        List<TicketCheckInEntity> ticketsCheckIn = new ArrayList<>();
        for(TicketEntity t: tickets){
            TicketCheckInEntity tc = new TicketCheckInEntity();
            tc.setId(t.getId());
            tc.setFlightNo(t.getFlight().getFlightNo());
            tc.setFlightId(t.getFlight().getId());
            tc.setOriginAN(t.getFlight().getRoute().getOriginAirport().getAirportName());
            tc.setOriginCity(t.getFlight().getRoute().getOriginAirport().getCityName());
            tc.setDestinationAN(t.getFlight().getRoute().getDestinationAirport().getAirportName());
            tc.setDestinationCity(t.getFlight().getRoute().getDestinationAirport().getCityName());
            Date depD = t.getFlight().getDepartureDate();
            Date ariD = t.getFlight().getArrivalDate();
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("d MMM yyyy HH:mm");
            tc.setDepD(simpleDateFormat1.format(depD));
            tc.setAriD(simpleDateFormat1.format(ariD));
            ticketsCheckIn.add(tc);
        }
         System.err.println(ticketsCheckIn);
        return ticketsCheckIn;
    }
    
    
    @GET
    @Path(value = "webCheckIn")
    @Produces("application/json")
    public String webCheckIn (@QueryParam("ticketId") long ticketId){
        TicketEntity ticket = appGenerateItinerary.findTicketById(ticketId);
        System.out.println("ticketId"+ticketId);
           List<List<SeatHelperClass>> seatHelper = webCheckInSessionBean.fetchAllSeats(ticket.getFlight().getAircraft().getId(), ticket.getFlight().getId(), ticket.getBookingClassName());
          
           boolean selected = false;
           String seatNumber = "";
           for(int i=0;i<seatHelper.size();i++){
              for(int j = 0; j < seatHelper.get(i).size();j++){
                  SeatHelperClass sth = seatHelper.get(i).get(j);
                  if(!selected){
                  if(!sth.isOccupied()&&sth.isEligible()){
                      sth.setSelected(true);
                      selected = true;
                      seatNumber = sth.getSeatNumber();
                  }
                  }
              }
          }
           webCheckInSessionBean.completeWebCheckIn(seatHelper, ticket);
           System.out.println("tickettStatus"+ticket.getIssued());
         System.out.println("seatnumber"+seatNumber);
        return seatNumber;
    }
    
    @GET
    @Path(value = "getPromotedFlights")
    @Produces("application/json")
    public List<TicketFlightEntity> getPromotedFlights(@QueryParam("cityName") String cityName) throws ParseException {
        System.err.println("Server running: getOneWayflightsby route and date execute");
      
        List<FlightEntity> flights = new ArrayList<FlightEntity>();
        AirportEntity originA = appFlightLookup.getOneAirportFromCityName(cityName);
        if (originA == null) {
            System.err.println("cannot find origin or destination");
            return null;
        }
        Date today = new Date();
        Calendar call = Calendar.getInstance();
        call.add(Calendar.YEAR, 1);
        today = call.getTime();
        call.add(Calendar.DATE,10);
        Date tenDaysLater = call.getTime();
       
        flights = appFlightLookup.getPromotedFlights(originA, today, tenDaysLater);
        System.err.println("flights" + flights);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("d MMM yyyy HH:mm");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MMM d yyyy EEE");
        SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("HH:mm");

        List<TicketFlightEntity> test = new ArrayList<>();
        List<BookingClassEntity> bcs1All = new ArrayList<>();
        List<BookingClassEntity> bcs2All = new ArrayList<>();
        List<BookingClassEntity> bcs3All = new ArrayList<>();
        List<BookingClassEntity> bcs4All = new ArrayList<>();
 List<BookingClassEntity> bcs = new ArrayList<>();
        for (FlightEntity f : flights) {
           
            
                List<BookingClassEntity> bcs1 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "First Class", 1);
                List<BookingClassEntity> bcs2 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "Business Class", 1);
                List<BookingClassEntity> bcs3 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "Premium Economy Class", 1);
                List<BookingClassEntity> bcs4 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "Economy Class", 1);
                
                bcs1All.addAll(bcs1);
                bcs2All.addAll(bcs2);
                bcs3All.addAll(bcs3);
                bcs4All.addAll(bcs4);


                }
 
                Collections.sort(bcs1All, new BookingClassComparator());
                Collections.sort(bcs2All, new BookingClassComparator());
                Collections.sort(bcs3All, new BookingClassComparator());
                Collections.sort(bcs4All, new BookingClassComparator());
                
                bcs.add(bcs1All.get(0));
                bcs.add(bcs1All.get(1));
                bcs.add(bcs1All.get(2));
                bcs.add(bcs1All.get(3));
                bcs.add(bcs1All.get(4));
                bcs.add(bcs1All.get(5));
                bcs.add(bcs2All.get(0));
                bcs.add(bcs2All.get(1));
                bcs.add(bcs2All.get(2));
                bcs.add(bcs2All.get(3));
                bcs.add(bcs2All.get(4));
                bcs.add(bcs2All.get(5));
                bcs.add(bcs2All.get(6));
                bcs.add(bcs3All.get(0));
                bcs.add(bcs3All.get(1));
                bcs.add(bcs3All.get(2));
                bcs.add(bcs3All.get(3));
                 bcs.add(bcs3All.get(4));
                bcs.add(bcs3All.get(5));
                bcs.add(bcs3All.get(6));
                bcs.add(bcs3All.get(7));
                bcs.add(bcs4All.get(0));
                bcs.add(bcs4All.get(1));
                bcs.add(bcs4All.get(2));
                bcs.add(bcs4All.get(3));
                bcs.add(bcs4All.get(4));
                bcs.add(bcs4All.get(5));
                bcs.add(bcs4All.get(6));
                bcs.add(bcs4All.get(7));
                bcs.add(bcs4All.get(8));
           
            for (BookingClassEntity bc : bcs) {
                TicketFlightEntity f1 = new TicketFlightEntity();
                FlightEntity f = bc.getFlight();
                f1.setId(f.getId());
                f1.setFlightNo(f.getFlightNo());
                f1.setDepartureDate(simpleDateFormat1.format(f.getDepartureDate()));
                f1.setArrivalDate(simpleDateFormat1.format(f.getArrivalDate()));
                DecimalFormat df = new DecimalFormat("0.0");

                f1.setPrice(bc.getPrice());
                f1.setBookingClassName(bc.getSeatClass());
                f1.setOrigin(cityName);
                f1.setDestination(f.getRoute().getDestinationAirport().getCityName());
                f1.setOriAirportName(originA.getAirportName());
                f1.setOriAirportCode(originA.getAirportCode());
                f1.setDesAirportName(f.getRoute().getDestinationAirport().getAirportName());
                f1.setDesAirportCode(f.getRoute().getDestinationAirport().getAirportCode());
                f1.setAircraftTailN(f.getAircraft().getAircraftType().getIATACode());
                Date departureDatet = f.getDepartureDate();
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(departureDatet);
                cal2.add(Calendar.YEAR,-1);
                departureDatet = cal2.getTime();
                
                Date arriveDatet = f.getArrivalDate();
                
                cal2.setTime(arriveDatet);
                cal2.add(Calendar.YEAR,-1);
                arriveDatet = cal2.getTime();
                
                f1.setDepDayWE(simpleDateFormat2.format(departureDatet));
                f1.setDepTimeE(simpleDateFormat3.format(departureDatet));
                f1.setAriDayWE(simpleDateFormat2.format(arriveDatet));
                f1.setAriTimeE(simpleDateFormat3.format(arriveDatet));
                long diff = f.getArrivalDate().getTime() - f.getDepartureDate().getTime();
                double diff1 = diff / (60 * 60 * 1000) % 24;

                String timeD = df.format(diff1);
                f1.setTimeDuration(timeD);

                test.add(f1);
            
        }

        System.out.println("test" + test);

        return test;
    }
    
    
    
//    @GET
//    @Path(value = "getTwoWayFlightsByRouteDate")
//    @Produces("application/json")
//    public List<TicketFlightEntity> getTwoWayFlightsByRouteDate(@QueryParam("origin") String origin, @QueryParam("destination") String destination, @QueryParam("departureD") String departureD, @QueryParam("departureRD") String departureRD, @QueryParam("bcName") String bcName) throws ParseException {
//        System.err.println("Server running: getOneWayflightsby route and date execute");
//        System.err.println("destination" + destination + "origin" + origin + "departure" + departureD);
//        List<FlightEntity> flights = new ArrayList<FlightEntity>();
//        AirportEntity originA = appFlightLookup.getOneAirportFromCityName(origin);
//        AirportEntity destinationA = appFlightLookup.getOneAirportFromCityName(destination);
//        if (originA == null || destinationA == null) {
//            System.err.println("cannot find origin or destination");
//            return null;
//        }
//        System.err.println("origin" + originA);
//        System.err.println("destination" + destinationA);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy");
//        Date depD = simpleDateFormat.parse(departureD);
//        Date depDR = simpleDateFormat.parse(departureRD);
//        
//        RouteEntity route = flightLookup.getDirectRoute(originA, destinationA);
//        RouteEntity routeR = flightLookup.getDirectRoute(destinationA, originA);
//        
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(depD);
//        cal.set(Calendar.HOUR_OF_DAY, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 0);
//        Date lowB = cal.getTime();
//        System.err.println("Lower Bound in getOneWayFlightsByRouteDate: " + lowB);
//        cal.set(Calendar.HOUR_OF_DAY, 23);
//        cal.set(Calendar.MINUTE, 59);
//        cal.set(Calendar.SECOND, 59);
//        Date highB = cal.getTime();
//        System.err.println("Higher Bound in getOneWayFlightsByRouteDate: " + highB);
//        
//        cal.setTime(depDR);
//        cal.set(Calendar.HOUR_OF_DAY, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 0);
//        Date lowBR = cal.getTime();
//        System.err.println("Lower Bound in getOneWayFlightsByRouteDate: " + lowBR);
//        cal.set(Calendar.HOUR_OF_DAY, 23);
//        cal.set(Calendar.MINUTE, 59);
//        cal.set(Calendar.SECOND, 59);
//        Date highBR = cal.getTime();
//        System.err.println("Higher Bound in getOneWayFlightsByRouteDate: " + highBR);
//
//        flights = flightLookup.getAvailableFlights(route, depD, highB);
//        System.err.println("flights" + flights);
//        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("d MMM yyyy HH:mm");
//        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("EEE MMM d yyyy");
//        SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("h:mm a");
//
//        List<TicketFlightEntity> test = new ArrayList<>();
//        for (FlightEntity f : flights) {
//            List<BookingClassEntity> bcs = new ArrayList<>();
//            if (!bcName.equals("All Classes")) {
//                bcs = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, bcName, 1);
//            } else {
//                List<BookingClassEntity> bcs1 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "First Class", 1);
//                List<BookingClassEntity> bcs2 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "Business Class", 1);
//                List<BookingClassEntity> bcs3 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "Premium Economy Class", 1);
//                List<BookingClassEntity> bcs4 = flightLookup.getAvailableBookingClassUnderFlightUnderSeatClass(f, "Economy Class", 1);
//                bcs = bcs1;
//                bcs.addAll(bcs2);
//                bcs.addAll(bcs3);
//                bcs.addAll(bcs4);
//            }
//            for (BookingClassEntity bc : bcs) {
//                TicketFlightEntity f1 = new TicketFlightEntity();
//                f1.setId(f.getId());
//                f1.setFlightNo(f.getFlightNo());
//                f1.setDepartureDate(simpleDateFormat1.format(f.getDepartureDate()));
//                f1.setArrivalDate(simpleDateFormat1.format(f.getArrivalDate()));
//                DecimalFormat df = new DecimalFormat("0.0");
//
//                f1.setPrice(bc.getPrice());
//                f1.setBookingClassName(bc.getSeatClass());
//                f1.setOrigin(origin);
//                f1.setDestination(destination);
//                f1.setOriAirportName(originA.getAirportName());
//                f1.setOriAirportCode(originA.getAirportCode());
//                f1.setDesAirportName(destinationA.getAirportName());
//                f1.setDesAirportCode(destinationA.getAirportCode());
//                f1.setAircraftTailN(f.getAircraft().getAircraftType().getIATACode());
//                f1.setDepDayWE(simpleDateFormat2.format(f.getDepartureDate()));
//                f1.setDepTimeE(simpleDateFormat3.format(f.getDepartureDate()));
//                f1.setAriDayWE(simpleDateFormat2.format(f.getArrivalDate()));
//                f1.setAriTimeE(simpleDateFormat3.format(f.getArrivalDate()));
//                long diff = f.getArrivalDate().getTime() - f.getDepartureDate().getTime();
//                double diff1 = diff / (60 * 60 * 1000) % 24;
//
//                String timeD = df.format(diff1);
//                f1.setTimeDuration(timeD);
//
//                test.add(f1);
//            }
//        }
//
//        System.out.println("test" + test);
//
//        return test;
//    }
    
    /**
     * Retrieves representation of an instance of webservice.GenericResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of GenericResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }

}
