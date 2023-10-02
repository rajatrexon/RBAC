package com.esq.rbac.service.user.userphoto.rest;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.user.userphoto.domain.UserPhoto;
import com.esq.rbac.service.user.userphoto.service.UserPhotoDal;
import com.esq.rbac.service.util.AuditLogger;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/userPhoto")
public class UserPhotoRest {

    private static final Charset UTF8_CHARSET = Charset.forName("utf8");
    private UserPhotoDal userPhotoDal;
    private AuditLogger auditLogger;
    private UserDal userDal;
    private int THREAD_SIZE = 1;
    ExecutorService userPhotoExecutor = Executors.newFixedThreadPool(THREAD_SIZE);

    @Autowired
    public void setUserPhotoDal(UserPhotoDal userPhotoDal, AuditLogService auditLogDal, UserDal userDal) {
        log.trace("setUserPhotoDal; {}", userPhotoDal);
        this.userPhotoDal = userPhotoDal;
        this.auditLogger = new AuditLogger(auditLogDal);
        this.userDal = userDal;
    }


    @PostMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA)
    public void upload(@PathVariable("userId") int userId, MultipartFile multiPart, @RequestHeader HttpHeaders headers) throws Exception {
        // expecting only one body part (one file upload)

        Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
        String contentType = multiPart.getContentType();
        //String fileName = new String(bodyPart.getContentDisposition().getFileName().getBytes(), UTF8_CHARSET);
        String fileName = multiPart.getOriginalFilename();

        log.trace("upload; userId={}; contentType={}; fileName={}",
                userId, contentType, fileName);

        UserPhoto userPhoto = new UserPhoto();
        userPhoto.setUserId(userId);
        userPhoto.setContentType(contentType.toString());
        userPhoto.setPhoto(multiPart.getBytes());
        User user = userDal.getById(userId);
        userPhotoDal.set(userId, loggedInUserId, userPhoto, user.getUserName());
        userPhotoExecutor.execute(getUserPhotoLogRunnable(loggedInUserId, user.getUserName()));
    }

    public Runnable getUserPhotoLogRunnable(final int userId, final String userName){
        return new Runnable(){
            @Override
            public void run() {
                log.debug("run;");
                try {

                    auditLogger.logCreate(userId, userName, "User", "Update", userPhotoDal.getObjectChangeSet());
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        };

    }

    @GetMapping(value = "/{userId}")
    public Response download(@PathVariable("userId") int userId) throws Exception {
        log.trace("download; userId={}", userId);

        UserPhoto userPhoto = userPhotoDal.get(userId);
        if (userPhoto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        log.trace("download; contentType={}; contentSize={}", userPhoto.getContentType(), userPhoto.getPhoto().length);
        return Response.ok()
                .type(userPhoto.getContentType())
                .entity(userPhoto.getPhoto())
                .build();
    }
}