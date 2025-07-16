package com.example.demo.model.service;

import com.example.demo.model.User;
import com.mybatisflex.core.paginate.Page;
import java.lang.Long;
import java.lang.String;
import java.util.List;

/**
 * Generated class for User
 * Service interface for User
 */
public interface UserService {
  /**
   * Selects a User by ID.
   * @param id the ID of the User
   * @return the User entity
   */
  User selectById(Long id);

  /**
   * Inserts a new User.
   * @param entity the User to insert
   */
  void insert(User entity);

  /**
   * Updates a User by ID.
   * @param entity the User to update
   */
  void updateById(User entity);

  /**
   * Deletes a User by ID.
   * @param id the ID of the User to delete
   */
  void deleteById(Long id);

  /**
   * Queries a list of User based on conditions.
   * @param id the id to filter by
   * @param username the username to filter by
   * @param email the email to filter by
   * @return the list of User entities
   */
  List<User> selectList(Long id, String username, String email);

  /**
   * Queries a paginated list of User based on conditions.
   * @param pageNumber the page number
   * @param pageSize the page size
   * @param id the id to filter by
   * @param username the username to filter by
   * @param email the email to filter by
   * @return the paginated list of User entities
   */
  Page<User> selectPage(int pageNumber, int pageSize, Long id, String username, String email);
}
