package org.ednovo.gooru.domain.service;

import java.util.List;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.CollectionType;
import org.ednovo.gooru.core.api.model.Content;
import org.ednovo.gooru.core.api.model.Sharing;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.messenger.IndexProcessor;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionDao;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
public class CollectionDeleteHandler implements ParameterProperties {

	@Autowired
	private CollectionDao collectionDao;

	@javax.annotation.Resource(name = "sessionFactory")
	private SessionFactory sessionFactory;

	@javax.annotation.Resource(name = "transactionManager")
	private HibernateTransactionManager transactionManager;

	@Autowired
	protected IndexProcessor indexProcessor;

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionDeleteHandler.class);

	protected TransactionStatus initTransaction(String name, boolean isReadOnly) {

		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName(AUTHENTICATE_USER);
		if (isReadOnly) {
			def.setReadOnly(isReadOnly);
		} else {
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		}

		return getTransactionManager().getTransaction(def);

	}

	public void deleteCourse(final String courseId) {
		Collection course = getCollectionDao().getCollectionWithoutDeleteCheck(courseId);
		if (course != null) {
			List<CollectionItem> courseItems = getCollectionDao().getCollectionItems(courseId);
			if (courseItems != null) {
				for (CollectionItem collectionItem : courseItems) {
					deleteUnit(collectionItem.getContent().getGooruOid());
				}
			}
			getCollectionDao().remove(course);
		}
	}

	public void deleteUnit(final String unitId) {
		Collection unit = getCollectionDao().getCollectionWithoutDeleteCheck(unitId);
		if (unit != null) {
			List<CollectionItem> unitItems = getCollectionDao().getCollectionItems(unitId);
			if (unitItems != null) {
				for (CollectionItem collectionItem : unitItems) {
					deleteLesson(collectionItem.getContent().getGooruOid());
				}
			}
			getCollectionDao().remove(unit);
		}
	}

	public void deleteLesson(final String lessonId) {
		Collection lesson = getCollectionDao().getCollectionWithoutDeleteCheck(lessonId);
		if (lesson != null) {
			List<CollectionItem> lessonItems = getCollectionDao().getCollectionItems(lessonId);
			if (lessonItems != null) {
				for (CollectionItem collectionItem : lessonItems) {
					deleteCollection(collectionItem.getContent().getGooruOid());
				}
			}
			getCollectionDao().remove(lesson);
		}
	}

	public void deleteCollection(final String collectionId) {
		Collection collection = getCollectionDao().getCollectionWithoutDeleteCheck(collectionId);
		if (collection != null) {
			List<CollectionItem> collectionItems = getCollectionDao().getCollectionItems(collectionId);
			if (collectionItems != null) {
				for (CollectionItem collectionItem : collectionItems) {
					if (collectionItem.getContent().getSharing().equalsIgnoreCase(Sharing.PUBLIC.getSharing())) {
						getCollectionDao().remove(collectionItem);
					} else {
						Content content = collectionItem.getContent();
						getCollectionDao().remove(content);
					}
				}
			}
			try {
				getIndexProcessor().indexByKafkaQueue(collection.getGooruOid(), IndexProcessor.DELETE, SCOLLECTION, false, false);
			} catch(Exception e) { 
				LOGGER.debug("Failed to push the  deleted content details to kafka queue.");
			}
			getCollectionDao().remove(collection);
		}
	}

	public void deleteContent(String gooruOid, String collectionType) {
		TransactionStatus transactionStatus = null;
		Session session = null;
		try {
			transactionStatus = initTransaction(VALIDATE_RESOURCE, false);
			session = getSessionFactory().openSession();
			if (collectionType.equalsIgnoreCase(CollectionType.COURSE.getCollectionType())) {
				deleteCourse(gooruOid);
			} else if (collectionType.equalsIgnoreCase(CollectionType.UNIT.getCollectionType())) {
				deleteUnit(gooruOid);
			} else if (collectionType.equalsIgnoreCase(CollectionType.LESSON.getCollectionType())) {
				deleteLesson(gooruOid);
			} else if (collectionType.equalsIgnoreCase(CollectionType.COLLECTION.getCollectionType()) || collectionType.equalsIgnoreCase(CollectionType.ASSESSMENT.getCollectionType()) || collectionType.equalsIgnoreCase(CollectionType.ASSESSMENT_URL.getCollectionType())) {
				deleteCollection(gooruOid);
			}
			getTransactionManager().commit(transactionStatus);
		} catch (Exception ex) {
			LOGGER.error("Failed  to delete content : " + gooruOid, ex);
			getTransactionManager().rollback(transactionStatus);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	public CollectionDao getCollectionDao() {
		return collectionDao;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public HibernateTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public IndexProcessor getIndexProcessor() {
		return indexProcessor;
	}
}
