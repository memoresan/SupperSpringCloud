package core.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import core.entity.PageUtil;
import core.entity.User;
import core.mybatisplus.mapper.UserMapper;
import core.service.UserService;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MybatisTest {

    @Autowired
    private UserService service;
    @PostMapping("/select")
    public void selectTest(){
        //SELECT id,username AS name,age,email  FROM t_user WHERE (username LIKE ? AND age BETWEEN ? AND ? AND email IS NOT NULL) order by age desc ,id desc
        // SELECT uid AS id,name,age,email FROM t_user WHERE (name LIKE ? AND age BETWEEN ? AND ? OR age >= ? AND email IS NOT NULL) ORDER BY age DESC,id DESC
        //SELECT uid AS id,name,age,email FROM t_user WHERE (name LIKE ? AND age BETWEEN ? AND ? OR (age >= ? AND email IS NOT NULL)) ORDER BY age DESC,id DESC
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(User.class,x->!x.getColumn().equals("name")).like("name","jack")
                .between("age",20,30)
                .or(i-> i.ge("age",20)
                        .isNotNull("email"))
                //我們打算把后面括起来
                .orderByDesc("age")
                .orderByDesc("uid");
        List<User> list = service.list(queryWrapper);
        //生成map的方式
        List<Map<String, Object>> maps = service.listMaps(queryWrapper);
        list.forEach(System.out::println);
        ////SELECT id,username AS name,age,email,is_deleted FROM t_user WHERE (id IN (select id from t_user where id <= 3))
        //queryWrapper.inSql("id", "select id from t_user where id <= 3");
    }

    @PostMapping("/insert")
    public void insertTest(){
        User user = new User(null, "张三", 23, "zhangsan@atguigu.com");
        service.save(user);
        //System.out.println("受影响行数："+result);
    }
    @PostMapping("/delete")
    public void deleteTest(){
       // int result =userMapper.deleteById(1475754982694199298L);
        List<Long> idList = Arrays.asList(1L, 2L, 3L);
        service.removeByIds(idList);
        //通过条件删除相当于where语句
        Map<String, Object> map = new HashMap<>();
        map.put("age", 23);
        map.put("name", "张三");
        service.removeByMap(map);
       // System.out.println("受影响行数："+result);
    }
    @PostMapping("/update")
    public void updateTest(){
        User user = new User(4L, "admin", 22, null);
        service.updateById(user);
    }

    @PostMapping("/selectByXml")
    public void selectByXml(){
       service.selectUserByXml("jack");
    }


    @PostMapping("/selectJoin")
    public void selectJoin(){
        Page<User> page= new Page<>(2,2);
        MPJQueryWrapper<User> objectMPJLambdaWrapper = new MPJQueryWrapper<User>();
        objectMPJLambdaWrapper.select("t.uid as id").select("t1.name as name1")
                .leftJoin("t_user t1 on t.uid=t1.uid");
        IPage<User> userIPage = service.selectJoinListPage(page, User.class, objectMPJLambdaWrapper);
        List<User> list = userIPage.getRecords();
        list.forEach(System.out::println);
        System.out.println("当前页："+page.getCurrent());
        System.out.println("每页显示的条数："+page.getSize());
        System.out.println("总记录数："+page.getTotal());
        System.out.println("总页数："+page.getPages());
        System.out.println("是否有上一页："+page.hasPrevious());
        System.out.println("是否有下一页："+page.hasNext());
    }

    @PostMapping("/selectPage")
    public void selectPage(){
        PageUtil pageUtil = new PageUtil();
        pageUtil.setStartNum(0);
        pageUtil.setPageSize(0);
        pageUtil.setCount(0);
        pageUtil.setLimit(2);
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("page", pageUtil);
        service.selectAllByPage(objectObjectHashMap).forEach(System.out::println);

        //System.out.println("受影响行数："+result);
    }

}
