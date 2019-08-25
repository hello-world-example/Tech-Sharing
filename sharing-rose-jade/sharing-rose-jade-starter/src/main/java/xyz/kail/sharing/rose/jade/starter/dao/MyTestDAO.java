package xyz.kail.sharing.rose.jade.starter.dao;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;

@DAO
public interface MyTestDAO {

    @SQL("SELECT COUNT(*) FROM MY_TEST")
    Integer count();

}
