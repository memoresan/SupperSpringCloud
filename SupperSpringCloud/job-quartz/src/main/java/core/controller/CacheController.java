package core.controller;

import core.entity.User;
import core.service.UserCacheService;
import core.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheController {
    private Logger logger = LoggerUtil.getLogger();
    @Autowired
    private UserCacheService userCacheService;

    @PostMapping("/cache")
    public void cache()  {
        User user = new User();
        //user.setId("2");
        userCacheService.saveUser(user);
        try {
            userCacheService.delUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info(userCacheService.getUser(user));

    }
}
