/*
 *JobRepositoryHibernate.java
 * gooru-api
 * Created by Gooru on 2014
 * Copyright (c) 2014 Gooru. All rights reserved.
 * http://www.goorulearning.org/
 *      
 * Permission is hereby granted, free of charge, to any 
 * person obtaining a copy of this software and associated 
 * documentation. Any one can use this software without any 
 * restriction and can use without any limitation rights 
 * like copy,modify,merge,publish,distribute,sub-license or 
 * sell copies of the software.
 * The seller can sell based on the following conditions:
 * 
 * The above copyright notice and this permission notice shall be   
 * included in all copies or substantial portions of the Software. 
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY    
 *  KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE  
 *  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR   
 *  PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS 
 *  OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 *  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT 
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
 *  THE SOFTWARE.
 */

package org.ednovo.gooru.infrastructure.persistence.hibernate;

import java.util.List;
import java.util.Map;

import org.ednovo.gooru.core.api.model.InviteUser;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Repository;

@Repository
public class InviteRepositoryHibernate extends BaseRepositoryHibernate implements InviteRepository, ParameterProperties, ConstantProperties {

	private final static String INVITE_USERS = "select   email, username, user_uid as gooruUId from invite_user inner join  custom_table_value ctv on status_id = ctv.custom_table_value_id  left join identity i on i.external_id = email left join user u on u.gooru_uid =  i.user_uid where gooru_oid =:gooruOid and ctv.key_value =:key";

	private final static String INVITE_USERS_COUNT = "select count(1) as count from invite_user inner join  custom_table_value ctv on status_id = ctv.custom_table_value_id  left join identity i on i.external_id = email left join user u on u.gooru_uid =  i.user_uid where gooru_oid =:gooruOid and ctv.key_value =:key";

	private final static String DELETE_USER_INVITE = "delete from invite_user where gooru_oid =:gooruOid and email =:email";

	@Override
	public InviteUser findInviteUserById(String mailId, String gooruOid, String status) {
		String hql = "from InviteUser iu where iu.emailId=:mailId and iu.gooruOid=:gooruOid  ";
		if (status != null) {
			hql += " and iu.status.value=:pending";
		}
		Query query = getSession().createQuery(hql);
		query.setParameter("gooruOid", gooruOid);
		query.setParameter("mailId", mailId);
		if (status != null) {
			query.setParameter("pending", status);
		}
		return (InviteUser) ((query.list().size() > 0) ? query.list().get(0) : null);
	}

	@Override
	public List<InviteUser> getInviteUsersById(String gooruOid) {
		String hql = "from InviteUser iu where  iu.gooruOid=:gooruOid and iu.status.value=:pending order by createdDate desc";
		Query query = getSession().createQuery(hql);
		query.setParameter("pending", "pending");
		query.setParameter("gooruOid", gooruOid);
		return list(query);
	}

	@Override
	public List<InviteUser> getInviteUserByMail(String mailId, String inviteType) {
		String hql = "from InviteUser iu where  iu.emailId=:mailId and iu.status.value=:pending and iu.invitationType=:inviteType order by createdDate desc";
		Query query = getSession().createQuery(hql);
		query.setParameter("mailId", mailId);
		query.setParameter("pending", "pending");
		query.setParameter("inviteType", "collaborator");
		return list(query);
	}

	@Override
	public Long getInviteUsersCountById(String gooruOid) {
		String hql = "select count(*) from InviteUser iu where  iu.gooruOid=:gooruOid and iu.status.value=:pending";
		Query query = getSession().createQuery(hql);
		query.setParameter("gooruOid", gooruOid);
		query.setParameter("pending", "pending");
		return (Long) query.list().get(0);
	}

	@Override
	public List<Map<String, Object>> getInvitee(String gooruOid, String statusKey, int limit, int offset) {
		Query query = getSession().createSQLQuery(INVITE_USERS);
		query.setParameter(GOORU_OID, gooruOid);
		query.setParameter(KEY, statusKey);
		query.setFirstResult(offset);
		query.setMaxResults(limit != 0 ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	@Override
	public int getInviteeCount(String gooruOid, String statusKey) {
		Query query = getSession().createSQLQuery(INVITE_USERS_COUNT).addScalar(COUNT, StandardBasicTypes.INTEGER);
		query.setParameter(GOORU_OID, gooruOid);
		query.setParameter(KEY, statusKey);
		List<Integer> result = list(query);
		return result != null && result.size() > 0 ? result.get(0) : 0;
	}

	@Override
	public void deleteInviteUser(String gooruOid, String email) {
		Query query = getSession().createSQLQuery(DELETE_USER_INVITE);
		query.setParameter(GOORU_OID, gooruOid);
		query.setParameter(EMAIL, email);
		query.executeUpdate();
	}
}
