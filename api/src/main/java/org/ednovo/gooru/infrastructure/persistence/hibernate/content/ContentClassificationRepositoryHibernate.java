/////////////////////////////////////////////////////////////
// ContentClassficationRepositoryHibernate.java
// gooru-api
// Created by Gooru on 2014
// Copyright (c) 2014 Gooru. All rights reserved.
// http://www.goorulearning.org/
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
/////////////////////////////////////////////////////////////
package org.ednovo.gooru.infrastructure.persistence.hibernate.content;

import java.util.List;

import org.ednovo.gooru.core.api.model.Code;
import org.ednovo.gooru.core.api.model.ContentClassification;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepositoryHibernate;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

@Repository
public class ContentClassificationRepositoryHibernate extends BaseRepositoryHibernate implements ContentClassificationRepository, ParameterProperties, ConstantProperties {

	private static final String DELETE_CONTENT_CLASSIFICATION = "DELETE FROM ContentClassification where content.contentId=:contentId and typeId=:typeId";

	private static final String GET_CODE_BY_IDS = "FROM Code where codeId in (:codeId)";
	
	private static final String CONTENT_CLASSIFICATION = "FROM ContentClassification where content.contentId=:contentId";


	@SuppressWarnings("unchecked")
	public ContentClassification findByContent(Long contentId) {
		List<ContentClassification> cc = getSession().createQuery("select c from ContentClassification c   where c.content.contentId = ? and " + generateAuthQueryWithDataNew("c.content.")).setLong(0, contentId).list();
		return cc.size() == 0 ? null : cc.get(0);
	}

	@SuppressWarnings("unchecked")
	public ContentClassification findByContentGooruId(String gooruContentId) {
		List<ContentClassification> cc = getSession().createQuery("select c from ContentClassification c  where c.content.gooruOid = ? and " + generateAuthQueryWithDataNew("c.content.")).setString(0, gooruContentId).list();
		return cc.size() == 0 ? null : cc.get(0);
	}

	@Override
	public void deleteContentClassification(Long contentId, Short typeId) {
		Query query = getSession().createQuery(DELETE_CONTENT_CLASSIFICATION);
		query.setParameter(CONTENT_ID, contentId);
		query.setParameter(TYPE_ID, typeId);
		query.executeUpdate();
	}

	@Override
	public List<Code> getCodes(List<Integer> codeIds) {
		Query query = getSession().createQuery(GET_CODE_BY_IDS);
		query.setParameterList(CODE_ID, codeIds);
		return list(query);
	}
	
	@Override
	public List<ContentClassification> getContentClassification(Long contentId) {
		Query query = getSession().createQuery(CONTENT_CLASSIFICATION);
		query.setParameter(CONTENT_ID, contentId);
		return list(query);
	}

}
