/////////////////////////////////////////////////////////////
//InviteUserRestV2Controller.java
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

import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.InviteService;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;

@Controller
@RequestMapping(value = "/v2/invite/class/{id}")
public class InviteUserRestV2Controller extends BaseController implements ConstantProperties {

	@Autowired
	private InviteService inviteService;

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_INVITE_CODE_ADD })
	@RequestMapping(method = RequestMethod.POST)
	public void inviteUserToClass(@PathVariable(ID) String gooruOid, @RequestBody String data, HttpServletRequest request, HttpServletResponse response) {
		User user = (User) request.getAttribute(Constants.USER);
		this.getInviteService().inviteUserForClass(JsonDeserializer.deserialize(data, new TypeReference<List<String>>() {
		}), gooruOid, user);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_INVITE_CODE_LIST })
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getInvitee(@PathVariable(ID) String gooruOid, @RequestParam(value = STATUS) String status, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit,
			HttpServletRequest request, HttpServletResponse response) {
		return toModelAndViewWithIoFilter(this.getInviteService().getInvites(gooruOid, status, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE, true, "*");
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_INVITE_CODE_DELETE })
	@RequestMapping(method = RequestMethod.DELETE)
	public void delete(@PathVariable(ID) String gooruOid, @RequestParam(value = EMAIL) String email, HttpServletRequest request, HttpServletResponse response) {
		this.getInviteService().deleteInvitee(gooruOid, email);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	public InviteService getInviteService() {
		return inviteService;
	}

}
