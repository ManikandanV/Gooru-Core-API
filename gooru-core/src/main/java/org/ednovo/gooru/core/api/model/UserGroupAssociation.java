package org.ednovo.gooru.core.api.model;

import java.io.Serializable;
import java.util.Date;

public class UserGroupAssociation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7821133985195541453L;

	/**
	 * 
	 */

	private User user;

	private UserGroup userGroup;

	private Integer isGroupOwner;

	private Date associationDate;

	public UserGroupAssociation() {
		user = new User();
		userGroup = new UserGroup();
	}

	public UserGroupAssociation(Integer isGroupOwner, User user, Date associationDate, UserGroup userGroup) {
		this.setIsGroupOwner(isGroupOwner);
		this.setUser(user);
		this.setAssociationDate(associationDate);
		this.setUserGroup(userGroup);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public UserGroup getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}

	public Integer getIsGroupOwner() {
		return isGroupOwner;
	}

	public void setIsGroupOwner(Integer isGroupOwner) {
		this.isGroupOwner = isGroupOwner;
	}

	public void setAssociationDate(Date associationDate) {
		this.associationDate = associationDate;
	}

	public Date getAssociationDate() {
		return associationDate;
	}

}
