/////////////////////////////////////////////////////////////
// OAuthRepositoryHibernate.java
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
package org.ednovo.gooru.infrastructure.persistence.hibernate.auth;

import java.util.List;

import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.api.model.LtiService;
import org.ednovo.gooru.core.api.model.OAuthClient;
import org.ednovo.gooru.core.application.util.CustomProperties;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepositoryHibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthRepositoryHibernate extends BaseRepositoryHibernate implements OAuthRepository, ParameterProperties, ConstantProperties{

	private static final String GET_USER_INFO = "select client_id from oauth_access_token where token_id = :accessToken";
	private static final String APPLICATION_STATUS_ACTIVE = CustomProperties.Table.APPLICATION_STATUS.getTable()+"_"+CustomProperties.ApplicationStatus.ACTIVE.getApplicationStatus();
	private static final String FIND_OAUTHCLIENT_BY_CONTENTID = " FROM OAuthClient oauthClient WHERE oauthClient.contentId=:oauthContentId";
	private static final String FIND_LTISERVICE_BY_OAUTH_CONTENTID = " FROM LtiService ltiService WHERE ltiService.oauthClient.contentId=:oauthContentId";
	
	@Override
	public String findClientByAccessToken(String accessToken) {
		SQLQuery query = getSession().createSQLQuery(GET_USER_INFO);
		query.setParameter("accessToken", accessToken);
		if(query.list().size() > 0){
			return (String) query.list().get(0);
		}
		return null;
	}

	@Override
	public OAuthClient findOAuthClientByOAuthKey(String oauthKey) {
		String hql = " FROM OAuthClient oauthClient WHERE oauthClient.key=:oauthKey";
		Query query = getSession().createQuery(hql);
		query.setParameter("oauthKey", oauthKey);
		List<OAuthClient> results = list(query);
		if(results.size() > 0){
			return results.get(0);
		}
		return null;
	}

	@Override
	public List<OAuthClient> listOAuthClient(String gooruUId, int pageNo,
			int pageSize) {
		String hql = " FROM OAuthClient oauthClient WHERE oauthClient.user.partyUid=:gooruUId";
		Query query = getSession().createQuery(hql);
		query.setParameter("gooruUId", gooruUId);
		query.setFirstResult(pageNo);
		query.setMaxResults(pageSize);
		List<OAuthClient> results = list(query);
		return results;
	}

	@Override
	public OAuthClient findOAuthClientByclientSecret(String clientSecret) {
		String hql = " FROM OAuthClient oauthClient WHERE oauthClient.secretKey=:clientSecret";
		Query query = getSession().createQuery(hql);
		query.setParameter("clientSecret", clientSecret);
		List<OAuthClient> results = list(query);
		if(results.size() > 0){
			return results.get(0);
		}
		return null;
	}

	@Override
	public List<OAuthClient> listOAuthClientByOrganization(String organizationUId, Integer offset, Integer limit,String grantType) {
		String hql = " FROM OAuthClient oauthClient WHERE oauthClient.organization.partyUid=:organizationUId";
		if (grantType != null){
		hql +=" AND	oauthClient.grantTypes=:grantTypes";	
		}
		Query query = getSession().createQuery(hql);
		query.setParameter("organizationUId", organizationUId);
		if (grantType != null){
			query.setParameter("grantTypes", grantType);	
		}
		query.setFirstResult(offset);
		query.setMaxResults(limit != null ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);

		return list(query);
	}

	@Override
	public Long getOauthClientCount(String organizationUId, String grantType) {
		String sql = "SELECT count(1) as count from  oauth2_client c WHERE organization_uid = '"+organizationUId+"'";
		if (grantType != null){
			sql +="AND  c.grant_types = '" + grantType +"'";
		}
		Query query = getSession().createSQLQuery(sql).addScalar("count", StandardBasicTypes.LONG);
		return (Long) query.list().get(0);
	}

	@Override
	public List<OAuthClient> findOAuthClientByApplicationKey(String apiKey) {
		String hql = " FROM OAuthClient oauthClient WHERE oauthClient.application.key=:apiKey AND oauthClient.application.status.keyValue=:type";
		Query query = getSession().createQuery(hql);
		query.setParameter("apiKey", apiKey);
		query.setParameter("type", APPLICATION_STATUS_ACTIVE);
		return list(query);
	}
	
	public LtiService getLtiServiceByOAuthContentId(OAuthClient oAuthClient){
		Long oauthContentId = oAuthClient.getContentId();
		Query query = getSession().createQuery(FIND_LTISERVICE_BY_OAUTH_CONTENTID);
		query.setParameter(LTI_OAUTH_CONTENT_ID, oauthContentId);
		List<LtiService> results = list(query);
		return results.size() > 0 ? results.get(0) : null;
	}

	@Override
	public OAuthClient getOauthClientByContentId(Long oauthContentId) {
		
		Query query = getSession().createQuery(FIND_OAUTHCLIENT_BY_CONTENTID);
		query.setParameter(LTI_OAUTH_CONTENT_ID, oauthContentId);
		List<OAuthClient> results = list(query);
		return results.size() > 0 ? results.get(0) : null;
	}
	
}
