/////////////////////////////////////////////////////////////
// EventServiceImpl.java
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
package org.ednovo.gooru.domain.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ednovo.gooru.core.api.model.CustomTableValue;
import org.ednovo.gooru.core.api.model.Event;
import org.ednovo.gooru.core.api.model.EventMapping;
import org.ednovo.gooru.core.api.model.Feedback;
import org.ednovo.gooru.core.api.model.Template;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.application.util.CustomProperties;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.exception.UnauthorizedException;
import org.ednovo.gooru.infrastructure.jira.SOAPClient;
import org.ednovo.gooru.infrastructure.jira.SOAPSession;
import org.ednovo.gooru.infrastructure.persistence.hibernate.EventRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.TemplateRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.customTable.CustomTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventServiceImpl extends BaseServiceImpl implements EventService, ParameterProperties, ConstantProperties {

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private TemplateRepository templateRepository;

	@Autowired
	private CustomTableRepository customTableRepository;

	@Autowired
	private SOAPClient soapClient;

	@Autowired
	private SOAPSession soapSession;

	@Autowired
	private FeedbackService feedbackService;

	private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Event createEvent(final Event event, final User user) {
		rejectIfNull(event.getName(), GL0006, EVENT__NAME);
		final Event eventData = this.getEventRepository().getEventByName(event.getName());
		if (eventData != null) {
			throw new UnauthorizedException("Event name already exists.");
		}
		event.setCreator(user);
		event.setCreatedDate(new Date(System.currentTimeMillis()));
		event.setDisplayName(event.getName().replace(" ", "_"));
		this.getEventRepository().save(event);
		return event;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Event updateEvent(final String id, final Event newEvent) {
		final Event event = this.getEventRepository().getEvent(id);
		rejectIfNull(event, GL0056, EVENT);
		if (newEvent.getName() != null) {
			event.setName(newEvent.getName());
			event.setDisplayName(newEvent.getName().replace(" ", "_"));
		}
		this.getEventRepository().save(event);
		return event;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Event getEvent(final String id) {
		return this.getEventRepository().getEvent(id);
	}

	@Override
	public void deleteEvent(final String eventId) {
		final Event event = this.getEventRepository().getEvent(eventId);
		if (event != null) {
			this.getEventRepository().remove(event);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public EventMapping createEventMapping(final EventMapping eventMapping, final User user) {
		rejectIfNull(eventMapping.getEvent(),GL0006, EVENT_ID);
		rejectIfNull(eventMapping.getEvent().getGooruOid(), GL0006,EVENT_ID);
		rejectIfNull(eventMapping.getTemplate(), GL0006, TEMPLATE_ID);
		rejectIfNull(eventMapping.getTemplate().getGooruOid(),GL0006, TEMPLATE_ID);
		final Event event = this.getEvent(eventMapping.getEvent().getGooruOid());
		rejectIfNull(event, GL0056, EVENT);
		final Template template = this.getTemplateRepository().getTemplate(eventMapping.getTemplate().getGooruOid());
		rejectIfNull(template, GL0056, TEMPLATE);
		eventMapping.setAssociatedBy(user);
		eventMapping.setCreatedDate(new Date(System.currentTimeMillis()));
		eventMapping.setTemplate(template);
		eventMapping.setEvent(event);
		final CustomTableValue customTableValue = this.getCustomTableRepository().getCustomTableValue(CustomProperties.Table.EVENT_STATUS.getTable(),
				(eventMapping.getStatus() != null && eventMapping.getStatus().getValue() != null) ? eventMapping.getStatus().getValue() : CustomProperties.EventStatus.IN_ACTIVE.getStatus());
		rejectIfNull(customTableValue, GL0056, EVENT_STATUS);
		eventMapping.setStatus(customTableValue);
		this.getEventRepository().save(eventMapping);
		return eventMapping;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public EventMapping updateEventMapping(final EventMapping newEventMapping, final User user) {
		rejectIfNull(newEventMapping.getEvent(),GL0006, EVENT_ID);
		rejectIfNull(newEventMapping.getEvent().getGooruOid(), GL0006,EVENT_ID);
		rejectIfNull(newEventMapping.getTemplate(), GL0006, TEMPLATE_ID);
		rejectIfNull(newEventMapping.getTemplate().getGooruOid(), GL0006,TEMPLATE_ID);
		final Event event = this.getEvent(newEventMapping.getEvent().getGooruOid());
		rejectIfNull(event, GL0056, EVENT);
		final Template template = this.getTemplateRepository().getTemplate(newEventMapping.getTemplate().getGooruOid());
		rejectIfNull(template, GL0056, TEMPLATE);
		final EventMapping eventMapping = this.getEventMapping(newEventMapping.getEvent().getGooruOid(), newEventMapping.getTemplate().getGooruOid());
		rejectIfNull(eventMapping, GL0056, EVENT_MAPPING);
		if (newEventMapping.getData() != null) {
			eventMapping.setData(newEventMapping.getData());
		}
		if (newEventMapping.getStatus() != null && newEventMapping.getStatus().getValue() != null) {
			final CustomTableValue customTableValue = this.getCustomTableRepository().getCustomTableValue(CustomProperties.Table.EVENT_STATUS.getTable(), newEventMapping.getStatus().getValue());
			rejectIfNull(customTableValue,GL0056, EVENT_STATUS);
			eventMapping.setStatus(customTableValue);
		}
		return eventMapping;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteEventMapping(String eventUid, final String templateUid) {
		EventMapping eventMapping = this.getEventMapping(eventUid, templateUid);
		if (eventMapping != null) {
			this.getEventRepository().remove(eventMapping);
		}
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public EventMapping getEventMapping(final String eventUid, final String templateUid) {
		return this.getEventRepository().getEventMapping(eventUid, templateUid);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<Event> getEvents(final Integer offset, final Integer limit) {
		return this.getEventRepository().getEvents(offset, limit);
	}

	@Override
	public List<EventMapping> getTemplatesByEvent(final String templateEventID) {
		return this.getEventRepository().getTemplatesByEvent(templateEventID);
	}

	@Override
	public EventMapping getTemplatesByEventName(final String name) {
		return this.getEventRepository().getEventMappingByType(name);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void handleJiraEvent(Map<String, String> fields) {
		final String issueKey = sendMessageToJira(fields);
		if (issueKey != null && fields != null && fields.get(EVENT_TYPE) != null && fields.get(EVENT_TYPE).equals(FEEDBACK)) {
				Feedback newFeedback = new Feedback();
				newFeedback.setReferenceKey(issueKey);
				this.getFeedbackService().updateFeedback(fields.get(IDS), newFeedback,null);			
		}
	}

	private String sendMessageToJira(Map<String, String> standardJiraFields) {
		String issueKey = null;
		try {
			issueKey = this.getSoapClient().createIssue(this.getSoapSession(), null, standardJiraFields);
		} catch (Exception e) {
			LOGGER.debug("Error while connecting JIRA", e);
		}
		return issueKey;
	}

	public EventRepository getEventRepository() {
		return eventRepository;
	}

	public TemplateRepository getTemplateRepository() {
		return templateRepository;
	}

	public CustomTableRepository getCustomTableRepository() {
		return customTableRepository;
	}

	public SOAPClient getSoapClient() {
		return soapClient;
	}

	public SOAPSession getSoapSession() {
		return soapSession;
	}

	public FeedbackService getFeedbackService() {
		return feedbackService;
	}
}
