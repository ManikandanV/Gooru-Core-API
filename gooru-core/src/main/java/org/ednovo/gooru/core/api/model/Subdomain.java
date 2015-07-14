/////////////////////////////////////////////////////////////
// Subdomain.java
// gooru-api
// Created by Gooru on 2015
// Copyright (c) 2015 Gooru. All rights reserved.
// http://www.goorulearning.org/
// Permission is hereby granted, free of charge, to any person      obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so,  subject to
// the following conditions:
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY  KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE    WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR  PURPOSE     AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR  COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
/////////////////////////////////////////////////////////////
package org.ednovo.gooru.core.api.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Transient;

public class Subdomain implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7789419722908609593L;

	private Integer subdomainId;

	private TaxonomyCourse taxonomyCourse;

	private Domain domain;

	private Date createdOn;

	private String uri;

	@Transient
	private Integer domainId;

	@Transient
	private Integer taxonomyCourseId;
	
	private String description;

	public Integer getSubdomainId() {
		return subdomainId;
	}

	public void setSubdomainId(Integer subdomainId) {
		this.subdomainId = subdomainId;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Integer getDomainId() {
		return domainId;
	}

	public void setDomainId(Integer domainId) {
		this.domainId = domainId;
	}

	public TaxonomyCourse getTaxonomyCourse() {
		return taxonomyCourse;
	}

	public void setTaxonomyCourse(TaxonomyCourse taxonomyCourse) {
		this.taxonomyCourse = taxonomyCourse;
	}

	public Integer getTaxonomyCourseId() {
		return taxonomyCourseId;
	}

	public void setTaxonomyCourseId(Integer taxonomyCourseId) {
		this.taxonomyCourseId = taxonomyCourseId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
