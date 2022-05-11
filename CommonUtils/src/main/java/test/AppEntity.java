package test;

import entity.java.BaseEntity;

public class AppEntity extends BaseEntity {

    @Override
    public void getUserName(){
        System.out.println("进入app");
    }

}
