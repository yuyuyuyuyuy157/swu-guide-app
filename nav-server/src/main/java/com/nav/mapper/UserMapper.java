package com.nav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nav.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 即可自动获得：selectById, insert, updateById, selectList 等所有软删除安全的方法

}