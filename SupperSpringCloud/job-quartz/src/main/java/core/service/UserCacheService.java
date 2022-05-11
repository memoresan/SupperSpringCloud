package core.service;

import core.entity.User;
import core.util.Log4j2Provider;
import core.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserCacheService {
    Logger LOG = LoggerUtil.getLogger();

    /**
     * 查找
     * 先查缓存，如果查不到，会查数据库并存入缓存
     * @param id
     */
    @Cacheable(value = "userCache2", key = "#user.id")
    public String getUser(User user){
        LOG.info("进入缓存");
        //查找数据库
        return user.getId()+"3";
    }

    /**
     * 更新/保存
     * @param user
     */
    @CachePut(value = "userCache2", key = "#user.id")
    public String saveUser(User user){
        LOG.info("添加缓存{}",user.getId());
        return "21";
    }

    /**
     * 删除
     * @param user
     */
    @CacheEvict(value = "userCache2",key = "#user.id",beforeInvocation=true)
    public void delUser(User user) throws Exception {
        throw new Exception("hhh");
    }
}

