package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckilltable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by jianjun-wu on 2017/3/1.
 */
public class RedisDao
{
    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    public RedisDao(String ip,int port)
    {
        jedisPool=new JedisPool(ip,port);
    }

    private RuntimeSchema<Seckilltable> schema=RuntimeSchema.createFrom(Seckilltable.class);

    public Seckilltable getSeckill(long seckillId)
    {
        //缓存redis操作逻辑
        try
        {
            Jedis jedis=jedisPool.getResource();

            try
            {
                String key="seckill:"+seckillId;
                //并没有实现内部序列化操作
                //get->byte[]->反序列化->Object(Seckill)
                //采用自定义序列化
                //protostuff:pojo
               byte[] bytes= jedis.get(key.getBytes());

               if(bytes != null)
                {
                    //空对象
                    Seckilltable seckilltable=schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes,seckilltable,schema);
                    //seckilltable 被反序列化
                    return seckilltable;
                }


            }
            finally
            {
                jedis.close();
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(),e);
        }

        return null;
    }

    public String putSeckill(Seckilltable seckilltable)
    {
        //set Object(seckill)->序列号->byte[]

        try
        {
            Jedis jedis=jedisPool.getResource();
            try {
                String key="seckill:"+seckilltable.getSeckillId();
                byte[] bytes=
                        ProtostuffIOUtil.toByteArray
                                (seckilltable,schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

                int timout=60*60;
               String result= jedis.setex(key.getBytes(),timout,bytes);
               return result;

            }finally {
                jedis.close();
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(),e);
        }

        return null;
    }

}
