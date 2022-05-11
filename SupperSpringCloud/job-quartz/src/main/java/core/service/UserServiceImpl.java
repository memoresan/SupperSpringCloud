package core.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.base.MPJBaseServiceImpl;
import core.entity.User;
import core.mybatisplus.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends MPJBaseServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    UserMapper mapper;

    @Override
    public void selectUserByXml(String name){
        mapper.selectByXml(name);
    }

    @Override
    public List<User> selectAllByPage(Map<String, Object> params) {
        return mapper.selectAllByPage(params);
    }
}
