/////////////////////////////////////////////////////////////

// ContentRepositoryHibernate.java
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

import java.math.BigInteger;
import java.util.List;

import org.ednovo.gooru.core.api.model.Code;
import org.ednovo.gooru.core.api.model.Content;
import org.ednovo.gooru.core.api.model.ContentAssociation;
import org.ednovo.gooru.core.api.model.ContentMeta;
import org.ednovo.gooru.core.api.model.ContentMetaAssociation;
import org.ednovo.gooru.core.api.model.ContentPermission;
import org.ednovo.gooru.core.api.model.ContentProvider;
import org.ednovo.gooru.core.api.model.ContentProviderAssociation;
import org.ednovo.gooru.core.api.model.ContentTagAssoc;
import org.ednovo.gooru.core.api.model.StatusType;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepositoryHibernate;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Repository;

@Repository
public class ContentRepositoryHibernate extends BaseRepositoryHibernate implements ContentRepository, ConstantProperties, ParameterProperties {

	private final static String DELETE_CONTENT_SUBDOMAIN_ASSOC = "DELETE FROM ContentSubdomainAssoc where content.contentId =:contentId";

	private final static String DELETE_CONTENT_META_ASSOC = "DELETE cm.* from content_meta_assoc cm  inner join custom_table_value ctv on cm.type_id = ctv.custom_table_value_id  inner join custom_table ct on ctv.custom_table_id = ct.custom_table_id where  cm.content_id =:contentId and  name =:key";

	private final static String GET_CONTENT_META_DATA = "FROM ContentMeta where content.contentId=:contentId";

	private final static String DELETE_CONTENT_TAXONOMY_COURSE_ASSOC = "DELETE FROM ContentTaxonomyCourseAssoc where content.contentId =:contentId";

	private final static String GET_CONTENT_META_ASSOCATION = "FROM ContentMetaAssociation where content.gooruOid =:gooruOid and  typeId.customTable.name =:key";

	private static final String CONTENT_META_ASSOC = "FROM ContentMetaAssociation where content.contentId=:contentId";
	
	private static final String CONTENT_LIST = "FROM Content where gooruOid in (:contentId)";


	@SuppressWarnings("unchecked")
	@Override
	public Content findByContent(Long contentId) {
		List<Content> cc = getSession().createQuery("SELECT  c FROM Content c  WHERE c.contentId = ? AND  " + generateAuthQueryWithDataNew("c.")).setLong(0, contentId).list();
		return cc.size() == 0 ? null : cc.get(0);
	}

	@Override
	public Content findContentByGooruId(String gooruContentId) {
		return findContentByGooruId(gooruContentId, false);
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public Content findContentByGooruId(String gooruContentId, boolean fetchUser) {
		if (!fetchUser) {
			List<Content> cc = getSession().createQuery("SELECT c FROM Content c   WHERE c.gooruOid = ? AND " + generateAuthQueryWithDataNew("c.")).setString(0, gooruContentId).list();
			return cc.size() == 0 ? null : cc.get(0);
		} else {
			Criteria crit = getSession().createCriteria(Content.class);
			crit.setFetchMode("user", FetchMode.EAGER).setFetchMode("userPermSet", FetchMode.JOIN).add(Restrictions.eq("gooruOid", gooruContentId));
			Content content = (Content) crit.uniqueResult();

			return content;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public User findContentOwner(String gooruContentId) {

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("user"));

		Criteria criteria = getSession().createCriteria(Content.class).setProjection(proList).add(Restrictions.eq("gooruOid", gooruContentId));
		List<Content> contents = addOrgAuthCriterias(criteria).list();
		return contents.size() == 0 ? null : contents.get(0).getUser();
	}

	@Override
	public void delete(String gooruContentId) {
		Content content = findContentByGooruId(gooruContentId);
		if (content != null) {
			getSession().delete(content);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ContentAssociation getCollectionAssocContent(String contentGooruOid) {
		String hql = "SELECT contentAssociation FROM ContentAssociation contentAssociation JOIN Content content  LEFT JOIN content.contentPermissions cps WHERE content.gooruOid = '" + contentGooruOid + "' AND  " + generateAuthQueryWithDataNew("content.");
		List<ContentAssociation> result = (List<ContentAssociation>) find(hql);
		return (result.size() > 0) ? null : result.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public StatusType getStatusType(String name) {
		String hql = "FROM StatusType statusType  WHERE statusType.name = '" + name + "'";
		List<StatusType> result = (List<StatusType>) find(hql);
		return (result.size() > 0) ? null : result.get(0);
	}

	@SuppressWarnings("unchecked")
	public Code getCodeByName(String name) {

		List<Code> cc = getSession().createQuery("SELECT c FROM Code c   WHERE c.label = ?  AND  " + generateAuthQueryWithDataNew("c.taxonomySet.")).setString(0, name).list();
		return cc.size() == 0 ? null : cc.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean checkContentPermission(Long contentId, String partyUid) {
		String hql = "FROM ContentPermission cp where cp.content.contentId=:contentId and cp.party.partyUid=:partyUid";
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setParameter("contentId", contentId);
		query.setParameter("partyUid", partyUid);
		List<ContentPermission> permissions = query.list();
		return (permissions.size() > 0) ? true : false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Content> getContentByUserUId(String userUId) {
		Session session = getSession();
		String hql = "FROM Content content WHERE content.user.partyUid = '" + userUId + "'";
		Query query = session.createQuery(hql);
		return query.list();
	}

	@Override
	public void deleteContentByContentId(String contentId) {
		try {
			String hql = "DELETE Content content  where content.gooruOid = '" + contentId + "'";
			Session session = getSession();
			Query query = session.createQuery(hql);
			query.executeUpdate();
		} catch (Exception e) {
			getLogger().error("couldn't delete content", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ContentTagAssoc getContentTagById(String gooruOid, String tagGooruOid, String gooruUid) {
		Session session = getSession();
		String hql = "select contentTagAssoc From ContentTagAssoc contentTagAssoc where contentTagAssoc.contentGooruOid='" + gooruOid + "'";
		if (tagGooruOid != null) {
			hql += " and contentTagAssoc.tagGooruOid='" + tagGooruOid + "'";
		}
		if (gooruUid != null) {
			hql += " and contentTagAssoc.associatedUid='" + gooruUid + "'";
		}
		Query query = session.createQuery(hql);
		List<ContentTagAssoc> contentTagAssocs = query.list();
		return (contentTagAssocs.size() > 0) ? contentTagAssocs.get(0) : null;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContentTagAssoc> getContentTagByContent(String gooruOid, String gooruUid) {
		String hql = "select contentTagAssoc From ContentTagAssoc contentTagAssoc where contentTagAssoc.contentGooruOid='" + gooruOid + "'";
		if (gooruUid != null) {
			hql += " and contentTagAssoc.associatedUid='" + gooruUid + "'";
		}
		Query query = getSession().createQuery(hql);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContentPermission> getContentPermission(Long contentId, String partyUid) {
		String hql = "FROM ContentPermission cp where cp.content.contentId=:contentId and cp.party.partyUid=:partyUid";
		Session session = getSession();
		Query query = session.createQuery(hql);
		query.setParameter("contentId", contentId);
		query.setParameter("partyUid", partyUid);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContentProvider> getContentProvider(Integer offset, Integer limit) {
		Session session = getSession();
		String hql = " FROM ContentProvider contentProvider WHERE " + generateOrgAuthQueryWithData("contentProvider.") + " and " + "contentProvider.activeFlag = 1";
		Query query = session.createQuery(hql);
		query.setFirstResult(offset);
		query.setMaxResults(limit);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContentProviderAssociation> getContentProviderByGooruOid(String gooruOid, String name, String providerType) {
		String hql = " FROM ContentProviderAssociation cpa WHERE " + generateOrgAuthQueryWithData("cpa.contentProvider.") + " and " + "cpa.gooruOid=:gooruOid" + " and " + "cpa.contentProvider.activeFlag = 1";

		if (name != null) {
			hql += " and cpa.contentProvider.name ='" + name + "'";
		}
		if (providerType != null) {
			hql += " and cpa.contentProvider.type.value =:providerType";
		}
		Query query = getSession().createQuery(hql);
		query.setParameter("gooruOid", gooruOid);
		if (providerType != null) {
			query.setParameter("providerType", providerType);
		}
		return query.list();
	}

	@Override
	public ContentProvider getContentProviderByName(String name, String keyValue) {
		String hql = " FROM ContentProvider cp WHERE " + generateOrgAuthQueryWithData("cp.") + " and cp.activeFlag = 1 and cp.name =:name";
		if (keyValue != null) {
			hql += " and cp.type.keyValue =:keyValue";
		}
		Query query = getSession().createQuery(hql);

		query.setParameter(NAME, name);
		if (keyValue != null) {
			query.setParameter(KEY_VALUE, keyValue);
		}
		return query.list().size() > 0 ? (ContentProvider) query.list().get(0) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getUserContentTagList(String gooruUid, Integer limit, Integer offset) {
		String sql = "select  count(1) as count, t.label as label , ct.tag_gooru_oid as tagGooruOid from tags t inner join content c on  (t.content_id = c.content_id) inner join content_tag_assoc ct on (c.gooru_oid= ct.tag_gooru_oid) where associated_uid  =  '" + gooruUid
				+ "' group by ct.tag_gooru_oid";
		Query query = getSession().createSQLQuery(sql).addScalar("count", StandardBasicTypes.INTEGER).addScalar("label", StandardBasicTypes.STRING).addScalar("tagGooruOid", StandardBasicTypes.STRING);
		query.setFirstResult(offset);
		query.setMaxResults(limit != null ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		return query.list();
	}

	@SuppressWarnings("rawtypes")
    @Override
    public List getIdsByUserUId(String userUId, String typeName, Integer pageNo, Integer pageSize) {
            Session session = getSession();
            String sql = "SELECT c.content_id,c.gooru_oid, r.type_name FROM content c INNER JOIN resource r ON (r.content_id=c.content_id) WHERE c.user_uid = '" + userUId + "' OR c.creator_uid = '" + userUId + "'";
            if (typeName != null) {
                    sql += " and r.type_name in ('" + typeName + "')";
            }
            if (pageNo != null && pageSize != null) {
                    sql += " limit " + pageNo + " , " + pageSize;
            }
            SQLQuery query = session.createSQLQuery(sql);
            return query.list();
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getResourceContentTagList(String gooruOid, Integer limit, Integer offset) {
		String sql = "select  count(1) as count, t.label as label , ct.tag_gooru_oid as tagGooruOid from tags t inner join content c on  (t.content_id = c.content_id) inner join content_tag_assoc ct on (c.gooru_oid= ct.tag_gooru_oid) where content_gooru_oid  =  '" + gooruOid
				+ "' group by ct.tag_gooru_oid";
		Query query = getSession().createSQLQuery(sql).addScalar("count", StandardBasicTypes.INTEGER).addScalar("label", StandardBasicTypes.STRING).addScalar("tagGooruOid", StandardBasicTypes.STRING);
		query.setFirstResult(offset);
		query.setMaxResults(limit != null ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		return query.list();
	}

	@Override
	public Long getUserContentTagCount(String gooruUid) {
		String sql = "select count(*) as count from  (select  count(1) as count, t.label as label  from tags t inner join content c on  (t.content_id = c.content_id) inner join content_tag_assoc ct on (c.gooru_oid= ct.tag_gooru_oid) where associated_uid  =  '" + gooruUid
				+ "' group by ct.tag_gooru_oid) sq";
		Query query = getSession().createSQLQuery(sql);
		return ((BigInteger) query.list().get(0)).longValue();
	}

	@Override
	public Long getResourceContentTagCount(String gooruOid) {
		String sql = "select count(*) as count from  (select  count(1) as count, t.label as label  from tags t inner join content c on  (t.content_id = c.content_id) inner join content_tag_assoc ct on (c.gooru_oid= ct.tag_gooru_oid) where content_gooru_oid  =  '" + gooruOid
				+ "' group by ct.tag_gooru_oid) sq";
		Query query = getSession().createSQLQuery(sql);
		return ((BigInteger) query.list().get(0)).longValue();
	}

	@Override
	public void deleteContentProvider(String gooruOid, String providerType, String name) {
		String sql = "delete cpa from content_provider_assoc cpa inner join content_provider cp on cpa.content_provider_uid = cp.content_provider_uid inner join custom_table_value ctv on cp.content_provider_type = ctv.custom_table_value_id where cpa.gooru_oid = '" + gooruOid + "' and ctv.value = '"
				+ providerType + "' and cp.name = '" + name + "'";
		Query query = getSession().createSQLQuery(sql);
		query.executeUpdate();
	}

	@Override
	public ContentMeta getContentMeta(Long contentId) {
		Query query = getSession().createQuery(GET_CONTENT_META_DATA);
		query.setParameter(CONTENT_ID, contentId);
		List<ContentMeta> results = list(query);
		return (ContentMeta) (results.size() > 0 ? results.get(0) : null);
	}

	@Override
	public void deleteContentTaxonomyCourseAssoc(Long contentId) {
		Query query = getSession().createQuery(DELETE_CONTENT_TAXONOMY_COURSE_ASSOC);
		query.setParameter(CONTENT_ID, contentId);
		query.executeUpdate();
	}

	@Override
	public void deleteContentMetaAssoc(Long contentId, String key) {
		Query query = getSession().createSQLQuery(DELETE_CONTENT_META_ASSOC);
		query.setParameter(CONTENT_ID, contentId);
		query.setParameter(KEY, key);
		query.executeUpdate();
	}

	@Override
	public void deleteContentSubdomainAssoc(Long contentId) {
		Query query = getSession().createQuery(DELETE_CONTENT_SUBDOMAIN_ASSOC);
		query.setParameter(CONTENT_ID, contentId);
		query.executeUpdate();
	}

	@Override
	public List<ContentMetaAssociation> getContentMetaAssociation(String gooruOid, String key) {
		Query query = getSession().createQuery(GET_CONTENT_META_ASSOCATION);
		query.setParameter(GOORU_OID, gooruOid);
		query.setParameter(KEY, key);
		return list(query);
	}
	
	public List<ContentMetaAssociation> getContentMetaAssoc(Long contentId) {
		Query query = getSession().createQuery(CONTENT_META_ASSOC);
        	query.setParameter(CONTENT_ID, contentId);
		return list(query);
	}

	@Override
	public List<Content> getContentListById(List<String> gooruOids) {
		Query query = getSession().createQuery(CONTENT_LIST);
    	query.setParameterList(CONTENT_ID, gooruOids);
	    return list(query);
	}
}
