package core.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.base.MPJBaseMapper;
import core.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserMapper extends MPJBaseMapper<User> {
    //这里面的名称和id要一致
    List<User> selectByXml(@Param("name") String name);

    @Select("SELECT * FROM user ${ew.customSqlSegment}")
    List<User> selectByMyWrapper(@Param(Constants.WRAPPER) Wrapper<User> userWrapper);

    List<User> selectAllByPage(Map<String,Object> params);
}
