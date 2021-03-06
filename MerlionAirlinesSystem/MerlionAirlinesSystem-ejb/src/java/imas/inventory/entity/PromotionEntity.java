/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imas.inventory.entity;

import imas.crm.entity.MemberEntity;
import imas.distribution.entity.TicketEntity;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;

/**
 *
 * @author Scarlett
 */
@Entity
public class PromotionEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String promoCode;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date startDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date endDate;
    private boolean discount;
    private double discountRate;
    private double waiveAmount;
    @OneToMany(mappedBy = "promotion")
    private List<TicketEntity> tickets;
    @ManyToMany(mappedBy = "promotionEntities")
    private List<MemberEntity> members;

    public PromotionEntity() {
    }

    public PromotionEntity createDiscountPromotion(String promoCode, Date startDate, Date enddate, double discountRate) {
        PromotionEntity p = new PromotionEntity();
        p.setPromoCode(promoCode);
        p.setStartDate(startDate);
        p.setEndDate(enddate);
        p.setDiscount(true);
        p.setDiscountRate(discountRate);
        return p;
    }
    
    public PromotionEntity createWaivePromotion(String promoCode, Date startDate, Date enddate, double waiveAmount) {
        PromotionEntity p = new PromotionEntity();
        p.setPromoCode(promoCode);
        p.setStartDate(startDate);
        p.setEndDate(enddate);
        p.setDiscount(false);
        p.setWaiveAmount(waiveAmount);
        return p;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isDiscount() {
        return discount;
    }

    public void setDiscount(boolean discount) {
        this.discount = discount;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getWaiveAmount() {
        return waiveAmount;
    }

    public void setWaiveAmount(double waiveAmount) {
        this.waiveAmount = waiveAmount;
    }

    public List<TicketEntity> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketEntity> tickets) {
        this.tickets = tickets;
    }

    public List<MemberEntity> getMembers() {
        return members;
    }

    public void setMembers(List<MemberEntity> members) {
        this.members = members;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PromotionEntity)) {
            return false;
        }
        PromotionEntity other = (PromotionEntity) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        if (discount) {
            return promoCode + ": Discount at " + String.format("%.2f", discountRate * 100) + "%";
        } else {
            return promoCode + ": Waive cash S$" + String.format("%.2f", waiveAmount);
        }
        
    }
    
}
