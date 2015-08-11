/////////////////////////////////////////////////////////////
// PartyServiceImpl.java
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.ednovo.gooru.application.util.TaxonomyUtil;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.Party;
import org.ednovo.gooru.core.api.model.PartyCategoryType;
import org.ednovo.gooru.core.api.model.PartyCustomField;
import org.ednovo.gooru.core.api.model.Profile;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.api.model.UserGroupSupport;
import org.ednovo.gooru.core.constant.ConfigConstants;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.exception.NotFoundException;
import org.ednovo.gooru.domain.service.eventlogs.UserEventLog;
import org.ednovo.gooru.domain.service.redis.RedisService;
import org.ednovo.gooru.domain.service.setting.SettingService;
import org.ednovo.gooru.infrastructure.messenger.IndexHandler;
import org.ednovo.gooru.infrastructure.messenger.IndexProcessor;
import org.ednovo.gooru.infrastructure.persistence.hibernate.party.PartyRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.taxonomy.TaxonomyRespository;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

@Service
public class PartyServiceImpl extends BaseServiceImpl implements PartyService, ParameterProperties, ConstantProperties {

	@Autowired
	private PartyRepository partyRepository;

	@Autowired
	private UserEventLog userEventlog;

	@Autowired
	private TaxonomyRespository taxonomyRespository;

	@Autowired
	private SettingService settingService;

	@Autowired
	private RedisService redisService;
	
	@Autowired
	private IndexHandler indexHandler;

	private static final Logger LOGGER = LoggerFactory.getLogger(PartyServiceImpl.class);

	private static ResourceBundle userDefaultCustomAttributes = ResourceBundle.getBundle("properties/userDefaultCustomAttributes");

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ActionResponseDTO<PartyCustomField> createPartyCustomField(String partyId, PartyCustomField partyCustomField, User user) {
		if (partyId != null && partyId.equalsIgnoreCase(MY)) {
			partyId = user.getUserUid();
		}
		final Party party = getPartyRepository().findPartyById(partyId);
		final Errors error = validatePartyCustomField(partyCustomField, party);
		if (!error.hasErrors()) {
			partyCustomField.setPartyUid(partyId);
			getPartyRepository().save(partyCustomField);
			indexHandler.setReIndexRequest(partyId, IndexProcessor.INDEX, USER, null, false, false);	
			
			
		}
		return new ActionResponseDTO<PartyCustomField>(partyCustomField, error);
	}

	private Errors validatePartyCustomField(PartyCustomField partyCustomField, final Party party) {
		final Map<Object, String> partyCategory = getCategory();
		final Errors errors = new BindException(partyCustomField, PARTY_CUSTOM_FIELD);
		rejectIfNull(errors, party, PARTY, GL0056, generateErrorMessage(GL0056, PARTY));
		rejectIfInvalidType(errors, partyCustomField.getCategory(), CATEGORY, GL0007, generateErrorMessage(GL0007, CATEGORY), partyCategory);
		return errors;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<PartyCustomField> getPartyCustomFields(String partyId, final PartyCustomField partyCustomField, User user) {

		return getPartyRepository().getPartyCustomFields(partyId, partyCustomField != null ? partyCustomField.getOptionalKey() : null, partyCustomField != null ? partyCustomField.getCategory() : null);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public PartyCustomField getPartyCustomeField(String partyId, final String optionalKey, User user) {

		return getPartyRepository().getPartyCustomField(partyId, optionalKey);
	}

	@Override
	public Profile getUserDateOfBirth(String partyId, final User user) {
		return getPartyRepository().getUserDateOfBirth(partyId, user);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public PartyCustomField updatePartyCustomField(String partyId, final PartyCustomField newPartyCustomField, final User user) {
		PartyCustomField partyCustomField = null;
		if (partyId != null && partyId.equalsIgnoreCase(MY)) {
			partyId = user.getUserUid();
		}
		partyCustomField = this.getPartyRepository().getPartyCustomField(partyId, newPartyCustomField.getOptionalKey());
		if (partyCustomField == null) {
			partyCustomField = new PartyCustomField(partyId, USER_META, newPartyCustomField.getOptionalKey(), newPartyCustomField.getOptionalValue());
		} else {
			if (newPartyCustomField.getOptionalValue() != null) {
				partyCustomField.setOptionalValue(newPartyCustomField.getOptionalValue());
			}
			if (newPartyCustomField.getCategory() != null) {
				partyCustomField.setCategory(newPartyCustomField.getCategory());
			}
		}

		this.getPartyRepository().save(partyCustomField);
		if (newPartyCustomField.getOptionalKey() != null && newPartyCustomField.getOptionalKey().equalsIgnoreCase(SHOW_PROFILE_PAGE)) {
			indexHandler.setReIndexRequest(partyId, IndexProcessor.INDEX, USER, null, true, false);					
		} else {
			indexHandler.setReIndexRequest(partyId, IndexProcessor.INDEX, USER, null, false, false);					
		}
		if (newPartyCustomField.getOptionalKey() != null && newPartyCustomField.getOptionalKey().equalsIgnoreCase(USER_TAXONOMY_ROOT_CODE)) {
			this.redisService.deleteKey(SESSION_TOKEN_KEY + UserGroupSupport.getSessionToken());
		}
		this.getUserEventlog().getEventLogs(true, false, user, null, true, true);
		return partyCustomField;
	}

	private Errors validateUpdatePartyCustomField(final PartyCustomField partyCustomField, final PartyCustomField newPartyCustomField) {
		final Errors errors = new BindException(partyCustomField, PARTY_CUSTOM_FIELD);
		rejectIfNull(errors, newPartyCustomField, PARTY_CUSTOM_FIELD, GL0056, generateErrorMessage(GL0056, PARTY_CUSTOM_FIELD));
		return errors;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteCustomField(String partyId, final PartyCustomField newpartyCustomField, User user) throws Exception {
		if (newpartyCustomField != null) {
			if (partyId != null && partyId.equalsIgnoreCase(MY)) {
				partyId = user.getUserUid();
			}

			final PartyCustomField partyCustomField = getPartyRepository().getPartyCustomField(partyId, newpartyCustomField.getOptionalKey());
			if (partyCustomField != null) {
				getPartyRepository().deletePartyCustomField(partyId, partyCustomField.getOptionalKey());
			} else {
				throw new NotFoundException(generateErrorMessage(GL0056, PARTY_CUSTOMFIELD), GL0056);
			}
		}
	}

	private Map<Object, String> getCategory() {
		final Map<Object, String> partyCategory = new HashMap<Object, String>();
		partyCategory.put(PartyCategoryType.USER_INFO.getpartyCategoryType(), CATEGORY);
		partyCategory.put(PartyCategoryType.USER_META.getpartyCategoryType(), CATEGORY);
		partyCategory.put(PartyCategoryType.ORGANIZATION_INFO.getpartyCategoryType(), CATEGORY);
		partyCategory.put(PartyCategoryType.ORGANIZATION_META.getpartyCategoryType(), CATEGORY);
		partyCategory.put(PartyCategoryType.GROUP_INFO.getpartyCategoryType(), CATEGORY);
		partyCategory.put(PartyCategoryType.GROUP_META.getpartyCategoryType(), CATEGORY);
		return partyCategory;
	}

	@Override
	public List<PartyCustomField> createUserDefaultCustomAttributes(String partyId, User user) {
		final Enumeration<String> keys = userDefaultCustomAttributes.getKeys();
		if (partyId != null && partyId.equalsIgnoreCase(MY)) {
			partyId = user.getUserUid();
		}
		List<PartyCustomField> partyCustomFields = null;
		final Party party = getPartyRepository().findPartyById(partyId);
		if (keys != null && party != null) {
			partyCustomFields = new ArrayList<PartyCustomField>();
			while (keys.hasMoreElements()) {
				final PartyCustomField partyCustomField = new PartyCustomField();
				partyCustomField.setCategory(PartyCategoryType.USER_META.getpartyCategoryType());
				final String key = keys.nextElement();
				partyCustomField.setOptionalKey(key);
				partyCustomField.setPartyUid(partyId);
				partyCustomField.setOptionalValue(userDefaultCustomAttributes.getString(key));
				partyCustomFields.add(partyCustomField);
			}
			getPartyRepository().saveAll(partyCustomFields);
		}
		return partyCustomFields;
	}

	@Override
	public void createTaxonomyCustomAttributes(final String partyId, User user) {
		final String taxonomyList = this.getTaxonomyRespository().getFindTaxonomyList(settingService.getConfigSetting(ConfigConstants.GOORU_EXCLUDE_TAXONOMY_PREFERENCE, 0, TaxonomyUtil.GOORU_ORG_UID));
		if (taxonomyList != null) {
			final PartyCustomField partyCustomField = new PartyCustomField();
			partyCustomField.setCategory(PartyCategoryType.USER_TAXONOMY.getpartyCategoryType());
			partyCustomField.setOptionalKey(USER_TAXONOMY_ROOT_CODE);
			partyCustomField.setPartyUid(partyId);
			partyCustomField.setOptionalValue(taxonomyList);
			getPartyRepository().save(partyCustomField);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<PartyCustomField> createPartyDefaultCustomAttributes(String partyId, User user, final String type) {
		if (type != null && type.equalsIgnoreCase(USER_TYPE)) {
			return createUserDefaultCustomAttributes(partyId, user);
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<Map<Object, Object>> getPartyDetails() {
		return this.getPartyRepository().getPartyDetails();
	}

	public PartyRepository getPartyRepository() {
		return partyRepository;
	}

	public TaxonomyRespository getTaxonomyRespository() {
		return taxonomyRespository;
	}

	public UserEventLog getUserEventlog() {
		return userEventlog;
	}

}
