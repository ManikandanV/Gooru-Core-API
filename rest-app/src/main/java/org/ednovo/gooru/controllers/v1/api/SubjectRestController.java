/////////////////////////////////////////////////////////////
// SubjectRestController.java
// rest-app
// Created by Gooru on 2015
// Copyright (c) 2015 Gooru. All rights reserved.
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
package org.ednovo.gooru.controllers.v1.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.RequestMappingUri;
import org.ednovo.gooru.core.api.model.Subject;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.subject.SubjectService;
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
@RequestMapping(value = { RequestMappingUri.SUBJECT })
public class SubjectRestController extends BaseController implements ConstantProperties {

	@Autowired
	private SubjectService subjectService;

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SUBJECT_ADD })
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView createSubject(HttpServletRequest request, HttpServletResponse response, @RequestBody String data)   {
		User user = (User) request.getAttribute(Constants.USER);
		final ActionResponseDTO<Subject> responseDTO = this.getSubjectService().createSubject(buildSubjectFromInputParameters(data), user);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}else {		
			response.setStatus(HttpServletResponse.SC_CREATED);		
			responseDTO.getModel().setUri(RequestMappingUri.SUBJECT + RequestMappingUri.SEPARATOR + responseDTO.getModel().getSubjectId());		
		}
		String includes[] = (String[]) ArrayUtils.addAll(CREATE_INCLUDES, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(responseDTO.getModelData(), FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SUBJECT_READ })
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getSubjects(@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit, HttpServletResponse response, HttpServletRequest request, @RequestParam(value = CLASSIFICATION_TYPE_ID, required = false) Integer classificationTypeId)   {
		return toModelAndViewWithIoFilter(this.getSubjectService().getSubjects(classificationTypeId,limit, offset), FORMAT_JSON, EXCLUDE_ALL, true, SUBJECT_INCLUDES);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SUBJECT_READ })
	@RequestMapping(value = RequestMappingUri.ID, method = RequestMethod.GET)
	public ModelAndView getSubject(HttpServletResponse response, HttpServletRequest request, @PathVariable(ID) Integer SubjectId)   {
		return toModelAndViewWithIoFilter(this.getSubjectService().getSubject(SubjectId), FORMAT_JSON, EXCLUDE_ALL, true, SUBJECT_INCLUDES);
	}

	@AuthorizeOperations(operations ={GooruOperationConstants.OPERATION_SUBJECT_UPDATE})
	@RequestMapping(value = RequestMappingUri.ID, method = RequestMethod.PUT)
	public void updateSubject(HttpServletResponse response, HttpServletRequest request, @RequestBody String data, @PathVariable(ID) Integer subjectId)   {
		this.getSubjectService().updateSubject(buildSubjectFromInputParameters(data), subjectId);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SUBJECT_DELETE })
	@RequestMapping(value = RequestMappingUri.ID, method = RequestMethod.DELETE)
	public void deleteSubject(HttpServletResponse response, HttpServletRequest request, @PathVariable(ID) Integer subjectId)   {
		this.getSubjectService().deleteSubject(subjectId);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	@AuthorizeOperations(operations={GooruOperationConstants.OPERATION_SUBJECT_READ})
	@RequestMapping(value = RequestMappingUri.TAXONOMY_COURSE_BY_SUBJECT, method = RequestMethod.GET)
    public ModelAndView getCourses(HttpServletResponse response, HttpServletRequest request,@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit,@PathVariable(ID) int subjectId) {
    	 return toModelAndView(this.getSubjectService().getCourses(offset, limit, subjectId), RESPONSE_FORMAT_JSON);
    }
	
	public SubjectService getSubjectService() {
		return subjectService;
	}

	private Subject buildSubjectFromInputParameters(String data) {
		return JsonDeserializer.deserialize(data, Subject.class);
	}

}
