package org.seckill.dao.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckilltableDao;
import org.seckill.entity.Seckilltable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by jianjun-wu on 2017/3/1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest
{
    private long id=1001;
    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SeckilltableDao seckilltableDao;

    @Test
    public void testSeckill() throws Exception
    {
        //get and put
        Seckilltable seckilltable=redisDao.getSeckill(id);
        if(seckilltable==null)
        {
            seckilltable=seckilltableDao.queryById(id);
            if(seckilltable!=null)
            {
                String result=redisDao.putSeckill(seckilltable);
                System.out.println(result);
                seckilltable= redisDao.getSeckill(id);
                System.out.println(seckilltable);
            }
        }
    }


}