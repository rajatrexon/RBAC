package com.esq.rbac.service.makerchecker.contraints;

import com.esq.rbac.service.util.SpecialCharValidator;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public class MakerCheckerConstraints implements Serializable {

    @Size(min = 0, max = 500)
    @Pattern(regexp = "^([^<>=]*)$", message = "Reject Reason should not have <,> and =")
    @SpecialCharValidator
    private String rejectReason;

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

}

