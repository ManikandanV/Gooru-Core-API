/////////////////////////////////////////////////////////////
//FeedbackRestV2Controller.java
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
package org.ednovo.gooru.controllers.v2.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.CustomTableValue;
import org.ednovo.gooru.core.api.model.Feedback;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.application.util.CustomProperties;
import org.ednovo.gooru.core.application.util.ServerValidationUtils;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.FeedbackService;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = { "/v2/rating", "/v2/flag", "/v2/report", "/v2/reaction" })
public class FeedbackRestV2Controller extends BaseController implements ParameterProperties, ConstantProperties {

	@Autowired
	private FeedbackService feedbackService;

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_FEEDBACK_ADD })
	@RequestMapping(method = RequestMethod.POST, value = "")
	public ModelAndView createFeedback(@RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final Feedback newFeedback = this.buildFeedbackFromInputParameters(data, request);
		final boolean list = newFeedback.getTypes() == null ? false : true;
		final List<Feedback> feedbacks = getFeedbackService().createFeedbacks(newFeedback, user);
		final Feedback feedback = feedbacks.get(0);
		response.setStatus(HttpServletResponse.SC_CREATED);
		final String includes[] = (String[]) ArrayUtils.addAll(FEEDBACK_INCLUDE_FIELDS, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(list ? feedbacks : feedback, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_FEEDBACK_UPDATE })
	@RequestMapping(method = RequestMethod.PUT, value = "/{id}")
	public ModelAndView updateFeedback(@RequestBody final String data, @PathVariable(value = ID) final String feedbackId, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final List<Feedback> feedbacks = getFeedbackService().updateFeedback(feedbackId, this.buildFeedbackFromInputParameters(data, request), user);
		final String includes[] = (String[]) ArrayUtils.addAll(FEEDBACK_INCLUDE_FIELDS, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(feedbacks, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_FEEDBACK_READ })
	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	public ModelAndView getFeedback(@PathVariable(value = ID) final String feedbackId, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final String includes[] = (String[]) ArrayUtils.addAll(FEEDBACK_INCLUDE_FIELDS, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(this.getFeedbackService().getFeedback(feedbackId), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_FEEDBACK_READ })
	@RequestMapping(method = RequestMethod.GET, value = "/{type}/value")
	public ModelAndView getCustomValues(final HttpServletRequest request, @PathVariable(value = TYPE) final String type, final HttpServletResponse response) throws Exception {

		return toModelAndViewWithIoFilter(this.getFeedbackService().getCustomValues(getFeedbackCategory(request), type), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, CUSTOM_VALUE_INCLUDE);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_FEEDBACK_READ })
	@RequestMapping(method = RequestMethod.GET, value = "")
	public ModelAndView getFeedbacks(final HttpServletRequest request, @RequestParam(value = TYPE, required = true) final String type, @RequestParam(value = TARGET_TYPE, required = true) final String targetType, @RequestParam(value = CREATOR_UID, required = false) final String creatorUid,
			@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") final Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "20") final Integer limit, final HttpServletResponse response) throws Exception {
		final String includes[] = (String[]) ArrayUtils.addAll(FEEDBACK_INCLUDE_FIELDS, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(this.getFeedbackService().getFeedbacks(getFeedbackCategory(request), targetType, type, creatorUid, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_FEEDBACK_READ })
	@RequestMapping(method = RequestMethod.GET, value = { "/resource", "/collection" })
	public ModelAndView getContentFlags(HttpServletRequest request, @RequestParam(value = STATUS, required = false) final String status, @RequestParam(value = REPORTED_FLAG_TYPE, required = false) final String reportedFlagType, @RequestParam(value = "startDate", required = false) final String startDate,
			@RequestParam(value = END_DATE, required = false) final String endDate, @RequestParam(value = SEARCH_QUERY, required = false) final String searchQuery, @RequestParam(value = DESCRIPTION, required = false) final String description,
			@RequestParam(value = REPORT_QUERY, required = false) final String reportQuery, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") final Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") final Integer limit,
			final HttpServletResponse response) throws Exception {

		return toJsonModelAndView(this.getFeedbackService().getFlags(limit, offset, getFeedbackCategory(request), getSummaryCategory(request), status, reportedFlagType, startDate, endDate, searchQuery, description, reportQuery), true);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_FEEDBACK_DELETE })
	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	public void deleteFeedback(@PathVariable(value = ID) final String feedbackId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getFeedbackService().deleteFeedback(feedbackId, user);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	private Feedback buildFeedbackFromInputParameters(final String data, final HttpServletRequest request) {
		final Feedback feedback = JsonDeserializer.deserialize(data, Feedback.class);
		final CustomTableValue feedbackCategory = new CustomTableValue();
		feedbackCategory.setValue(getFeedbackCategory(request));
		feedback.setCategory(feedbackCategory);
		final CustomTableValue product = new CustomTableValue();
		product.setValue(isMobileDevice(request) ? CustomProperties.Product.MOBILE.getProduct() : CustomProperties.Product.WEB.getProduct());
		feedback.setProduct(product);
		return feedback;
	}

	private String getFeedbackCategory(final HttpServletRequest request) {
		String category = null;
		if (request != null && request.getRequestURL() != null) {
			if (request.getRequestURL().toString().contains(CustomProperties.FeedbackCategory.RATING.getFeedbackCategory())) {
				category = CustomProperties.FeedbackCategory.RATING.getFeedbackCategory();
			} else if (request.getRequestURL().toString().contains(CustomProperties.FeedbackCategory.REPORT.getFeedbackCategory())) {
				category = CustomProperties.FeedbackCategory.REPORT.getFeedbackCategory();
			} else if (request.getRequestURL().toString().contains(CustomProperties.FeedbackCategory.FLAG.getFeedbackCategory())) {
				category = CustomProperties.FeedbackCategory.FLAG.getFeedbackCategory();
			} else if (request.getRequestURL().toString().contains(CustomProperties.FeedbackCategory.REACTION.getFeedbackCategory())) {
				category = CustomProperties.FeedbackCategory.REACTION.getFeedbackCategory();
			}
		}
		ServerValidationUtils.rejectIfNull(category, GL0007, REQUEST_PATH);
		return category;
	}

	private String getSummaryCategory(HttpServletRequest request) {
		String category = null;
		if (request.getRequestURL().toString().contains(RESOURCE)) {
			category = RESOURCE;
		} else if (request.getRequestURL().toString().contains(COLLECTION)) {
			category = COLLECTION;
		}
		return category;
	}

	public void setFeedbackService(FeedbackService feedbackService) {
		this.feedbackService = feedbackService;
	}

	public FeedbackService getFeedbackService() {
		return feedbackService;
	}

}
