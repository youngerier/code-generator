package com.example.demo.model.service.impl;

import com.example.demo.model.User;
import com.example.demo.model.mapper.flex.UserMapper;
import com.example.demo.model.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Generated class for User
 * Service implementation for User
 */
@Service
public class UserServiceImpl implements UserService {
  /**
   * The MyBatis-Flex mapper for User
   */
  private final UserMapper mapper;

  /**
   * Constructs a new UserServiceImpl.
   * @param mapper the MyBatis-Flex mapper
   */
  public UserServiceImpl(UserMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * Selects a User by ID.
   * @param id the ID of the User
   * @return the User entity
   */
  @Override
  public User selectById(Long id) {
    return mapper.selectOneById(id);
  }

  /**
   * Inserts a new User.
   * @param entity the User to insert
   */
  @Override
  public void insert(User entity) {
    mapper.insert(entity);
  }

  /**
   * Updates a User by ID.
   * @param entity the User to update
   */
  @Override
  public void updateById(User entity) {
    mapper.update(entity);
  }

  /**
   * Deletes a User by ID.
   * @param id the ID of the User to delete
   */
  @Override
  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  /**
   * @param id the id to filter by
   * @param username the username to filter by
   * @param email the email to filter by
   * Queries a list of User based on conditions.
   * @return the list of User entities
   */
  @Override
  public List<User> selectList(Long id, String username, String email) {
    QueryWrapper queryWrapper = new QueryWrapper();
    if (id != null) queryWrapper.where(User.id.eq(id));
    if (username != null) queryWrapper.where(User.username.eq(username));
    if (email != null) queryWrapper.where(User.email.eq(email));
    return mapper.selectListByQuery(queryWrapper);
  }

  /**
   * @param id the id to filter by
   * @param username the username to filter by
   * @param email the email to filter by
   * Queries a paginated list of User based on conditions.
   * @return the paginated list of User entities
   */
  @Override
  public Page<User> selectPage(int pageNumber, int pageSize, Long id, String username,
      String email) {
    QueryWrapper queryWrapper = new QueryWrapper();
    Page<User> page = new Page<>(pageNumber, pageSize);
    if (id != null) queryWrapper.where(User.id.eq(id));
    if (username != null) queryWrapper.where(User.username.eq(username));
    if (email != null) queryWrapper.where(User.email.eq(email));
    return mapper.paginate(page, queryWrapper);
  }
}
