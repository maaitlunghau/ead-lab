package com.fpt.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "accounts")
@NamedQueries({
    @NamedQuery(name = "Accounts.findAll",     query = "SELECT a FROM Accounts a"),
    @NamedQuery(name = "Accounts.findByAccId", query = "SELECT a FROM Accounts a WHERE a.accId = :accId")
})
public class Accounts implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "AccId")
    private Integer accId;

    @Basic(optional = false)
    @Column(name = "Username")
    private String username;

    @Basic(optional = false)
    @Column(name = "Password")
    private String password;

    @Basic(optional = false)
    @Column(name = "Role")
    private String role;

    @Basic(optional = false)
    @Column(name = "Image")
    private String image;

    public Accounts() {}

    public Accounts(Integer accId) {
        this.accId = accId;
    }

    public Integer getAccId()               { return accId; }
    public void    setAccId(Integer accId)  { this.accId = accId; }

    public String getUsername()                { return username; }
    public void   setUsername(String username) { this.username = username; }

    public String getPassword()                { return password; }
    public void   setPassword(String password) { this.password = password; }

    public String getRole()             { return role; }
    public void   setRole(String role)  { this.role = role; }

    public String getImage()              { return image; }
    public void   setImage(String image)  { this.image = image; }

    @Override
    public int hashCode() {
        return accId != null ? accId.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Accounts)) return false;
        Accounts other = (Accounts) object;
        return (this.accId != null) && this.accId.equals(other.accId);
    }

    @Override
    public String toString() {
        return "Accounts[ accId=" + accId + " ]";
    }
}
