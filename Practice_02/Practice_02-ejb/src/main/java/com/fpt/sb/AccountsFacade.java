package com.fpt.sb;

import com.fpt.entity.Accounts;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class AccountsFacade extends AbstractFacade<Accounts> implements AccountsFacadeLocal {

    @PersistenceContext(unitName = "AccountPU")
    private EntityManager em;

    public AccountsFacade() {
        super(Accounts.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    public Accounts checkLogin(String username, String password) {
        if (username == null || password == null) return null;
        try {
            TypedQuery<Accounts> q = em.createQuery(
                "SELECT a FROM Accounts a WHERE a.username = :u AND a.password = :p",
                Accounts.class);
            q.setParameter("u", username.trim());
            q.setParameter("p", password.trim());
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
