                                                                                                              /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.planning.sessionbean;

import imas.planning.entity.AircraftEntity;
import imas.planning.entity.AircraftGroupEntity;
import imas.planning.entity.AircraftTypeEntity;
import imas.planning.entity.AirportEntity;
import imas.planning.entity.FlightEntity;
import imas.planning.entity.MaintenanceScheduleEntity;
import imas.planning.entity.RouteEntity;
import imas.planning.entity.SeatEntity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Scarlett
 */
@Stateless
public class AircraftSessionBean implements AircraftSessionBeanLocal, AircraftSessionBeanRemote{

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<AircraftGroupEntity> getAircraftGroups() {
        Query query = em.createQuery("SELECT a FROM AircraftGroupEntity a");
        List<AircraftGroupEntity> aircraftGroups = (List<AircraftGroupEntity>) query.getResultList();
        return aircraftGroups;
    }

    @Override
    public List<AirportEntity> getAirports() {
        Query query = em.createQuery("SELECT a FROM AirportEntity a");
        List<AirportEntity> airports = (List<AirportEntity>) query.getResultList();
        return airports;
    }

    @Override
    public List<String> getSeatClasses() {
        List<String> seatClasses = new ArrayList();
        seatClasses.add("First Class");
        seatClasses.add("Business Class");
        seatClasses.add("Premuim Economy Class");
        seatClasses.add("Economy Class");

        return seatClasses;
    }

    @Override
    public List<AircraftTypeEntity> getAircraftTypes() {
        Query query = em.createQuery("SELECT a FROM AircraftTypeEntity a");
        List<AircraftTypeEntity> aircraftTypes = (List<AircraftTypeEntity>) query.getResultList();
        return aircraftTypes;
    }

    @Override
    public boolean checkAircraftExistense(String tailId) {
        Query q = em.createQuery("SELECT a FROM AircraftEntity a WHERE a.tailId = :tailId");
        q.setParameter("tailId", tailId);
        return !q.getResultList().isEmpty();
    }

    @Override
    public void addAircraft(String tailId, AircraftTypeEntity aircraftType, Double purchasePrice, Double deprecation, Double netAssetValue,
            Double aircraftLife, Double operationYear, String conditionDescription, AirportEntity airportHub, AirportEntity currentAirport, int FirstClassColumnNo, int FirstClassRowNo, int BusinessClassColumnNo, int BusinessClassRowNo,
            int PremiumEconomyClassColumnNo, int PremiumEconomyClassRowNo, int EconomyClassColumnNo, int EconomyClassRowNo, Double turnaroundtime) {

        AircraftEntity newAircraft = new AircraftEntity(tailId, aircraftType, purchasePrice, deprecation, netAssetValue, aircraftLife, operationYear, conditionDescription, airportHub, currentAirport, turnaroundtime);

        em.persist(newAircraft);
        int startRow = 1;
        createSeats(newAircraft, FirstClassColumnNo, startRow, FirstClassRowNo, "First Class");
        startRow = startRow + FirstClassRowNo;
        createSeats(newAircraft, BusinessClassColumnNo, startRow, BusinessClassRowNo, "Business Class");
        startRow = startRow + BusinessClassRowNo;
        createSeats(newAircraft, PremiumEconomyClassColumnNo, startRow, PremiumEconomyClassRowNo, "Premium Economy Class");
        startRow = startRow + PremiumEconomyClassRowNo;
        createSeats(newAircraft, EconomyClassColumnNo, startRow, EconomyClassRowNo, "Economy Class");
//        System.out.println("seats created ");
    }

    private void createSeats(AircraftEntity aircraft, int column, int startRow, int row, String seatClass) {
        char endAlphabet = 'A';
        switch (column) {
            case 4:
                endAlphabet = 'D';
                break;
            case 6:
                endAlphabet = 'F';
                break;
            case 7:
                endAlphabet = 'G';
                break;
        }

        for (int i = startRow; i < startRow + row; i++) {
            for (char alphabet = 'A'; alphabet <= endAlphabet; alphabet++) {
                String seatNo = Integer.toString(i) + alphabet;
                SeatEntity newSeat = new SeatEntity(aircraft, seatNo, seatClass);
                em.persist(newSeat);
            }
        }
    }

    @Override
    public List<AircraftEntity> getAircrafts() {
        Query query = em.createQuery("SELECT a FROM AircraftEntity a");
        List<AircraftEntity> aircrafts = (List<AircraftEntity>) query.getResultList();
        return aircrafts;
    }

    @Override
    public Boolean deleteAircraft(AircraftEntity aircraft) {
//        System.out.println("debug");
        if (aircraft.getFlights().isEmpty() || aircraft.getFlights() == null) {

            AircraftEntity aircraftToDelete = em.find(AircraftEntity.class, aircraft.getId());
//            System.out.print("session bean: " + aircraftToDelete);
            em.remove(aircraftToDelete);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateAircraft(AircraftEntity aircraftUpdated) {
        System.out.println(aircraftUpdated.getConditionDescription());
        AircraftEntity aircraftEntityToUpdate = em.find(AircraftEntity.class, aircraftUpdated.getId());
        aircraftEntityToUpdate.setDeprecation(aircraftUpdated.getDeprecation());
        aircraftEntityToUpdate.setNetAssetValue(aircraftUpdated.getNetAssetValue());
        aircraftEntityToUpdate.setAircraftLife(aircraftUpdated.getAircraftLife());
        aircraftEntityToUpdate.setOperationYear(aircraftUpdated.getOperationYear());
        aircraftEntityToUpdate.setConditionDescription(aircraftUpdated.getConditionDescription());
        aircraftEntityToUpdate.setAirportHub(aircraftUpdated.getAirportHub());
        aircraftEntityToUpdate.setCurrentAirport(aircraftUpdated.getCurrentAirport());
        aircraftEntityToUpdate.setAircraftGroup(aircraftUpdated.getAircraftGroup());
    }

    @Override
    public List<AircraftEntity> retrieveAvailableAircrafts(RouteEntity route, Date departureDate, Date arrivalDate) {
        AirportEntity origin = route.getOriginAirport();
        AirportEntity destination = route.getDestinationAirport();
        List<AircraftEntity> aircrafts = getAircrafts();
        List<AircraftEntity> availableAircrafts = new ArrayList<>();
        Double hours = route.getFlightHours();
        Calendar cal = Calendar.getInstance();
        cal.setTime(departureDate);
        cal.add(Calendar.MINUTE, (int) (route.getFlightHours() * 60 * 2 + 60 + 0.5d));
        Date newArrivalDate = cal.getTime();
        for(AircraftEntity a: aircrafts){
            List<FlightEntity> flights = a.getFlights();
            if(!a.getAirportHub().equals(origin)){
                System.err.println(a+"not the correct hub");
                continue;
            }
            Boolean conflict = false;    
            for(FlightEntity f: flights){
                Date tempDeparture = f.getDepartureDate();
                Date tempArrival = f.getArrivalDate();
                
                if((tempDeparture.after(departureDate)&&tempDeparture.before(newArrivalDate))||(tempArrival.after(departureDate)&&tempArrival.before(newArrivalDate))||(tempDeparture.before(departureDate)&&tempArrival.after(newArrivalDate))){
                    conflict = true; 
                    break;
                }
                
            }
            List<MaintenanceScheduleEntity> maintenanceScheduleList = a.getMaintenances();
            for(MaintenanceScheduleEntity m: maintenanceScheduleList){
                Date mStart = m.getStartingTime();
                Date mEnd = m.getEndingTime();
                if((mStart.after(departureDate)&&mStart.before(newArrivalDate))||(mEnd.after(departureDate)&&mEnd.before(newArrivalDate))||(mStart.before(departureDate)&&mEnd.after(newArrivalDate))){
                    conflict = true;
                    break;
                }
            }
            if(!conflict)
                availableAircrafts.add(a);
        }
        
    
        return availableAircrafts;
    }

}
