package com.esq.rbac.service.user.userphoto.repository;

import com.esq.rbac.service.user.userphoto.domain.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto,Integer> {
}
