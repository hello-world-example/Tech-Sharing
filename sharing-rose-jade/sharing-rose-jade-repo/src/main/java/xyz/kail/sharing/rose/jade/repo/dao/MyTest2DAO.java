package xyz.kail.sharing.rose.jade.repo.dao;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;

@DAO
public interface MyTest2DAO {

    @SQL("SELECT COUNT(*) FROM MY_TEST2")
    Integer count();

}
