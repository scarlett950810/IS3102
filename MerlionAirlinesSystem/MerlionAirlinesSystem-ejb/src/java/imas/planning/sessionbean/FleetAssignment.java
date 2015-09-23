/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.planning.sessionbean;

import imas.planning.entity.AircraftEntity;
import imas.planning.entity.AirportEntity;
import imas.planning.entity.FlightEntity;
import imas.planning.entity.MaintenanceScheduleEntity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author ruicai
 */
@Stateful
public class FleetAssignment implements FleetAssignmentLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Date> getAllPlanningPeriodStartDateByYear() {
        Query q = em.createQuery("SELECT DISTINCT a.operatingYear FROM FlightEntity a");
        List<Integer> ops = (List<Integer>) q.getResultList();

        List<Date> result = new ArrayList<Date>();
        for (Integer i : ops) {
            System.err.println("operatingYear" + i);
            q = em.createQuery("SELECT a FROM FlightEntity a Where a.operatingYear = :year ORDER BY a.departureDate");
            q.setParameter("year", i);
            List<FlightEntity> fs = (List<FlightEntity>) q.getResultList();
            FlightEntity f = fs.get(0);
            result.add(f.getDepartureDate());
        }
        return result;
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    @Override
    public List<FlightEntity> getAllFlightsWithinPlanningPeriod(Date startingDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startingDate);
        cal.add(Calendar.YEAR, 1);
        Date endingDate = cal.getTime();
        Query q = em.createQuery("SELECT a FROM FlightEntity a WHERE a.departureDate >= :startingDate AND a.arrivalDate <= :endingDate");
        q.setParameter("startingDate", startingDate);
        q.setParameter("endingDate", endingDate);
        return (List<FlightEntity>) q.getResultList();
    }

    @Override
    public List<FlightEntity> fleetAssignment(List<FlightEntity> flights, List<AircraftEntity> aircrafts) {
        System.err.println("enter fleetAssignment");
        for (AircraftEntity a : aircrafts) {
            if (!flights.isEmpty()) {
                flights = oneAircraftAssignment(a, flights);
                System.err.println("finsh one fleetassignment");
            } else {
                break;
            }
        }
        System.err.println("finish fleetA"+flights.size());
        return flights;
        

    }

    @Override
    public List<FlightEntity> oneAircraftAssignment(AircraftEntity aircraft, List<FlightEntity> flightsAll) {
        List<FlightEntity> flightsAvai = new ArrayList<FlightEntity>();
         System.err.println("enter one aircraftAssignment");
         
        for (FlightEntity f : flightsAll) {
            if (aircraft.getAircraftType().getAircraftRange() > f.getRoute().getDistance()) {
                flightsAvai.add(f);
            }
        }
        //find the available flights based on aircraf capability  
        if (flightsAvai.isEmpty()) {
            return flightsAll;
        }
        System.err.println("1");
        Date earliestDep = flightsAvai.get(0).getDepartureDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(earliestDep);
        cal.add(Calendar.YEAR, 1);
        earliestDep = cal.getTime();
        System.err.println("2 earliest Dep"+earliestDep);

        //Adjust the date one year later 
        boolean hasHubOrNot = false;
        FlightEntity earliestFlight = new FlightEntity();
        for (FlightEntity f : flightsAvai) {
            Date depTemp = f.getDepartureDate();
            if (depTemp.compareTo(earliestDep) < 0 && aircraft.getAirportHub().getAirportCode().equals(f.getRoute().getOriginAirport().getAirportCode())) {
                //Find the earliest flight which departs at the aircraft's hub
                earliestFlight = em.find(FlightEntity.class, f.getId());
                earliestDep = f.getDepartureDate();
                hasHubOrNot = true;
                
            }

        }
        System.err.println("3 + earliest flight"+earliestFlight.getDepartureDate());

        if (!hasHubOrNot) {
            System.err.println("all flights do not departure at the aircraft's hub");
            return flightsAll;

        }
        
        if(aircraft.getFlights()!=null){
        aircraft.getFlights().add(earliestFlight);
        
        }
        else {
            aircraft.setFlights(new ArrayList<FlightEntity>());
            aircraft.getFlights().add(earliestFlight);
        }
        earliestFlight.setAircraftFlight(aircraft);
        earliestDep = earliestFlight.getArrivalDate();
        System.err.println("4");
            
        flightsAvai.remove(earliestFlight);
        //assign the first flight
        FlightEntity flightAssigned = earliestFlight;
        boolean findNextFlight = true;
        AirportEntity currentLoc = flightAssigned.getRoute().getDestinationAirport();
        System.err.println("5");
        Date mtAcc = flightAssigned.getDepartureDate();
        System.err.println("mtacc before while"+mtAcc);
        while (findNextFlight) {
        System.err.println("5.1");

            cal.setTime(earliestDep);
            cal.add(Calendar.MINUTE, (int) (aircraft.getTurnAroundTime() + 0.5d));

            earliestDep = cal.getTime();
        System.err.println("6");
            System.err.println("earliestDep"+earliestDep);
            findNextFlight = false;
           Date findSoonest = null;   
           for (FlightEntity f : flightsAvai) {
                
                if (currentLoc.equals(aircraft.getAirportHub())) {
                    if (f.getRoute().getOriginAirport().getAirportCode().equals(currentLoc.getAirportCode()) && f.getDepartureDate().compareTo(earliestDep) > 0 ) {
                        findSoonest = f.getDepartureDate();
                        flightAssigned = em.find(FlightEntity.class, f.getId());
                    }
                } else if (f.getRoute().getDestinationAirport().getAirportCode().equals(aircraft.getAirportHub().getAirportCode()) && f.getRoute().getOriginAirport().getAirportCode().equals(currentLoc.getAirportCode()) && f.getDepartureDate().compareTo(earliestDep) > 0) {
                    findSoonest = f.getDepartureDate();
                    flightAssigned = em.find(FlightEntity.class, f.getId());
                }

            }
           //find a suitable flight
           if(findSoonest == null){
               findNextFlight = false;
           }else{
              
            for (FlightEntity f : flightsAvai) {
                
                if (currentLoc.equals(aircraft.getAirportHub())) {
                    if (f.getRoute().getOriginAirport().getAirportCode().equals(currentLoc.getAirportCode()) && f.getDepartureDate().compareTo(earliestDep) > 0 && f.getDepartureDate().compareTo(findSoonest) < 0 ) {
                        flightAssigned = em.find(FlightEntity.class, f.getId());
                        findSoonest = f.getDepartureDate();
                        findNextFlight = true;
                    }
                } else if (f.getRoute().getDestinationAirport().getAirportCode().equals(aircraft.getAirportHub().getAirportCode()) && f.getRoute().getOriginAirport().getAirportCode().equals(currentLoc.getAirportCode()) && f.getDepartureDate().compareTo(earliestDep) > 0&& f.getDepartureDate().compareTo(findSoonest) < 0 ) {
                    flightAssigned = em.find(FlightEntity.class, f.getId());
                    findSoonest = f.getDepartureDate();
                    findNextFlight = true;
                }

            }
           }
            //find the nearest flight
         System.err.println("7");
           
            Double flyingHoursAC = calculateMaintenanceHours(aircraft, mtAcc);
          System.err.println("7.1 flyingHours"+flyingHoursAC);

            if(findNextFlight){
                System.err.println("findNextFlight is true");
            if (flyingHoursAC + flightAssigned.getRoute().getFlightHours() >= 125.0) {
                System.err.println("flightAssigned FLIGHT hours"+flightAssigned.getRoute().getFlightHours());
                MaintenanceScheduleEntity maintenanceSchedule = new MaintenanceScheduleEntity();
                maintenanceSchedule.setMaintenanceType("A");
                maintenanceSchedule.setStartingTime(earliestDep);
                cal.setTime(earliestDep);
                cal.add(Calendar.HOUR, (int) (aircraft.getAircraftType().getMaintenanceHoursRequiredACheck() + 0.5d));
                System.err.println("maintenance hours requireed:"+aircraft.getAircraftType().getMaintenanceHoursRequiredACheck()+" sishewuru"+(int) (aircraft.getAircraftType().getMaintenanceHoursRequiredACheck() + 0.5d));
                earliestDep = cal.getTime();
                maintenanceSchedule.setEndingTime(earliestDep);
                if (aircraft.getMaintenances() != null) {
                    aircraft.getMaintenances().add(maintenanceSchedule);
                } else {
                    aircraft.setMaintenances(new ArrayList<MaintenanceScheduleEntity>());
                    aircraft.getMaintenances().add(maintenanceSchedule);
                }
                maintenanceSchedule.setAircraft(aircraft);
                em.merge(maintenanceSchedule);
                em.merge(aircraft);

                currentLoc = aircraft.getAirportHub();
                mtAcc = earliestDep;
                        System.err.println("7.2");

            } else {
                aircraft.getFlights().add(flightAssigned);
                flightAssigned.setAircraftFlight(aircraft);
                flightsAvai.remove(flightAssigned);
                em.merge(aircraft);

                earliestDep = flightAssigned.getArrivalDate(); // later can change to calculate 
                currentLoc = flightAssigned.getRoute().getDestinationAirport();
                        System.err.println("7.2.1");

            }
            }
             System.err.println("8");
      
        }

        em.merge(aircraft);
        return flightsAvai;
        //route with higher demand operate with larger aircraft
        //longer distance larger aircraft
        //maintenance time 
        //remember to destroy the flights list after one year planning
        //B Check
        //before executing the function need to have maintenance hours required
    }

    @Override
    public Double calculateMaintenanceHours(AircraftEntity aircraft, Date mtAcc) {
        System.err.println("date mtacc"+ mtAcc);
        Double flyingHours = 0.0;
        List<FlightEntity> fL = aircraft.getFlights();
        for (FlightEntity f : fL) {
            if(f.getDepartureDate().compareTo(mtAcc)>0){
            flyingHours = flyingHours + f.getRoute().getFlightHours();
            }
        }
        return flyingHours;
    }

}
