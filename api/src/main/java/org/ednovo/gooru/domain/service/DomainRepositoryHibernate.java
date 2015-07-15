/////////////////////////////////////////////////////////////
// DomainRepositoryHibernate.java
// gooru-api
// Created by Gooru on 2015
// Copyright (c) 2015 Gooru. All rights reserved.
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
package org.ednovo.gooru.domain.service;

import java.util.List;
import java.util.Map;

import org.ednovo.gooru.core.api.model.Domain;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepositoryHibernate;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

@Repository
public class DomainRepositoryHibernate extends BaseRepositoryHibernate
		implements DomainRepository, ParameterProperties, ConstantProperties {

	private static final String GET_DOMAIN = "FROM Domain d  WHERE d.domainId=:domainId";

	private static final String GET_DOMAINS = "FROM Domain";

	private static final String GET_MAX = "SELECT COALESCE(MAX(displaySequence),0) FROM Domain";

	private static final String GET_DOMAIN_ATTRIBUTES = "SELECT c.code_id codeId,c.label,c.code,c.description FROM code c inner join subdomain_attribute_mapping sa ON sa.code_id=c.code_id inner join subdomain s ON s.subdomain_id=sa.subdomain_id WHERE s.domain_id=:domainId and s.course_id=:courseId";

	private static final String GET_DOMAINS_BY_IDS = "FROM Domain  WHERE domainId  in (:domainId)";

	@Override
	public Domain getDomain(Integer domainId) {
		Query query = getSession().createQuery(GET_DOMAIN).setParameter(
				DOMAIN_ID, domainId);
		return (Domain) (query.list().size() > 0 ? query.list().get(0) : null);
	}

	@Override
	public List<Domain> getDomains(int limit, int offset) {
		Query query = getSession().createQuery(GET_DOMAINS);
		query.setMaxResults(limit != 0 ? (limit > MAX_LIMIT ? MAX_LIMIT : limit)
				: limit);
		query.setFirstResult(offset);
		return list(query);
	}

	@Override
	public Integer getMaxSequence() {
		Query query = getSession().createQuery(GET_MAX);
		return (Integer) query.list().get(0);
	}

	@Override
	public List<Map<String, String>> getDomainAttributes(Integer courseId,
			Integer domainId, int limit, int offset) {
		Query query = getSession().createSQLQuery(GET_DOMAIN_ATTRIBUTES);
		query.setParameter(COURSE_ID, courseId);
		query.setParameter(DOMAIN_ID, domainId);
		query.setMaxResults(limit > MAX_LIMIT ? MAX_LIMIT : limit);
		query.setFirstResult(offset);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	@Override
	public List<Domain> getDomains(List<Integer> domainIds) {
		Query query = getSession().createQuery(GET_DOMAINS_BY_IDS)
				.setParameterList(DOMAIN_ID, domainIds);
		return list(query);
	}

}
