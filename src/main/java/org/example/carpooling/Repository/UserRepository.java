package org.example.carpooling.Repository;

import org.example.carpooling.Entity.Role;
import org.example.carpooling.Entity.Users;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByEmail(String email); // Thêm phương thức này
    Optional<Users> findByEmail(String email);

    @Query("SELECT u from Users u join u.role r where r.name = :roleName")
    List<Users> findAllByRole(@Param("roleName") String roleName);

    Optional<Users> findUsersById(Long id);
    void deleteUserById(Long id);

}
