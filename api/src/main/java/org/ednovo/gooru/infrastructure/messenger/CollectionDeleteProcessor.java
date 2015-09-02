package org.ednovo.gooru.infrastructure.messenger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.ednovo.gooru.domain.service.CollectionDeleteHandler;
import org.ednovo.gooru.kafka.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class CollectionDeleteProcessor {

	@Autowired
	private HibernateTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Autowired
	private CollectionDeleteHandler collectionDeleteHandler;

	private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
	

	@PostConstruct
	public void init() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public void deleteContent(final String gooruOid, final String collectionType) {
		getExecutorservice().execute(new Runnable() {
			public void run() {
				getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						getCollectionDeleteHandler().deleteContent(gooruOid, collectionType);
					}
				});
			}
		});
	}

	
	public CollectionDeleteHandler getCollectionDeleteHandler() {
		return collectionDeleteHandler;
	}

	public static ExecutorService getExecutorservice() {
		return executorService;
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}
}
