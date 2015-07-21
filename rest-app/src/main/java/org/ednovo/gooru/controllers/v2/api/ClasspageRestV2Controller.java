/////////////////////////////////////////////////////////////
//ClasspageRestV2Controller.java
//rest-v2-app
// Created by Gooru on 2014
// Copyright (c) 2014 Gooru. All rights reserved.
// http://www.goorulearning.org/
// Permission is hereby granted, free of charge, to any person      obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so,  subject to
// the following conditions:
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY  KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE    WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR  PURPOSE     AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR  COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
/////////////////////////////////////////////////////////////
/**
 * 
 */
package org.ednovo.gooru.controllers.v2.api;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.Classpage;
import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.CollectionType;
import org.ednovo.gooru.core.api.model.ContentType;
import org.ednovo.gooru.core.api.model.ResourceType;
import org.ednovo.gooru.core.api.model.Sharing;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.application.util.BaseUtil;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.CollectionService;
import org.ednovo.gooru.domain.service.classpage.ClasspageService;
import org.ednovo.gooru.domain.service.redis.RedisService;
import org.ednovo.gooru.domain.service.search.SearchResults;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionRepository;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;

@Controller
@RequestMapping(value = { "/v2/classpage", "/v2/class" })
public class ClasspageRestV2Controller extends BaseController implements ConstantProperties, ParameterProperties {

	@Autowired
	private ClasspageService classpageService;

	@Autowired
	private CollectionService collectionService;

	@Autowired
	private CollectionRepository collectionRepository;

	@Autowired
	private RedisService redisService;

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ADD })
	@RequestMapping(value = "", method = RequestMethod.POST)
	public ModelAndView createClasspage(@RequestBody final String data, @RequestParam(value = ADD_TO_SHELF, defaultValue = TRUE, required = false) final boolean addToMy, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		ActionResponseDTO<Classpage> responseDTO = null;
		final JSONObject json = requestData(data);
		if (getValue(CLASSPAGE, json) != null) {
			responseDTO = getClasspageService().createClasspage(this.buildClasspageFromInputParameters(getValue(CLASSPAGE, json), user), getValue(COLLECTION_ITEM, json) != null ? this.buildCollectionItemFromInputParameters(getValue(COLLECTION_ITEM, json)) : null, getValue(COLLECTION_ID, json),
					user, addToMy);
		} else {
			responseDTO = getClasspageService().createClasspage(this.buildClasspageFromInputParameters(data, user), getValue(COLLECTION_ID, json), addToMy);
		}
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_CREATED);
		}
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_COLLECTION_ITEM_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, ERROR_INCLUDE);

		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_UPDATE })
	@RequestMapping(value = "/{id}", method = { RequestMethod.PUT })
	public ModelAndView updateClasspage(@PathVariable(value = ID) final String classpageId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final JSONObject json = requestData(data);

		final ActionResponseDTO<Classpage> responseDTO = getClasspageService().updateClasspage(this.buildClasspageForUpdateParameters(getValue(CLASSPAGE, json) != null ? getValue(CLASSPAGE, json) : data), classpageId, hasUnrestrictedContentAccess(), data);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		String[] includes = (String[]) ArrayUtils.addAll(CLASSPAGE_INCLUDE_FIELDS, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ModelAndView getClasspage(@PathVariable(value = ID) final String classpageId, @RequestParam(value = DATA_OBJECT, required = false) final String data, @RequestParam(value = INCLUDE_COLLECTION_ITEM, required = false, defaultValue = FALSE) final boolean includeCollectionItem,
			@RequestParam(value = MERGE, required = false) final String merge, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_ITEM_TAGS);
		if (includeCollectionItem) {
			includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_ITEM_INCLUDE_FIELDS);
		}
		final User user = (User) request.getAttribute(Constants.USER);
		return toModelAndViewWithIoFilter(getClasspageService().getClasspage(classpageId, user, merge), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = "", method = RequestMethod.GET)
	public ModelAndView getClasspages(@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit,
		    @RequestParam(value = TITLE, required = false) final String title, @RequestParam(value = AUTHOR, required = false) final String author,
			@RequestParam(value = USERNAME, required = false) final String userName, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		String[] includes = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_META_INFO);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_ITEM_INCLUDE_FIELDS);
		return toModelAndView(serialize(getClasspageService().getClasspages(offset, limit, user, title, author, userName), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, includes));
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
	public ModelAndView getClasspageByCode(@PathVariable(value = CLASSPAGE_CODE) final String classpageCode, @RequestParam(value = DATA_OBJECT, required = false) final String data, @RequestParam(value = INCLUDE_COLLECTION_ITEM, required = false, defaultValue = FALSE) final boolean includeCollectionItem,
			final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_ITEM_TAGS);
		if (includeCollectionItem) {
			includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_ITEM_INCLUDE_FIELDS);
		}
		return toModelAndViewWithIoFilter(getClasspageService().getClasspage(classpageCode, user), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_DELETE })
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public void deleteClasspage(@PathVariable(value = ID) final String classpageId, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		getClasspageService().deleteClasspage(classpageId,user);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_ADD })
	@RequestMapping(value = "/{id}/item", method = RequestMethod.POST)
	public ModelAndView createClasspageItem(@PathVariable(value = ID) final String classpageId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final JSONObject json = requestData(data);
		final ActionResponseDTO<CollectionItem> responseDTO = getClasspageService().createClasspageItem(getValue(COLLECTION_ID, json), classpageId, this.buildCollectionItemFromInputParameters(getValue(COLLECTION_ITEM, json)), user, CollectionType.CLASSPAGE.getCollectionType());
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_CREATED);
		}
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_COLLECTION_ITEM_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_ADD })
	@RequestMapping(value = "/collection/{id}/item", method = RequestMethod.POST)
	public ModelAndView createClasspageItems(@PathVariable(value = ID) final String collectionId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final List<CollectionItem> collectionItems = getCollectionService().createCollectionItems(JsonDeserializer.deserialize(data, new TypeReference<List<String>>() {
		}), collectionId, user);
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_COLLECTION_ITEM_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_CREATE_ITEM_INCLUDE_FILEDS);
		return toModelAndViewWithIoFilter(collectionItems, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_ADD })
	@RequestMapping(value = "/{id}/assign/{cid}", method = RequestMethod.POST)
	public ModelAndView assignCollection(@PathVariable(value = ID) final String classPageId,@RequestParam(value="isRequired", required=false ) final Boolean isRequired ,@RequestParam(value="direction", required=false ) final String direction,@RequestParam(value="planedEndDate", required=false ) final String planedEndDate,@PathVariable(value = CID) final String collectionId, final HttpServletRequest request, final HttpServletResponse response
			,@RequestParam(value="minimumScore", required=false ) final String minimumScore,@RequestParam(value="estimatedTime", required=false ) final String estimatedTime,@RequestParam(value="showAnswerByQuestions", required=false ) final Boolean showAnswerByQuestions,@RequestParam(value="showHints", required=false ) final Boolean showHints,@RequestParam(value="showAnswerEnd", required=false ) final Boolean showAnswerEnd) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final List<CollectionItem> collectionItems = getCollectionService().assignCollection(classPageId, collectionId, user, direction,planedEndDate, isRequired,minimumScore,estimatedTime,showAnswerByQuestions,showAnswerEnd,showHints );
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_COLLECTION_ITEM_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_CREATE_ITEM_INCLUDE_FILEDS);
		return toModelAndViewWithIoFilter(collectionItems, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_UPDATE })
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@RequestMapping(value = { "/item/{id}" }, method = RequestMethod.PUT)
	public ModelAndView updateClasspageItem(@PathVariable(value = ID) final String collectionItemId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final JSONObject json = requestData(data);
		final CollectionItem newCollectionItem = this.buildCollectionItemFromInputParameters(getValue(COLLECTION_ITEM, json));
		final ActionResponseDTO<CollectionItem> responseDTO = getCollectionService().updateCollectionItem(newCollectionItem, collectionItemId, user, data);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, COLLECTION_ITEM_INCLUDE_FILEDS);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_COLLECTION_ITEM_INCLUDE_FIELDS);
		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_READ })
	@RequestMapping(value = "item/{id}", method = RequestMethod.GET)
	public ModelAndView getClasspageItem(@PathVariable(value = ID) final String collectionItemId, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_CREATE_ITEM_INCLUDE_FILEDS);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_INCLUDE_FIELDS);
		return toModelAndViewWithIoFilter(getCollectionService().getCollectionItem(collectionItemId, false, user, null), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_READ })
	@RequestMapping(value = "/{cid}/item", method = RequestMethod.GET)
	public ModelAndView getClasspageItems(@PathVariable(value = COLLECTIONID) final String classpageId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") final Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue= "10") Integer limit,
		@RequestParam(value = ORDER_BY, defaultValue = PLANNED_END_DATE, required = false) final String orderBy,@RequestParam(value=CLEAR_CACHE, required=false, defaultValue="false" ) final Boolean clearCache, @RequestParam(value = OPTIMIZE, required = false, defaultValue = FALSE) final Boolean optimize, @RequestParam(value = STATUS, required = false) final String status, @RequestParam(value = TYPE, required = false) final String type, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_COLLECTION_ITEM_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
		final User user = (User) request.getAttribute(Constants.USER);
		final String cacheKey = "v2-class-data-" + classpageId + "-" + offset + "-" + limit  + "-" + optimize + "-" + orderBy +"-"+ status+"-"+type ;
		String data = null;
		if (!clearCache) {
			data = getRedisService().getValue(cacheKey);
		}
		if (data == null) {
			final List<Map<String, Object>> collectionItems = this.getClasspageService().getClasspageItems(classpageId, limit != null ? limit : (optimize ? limit : 5), offset, user, orderBy, optimize, status, type);
			final SearchResults<Map<String, Object>> result = new SearchResults<Map<String, Object>>();
			result.setSearchResults(collectionItems);
			result.setTotalHitCount(this.getCollectionRepository().getClasspageCollectionCount(classpageId, status, user.getPartyUid(), orderBy, type));
			data = serialize(result, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, true, includes);
			getRedisService().putValue(cacheKey, data, Constants.CACHE_EXPIRY_TIME_IN_SEC);
		}
		return toModelAndView(data);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_DELETE })
	@RequestMapping(value = "/item/{id}", method = RequestMethod.DELETE)
	public void deleteClasspageItem(@PathVariable(value = ID) final String collectionItemId, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		getCollectionService().deleteCollectionItem(collectionItemId, user,true);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ADD })
	@RequestMapping(value = "/{code}/member/join", method = RequestMethod.POST)
	public ModelAndView classpageUserJoin(@PathVariable(value = CODE) final String code, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User apiCaller = (User) request.getAttribute(Constants.USER);
		return toJsonModelAndView(this.getClasspageService().classpageUserJoin(code, JsonDeserializer.deserialize(data, new TypeReference<List<String>>() {
		}), apiCaller), true);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_DELETE })
	@RequestMapping(value = "/{code}/member/remove", method = RequestMethod.DELETE)
	public void classpageUserRemove(@PathVariable(value = CODE) final String code, @RequestParam final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User apiCaller = (User) request.getAttribute(Constants.USER);

		this.getClasspageService().classpageUserRemove(code, JsonDeserializer.deserialize(data, new TypeReference<List<String>>() {
		}), apiCaller);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = { "/{id}/member" }, method = RequestMethod.GET)
	public ModelAndView getClassMemberList(@PathVariable(ID) final String code, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit,
		    @RequestParam(value = GROUP_BY_STATUS, defaultValue = "false", required = false) final Boolean groupByStatus, @RequestParam(value = FILTER_BY, required = false) final String filterBy,
		    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

		return toModelAndView(serialize(this.getClasspageService().getMemberList(code, offset, limit, filterBy), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, CLASS_MEMBER_FIELDS));
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = { "/my/{type}" }, method = RequestMethod.GET)
	public ModelAndView getMyTeachAndStudy(@PathVariable(value = TYPE) final String type, final HttpServletRequest request, final HttpServletResponse response,  @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset,
			@RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit, 
			@RequestParam(value = ITEM_TYPE, required = false) final String itemType,@RequestParam(value = ORDER_BY, defaultValue = "desc", required = false) final String orderBy) throws Exception {
		final User apiCaller = (User) request.getAttribute(Constants.USER);
		return toModelAndView(serialize(this.getClasspageService().getMyStudy(apiCaller, orderBy, offset, limit, type,itemType), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, STUDY_RESOURCE_FIELDS));
	}
	

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = "/my", method = RequestMethod.GET)
	public ModelAndView getMyClasspage(final HttpServletRequest request, @RequestParam(value = DATA_OBJECT, required = false) final String data, @RequestParam(value = SKIP_PAGINATION, required = false, defaultValue = "false") final boolean skipPagination,
			@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit, @RequestParam(value = ORDER_BY, required = false, defaultValue = DESC) final String orderBy,
			final HttpServletResponse resHttpServletResponse) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final List<Classpage> classpage = this.getClasspageService().getMyClasspage(offset, limit, user, skipPagination,orderBy);
		String[] includes = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_META_INFO);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_ITEM_INCLUDE_FIELDS);
		if (!skipPagination) {
			final SearchResults<Classpage> result = new SearchResults<Classpage>();
			result.setSearchResults(classpage);
			result.setTotalHitCount(this.getClasspageService().getMyClasspageCount(user.getGooruUId()));
			return toModelAndViewWithIoFilter(result, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
		} else {
			return toModelAndViewWithIoFilter(getClasspageService().getMyClasspage(offset, limit, user, true,orderBy), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
		}
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = { "/member/suggest" }, method = RequestMethod.GET)
	public ModelAndView classMemberSuggest(@RequestParam(value = QUERY) final String queryText, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		return toModelAndView(this.getClasspageService().classMemberSuggest(queryText, user.getPartyUid()), RESPONSE_FORMAT_JSON);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_ITEM_UPDATE })
	@RequestMapping(value = { "/item/{id}/reorder/{sequence}" }, method = RequestMethod.PUT)
	public ModelAndView reorderCollectionItemSequence(@PathVariable(value = ID) final String collectionItemId, @PathVariable(value = SEQUENCE) final int newSequence, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final ActionResponseDTO<CollectionItem> responseDTO = getCollectionService().reorderCollectionItem(collectionItemId, newSequence,user);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, COLLECTION_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
		includes = (String[]) ArrayUtils.addAll(includes, ERROR_INCLUDE);

		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = "/item", method = RequestMethod.GET)
	public ModelAndView getClasspageAssoc(@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit,@RequestParam(value = CLASSPAGE_ID, required = false) final String classpageId ,
			@RequestParam(value = COLLECTION_ID, required = false) final String collectionId,@RequestParam(value = TITLE, required = false) final String title,@RequestParam(value = COLLECTION_TITLE, required = false) final String collectionTitle, @RequestParam(value = CLASS_CODE, required = false) final String classCode,@RequestParam(value = COLLECTION_CREATOR, required = false) final String collectionCreator,@RequestParam(value = COLLECTION_ITEM_ID, required = false) final String collectionItemId ){	
		return toJsonModelAndView(this.getClasspageService().getClasspageAssoc(offset, limit, classpageId,collectionId,title,collectionTitle,classCode,collectionCreator,collectionItemId),true);
	}
	
	/********************pathway ***********************/
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ADD })
	@RequestMapping(value = "/{id}/pathway", method = RequestMethod.POST)
	public ModelAndView createPathway(@RequestBody final String data, @PathVariable(value= ID) final String classId , final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final JSONObject json = requestData(data);
		final Collection collection = this.getClasspageService().createPathway(classId,this.buildPathwayFromInputParameters(data, user),getValue(COLLECTION_ID, json), getValue(IS_REQUIRED, json) != null ? Boolean.parseBoolean(getValue(IS_REQUIRED, json)) : false, user);
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, COLLECTION_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
		includes = (String[]) ArrayUtils.addAll(includes, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(collection , RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_UPDATE })
	@RequestMapping(value = "/{id}/pathway/{pid}", method = RequestMethod.PUT)
	public ModelAndView updatePathway(@RequestBody final String data, @PathVariable(value= ID) final String classId , @PathVariable(value= "pid") final String pathwayGooruOid , final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final Collection pathwayCollection = this.getClasspageService().updatePathway(classId, pathwayGooruOid, this.buildUpadtePathwayCollectionFromInputParameters(data), user, data);
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, COLLECTION_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
		includes = (String[]) ArrayUtils.addAll(includes, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(pathwayCollection , RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_DELETE })
	@RequestMapping(value = { "/{id}/pathway/{pid}" }, method = RequestMethod.DELETE)
	public void deletePathway(@PathVariable(value= ID) final String classId , @PathVariable(value= "pid") final String pathwayGooruOid, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getClasspageService().deletePathway(classId,pathwayGooruOid, user);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_UPDATE })
	@RequestMapping(value = { "/{id}/pathway/{pid}/item/{itemId}" }, method = RequestMethod.PUT)
	public ModelAndView updatePathwayItem(@PathVariable(value= ID) final String classId , @PathVariable(value= "itemId") final String collectionItemId,@PathVariable(value= "pid") final String pathwayGooruOid, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final JSONObject json = requestData(data);
		final CollectionItem newCollectionItem = this.buildCollectionItemFromInputParameters(getValue(COLLECTION_ITEM, json));
		final ActionResponseDTO<CollectionItem> responseDTO = getClasspageService().updatePathwayItem(classId,pathwayGooruOid,collectionItemId,newCollectionItem,  user, data);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, COLLECTION_ITEM_INCLUDE_FILEDS);
		includes = (String[]) ArrayUtils.addAll(includes, CLASSPAGE_COLLECTION_ITEM_INCLUDE_FIELDS);
		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_DELETE })
	@RequestMapping(value = { "/{id}/pathway/{pid}/item/{itemId}" }, method = RequestMethod.DELETE)
	public void deletePathwayItem(@PathVariable(value= ID) final String classId , @PathVariable(value= "itemId") final String collectionItemId,@PathVariable(value= "pid") final String pathwayGooruOid, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getClasspageService().deletePathwayItem(classId,pathwayGooruOid,collectionItemId ,user);
	}
	
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_READ })
	@RequestMapping(value = "/{id}/pathway/{pid}", method = RequestMethod.GET)
	public ModelAndView getPathwayItems(@PathVariable(value = ID) final String classId, @PathVariable(value = "pid") final String pathId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset,
			@RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = "false") final Boolean clearCache, @RequestParam(value = ORDER_BY, defaultValue = SEQUENCE, required = false) final String orderBy,
			final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final String cacheKey = "v2-class-data-" + classId+ "-"+ pathId + "-" + offset + "-" + limit + "-" + orderBy;
		String data = null;
		if (!clearCache) {
			data = getRedisService().getValue(cacheKey);
		}
		if (data == null) {
			final SearchResults<CollectionItem> searchResults = this.getClasspageService().getPathwayItemsSearchResults(classId, pathId, offset, limit, orderBy, user);
			String includesDefault[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, COLLECTION_ITEM_INCLUDE_FILEDS);
			includesDefault = (String[]) ArrayUtils.addAll(includesDefault, COLLECTION_ITEM_TAGS);
			includesDefault = (String[]) ArrayUtils.addAll(includesDefault, CLASSPAGE_COLLECTION_ITEM_INCLUDE_FIELDS);
			includesDefault = (String[]) ArrayUtils.addAll(includesDefault, COLLECTION_WORKSPACE);
			String includes[] = (String[]) ArrayUtils.addAll(includesDefault, ERROR_INCLUDE);
			data = serialize(searchResults, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, true, includes);
			getRedisService().putValue(cacheKey, data, Constants.CACHE_EXPIRY_TIME_IN_SEC);
		}
		return toModelAndView(data);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ITEM_ADD })
	@RequestMapping(value = "/{id}/pathway/{pid}/assign/{cid}", method = RequestMethod.POST)
	public ModelAndView assignCollectionToPathway(@PathVariable(value = ID) final String classPageId, @PathVariable(value= "pid") final String pathwayId, @RequestParam(value="direction", required=false ) final String direction,@RequestParam(value="planedEndDate", required=false ) final String planedEndDate,@PathVariable(value = CID) final String collectionId,@RequestParam(value="isRequired", required=false ) final Boolean isRequired , final HttpServletRequest request, final HttpServletResponse response, 
			@RequestParam(value="minimumScore", required=false ) final String minimumScore,@RequestParam(value="estimatedTime", required=false ) final String estimatedTime,@RequestParam(value="showAnswerByQuestions", required=false ) final Boolean showAnswerByQuestions,@RequestParam(value="showHints", required=false ) final Boolean showHints,@RequestParam(value="showAnswerEnd", required=false ) final Boolean showAnswerEnd) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final List<CollectionItem> collectionItems = getCollectionService().assignCollectionToPathway(classPageId, pathwayId ,collectionId, user, direction,planedEndDate,isRequired,minimumScore,estimatedTime,showAnswerByQuestions,showAnswerEnd,showHints);
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, CLASSPAGE_COLLECTION_ITEM_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_CREATE_ITEM_INCLUDE_FILEDS);
		return toModelAndViewWithIoFilter(collectionItems, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_ITEM_UPDATE })
	@RequestMapping(value = { "/{id}/pathway/{pid}/reorder/{sequence}" }, method = RequestMethod.PUT)
	public ModelAndView reorderPathwaySequence(@PathVariable(value = ID) final String classId, @PathVariable(value= "pid") final String pathwayId, @PathVariable(value = SEQUENCE) final int newSequence, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final ActionResponseDTO<CollectionItem> responseDTO = this.getClasspageService().reorderPathwaySequence(classId,pathwayId ,newSequence, user);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, COLLECTION_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
		includes = (String[]) ArrayUtils.addAll(includes, ERROR_INCLUDE);

		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}
	
	
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_ITEM_UPDATE })
	@RequestMapping(value = { "/{id}/pathway/{pid}/item/{itemId}/move" }, method = RequestMethod.PUT)
	public ModelAndView pathwayItemMoveWithReorder(@PathVariable(value= ID) final String classId , @PathVariable(value= "itemId") final String collectionItemId,@PathVariable(value= "pid") final String pathwayId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final JSONObject json = requestData(data);
		final CollectionItem collectionItem = getClasspageService().pathwayItemMoveWithReorder(classId, pathwayId,collectionItemId, json != null && getValue(TARGET_ID, json) != null ? getValue(TARGET_ID, json) : null, json != null && getValue("newSequence", json) != null ? Integer.parseInt(getValue("newSequence", json)) : null , user);

		String includes[] = (String[]) ArrayUtils.addAll(RESOURCE_INCLUDE_FIELDS, COLLECTION_INCLUDE_FIELDS);
		includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
		includes = (String[]) ArrayUtils.addAll(includes, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(collectionItem, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_ITEM_READ })
	@RequestMapping(value = { "/assignment/{id}" }, method = RequestMethod.GET)
	public ModelAndView getParentDetails( @PathVariable(value= ID) final String collectionItemId,  final HttpServletRequest request, final HttpServletResponse response) throws Exception {

		return toModelAndView(serializeToJson(this.getClasspageService().getParentDetails(collectionItemId), false, true));
	}
	
	
	

	private Classpage buildClasspageFromInputParameters(final String data, final User user) {
		final Classpage classpage = JsonDeserializer.deserialize(data, Classpage.class);
		classpage.setGooruOid(UUID.randomUUID().toString());
		classpage.setClasspageCode(BaseUtil.generateBase48Encode(7));
		classpage.setContentType(getCollectionService().getContentType(ContentType.RESOURCE));
		classpage.setLastModified(new Date(System.currentTimeMillis()));
		classpage.setCreatedOn(new Date(System.currentTimeMillis()));
		classpage.setUser(user);
		classpage.setCollectionType(ResourceType.Type.CLASSPAGE.getType());
		classpage.setOrganization(user.getPrimaryOrganization());
		classpage.setCreator(user);
		classpage.setLastUpdatedUserUid(user.getGooruUId());
		if (classpage.getSharing() != null && (classpage.getSharing().equalsIgnoreCase(Sharing.PRIVATE.getSharing()) || classpage.getSharing().equalsIgnoreCase(Sharing.PUBLIC.getSharing()))) {
			classpage.setSharing(classpage.getSharing());
		} else {
			classpage.setSharing(Sharing.PUBLIC.getSharing());
		}
		return classpage;
	}
	
	private Collection buildPathwayFromInputParameters(final String data, final User user) {
		final Collection collection = JsonDeserializer.deserialize(data, Collection.class);
		collection.setGooruOid(UUID.randomUUID().toString());
		collection.setContentType(getCollectionService().getContentType(ContentType.RESOURCE));
		collection.setLastModified(new Date(System.currentTimeMillis()));
		collection.setCreatedOn(new Date(System.currentTimeMillis()));
		collection.setUser(user);
		collection.setCollectionType(ResourceType.Type.PATHWAY.getType());
		collection.setOrganization(user.getPrimaryOrganization());
		collection.setCreator(user);
		collection.setLastUpdatedUserUid(user.getGooruUId());
		if (collection.getSharing() != null && (collection.getSharing().equalsIgnoreCase(Sharing.PRIVATE.getSharing()) || collection.getSharing().equalsIgnoreCase(Sharing.PUBLIC.getSharing()))) {
			collection.setSharing(collection.getSharing());
		} else {
			collection.setSharing(Sharing.PUBLIC.getSharing());
		}

		return collection;
	}
	
	private Collection buildUpadtePathwayCollectionFromInputParameters(final String data) {
		return JsonDeserializer.deserialize(data, Collection.class);
	}

	private Classpage buildClasspageForUpdateParameters(final String data) {
		return JsonDeserializer.deserialize(data, Classpage.class);
	}

	private CollectionItem buildCollectionItemFromInputParameters(final String data) {

		return JsonDeserializer.deserialize(data, CollectionItem.class);
	}

	public ClasspageService getClasspageService() {
		return classpageService;
	}

	public CollectionService getCollectionService() {
		return collectionService;
	}

	public CollectionRepository getCollectionRepository() {
		return collectionRepository;
	}

	public RedisService getRedisService() {
		return redisService;
	}

}
