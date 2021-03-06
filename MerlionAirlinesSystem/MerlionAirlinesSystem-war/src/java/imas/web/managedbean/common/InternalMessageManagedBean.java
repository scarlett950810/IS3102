/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.web.managedbean.common;

import imas.common.entity.InternalMessageEntity;
import imas.common.entity.StaffEntity;
import imas.common.sessionbean.InternalMessageSessionBeanLocal;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.persistence.PostRemove;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Scarlett
 */
@Named(value = "internalMessageManagedBean")
@ViewScoped
public class InternalMessageManagedBean implements Serializable {

    @EJB
    private InternalMessageSessionBeanLocal internalMessageSessionBean;

    private StaffEntity loggedInStaff;
    private StaffEntity receiver;
    private String content;
    private StaffEntity staffToReply;
    private String replyContent;
    private List<StaffEntity> staffList;
    private List<InternalMessageEntity> allMessages;

    /**
     * Creates a new instance of InternalMessageManagedBean
     */
    public InternalMessageManagedBean() {
    }

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        String staffNo = (String) ec.getSessionMap().get("staffNo");

        loggedInStaff = internalMessageSessionBean.getStaffEntityByStaffNo(staffNo);

        staffList = internalMessageSessionBean.getAllStaff();
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("staffList", staffList);
        allMessages = internalMessageSessionBean.getAllMessages(loggedInStaff);
    }

    @PostRemove
    public void destroy() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("staffList");
    }

    public List<StaffEntity> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<StaffEntity> staffList) {
        this.staffList = staffList;
    }

    public StaffEntity getLoggedInStaff() {
        return loggedInStaff;
    }

    public void setLoggedInStaff(StaffEntity loggedInStaff) {
        this.loggedInStaff = loggedInStaff;
    }

    public StaffEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(StaffEntity receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<InternalMessageEntity> getAllMessages() {
        return this.allMessages;
    }

    public void setAllMessages(List<InternalMessageEntity> allMessages) {
        this.allMessages = allMessages;
    }

    public StaffEntity getStaffToReply() {
        return staffToReply;
    }

    public void setStaffToReply(StaffEntity staffToReply) {
        this.staffToReply = staffToReply;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public void send() {
        internalMessageSessionBean.sendMessage(loggedInStaff, receiver, content);
        allMessages = internalMessageSessionBean.getAllMessages(loggedInStaff);

    }

    public void toggleRead(InternalMessageEntity internalMessageEntity) {
        internalMessageSessionBean.toggleRead(internalMessageEntity);
    }

    public void refreshMessages(ActionEvent event) {
//        System.out.println("message refreshed!");
        allMessages = internalMessageSessionBean.getAllMessages(loggedInStaff);
        RequestContext.getCurrentInstance().execute("PF('message').show();");
    }

    public void showMessage(String sender, String content) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, sender, content);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public void reply(StaffEntity staff) {
        staffToReply = staff;
        RequestContext.getCurrentInstance().execute("PF('reply').show();");
        System.out.println(staffToReply.getDisplayName());
    }
    public void sendReply() {
        internalMessageSessionBean.sendMessage(loggedInStaff, staffToReply, replyContent);
        FacesMessage msg = new FacesMessage("Successful", "You have replied " + staffToReply.getDisplayName() + ".");
        FacesContext.getCurrentInstance().addMessage("reply", msg);
        RequestContext.getCurrentInstance().execute("PF('reply').hide();");
    }

}
