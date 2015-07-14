package org.ednovo.gooru.core.api.model;

import java.io.Serializable;
import java.util.Date;

public class InviteUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4627013229669491613L;

	private String inviteUid;
	private String emailId;
	private String gooruOid;
	private String invitationType;
	private Date createdDate;
	private Date joinedDate;
	private CustomTableValue status;
	private User associatedUser;

	public InviteUser() {

	}

	public InviteUser(String email, String gooruOid, String invitationType, User user, CustomTableValue status) {
		this.setEmailId(email);
		this.setCreatedDate(new Date(System.currentTimeMillis()));
		this.setGooruOid(gooruOid);
		this.setInvitationType(invitationType);
		this.setAssociatedUser(user);
		this.setStatus(status);
	}

	public void setInvitationType(String invitationType) {
		this.invitationType = invitationType;
	}

	public String getInvitationType() {
		return invitationType;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setJoinedDate(Date joinedDate) {
		this.joinedDate = joinedDate;
	}

	public Date getJoinedDate() {
		return joinedDate;
	}

	public void setStatus(CustomTableValue status) {
		this.status = status;
	}

	public CustomTableValue getStatus() {
		return status;
	}

	public void setGooruOid(String gooruOid) {
		this.gooruOid = gooruOid;
	}

	public String getGooruOid() {
		return gooruOid;
	}

	public void setInviteUid(String inviteUid) {
		this.inviteUid = inviteUid;
	}

	public String getInviteUid() {
		return inviteUid;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setAssociatedUser(User associatedUser) {
		this.associatedUser = associatedUser;
	}

	public User getAssociatedUser() {
		return associatedUser;
	}

}
