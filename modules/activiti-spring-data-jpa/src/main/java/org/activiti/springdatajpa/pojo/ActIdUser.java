package org.activiti.springdatajpa.pojo;
// Generated Nov 21, 2015 11:41:58 AM by Hibernate Tools 3.2.2.GA


import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * ActIdUser generated by hbm2java
 */
@Entity
@Table(name="act_id_user"
    ,schema="public"
)
public class ActIdUser  implements java.io.Serializable {


     private String id;
     private Integer rev;
     private String first;
     private String last;
     private String email;
     private String pwd;
     private String pictureId;
     private Set<ActIdGroup> actIdGroups = new HashSet<ActIdGroup>(0);

    public ActIdUser() {
    }

	
    public ActIdUser(String id) {
        this.id = id;
    }
    public ActIdUser(String id, Integer rev, String first, String last, String email, String pwd, String pictureId, Set<ActIdGroup> actIdGroups) {
       this.id = id;
       this.rev = rev;
       this.first = first;
       this.last = last;
       this.email = email;
       this.pwd = pwd;
       this.pictureId = pictureId;
       this.actIdGroups = actIdGroups;
    }
   
     @Id 
    
    @Column(name="id_", unique=true, nullable=false, length=64)
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Column(name="rev_")
    public Integer getRev() {
        return this.rev;
    }
    
    public void setRev(Integer rev) {
        this.rev = rev;
    }
    
    @Column(name="first_")
    public String getFirst() {
        return this.first;
    }
    
    public void setFirst(String first) {
        this.first = first;
    }
    
    @Column(name="last_")
    public String getLast() {
        return this.last;
    }
    
    public void setLast(String last) {
        this.last = last;
    }
    
    @Column(name="email_")
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Column(name="pwd_")
    public String getPwd() {
        return this.pwd;
    }
    
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    
    @Column(name="picture_id_", length=64)
    public String getPictureId() {
        return this.pictureId;
    }
    
    public void setPictureId(String pictureId) {
        this.pictureId = pictureId;
    }
@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinTable(name="act_id_membership", schema="public", joinColumns = { 
        @JoinColumn(name="user_id_", nullable=false, updatable=false) }, inverseJoinColumns = { 
        @JoinColumn(name="group_id_", nullable=false, updatable=false) })
    public Set<ActIdGroup> getActIdGroups() {
        return this.actIdGroups;
    }
    
    public void setActIdGroups(Set<ActIdGroup> actIdGroups) {
        this.actIdGroups = actIdGroups;
    }




}


