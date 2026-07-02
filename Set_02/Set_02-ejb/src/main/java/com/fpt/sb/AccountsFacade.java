/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fpt.sb;

import com.fpt.entity.Accounts;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

/**
 *
 * @author hp
 */
@Stateless
public class AccountsFacade extends AbstractFacade<Accounts> implements AccountsFacadeLocal {

    @PersistenceContext(unitName = "AccountPU")
    private EntityManager em;

    @Override
    protected jakarta.persistence.EntityManager getEntityManager() {
        return em;
    }

    public AccountsFacade() {
        super(Accounts.class);
    }

    @Override
    public Accounts checkLogin(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        String sql = "SELECT a FROM Accounts a WHERE a.username = :username and a.password = :password";
        try {
            TypedQuery<Accounts> query = em.createQuery(sql, Accounts.class);
        query.setParameter("username", username.trim());
        query.setParameter("password", password.trim());
        
        
        return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
}
