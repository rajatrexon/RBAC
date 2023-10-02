package com.esq.rbac.service.user.userphoto.service;

import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.user.userphoto.domain.UserPhoto;
import com.esq.rbac.service.user.userphoto.repository.UserPhotoRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserPhotoDalJpa extends BaseDalJpa implements UserPhotoDal {


    private UserPhotoRepository userPhotoRepository;

    @Autowired
    public UserPhotoDalJpa(UserPhotoRepository userPhotoRepository) {
        this.userPhotoRepository = userPhotoRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void set(int userId, int loggedInUserId, UserPhoto userPhoto, String userName) {
        userPhoto.setUserId(userId);
        setObjectChangeSet(userPhoto, userName);
        userPhoto.setUpdatedOn(DateTime.now().toDate());
        userPhoto.setUpdatedBy(loggedInUserId);
        userPhotoRepository.save(userPhoto);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public UserPhoto get(int userId) {
        return em.find(UserPhoto.class, userId);
    }

    private void setObjectChangeSet(UserPhoto userPhoto, String userName) {
        clearObjectChangeSet();
        putToObjectChangeSet(OBJECTCHANGES_USERID, userPhoto.getUserId().toString());
        putToObjectChangeSet(OBJECTNAME, userName);
        checkObjectPutToObjectChangeSet(OBJECTCHANGES_USER_IMAGE);
    }
}

