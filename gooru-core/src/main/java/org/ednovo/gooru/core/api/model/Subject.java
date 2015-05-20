package org.ednovo.gooru.core.api.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;

public class Subject extends OrganizationModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6184925958268911061L;
	/**
	 * 
	 */

	@Id
	private Integer subjectId;

	@Column
	private String name;
	
	@Column
	private String description;

	@Column
	private short activeFlag;
	
	@Column
	private String imagePath;

	@Column
	private Integer displaySequence;
	
	@Column
	private Date createdOn;
	
	@Column
	private Date lastModified;
	
	@Column
	private User creator;
	
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
		
	public Integer getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getImagePath() {
		return imagePath;
	}
	
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	public Integer getDisplaySequence() {
		return displaySequence;
	}
	
	public void setDisplaySequence(Integer displaySequence) {
		this.displaySequence = displaySequence;
	}
	
	public Date getCreatedOn() {
		return createdOn;
	}
	
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
    public short getActiveFlag() {
	    return activeFlag;
    }
    
    public void setActiveFlag(short activeFlag) {
	    this.activeFlag = activeFlag;
    }
    
}
