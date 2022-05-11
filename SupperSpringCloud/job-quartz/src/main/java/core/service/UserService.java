package core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yulichang.base.MPJBaseService;
import core.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


public interface UserService extends MPJBaseService<User> {
      void selectUserByXml(String name);
       List<User> selectAllByPage(Map<String,Object> params);

}
