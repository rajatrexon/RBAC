package com.esq.rbac.service.base.error;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
public class RestError {

    private String messageCode;
    private String message;
    private List<String> paramaters = new ArrayList<String>();
    private String exceptionMessage;
}
