package com.example.demo.model.mapper.mapstruct;

import com.example.demo.model.User;
import com.example.demo.model.dto.UserDto;
import org.mapstruct.Mapper;

/**
 * Generated class for User
 * MapStruct mapper for converting between User and UserDto
 */
@Mapper
public interface UserMapStruct {
  /**
   * Converts a UserDto to a User entity.
   * @param dto the DTO to convert
   * @return the User entity
   */
  User toEntity(UserDto dto);

  /**
   * Converts a User entity to a UserDto.
   * @param entity the entity to convert
   * @return the UserDto
   */
  UserDto toDto(User entity);
}
