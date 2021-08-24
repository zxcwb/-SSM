package com.zxc.o2o.dao;

import com.zxc.o2o.BaseTest;
import com.zxc.o2o.entity.PersonInfo;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;


/*
 * @Author: zxc of Russell
 * @Date: 2021/8/2 22:31
 * @Version 1.0
 *
 */
//定义test方法的执行顺序，有三种，这种是根据名称字典顺序执行
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersonInfoDaoTest extends BaseTest {

    @Autowired
    private PersonInfoDao personInfoDao;

    @Test
    public void queryPersonInfoById() {
        PersonInfo personInfo = personInfoDao.queryPersonInfoById(8);
        System.out.println(personInfo);
    }

    @Test
    public void insertPersonInfo() {
    }
}