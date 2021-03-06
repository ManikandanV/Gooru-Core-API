/////////////////////////////////////////////////////////////
// TagRepositoryHibernate.java
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
package org.ednovo.gooru.infrastructure.persistence.hibernate.tag;

import java.util.List;

import org.ednovo.gooru.core.api.model.ContentTagAssoc;
import org.ednovo.gooru.core.api.model.Tag;
import org.ednovo.gooru.core.api.model.TagSynonyms;
import org.ednovo.gooru.core.api.model.UserTagAssoc;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepositoryHibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Repository;

@Repository
public class TagRepositoryHibernate extends BaseRepositoryHibernate implements TagRepository,ConstantProperties,ParameterProperties {
	
	private final String RETIREVE_TAG_BY_LABEL = "From Tag t   where t.label=:label  and  "+generateOrgAuthQuery("t.");
	
	private final String RETIREVE_SYNONYMS_BY_NAME = "From TagSynonyms ts   where ts.targetTagName=:targetTagName ";
	
	private final String RETIREVE_SYNONYMS_BY_ID = "From TagSynonyms ts   where ts.tagSynonymsId=:tagSynonymsId ";
	
	private final String RETIREVE_SYNONYMS_BY_TAG_AND_SYNONYM = "From TagSynonyms ts   where ts.tagSynonymsId=:tagSynonymsId AND ts.tagContentGooruOid=:tagContentGooruOid";
	
	private final String RETIREVE_SYNONYMS_BY_TAG = "From TagSynonyms ts   where ts.tagContentGooruOid=:tagContentGooruOid ";
	
	private final String RETIREVE_TAG_BY_TAGID = "From Tag t   where t.gooruOid=:gooruOid and "+generateOrgAuthQuery("t.");
	
	private final String RETIREVE_TAG_BY_USER = "From Tag t   where t.user.userId=:userId and t.activeFlag=1 and  "+generateOrgAuthQuery("t.");
	
	
	@SuppressWarnings("unchecked")
	public Tag findTagByLabel(String label){
		Session session = getSession();
		Query query = session.createQuery(RETIREVE_TAG_BY_LABEL);
		query.setParameter("label", label);
		addOrgAuthParameters(query);
		List<Tag> tags = query.list();
		return (tags.size() > 0) ? tags.get(0) : null;
	}
	
	@SuppressWarnings("unchecked")
	public Tag findTagByTagId(String gooruOid){
		Session session = getSession();
		Query query = session.createQuery(RETIREVE_TAG_BY_TAGID);
		query.setParameter("gooruOid", gooruOid);
		addOrgAuthParameters(query);
		List<Tag> tags = query.list();
		return (tags.size() > 0) ? tags.get(0) : null;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Tag> getTagByUser(Integer userId) {
		Session session = getSession();
		Query query = session.createQuery(RETIREVE_TAG_BY_USER);
		query.setParameter("userId", userId);
		addOrgAuthParameters(query);
		List<Tag> tags = ((List<Tag>)query.list());
		return tags.size() == 0 ? null : tags;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Tag> getTags(Integer offset, Integer limit) {
		String hql = "FROM Tag tag where " + generateOrgAuthQuery("tag.");
		Session session = getSession();
		Query query = session.createQuery(hql);
		addOrgAuthParameters(query);
		query.setFirstResult(offset);
		query.setMaxResults(limit);
		return query.list();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Tag> getTag(String gooruOid) {
		Session session = getSession();
		String hql = " FROM Tag tag WHERE   " + generateOrgAuthQuery("tag.");
		if (gooruOid != null) {
			if (gooruOid.contains(",")) {
				gooruOid = gooruOid.replace(",", "','");
			}
			hql += " and  tag.gooruOid in ('" + gooruOid + "')  ";
		}
		Query query = session.createQuery(hql);
		addOrgAuthParameters(query);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContentTagAssoc> getTagContentAssoc( String tagGooruOid, Integer limit, Integer offset) {
		Session session = getSession();
		String hql = "select contentTagAssoc From ContentTagAssoc contentTagAssoc where contentTagAssoc.tagGooruOid='"+tagGooruOid+"'" ;
		Query query = session.createQuery(hql);	
			query.setFirstResult(offset);
			query.setMaxResults(limit != null ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public UserTagAssoc getUserTagassocById(String gooruUid, String tagGooruOid) {
		Session session = getSession();
		String hql = "select userTagAssoc From UserTagAssoc userTagAssoc where userTagAssoc.user.partyUid='"+gooruUid+"'" ;
		if(tagGooruOid != null){
			hql += "and userTagAssoc.tagGooruOid='" +tagGooruOid+ "'";
		}
		Query query = session.createQuery(hql);
		List<UserTagAssoc> userTagAssocs = query.list();
		return (userTagAssocs.size() > 0) ? userTagAssocs.get(0) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserTagAssoc> getContentTagByUser(String gooruUid, Integer limit, Integer offset) {
		Session session = getSession();
		String hql = "select userTagAssoc From UserTagAssoc userTagAssoc where userTagAssoc.user.partyUid='" + gooruUid + "'";
		Query query = session.createQuery(hql);
			query.setFirstResult(offset);
			query.setMaxResults(limit != null ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserTagAssoc> getTagAssocUser(String tagGooruOid, Integer limit, Integer offset) {
		Session session = getSession();
		String hql = "select userTagAssoc From UserTagAssoc userTagAssoc where userTagAssoc.tagGooruOid='"+tagGooruOid+"'" ;
		Query query = session.createQuery(hql);
		query.setFirstResult(offset);
		query.setMaxResults(limit != null ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public TagSynonyms findSynonymByName(String targetTagName) {
		Session session = getSession();
		Query query = session.createQuery(RETIREVE_SYNONYMS_BY_NAME);
		query.setParameter("targetTagName", targetTagName);
		List<TagSynonyms> tagSynonyms = query.list();
		return (tagSynonyms.size() > 0) ? tagSynonyms.get(0) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TagSynonyms findTagSynonymById(Integer tagSynonymsId) {
		Session session = getSession();
		Query query = session.createQuery(RETIREVE_SYNONYMS_BY_ID);
		query.setParameter("tagSynonymsId", tagSynonymsId);
		List<TagSynonyms> tagSynonyms = query.list();
		return (tagSynonyms.size() > 0) ? tagSynonyms.get(0) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TagSynonyms> getTagSynonyms(String tagContentGooruOid) {
		Session session = getSession();
		Query query = session.createQuery(RETIREVE_SYNONYMS_BY_TAG);
		query.setParameter("tagContentGooruOid", tagContentGooruOid);
		return  query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public TagSynonyms getSynonymByTagAndSynonymId(String tagContentGooruOid, Integer tagSynonymsId) {
		Session session = getSession();
		Query query = session.createQuery(RETIREVE_SYNONYMS_BY_TAG_AND_SYNONYM);
		query.setParameter("tagContentGooruOid", tagContentGooruOid);
		query.setParameter("tagSynonymsId", tagSynonymsId);
		List<TagSynonyms> tagSynonyms = query.list();
		return (tagSynonyms.size() > 0) ? tagSynonyms.get(0) : null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getResourceByLabel(String label, Integer limit, Integer offset, String gooruUid) {
		String hql = "select r.title, ci.gooru_oid, r.type_name, r.folder, r.thumbnail, cu.value, cu.display_name, r.views_total, r.url from  tags t inner join content c  on (c.content_id = t.content_id) inner join content_tag_assoc ct on (ct.tag_gooru_oid = c.gooru_oid) inner join content ci on (ci.gooru_oid = ct.content_gooru_oid) inner join resource r on r.content_id = ci.content_id left join custom_table_value cu on cu.custom_table_value_id = r.resource_format_id  where (t.label =:label or ct.tag_gooru_oid =:label) and  ct.associated_uid =:gooruUid" ;
		Query query = getSession().createSQLQuery(hql);
		query.setParameter("label", label);
		query.setParameter("gooruUid", gooruUid);
			query.setFirstResult(offset);
			query.setMaxResults(limit != null ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		return query.list();
	}
	
	public Long getResourceByLabelCount(String label, String gooruUid) {
		String sql = "select count(1) as count from  tags t inner join content c  on (c.content_id = t.content_id) inner join content_tag_assoc ct on (ct.tag_gooru_oid = c.gooru_oid) inner join content ci on (ci.gooru_oid = ct.content_gooru_oid) inner join resource r on r.content_id = ci.content_id left join  custom_table_value cu on cu.custom_table_value_id = r.resource_format_id  where (t.label =:label or ct.tag_gooru_oid =:label) and  ct.associated_uid =:gooruUid" ;
		Query query = getSession().createSQLQuery(sql).addScalar("count", StandardBasicTypes.LONG);
		query.setParameter("label", label);
		query.setParameter("gooruUid", gooruUid);
		return (Long)query.list().get(0);
	}
	
}
