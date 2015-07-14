package org.ednovo.gooru.domain.service.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.CollectionType;
import org.ednovo.gooru.core.api.model.ContentMeta;
import org.ednovo.gooru.core.api.model.MetaConstants;
import org.ednovo.gooru.core.api.model.Sharing;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class LessonServiceImpl extends AbstractCollectionServiceImpl implements LessonService {

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ActionResponseDTO<Collection> createLesson(String courseId, String unitId, Collection collection, User user) {
		final Errors errors = validateLesson(collection);
		if (!errors.hasErrors()) {
			Collection course = getCollectionDao().getCollectionByType(courseId, COURSE_TYPE);
			rejectIfNull(course, GL0056,404, COURSE);
			Collection parentCollection = getCollectionDao().getCollectionByType(unitId, UNIT_TYPE);
			rejectIfNull(parentCollection, GL0056,404, UNIT);
			collection.setSharing(Sharing.PRIVATE.getSharing());
			collection.setCollectionType(CollectionType.LESSON.getCollectionType());
			createCollection(collection, parentCollection, user);
			Map<String, Object> data = generateLessonMetaData(collection, collection, user);
			data.put(SUMMARY, MetaConstants.LESSON_SUMMARY);
			createContentMeta(collection, data);
			updateUnitMetaDataSummary(course.getContentId(), parentCollection.getContentId());
		}
		return new ActionResponseDTO<Collection>(collection, errors);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void updateLesson(String unitId, String lessonId, Collection newCollection, User user) {
		Collection collection = this.getCollectionDao().getCollection(lessonId);
		rejectIfNull(collection, GL0056,404, LESSON);
		Collection unit = getCollectionDao().getCollectionByType(unitId, UNIT_TYPE);
		rejectIfNull(unit, GL0056,404, UNIT);
		this.updateCollection(collection, newCollection, user);
		if(newCollection.getPosition() != null){
			this.resetSequence(unit, collection.getGooruOid() , newCollection.getPosition(), user.getPartyUid());
		}
		Map<String, Object> data = generateLessonMetaData(collection, newCollection, user);
		if (data != null && data.size() > 0) {
			ContentMeta contentMeta = this.getContentRepository().getContentMeta(collection.getContentId());
			updateContentMeta(contentMeta, data);
		}
	}
	
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteLesson(String courseId, String unitId, String lessonId, User user) {
		CollectionItem lesson = getCollectionDao().getCollectionItem(unitId,lessonId, user.getPartyUid());
		rejectIfNull(lesson, GL0056,404, LESSON);
		reject(this.getOperationAuthorizer().hasUnrestrictedContentAccess(lessonId, user), GL0099, 403, LESSON);
		Collection course = getCollectionDao().getCollectionByType(courseId, COURSE_TYPE);
		rejectIfNull(course, GL0056,404, COURSE);
		Collection unit = getCollectionDao().getCollectionByType(unitId, UNIT_TYPE);
		rejectIfNull(unit, GL0056,404, UNIT);
		this.deleteValidation(lesson.getContent().getContentId(), LESSON);
		this.resetSequence(unitId, lesson.getContent().getGooruOid(), user.getPartyUid());
		this.deleteCollection(lessonId);
		this.updateMetaDataSummary(course.getContentId(), unit.getContentId());
	}
	
	private void updateMetaDataSummary(Long courseId, Long unitId) {
		ContentMeta unitContentMeta = this.getContentRepository().getContentMeta(unitId);
		ContentMeta courseContentMeta = this.getContentRepository().getContentMeta(courseId);
		if (unitContentMeta != null) {
			updateSummaryMeta(unitContentMeta);
		}
		if (courseContentMeta != null) {
			updateSummaryMeta(courseContentMeta);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void updateSummaryMeta(ContentMeta contentMeta) {
		Map<String, Object> metaData = JsonDeserializer.deserialize(contentMeta.getMetaData(), new TypeReference<Map<String, Object>>() {
		});
		Map<String, Object> summary = (Map<String, Object>) metaData.get(SUMMARY);
		int lessonCount =  ((Number) summary.get(MetaConstants.LESSON_COUNT)).intValue() -1;
		summary.put(MetaConstants.LESSON_COUNT, lessonCount);
		metaData.put(SUMMARY, summary);
		updateContentMeta(contentMeta, metaData);
	}
	
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map<String, Object> getLesson(String lessonId) {
		return this.getCollection(lessonId, CollectionType.UNIT.getCollectionType());
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<Map<String, Object>> getLessons(String unitId, int limit, int offset) {
		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put(COLLECTION_TYPE, LESSON_TYPE);
		filters.put(PARENT_GOORU_OID, unitId);
		List<Map<String, Object>> results = this.getCollections(filters, limit, offset);
		List<Map<String, Object>> lessons = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> lesson : results) {
			lessons.add(mergeMetaData(lesson));
		}
		return lessons;
	}

	@SuppressWarnings("unchecked")
	private void updateUnitMetaDataSummary(Long courseId, Long unitId) {
		ContentMeta unitContentMeta = this.getContentRepository().getContentMeta(unitId);
		ContentMeta courseContentMeta = this.getContentRepository().getContentMeta(courseId);
		int lessonCount = this.getCollectionDao().getCollectionItemCount(unitId, CollectionType.LESSON.getCollectionType());
		if (unitContentMeta != null) {
			Map<String, Object> metaData = JsonDeserializer.deserialize(unitContentMeta.getMetaData(), new TypeReference<Map<String, Object>>() {
			});

			Map<String, Object> summary = (Map<String, Object>) metaData.get(SUMMARY);
			summary.put(MetaConstants.LESSON_COUNT, lessonCount);
			metaData.put(SUMMARY, summary);
			updateContentMeta(unitContentMeta, metaData);
		}
		if (courseContentMeta != null) {
			Map<String, Object> metaData = JsonDeserializer.deserialize(courseContentMeta.getMetaData(), new TypeReference<Map<String, Object>>() {
			});
			Map<String, Object> summary = (Map<String, Object>) metaData.get(SUMMARY);
			int courseLessonCount = ((Number) summary.get(MetaConstants.LESSON_COUNT)).intValue() + 1;
			summary.put(MetaConstants.LESSON_COUNT, courseLessonCount);
			metaData.put(SUMMARY, summary);
			updateContentMeta(courseContentMeta, metaData);
		}
	}

	private Map<String, Object> generateLessonMetaData(Collection collection, Collection newCollection, User user) {
		Map<String, Object> data = new HashMap<String, Object>();
		if (newCollection.getStandardIds() != null) {
			List<Map<String, Object>> standards = updateContentCode(collection, newCollection.getStandardIds(), MetaConstants.CONTENT_CLASSIFICATION_STANDARD_TYPE_ID);
			data.put(STANDARDS, standards);
		}
		return data;
	}

	private Errors validateLesson(final Collection collection) {
		final Errors errors = new BindException(collection, COLLECTION);
		if (collection != null) {
			rejectIfNullOrEmpty(errors, collection.getTitle(), TITLE, GL0006, generateErrorMessage(GL0006, TITLE));
		}
		return errors;
	}

}
