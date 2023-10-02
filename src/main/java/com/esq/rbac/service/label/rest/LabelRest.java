package com.esq.rbac.service.label.rest;


import com.esq.rbac.service.label.service.LabelDal;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/labels")
public class LabelRest {

    private LabelDal labelDal;

    @Autowired
    public void setLabelDal(LabelDal labelDal) {
        log.trace("setLabelDal");
        this.labelDal = labelDal;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<List<String>> list() {
        log.trace("list");
        try {
            List<String> list = labelDal.getAllLabelNames();
            return ResponseEntity.ok(list);

        } catch (Exception e) {
            log.error("list; exception={}", e);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
