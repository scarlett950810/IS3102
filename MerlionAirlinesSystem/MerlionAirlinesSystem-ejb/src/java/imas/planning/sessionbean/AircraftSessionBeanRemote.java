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
import imas.planning.entity.RouteEntity;
import java.util.Date;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author wutong
 */
@Remote
public interface AircraftSessionBeanRemote {

    public List<String> getSeatClasses();

    public List<AircraftGroupEntity> getAircraftGroups();

    public List<AirportEntity> getAirports();

    public List<AircraftTypeEntity> getAircraftTypes();

    public void addAircraft(String tailId, AircraftTypeEntity aircraftType, Double purchasePrice, Double deprecation, Double netAssetValue,
            Double aircraftLife, Double operationYear, String conditionDescription, AirportEntity airportHub, AirportEntity currentAirport,
            int FirstClassColumnNo, int FirstClassRowNo, int BusinessClassColumnNo, int BusinessClassRowNo,
            int PremiumEconomyClassColumnNo, int PremiumEconomyClassRowNo, int EconomyClassColumnNo, int EconomyClassRowNo, Double turnaroundtime);

    public List<AircraftEntity> getAircrafts();

    public Boolean deleteAircraft(AircraftEntity aircraft);

    public boolean checkAircraftExistense(String tailId);

    public void updateAircraft(AircraftEntity aircraftUpdated);

    List<AircraftEntity> retrieveAvailableAircrafts(RouteEntity route, Date departureDate, Date arrivalDate);
}
