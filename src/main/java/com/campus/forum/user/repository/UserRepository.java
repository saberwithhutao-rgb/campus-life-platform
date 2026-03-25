package com.campus.forum.user.repository;

import com.campus.forum.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Override
    @NonNull
    Optional<User> findById(@NonNull Integer id);

    @Override
    @NonNull
    List<User> findAllById(@NonNull Iterable<Integer> ids);
}