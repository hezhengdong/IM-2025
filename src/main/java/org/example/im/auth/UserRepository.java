package org.example.im.auth;

import org.example.im.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 查询除指定用户ID外的所有用户ID
     */
    @Query("SELECT u FROM User u WHERE u.id != :excludeUserId")
    List<User> findAllExcept(Integer excludeUserId);
}
