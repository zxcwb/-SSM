package com.zxc.o2o.dao;

import com.zxc.o2o.entity.Area;

import java.util.List;

public interface AreaDao {
    /*
    * 列出区域列表
    * areaList
    * */
    List<Area> queryArea();
}
