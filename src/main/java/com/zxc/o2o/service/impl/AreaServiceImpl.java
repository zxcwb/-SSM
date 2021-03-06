package com.zxc.o2o.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxc.o2o.cache.JedisUtil;
import com.zxc.o2o.dao.AreaDao;
import com.zxc.o2o.entity.Area;
import com.zxc.o2o.exceptions.AreaOperationException;
import com.zxc.o2o.service.AreaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service(value = "areaService")
public class AreaServiceImpl implements AreaService {
    @Autowired
    private AreaDao areaDao;

    @Resource
    private JedisUtil.Keys jedisKeys;

    @Resource
    private JedisUtil.Strings jedisStrings;

    private static Logger logger = LoggerFactory.getLogger(AreaService.class);

    @Override
    public List<Area> getAreaList() {
        String key = AREALISTKEY;
        List<Area> areaList = null;
        ObjectMapper mapper = new ObjectMapper();
        //若不存在，则从数据库里面取出相应的数据
        if (!jedisKeys.exists(key)){
            areaList = areaDao.queryArea();
            //将相关的实体类集合转换成string.存入redis里面对应的key中
            String jsonString;
            try {
                jsonString = mapper.writeValueAsString(areaList);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                throw new AreaOperationException(e.getMessage());
            }
            jedisStrings.set(key,jsonString);
        }else {
            String jsonString = jedisStrings.get(key);
            JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class,Area.class);
            try {
                areaList = mapper.readValue(jsonString,javaType);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                throw new AreaOperationException(e.getMessage());
            }
        }
        return areaList;
    }

}
