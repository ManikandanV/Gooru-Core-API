package org.ednovo.gooru.domain.service.eventlogs;

import java.util.List;

import net.sf.json.JSONArray;

import org.ednovo.gooru.application.util.SerializerUtil;
import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.SessionContextSupport;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.infrastructure.persistence.hibernate.ClassRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnitEventLog extends EventLog {

	@Autowired
	private ClassRepository classRepository;

	public void unitEventLogs(String courseId, CollectionItem unit, User user, Collection data, String action) {
		try {
			JSONObject context = SessionContextSupport.getLog().get(CONTEXT) != null ? new JSONObject(SessionContextSupport.getLog().get(CONTEXT).toString()) : new JSONObject();
			context.put(CONTENT_GOORU_ID, unit.getContent().getGooruOid());
			context.put(PARENT_GOORU_ID, courseId);
			SessionContextSupport.putLogParameter(CONTEXT, context.toString());
			JSONObject payLoadObject = SessionContextSupport.getLog().get(PAY_LOAD_OBJECT) != null ? new JSONObject(SessionContextSupport.getLog().get(PAY_LOAD_OBJECT).toString()) : new JSONObject();
			payLoadObject.put(TYPE, UNIT);
			payLoadObject.put(COURSE_GOORU_ID, courseId);
			payLoadObject.put(UNIT_GOORU_ID, unit.getContent().getGooruOid());
			List<String> classUids = this.getClassRepository().getClassUid(courseId);
			
			if (!classUids.isEmpty()) {
				JSONArray classIds = new JSONArray();
				classIds.addAll(classUids);
				payLoadObject.put(CLASS_GOORU_IDS, classIds);
				SessionContextSupport.putLogParameter(EVENT_NAME, action.equalsIgnoreCase(CREATE)? ITEM_CREATE:CLASS_ITEM_DELETE);
				payLoadObject.put(ITEM_TYPE, CLASS_COURSE_UNIT);
			} else {
				SessionContextSupport.putLogParameter(EVENT_NAME, action.equalsIgnoreCase(CREATE)? ITEM_CREATE:ITEM_DELETE);
				payLoadObject.put(ITEM_TYPE, SHELF_COURSE_UNIT);
			}
			
			payLoadObject.put(MODE, action);
			payLoadObject.put(ITEM_SEQUENCE,unit.getItemSequence());
			if(action.equalsIgnoreCase(CREATE)){
				payLoadObject.put(DATA, SerializerUtil.serializeToJson(data, EXCLUDES, true, true));
				payLoadObject.put(ITEM_ID,unit.getCollectionItemId());
			} else if (action.equalsIgnoreCase(EDIT)) {
				if (!classUids.isEmpty()) {
					JSONArray classIds = new JSONArray();
					classIds.addAll(classUids);
					payLoadObject.put(CLASS_GOORU_IDS, classIds);
					SessionContextSupport.putLogParameter(EVENT_NAME, ITEM_EDIT);
					payLoadObject.put(ITEM_TYPE, CLASS_COURSE_UNIT);
				} else {
					SessionContextSupport.putLogParameter(EVENT_NAME, ITEM_EDIT);
					payLoadObject.put(ITEM_TYPE, SHELF_COURSE_UNIT);
				}
				payLoadObject.put(ITEM_ID, unit.getCollectionItemId());
			}
			SessionContextSupport.putLogParameter(PAY_LOAD_OBJECT, payLoadObject.toString());
			JSONObject session = SessionContextSupport.getLog().get(SESSION) != null ? new JSONObject(SessionContextSupport.getLog().get(SESSION).toString()) : new JSONObject();
			session.put(ORGANIZATION_UID, user != null && user.getOrganization() != null ? user.getOrganization().getPartyUid() : null);
			SessionContextSupport.putLogParameter(SESSION, session.toString());
		} catch (Exception e) {
			LOGGER.error(_ERROR, e);
		}
	}

	public ClassRepository getClassRepository() {
		return classRepository;
	}

}
