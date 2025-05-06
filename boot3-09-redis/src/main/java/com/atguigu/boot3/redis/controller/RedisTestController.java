package com.atguigu.boot3.redis.controller;

import cn.hutool.core.date.DateUtil;
import com.atguigu.boot3.redis.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author lfy
 * @Description
 * @create 2023-04-28 15:43
 */
@RestController
public class RedisTestController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //为了后来系统的兼容性，应该所有对象都是以json的方式进行保存
    @Autowired //如果给redis中保存数据会使用默认的序列化机制，导致redis中保存的对象不可视
    RedisTemplate<Object, Object>  redisTemplate;

    @GetMapping("/count")
    public String count(){

        Long hello = stringRedisTemplate.opsForValue().increment("hello");

        //常见数据类型  k: v value可以有很多类型
        //string： 普通字符串 ： redisTemplate.opsForValue()
        //list:    列表：       redisTemplate.opsForList()
        //set:     集合:       redisTemplate.opsForSet()
        //zset:    有序集合:    redisTemplate.opsForZSet()
        //hash：   map结构：    redisTemplate.opsForHash()

        return "访问了【"+hello+"】次";
    }


    @GetMapping("/person/save")
    public String savePerson(){
        Person person = new Person(1L,"张三",18,new Date());

        //1、序列化： 对象转为字符串方式
        redisTemplate.opsForValue().set("person",person);

        return "ok";
    }

    @GetMapping("/person/get")
    public Person getPerson(){
        Person person = (Person) redisTemplate.opsForValue().get("person");

        return person;
    }
    @GetMapping("/bitmap")
    public void testBitMap(){

        Date date = new Date();
        int dayOfMonth = DateUtil.dayOfMonth(date);
        System.out.println(dayOfMonth);

        String key = "bitmap";

        //stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);

        //获取本月截止今天为止的所有的签到记录，返回的是一个十进制的数字 BITFIELD sign:999:202308 GET u18 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );

        System.out.println(result);

        if (result == null || result.isEmpty()) {
            // 没有任何签到结果
            System.out.println("没有签到");
        }
        //num为0，直接返回0
        Long num = result.get(0);
        if (num == null || num == 0) {
            System.out.println("没有签到");
        }
        // 6.循环遍历
        int count = 0;
        while (true) {
            // 6.1.让这个数字与1做与运算，得到数字的最后一个bit位  // 判断这个bit位是否为0
            if ((num & 1) == 0) {
                // 如果为0，说明未签到，结束
                break;
            }else {
                // 如果不为0，说明已签到，计数器+1
                count++;
            }
            // 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }

        System.out.println(count);
    }

}
