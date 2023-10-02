package com.esq.rbac.web.vo;

import java.io.Serializable;

public class MakerCheckerConstraints implements Serializable{

//	@Size(min = 0, max = 500)
//	@Pattern(regexp = "^([^<>=]*)$", message = "Reject Reason should not have <,> and =")
//	@SpecialCharValidator
	private String rejectReason;

	public String getRejectReason() {
		return rejectReason;
	}

	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}
	
}
