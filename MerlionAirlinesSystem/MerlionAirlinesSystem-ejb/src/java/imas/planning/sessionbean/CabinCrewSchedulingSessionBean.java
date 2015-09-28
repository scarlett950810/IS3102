/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.planning.sessionbean;

import imas.planning.entity.AircraftEntity;
import imas.planning.entity.FlightEntity;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author wutong
 */
@Stateless
public class CabinCrewSchedulingSessionBean implements CabinCrewSchedulingSessionBeanLocal {
    
    @PersistenceContext
    private EntityManager em;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    @Override
    public Integer getFlightCapacity(FlightEntity flight) {
        AircraftEntity a  = flight.getAircraft();
        Query q1 = em.createQuery("SELECT s FROM SeatEntity s WHERE s.aircraft = :aircraft AND s.seatClass = :seatClass");
        q1.setParameter("aircraft", a);
        q1.setParameter("seatClass", "First Class");
        Integer n1 = q1.getResultList().size();
        
        return null;
    }
}