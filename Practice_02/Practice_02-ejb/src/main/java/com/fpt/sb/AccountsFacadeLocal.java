package com.fpt.sb;

import com.fpt.entity.Accounts;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface AccountsFacadeLocal {

    void create(Accounts accounts);
    void edit(Accounts accounts);
    void remove(Accounts accounts);
    Accounts find(Object id);
    List<Accounts> findAll();
    List<Accounts> findRange(int[] range);
    int count();

    Accounts checkLogin(String username, String password);
}
