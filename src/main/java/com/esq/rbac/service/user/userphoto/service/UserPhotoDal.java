package com.esq.rbac.service.user.userphoto.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.user.userphoto.domain.UserPhoto;

public interface UserPhotoDal extends BaseDal {

    void set(int userId, int loggedinUserId, UserPhoto userPhoto, String userName);

    UserPhoto get(int userId);
}
