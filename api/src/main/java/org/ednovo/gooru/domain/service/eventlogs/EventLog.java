package org.ednovo.gooru.domain.service.eventlogs;

import org.ednovo.gooru.core.api.model.SessionContextSupport;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventLog implements ConstantProperties, ParameterProperties{

	public static final Logger LOGGER = LoggerFactory.getLogger(CourseEventLog.class);
	
	public final static String CLASS_ITEM_MOVE = "class.item.move";

	public static final String CLASS_ITEM_DELETE = "class.item.delete";
	
	public static final String CLASS_GOORU_IDS = "classGooruIds";
	
	public static final String CLASS_ITEM_EDIT = "class.item.edit";
	
	//lesson
	public static final String SHELF_COURSE_LESSON = "shelf.course.unit.lesson";

	public static final String CLASS_COURSE_LESSON = "class.course.unit.lesson";
	
	//unit
	public static final String SHELF_COURSE_UNIT = "shelf.course.unit";

	public static final String CLASS_COURSE_UNIT = "class.course.unit";
	
	//course
	public static final String CLASS_COURSE = "class.course";

	public static final String SHELF_COURSE = "shelf.course";
	
	//collection
	public static final String SHELF_COURSE_COLLECTION = "shelf.course.unit.lesson.collection";

	public static final String CLASS_COURSE_COLLECTION = "class.course.unit.lesson.collection";

	public static final String SHELF_COURSE_ASSESSMENT = "shelf.course.unit.lesson.assessment";

	public static final String CLASS_COURSE_ASSESSMENT = "class.course.unit.lesson.assessment";

	public static final String SHELF_COURSE_ASSESSMENT_URL = "shelf.course.unit.lesson.assessment-url";

	public static final String CLASS_COURSE_ASSESSMENT_URL = "class.course.unit.lesson.assessment-url";

	public static final String SHELF_COURSE_RESOURCE = "shelf.course.unit.lesson.collection.resource";

	public static final String SHELF_COURSE_QUESTION = "shelf.course.unit.lesson.collection.question";
	
	public static final String[] EXCLUDES = {"*.class"};

	public void putValue(String entityName, String key, String value) throws JSONException {
		JSONObject log = getLogParameter(entityName);
		log.put(key, value);
		SessionContextSupport.getLog().put(entityName,log.toString());
	}
	
	public JSONObject getLogParameter(String key) throws JSONException {
		return SessionContextSupport.getLog().get(key) != null ? new JSONObject(SessionContextSupport.getLog().get(key).toString()) : new JSONObject();
	}
}
