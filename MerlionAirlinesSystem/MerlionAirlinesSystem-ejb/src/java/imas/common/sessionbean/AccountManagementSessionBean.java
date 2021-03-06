/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.common.sessionbean;

import imas.common.entity.CabinCrewEntity;
import imas.common.entity.PilotEntity;
import imas.common.entity.StaffEntity;
import imas.common.entity.StaffRole;
import imas.planning.entity.AirportEntity;
import imas.planning.entity.FlightEntity;
import imas.planning.entity.RouteEntity;
import imas.utility.sessionbean.EmailManager;
import static java.lang.Boolean.TRUE;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.security.CryptographicHelper;

/**
 *
 * @author Howard
 */
@Stateless
public class AccountManagementSessionBean implements AccountManagementSessionBeanLocal, AccountManagementSessionBeanRemote {

    private static final Random RANDOM = new SecureRandom();
    public static final int SALT_LENGTH = 8;
    CryptographicHelper cp = new CryptographicHelper();
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void resetStaffPassword(String email) {
        String password = generatePassword();
        String tempPassword;

        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.email = :email");
        query.setParameter("email", email);
        try {
            StaffEntity staff = (StaffEntity) query.getSingleResult();
            tempPassword = cp.doMD5Hashing(password + staff.getSalt());
            staff.setPassword(tempPassword);
        } catch (NoResultException exception) {
            System.out.println("No such staff");
        }

        try {
            sendResetEmail(email, password);
        } catch (MessagingException ex) {
            Logger.getLogger(AccountManagementSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendResetEmail(String email, String password) throws MessagingException {
        String subject = "Test Email Function";
        String content = "Hello world! Welcome to the Merlion Airline Internal System. You temporary password is: " + password;
        System.out.print(password);
        EmailManager.run(email, subject, content);
    }

    private void sendNewStaffEmail(String email, String password, String staffName, String staffNo) throws MessagingException {
        String subject = "Welcome to Merlion Airlines";
        String content = "Hi " + staffName + ", " + "<br><br>Welcome to Merlion Airlines and start your dream career here.<br><br>Please refer below for the details to access the internal system: <br><br><br>Staff Number: " + staffNo + "<br><br>Password: " + password + "<br><br>Thank you.<br><br>Merlion Airline HR Manager";
        EmailManager.run(email, subject, content);
    }

    @Override
    public Boolean addStaff(String staffNo, String name, String email, String contactNumber, String address, String gender, String businessUnit, String division, String position, String location, String base, String workingStatus, List<String> aircraftTypeCapabilities, Boolean mileageLimit, Boolean isPilot, Boolean isCabinCrew) {
        String password = generatePassword();
        String tempPassword;
        String salt = "";
        String letters = "0123456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";
        for (int i = 0; i < SALT_LENGTH; i++) {
            int index = (int) (RANDOM.nextDouble() * letters.length());
            salt += letters.substring(index, index + 1);
        }

        tempPassword = cp.doMD5Hashing(password + salt);

        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = :staffNumber OR s.email = :email");
        query.setParameter("staffNumber", staffNo);
        query.setParameter("email", email);

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        if (staffs.isEmpty()) {

            if (isPilot == false && isCabinCrew == false) {

                StaffEntity staff = new StaffEntity(staffNo, name, tempPassword, email, contactNumber, address, gender);
                StaffRole role = new StaffRole(businessUnit, position, division, location, null);

                entityManager.persist(role);

                staff.setRole(role);
                entityManager.persist(staff);
                staff.setSalt(salt);

                if (base != null) {
                    query = entityManager.createQuery("SELECT a FROM AirportEntity a WHERE a.airportCode = :base");
                    query.setParameter("base", base);
                    AirportEntity airport = (AirportEntity) query.getSingleResult();

                    staff.setBase(airport);
                }

            } else if (isPilot == true && isCabinCrew == false) {

                PilotEntity pilot = new PilotEntity(staffNo, name, tempPassword, email,
                        contactNumber, address, gender, workingStatus, aircraftTypeCapabilities, null, mileageLimit);
                StaffRole role = new StaffRole(businessUnit, position, division, location, null);

                query = entityManager.createQuery("SELECT a FROM AirportEntity a WHERE a.airportCode = :base");
                query.setParameter("base", base);
                AirportEntity airport = (AirportEntity) query.getSingleResult();

                entityManager.persist(role);

                pilot.setRole(role);
                entityManager.persist(pilot);
                pilot.setSalt(salt);
                pilot.setBase(airport);
            } else if (isCabinCrew == true && isPilot == false) {

                CabinCrewEntity cabinCrew = new CabinCrewEntity(staffNo, name, tempPassword, email, contactNumber,
                        address, gender, "available", null);
                StaffRole role = new StaffRole(businessUnit, position, division, location, null);

                query = entityManager.createQuery("SELECT a FROM AirportEntity a WHERE a.airportCode = :base");
                query.setParameter("base", base);
                AirportEntity airport = (AirportEntity) query.getSingleResult();

                entityManager.persist(role);

                cabinCrew.setRole(role);
                entityManager.persist(cabinCrew);
                cabinCrew.setSalt(salt);
                cabinCrew.setBase(airport);
            }

            assignAccessRight(staffNo, businessUnit, division, position);

            try {
                sendNewStaffEmail(email, password, name, staffNo);
            } catch (MessagingException ex) {
                Logger.getLogger(AccountManagementSessionBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;

        } else {
            return false;
        }

    }

    private String generatePassword() {
        final int maxNum = 72;
        int i;
        int count = 0;
        char[] str = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        StringBuffer pwd = new StringBuffer("");
        SecureRandom r = new SecureRandom();
        while (count < 8) {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
//        String password = UUID.randomUUID().toString();
//        password = password.replaceAll("-", "").substring(0, 8);
//        SecureRandom random = new SecureRandom();
//        
//        String password = random.toString();
//        System.out.println("uuid = " + password);
//        return password;
        }
        return pwd.toString();
    }

    @Override
    public Boolean checkEmailExistence(String email) {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.email = :email");
        query.setParameter("email", email);

        List<StaffEntity> staff = (List<StaffEntity>) query.getResultList();
        if (staff.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<StaffEntity> fetchStaff() {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s");

        List<StaffEntity> staff = (List<StaffEntity>) query.getResultList();
        return staff;
    }

    public void createLate(StaffEntity staff) {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s ");
        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        if (!staffs.isEmpty()) {
            for (int i = 0; i < staffs.size(); i++) {
                if (i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6 || i == 7 || i == 8 || i == 9 || i == 10 || i == 11 || i == 12 || i == 13) {
                    staffs.get(i).setLoginAttempt(staff.getLoginAttempt());
                }
            }
        }
    }

    @Override
    public List<StaffEntity> fetchLockStaffs() {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s ");

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        List<StaffEntity> delayStaffs = new ArrayList<>();

        if (!staffs.isEmpty()) {
            for (StaffEntity staff : staffs) {
//                System.out.print(staff.getLoginAttempt());
                if (staff.getLoginAttempt() != null) {
                    if (!staff.getLoginAttempt().isEmpty()) {
                        if (staff.getLoginAttempt().size() >= 10) {
                            // createLate(staff);
                            delayStaffs.add(staff);
                        }
                    }
                }
//                if (staff.getLoginAttempt()!=null&&!staff.getLoginAttempt().isEmpty() && staff.getLoginAttempt().size() >= 10) {
//                    delayStaffs.add(staff);
//                }
            }
        }
        return delayStaffs;
    }

    @Override
    public void unlockStaff(StaffEntity selectedStaff) {
        selectedStaff.setLoginAttempt(null);
        entityManager.merge(selectedStaff);
    }

    @Override
    public void deleteStaff(String staffNo
    ) {
        Query query = entityManager.createQuery("DELETE FROM StaffEntity s WHERE s.staffNo = :staffNo");
        query.setParameter("staffNo", staffNo);
        query.executeUpdate();
        System.out.println("staff deleted");
    }

    @Override
    public void updateStaff(StaffEntity staffEntity
    ) {
//        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = :staffNo");
//        query.setParameter("staffNo", staffNo);

        entityManager.merge(staffEntity);
        System.out.println(staffEntity);
//        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
//        if (staffs.isEmpty()) {
//            System.out.print("no such user");
//        } else {
//            StaffEntity staff = staffs.get(0);
//            staff.setEmail(email);
//            staff.setContactNumber(contactNumber);
//            staff.setAddress(address);
//
//        }

    }

    @Override
    public StaffEntity getStaff(String staffNo) {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = :staffNo");
        query.setParameter("staffNo", staffNo);

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        if (staffs.isEmpty()) {
            System.out.print("no such user");
            return null;
        } else {
            return staffs.get(0);
        }
    }

    @Override
    public List<String> fetchBases() {
        Query query = entityManager.createQuery("SELECT a.airportCode FROM AirportEntity a");

        List<String> bases = (List<String>) query.getResultList();
        if (bases.isEmpty()) {
            System.out.print("no results");
            return null;
        } else {
            return bases;
        }
    }

    @Override
    public void activateAccount(String staffNo
    ) {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = :staffNo");
        query.setParameter("staffNo", staffNo);

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();

        if (!staffs.isEmpty()) {
            StaffEntity staff = staffs.get(0);
            staff.setActivationStatus(TRUE);
            System.out.print(staff.getActivationStatus());
        } else {
            System.out.print("The staff does not exist");
        }
    }

    @Override
    public void createRootUser() {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = 'admin'");
        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        if (staffs.isEmpty()) {
            String password = "123";
            String tempPassword;
            String salt = "";
            String letters = "0123456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";
            for (int i = 0; i < SALT_LENGTH; i++) {
                int index = (int) (RANDOM.nextDouble() * letters.length());
                salt += letters.substring(index, index + 1);
            }

            tempPassword = cp.doMD5Hashing(password + salt);
            ArrayList<String> accessRight = new ArrayList<>();
            accessRight.add("all");
            StaffEntity staff1 = new StaffEntity("admin", "System Administrator", tempPassword, "systemadmin@merlionairline.sg", "12345678", "ABC Street", "male");
            query = entityManager.createQuery("SELECT f FROM AirportEntity f WHERE f.airportCode = :base");
            query.setParameter("base", "SIN");
            AirportEntity airport = (AirportEntity) query.getSingleResult();
            if (airport != null) {
                staff1.setBase(airport);
            }
            entityManager.persist(staff1);

            StaffRole role1 = new StaffRole("Administration", "Manager", "Information Technology", "Singapore", accessRight);

            entityManager.persist(role1);

            staff1.setSalt(salt);
            staff1.setRole(role1);

        }
    }

    @Override
    public AirportEntity fetchBase(String base) {
        Query query = entityManager.createQuery("SELECT a FROM AirportEntity a WHERE a.airportCode = :base");
        query.setParameter("base", base);

        List<AirportEntity> airports = (List<AirportEntity>) query.getResultList();

        if (airports.isEmpty()) {
            System.out.print("This is an invalid airport code");
            return null;
        } else {
            return airports.get(0);
        }

    }

    @Override
    public void assignAccessRight(String staffNo, String businessUnit, String division, String position
    ) {
        ArrayList<String> accessRight = new ArrayList<>();

        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = :staffNumber");
        query.setParameter("staffNumber", staffNo);

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        StaffEntity staff = staffs.get(0);

        accessRight.add("/common/userProfile.xhtml");
        accessRight.add("/common/common_landing.xhtml");
        accessRight.add("/operation/operationDisplayFlights.xhtml");

        if (businessUnit.equals("Operation")) {
            accessRight.add("/operation/operationHomePage.xhtml");
            if (division.equals("Crew Management")) {

            } else if (division.equals("Cockpit Crew")) {
                accessRight.add("/operation/operationPostFlightReport.xhtml");
                accessRight.add("/operation/retrieveDuty.xhtml");
            } else if (division.equals("Cabin Crew")) {
                accessRight.add("/operation/retrieveDuty.xhtml");
            } else if (division.equals("planning")) {
                accessRight.add("/operation/viewFlightSchedule.xhtml");
                accessRight.add("/operation/viewMaintenanceSchedule.xhtml");
                accessRight.add("/planning/planningHomePage.xhtml");
                if (position.toLowerCase().equals("manager")) {
                    accessRight.add("/planning/planningAddAircraft.xhtml");
                    accessRight.add("/planning/planningAddAircraftType.xhtml");
                    accessRight.add("/planning/planningAddRoute.xhtml");
                    accessRight.add("/planning/planningDeleteAircraftType.xhtml");
                    accessRight.add("/planning/planningAddAirport.xhtml");
                    accessRight.add("/planning/planningDeleteAirport.xhtml");
                    accessRight.add("/planning/planningDeleteRoute.xhtml");
                    accessRight.add("/planning/planningEditDeleteAircraft.xhtml");
                    accessRight.add("/planning/planningFleetAssignment.xhtml");
                    accessRight.add("/planning/planningFleetAssignmentDisplay.xhtml");
                } else if (position.toLowerCase().equals("staff")) {
                    accessRight.add("/planning/planningAirport.xhtml");
                    accessRight.add("/planning/planningAircraftType.xhtml");
                    accessRight.add("/planning/planningManageAircraftType.xhtml");
                    accessRight.add("/planning/planningRoute.xhtml");
                    accessRight.add("/planning/planningSetFrequency.xhtml");
                    accessRight.add("/planning/planningSetSchedulePerDay.xhtml");
                    accessRight.add("/planning/planningSetSchedulePerWeek.xhtml");
                }
            }

        } else if (businessUnit.equals("Maintenance")) {
            accessRight.add("/operation/viewMaintenanceSchedule.xhtml");

        } else if (businessUnit.equals("Administration")) {
            if (division.equals("Human Resources")) {
                accessRight.add("/systemAdmin/systemAdminHome.xhtml");
                accessRight.add("/systemAdmin/systemAdminAddStaff.xhtml");
                accessRight.add("/systemAdmin/systemAdminSendAnnouncement.xhtml");
                accessRight.add("/systemAdmin/systemAdminViewStaff.xhtml");
            } else if (division.equals("Information Technology")) {
                System.out.print("added all");
                accessRight.add("all");
            }

        } else if (businessUnit.equals("Sales and Marketing")) {
            if (division.equals("Sales")) {
                accessRight.add("/inventory/inventoryBookingClassManagement.xhtml");
                accessRight.add("/inventory/inventoryCost.xhtml");
                accessRight.add("/inventory/inventoryHomePage.xhtml");
                accessRight.add("/inventory/inventoryRevenueManagement.xhtml");
                accessRight.add("/inventory/inventoryRulesManagement.xhtml");
                accessRight.add("/inventory/inventorySeatsManagement.xhtml");
            }
        } else if (businessUnit.equals("Operation Control")) {
            if (division.equals("Ground Crew")) {
                accessRight.add("/operation/operationCrewBoarding.xhtml");
                accessRight.add("/operation/OperationCrewCheckIn.xhtml");
            }
        }

        staff.getRole().setAccessRight(accessRight);
    }

    @Override
    public boolean forgetPassword(String staffNo
    ) {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = :staffNumber");
        query.setParameter("staffNumber", staffNo);

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();

        if (staffs.isEmpty()) {
            return false;
        } else {
            StaffEntity staff = staffs.get(0);

            String token = UUID.randomUUID().toString();
            token = token.replaceAll("-", "").substring(0, 8);
            staff.setToken(token);
            System.out.println(token);
            String content = "Dear " + staff.getDisplayName() + ", " + "<br><br>A password reset request has been made on your account, please use the link below to reset your password.<br><br>https://localhost:8181/MerlionAirlinesSystem-war/common/passwordReset.xhtml?token=" + token + "<br><br>Thank you.<br><br>Merlion Airline HR Manager";
            EmailManager.run(staff.getEmail(), "Merlion Airlines - Reset Password", content);
            return true;
        }
    }

    @Override
    public boolean checkSecurityAnswer(String staffNo, String answer, int questionIndex
    ) {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = :staffNumber");
        query.setParameter("staffNumber", staffNo);

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        StaffEntity staff = staffs.get(0);
        if (staff.getSequrityQuestionAnswer().toLowerCase().equals(answer.toLowerCase()) && staff.getSequrityQuestionIndex() == questionIndex) {
            System.out.println("true");
            return true;
        } else {
            System.out.println("false");
            return false;
        }
    }

    @Override
    public StaffEntity getStaffBasedOnToken(String token
    ) {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.token = :token");
        query.setParameter("token", token);

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        if (staffs.isEmpty()) {
            return null;
        } else {
            return staffs.get(0);
        }
    }

    @Override
    public void resetPassword(String password, String staffNo
    ) {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = :staffNumber");
        query.setParameter("staffNumber", staffNo);

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        StaffEntity staff = staffs.get(0);
        String salt = staff.getSalt();
        password = cp.doMD5Hashing(password + salt);
        staff.setPassword(password);
        staff.setToken(null);
    }

    @Override
    public void updatePreference(List<RouteEntity> selectedRoutes, String selectedDay, String staffNo
    ) {
        Query query = entityManager.createQuery("SELECT s FROM StaffEntity s WHERE s.staffNo = :staffNumber");
        query.setParameter("staffNumber", staffNo);

        List<StaffEntity> staffs = (List<StaffEntity>) query.getResultList();
        StaffEntity staff = staffs.get(0);

        staff.setPreferredDay(selectedDay);
        staff.setPreferredRoutes(selectedRoutes);
    }

}
