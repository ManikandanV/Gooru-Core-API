/////////////////////////////////////////////////////////////
// OrganizationServiceImpl.java
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
package org.ednovo.gooru.domain.service.party;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.Application;
import org.ednovo.gooru.core.api.model.CustomTableValue;
import org.ednovo.gooru.core.api.model.Organization;
import org.ednovo.gooru.core.api.model.OrganizationSetting;
import org.ednovo.gooru.core.api.model.PartyPermission;
import org.ednovo.gooru.core.api.model.PartyType;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.api.model.UserRole;
import org.ednovo.gooru.core.api.model.UserRole.UserRoleType;
import org.ednovo.gooru.core.api.model.UserRoleAssoc;
import org.ednovo.gooru.core.application.util.CustomProperties;
import org.ednovo.gooru.core.constant.ConfigConstants;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.exception.BadRequestException;
import org.ednovo.gooru.domain.service.BaseServiceImpl;
import org.ednovo.gooru.domain.service.CountryRepository;
import org.ednovo.gooru.domain.service.PartyService;
import org.ednovo.gooru.domain.service.apikey.ApplicationService;
import org.ednovo.gooru.domain.service.authentication.AccountService;
import org.ednovo.gooru.domain.service.search.SearchResults;
import org.ednovo.gooru.domain.service.setting.SettingService;
import org.ednovo.gooru.domain.service.user.UserService;
import org.ednovo.gooru.domain.service.user.impl.UserServiceImpl;
import org.ednovo.gooru.domain.service.userManagement.UserManagementService;
import org.ednovo.gooru.infrastructure.persistence.hibernate.OrganizationSettingRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.UserRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.customTable.CustomTableRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.party.OrganizationRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.storage.StorageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

@Service
public class OrganizationServiceImpl extends BaseServiceImpl implements OrganizationService, ParameterProperties, ConstantProperties {

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationSettingRepository organizationSettingRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private PartyService partyService;

	@Autowired
	private SettingService settingService;

	@Autowired
	private StorageRepository storageRepository;

	@Autowired
	private UserManagementService userManagementService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private CustomTableRepository customTableRepository;

	@Autowired
	private UserRepository userRepository;
	

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public Organization getOrganizationById(String organizationUid) {
		return (Organization) organizationRepository.get(Organization.class, organizationUid);
	}

	@Override
	public Organization getOrganizationByName(String partyName) {
		return organizationRepository.getOrganizationByName(partyName);
	}

	@Override
	public Organization getOrganizationByCode(String organizationCode) {
		return organizationRepository.getOrganizationByCode(organizationCode);
	}

	@Override
	public SearchResults<Organization> listAllOrganizations(Integer offset, Integer limit, Boolean fetchPremiumOrg) {
		List<Organization> organization = this.getOrganizationRepository().getOrganizations(null, null, null, offset, limit, fetchPremiumOrg);
		SearchResults<Organization> result = new SearchResults<Organization>();
		result.setSearchResults(organization);
		result.setTotalHitCount(this.getOrganizationRepository().getOrganizationCount(null, null, null, fetchPremiumOrg));
		return result;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ActionResponseDTO<Organization> saveOrganization(Organization organizationData, User user, HttpServletRequest request) {
		Errors errors = validateNullFields(organizationData);
		rejectIfMaxLimitExceed(400, organizationData.getPartyName(), GL0014, PARTY_NAME, "400");
		Organization newOrganization = new Organization();
		if (!errors.hasErrors()) {
			newOrganization.setPartyName(organizationData.getPartyName());
			String randomString = getRandomString(5);
			newOrganization.setOrganizationCode(randomString);
			newOrganization.setPartyType(PartyType.ORGANIZATION.getType());
			newOrganization.setCreatedOn(new Date(System.currentTimeMillis()));
			newOrganization.setS3StorageArea(storageRepository.getAvailableStorageArea(1));
			newOrganization.setNfsStorageArea(storageRepository.getAvailableStorageArea(2));
			newOrganization.setUserUid(user.getPartyUid());
			
			if (organizationData.getStateProvince() != null && organizationData.getStateProvince().getStateUid() != null) {
				newOrganization.setStateProvince(getCountryRepository().getState(organizationData.getStateProvince().getStateUid()));
			}
			if (organizationData.getType() != null && organizationData.getType().getValue() != null) {
				CustomTableValue type = this.getCustomTableRepository().getCustomTableValue(CustomProperties.Table.ORGANIZATION_CATEGORY.getTable(), organizationData.getType().getValue());
				rejectIfNull(type, GL0056, TYPE);
				newOrganization.setType(type);
			}

			if (organizationData.getParentId() != null) {
				newOrganization.setParentOrganization(this.getOrganizationById(organizationData.getParentId()));
			}
			organizationRepository.save(newOrganization);
			updateOrgSetting(newOrganization);
			User newUser = new User();
			newUser.setOrganization(newOrganization);
			newUser.setFirstName(FIRST);
			newUser.setLastName(LAST);
			newUser.setPartyUid(ANONYMOUS_ + randomString);
			newUser.setUsername(ANONYMOUS_ + randomString);
			newUser.setEmailId(ANONYMOUS_ + randomString + AT_GMAIL_DOT_COM);
			Application application = new Application();
			application.setTitle(newOrganization.getPartyName());
			application.setUrl(HTTP_URL + newOrganization.getPartyName() + DOT_COM);
			try {
				User newOrgUser = new User();
				newOrgUser = userManagementService.createUser(newUser, null, null, 1, null, null, null, null, null, null, null, null, request, null, null);
				OrganizationSetting newOrganizationSetting = new OrganizationSetting();
				newOrganizationSetting.setOrganization(newOrganization);
				newOrganizationSetting.setKey(ANONYMOUS);
				newOrganizationSetting.setValue(newOrgUser.getPartyUid());
				organizationSettingRepository.save(newOrganizationSetting);
				application.setOrganization(newOrganization);
				applicationService.createApplication(application, newOrgUser);
				accountService.createSessionToken(newOrgUser, application.getKey(), request);

			} catch (Exception e) {
				LOGGER.debug("Error" + e);
			}
		}
		return new ActionResponseDTO<Organization>(newOrganization, errors);
	}
	

	private void updateOrgSetting(Organization newOrganization) {
		OrganizationSetting newOrganizationSetting = new OrganizationSetting();
		newOrganizationSetting.setOrganization(newOrganization);
		newOrganizationSetting.setKey(ConfigConstants.ACCESS_GOORU_CONTENT);
		newOrganizationSetting.setValue("1");
		organizationSettingRepository.save(newOrganizationSetting);
	}

	private Errors validateNullFields(Organization organization) {
		final Errors errors = new BindException(organization, ORGANIZATION);
		rejectIfNull(errors, organization, PARTY_NAME, GL0056, generateErrorMessage(GL0056, PARTY_NAME));
		return errors;

	}

	@Override
	public ActionResponseDTO<OrganizationSetting> saveOrUpdateOrganizationSetting(String organizationUid, OrganizationSetting organizationSetting) throws Exception {
		boolean isUpdateOrInsertDone = false;
		if (organizationUid != null && organizationSetting != null && organizationSetting.getValue() != null) {
			OrganizationSetting existingOrganizationSetting = organizationSettingRepository.getOrganizationSettings(organizationUid, organizationSetting.getKey());
			if (existingOrganizationSetting == null) {
				OrganizationSetting newOrganizationSetting = new OrganizationSetting();
				newOrganizationSetting.setOrganization(organizationRepository.getOrganizationByUid(organizationUid));
				newOrganizationSetting.setKey(organizationSetting.getKey());
				newOrganizationSetting.setValue(organizationSetting.getValue());
				organizationSettingRepository.save(newOrganizationSetting);
				isUpdateOrInsertDone = true;
			} else {
				if (existingOrganizationSetting != null) {
					existingOrganizationSetting.setValue(organizationSetting.getValue());
					organizationSetting.setOrganization(organizationRepository.getOrganizationByUid(organizationUid));
					organizationSettingRepository.save(existingOrganizationSetting);
					organizationSetting = existingOrganizationSetting;
					isUpdateOrInsertDone = true;
				}
			}
			if (isUpdateOrInsertDone) {
				settingService.resetOrganizationSettings(organizationSetting.getKey());
			}
		} else {
			throw new BadRequestException("Values should not be null !");
		}

		return new ActionResponseDTO<OrganizationSetting>(organizationSetting, null);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ActionResponseDTO<Organization> updateOrganization(Organization newOrganization, String existingOrganizationUid, User apiCaller) throws Exception {
		Organization existingOrganization = null;
		Errors errors = validateNullFields(newOrganization);
		if (!errors.hasErrors()) {
			existingOrganization = organizationRepository.getOrganizationByUid(existingOrganizationUid);
			if (existingOrganization != null) {
				existingOrganization.setPartyName(newOrganization.getPartyName());
				existingOrganization.setLastModifiedOn(new Date(System.currentTimeMillis()));
				existingOrganization.setLastModifiedUserUid(apiCaller.getPartyUid());
				organizationRepository.save(existingOrganization);
			}
		}
		return new ActionResponseDTO<Organization>(existingOrganization, errors);
	}

	public static String getRandomString(int length) {
		String randomStr = UUID.randomUUID().toString();
		while (randomStr.length() < length) {
			randomStr += UUID.randomUUID().toString();
		}
		return randomStr.substring(0, length);
	}

	@Override
	public User updateUserOrganization(String organizationUid, String gooruUid) throws Exception {
		User user = null;
		if (organizationUid != null && gooruUid != null) {
			Organization organization = organizationRepository.getOrganizationByUid(organizationUid);
			user = userService.findByGooruId(gooruUid);
			if (organization != null && user != null) {
				if (!userService.isContentAdmin(user)) {
					UserRole userRole = userService.findUserRoleByName(UserRoleType.CONTENT_ADMIN.getType());
					UserRoleAssoc userRoleAssoc = new UserRoleAssoc();
					userRoleAssoc.setRole(userRole);
					userRoleAssoc.setUser(user);
					organizationRepository.save(userRoleAssoc);
				}
				PartyPermission partyPermission = new PartyPermission();
				partyPermission.setParty(organization);
				partyPermission.setPermittedParty(user);
				partyPermission.setPermission(ORG_ADMIN_KEY);
				partyPermission.setValidFrom(new Date(System.currentTimeMillis()));
				organizationRepository.save(partyPermission);
			}
			return user;
		}
		return user;
	}

	@Override
	public OrganizationSetting getOrganizationSetting(String organizationUid, String key) throws Exception {
		if (organizationUid != null && key != null) {
			return organizationSettingRepository.getOrganizationSettings(organizationUid, key);
		}
		return null;
	}

	@Override
	public Organization getOrganizationByIdpName(String idpDomainName) {
		if (idpDomainName != null) {
			return (Organization) organizationRepository.getOrganizationByIdpName(idpDomainName);
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public SearchResults<Organization> getOrganizations(String type, String parentOrganizationUid, String stateProvinceId, Integer offset, Integer limit) {
		SearchResults<Organization> result = new SearchResults<Organization>();
		result.setSearchResults(this.getOrganizationRepository().getOrganizations(CustomProperties.Table.ORGANIZATION_CATEGORY.getTable() + UNDER_SCORE + type, parentOrganizationUid, stateProvinceId, offset, limit, false));
		result.setTotalHitCount(this.getOrganizationRepository().getOrganizationCount(CustomProperties.Table.ORGANIZATION_CATEGORY.getTable() + UNDER_SCORE + type, parentOrganizationUid, stateProvinceId, false));
		return result;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public SearchResults<User> getUsersByOrganization(String type, String organizationUid, String parentOrganizationUid, Integer offset, Integer limit) {
		SearchResults<User> result = new SearchResults<User>();
		result.setSearchResults(this.getUserRepository().findUsersByOrganization(type.equalsIgnoreCase(CustomProperties.InstitutionType.SCHOOL_DISTRICT.getInstitutionType()) ? organizationUid : null, type.equalsIgnoreCase(CustomProperties.InstitutionType.SCHOOL.getInstitutionType()) ? parentOrganizationUid : null, offset, limit));
		result.setTotalHitCount(this.getUserRepository().getUsersByOrganizationCount(type.equalsIgnoreCase(CustomProperties.InstitutionType.SCHOOL_DISTRICT.getInstitutionType()) ? organizationUid : null, type.equalsIgnoreCase(CustomProperties.InstitutionType.SCHOOL.getInstitutionType()) ? parentOrganizationUid : null));
		return result;
	}

	public OrganizationRepository getOrganizationRepository() {
		return organizationRepository;
	}

	public CountryRepository getCountryRepository() {
		return countryRepository;
	}

	public CustomTableRepository getCustomTableRepository() {
		return customTableRepository;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

}
