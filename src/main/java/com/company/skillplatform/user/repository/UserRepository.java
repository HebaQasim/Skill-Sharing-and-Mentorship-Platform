package com.company.skillplatform.user.repository;
import com.company.skillplatform.auth.enums.RoleName;
import com.company.skillplatform.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    @Query("""
   select u from User u
   join u.roles r
   where r.name = :role
   order by u.createdAt asc
""")
    Optional<User> findFirstByRole(@Param("role") RoleName role);


    // -------- Colleagues (same dept) --------

    @Query("""
        select
          u.id as id,
          u.firstName as firstName,
          u.lastName as lastName,
          u.department as department,
          u.jobTitle as jobTitle,
          u.headline as headline,
          u.profileImageUrl as profileImageUrl
        from User u
        where u.enabled = true
          and u.department = :dept
          and (:q is null or :q = '' or lower(concat(u.firstName,' ',u.lastName)) like concat('%', lower(:q), '%'))
        order by u.lastName asc, u.firstName asc, u.id asc
    """)
    List<EmployeeRow> colleaguesFirstPage(@Param("dept") String dept, @Param("q") String q, Pageable pageable);

    @Query("""
        select
          u.id as id,
          u.firstName as firstName,
          u.lastName as lastName,
          u.department as department,
          u.jobTitle as jobTitle,
          u.headline as headline,
          u.profileImageUrl as profileImageUrl
        from User u
        where u.enabled = true
          and u.department = :dept
          and (:q is null or :q = '' or lower(concat(u.firstName,' ',u.lastName)) like concat('%', lower(:q), '%'))
          and (
               u.lastName > :lastName
            or (u.lastName = :lastName and u.firstName > :firstName)
            or (u.lastName = :lastName and u.firstName = :firstName and u.id > :id)
          )
        order by u.lastName asc, u.firstName asc, u.id asc
    """)
    List<EmployeeRow> colleaguesNextPage(
            @Param("dept") String dept,
            @Param("q") String q,
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("id") UUID id,
            Pageable pageable
    );

    // -------- Directory (all, ordered by dept) --------

    @Query("""
        select
          u.id as id,
          u.firstName as firstName,
          u.lastName as lastName,
          u.department as department,
          u.jobTitle as jobTitle,
          u.headline as headline,
          u.profileImageUrl as profileImageUrl
        from User u
        where u.enabled = true
          and (:q is null or :q = '' or lower(concat(u.firstName,' ',u.lastName)) like concat('%', lower(:q), '%'))
        order by u.department asc, u.lastName asc, u.firstName asc, u.id asc
    """)
    List<EmployeeRow> directoryFirstPage(@Param("q") String q, Pageable pageable);

    @Query("""
        select
          u.id as id,
          u.firstName as firstName,
          u.lastName as lastName,
          u.department as department,
          u.jobTitle as jobTitle,
          u.headline as headline,
          u.profileImageUrl as profileImageUrl
        from User u
        where u.enabled = true
          and (:q is null or :q = '' or lower(concat(u.firstName,' ',u.lastName)) like concat('%', lower(:q), '%'))
          and (
               u.department > :dept
            or (u.department = :dept and u.lastName > :lastName)
            or (u.department = :dept and u.lastName = :lastName and u.firstName > :firstName)
            or (u.department = :dept and u.lastName = :lastName and u.firstName = :firstName and u.id > :id)
          )
        order by u.department asc, u.lastName asc, u.firstName asc, u.id asc
    """)
    List<EmployeeRow> directoryNextPage(
            @Param("q") String q,
            @Param("dept") String dept,
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("id") UUID id,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
      select distinct u
      from User u
      join u.roles r
      where r.name = 'DEPARTMENT_ADMIN'
        and u.department = :dept
        and u.enabled = true
    """)
    List<User> findDeptAdmins(@Param("dept") String dept);

    @Query("""
      select distinct u
      from User u
      join u.roles r
      where r.name = 'ADMIN'
        and u.enabled = true
    """)
    List<User> findGlobalAdmins();
}


