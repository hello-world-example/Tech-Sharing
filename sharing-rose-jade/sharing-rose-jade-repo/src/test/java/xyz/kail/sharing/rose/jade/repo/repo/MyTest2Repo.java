package xyz.kail.sharing.rose.jade.repo.repo;

import org.springframework.stereotype.Service;
import xyz.kail.sharing.rose.jade.repo.dao.MyTest2DAO;

import javax.annotation.Resource;

@Service
public class MyTest2Repo {

    @Resource
    private MyTest2DAO myTest2DAO;

    public Integer count() {
        return myTest2DAO.count();
    }

}
