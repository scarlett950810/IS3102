/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emas.web.managedbean.CRM;

import imas.crm.entity.MemberEntity;
import imas.crm.sessionbean.CustomerAccountManagementSessionBeanLocal;
import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.persistence.Temporal;
import util.security.CryptographicHelper;

/**
 *
 * @author wutong
 */
@Named(value = "addMemberAccountManagedBean")
@ViewScoped
public class AddMemberAccountManagedBean implements Serializable {

    private static final Random RANDOM = new SecureRandom();
    CryptographicHelper cp = new CryptographicHelper();
    public static final int SALT_LENGTH = 8;
    private String title;
    private String lastName;
    private String firstName;
    private String gender;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date birthdate;
    private String nationality;
    private String email;
    private String confirmEmail;
    private String contactNumber;
    private String passportNumber;
    private String pin;
    private String confirmPin;
    private int securityQuestionIndex;
    private String answer;
    private MemberEntity member;
    @EJB
    private CustomerAccountManagementSessionBeanLocal customerAccountManagementSession;

    /**
     * Creates a new instance of AddMemberAccountManagedBean
     */
    public AddMemberAccountManagedBean() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail) {
        this.confirmEmail = confirmEmail;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getConfirmPin() {
        return confirmPin;
    }

    public void setConfirmPin(String confirmPin) {
        this.confirmPin = confirmPin;
    }

    public int getSecurityQuestionIndex() {
        return securityQuestionIndex;
    }

    public void setSecurityQuestionIndex(int securityQuestionIndex) {
        this.securityQuestionIndex = securityQuestionIndex;
    }

    public MemberEntity getMember() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public void create() throws IOException {
        System.err.println("Enter create account page");
        FacesMessage msg;
        if (!email.equals(confirmEmail)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Please input the same e-mail address twice");

        } else if (!pin.equals(confirmPin)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Please input the same pin number twice");

        } else {
            
            
            member = new MemberEntity();
            member.setTitle(title);
            member.setLastName(lastName);
            member.setFirstName(firstName);
            member.setEmail(email);
            member.setGender(gender);
            member.setNationality(nationality);
            member.setSequrityQuestionIndex(securityQuestionIndex);
            member.setSequrityQuestionanswer(answer);
            member.setBirthDate(birthdate);
            String salt = "";
            String letters = "0123456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";
            for (int i = 0; i < SALT_LENGTH; i++) {
                int index = (int) (RANDOM.nextDouble() * letters.length());
                salt += letters.substring(index, index + 1);
            }
            member.setSalt(salt);
            System.out.print(salt);
            pin = cp.doMD5Hashing(pin + salt);
            System.out.print(pin);
            member.setPinNumber(pin);
            member.setContactNumber(contactNumber);
            member.setPassportNumber(passportNumber);
            Date today = new Date();
            long diff = today.getTime() - birthdate.getTime();
            long diffYears = diff / (24 * 60 * 60 * 1000 * 365);
            System.err.println("Long diff: "+ diff + "Long diffYears" + diffYears);
            int age = (int) diffYears;
            member.setAge(age);
            customerAccountManagementSession.createCustomer(member);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Added successfully", "");

            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();
            ec.redirect("crmConfirmAccountCreation.xhtml");
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);

    }

    public void goLogin() throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        ec.redirect("crmCustomerLogin.xhtml");
    }

}
