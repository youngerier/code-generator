package com.example.demo.model.dal.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

/**
 * 用户实体
 */
@Data
@Table("tb_user")
public class User {
    /**
     * 用户ID
     */
    @Id
    private Long id;

    /**
     * 用户名称
     */
    @Column("user_name")
    private String username;

    /**
     * 用户邮箱
     */
    @Column("email_address")
    private String email;

    private Boolean deleted;

}