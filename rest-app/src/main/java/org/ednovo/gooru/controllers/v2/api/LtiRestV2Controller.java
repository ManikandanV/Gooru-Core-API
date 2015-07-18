package org.ednovo.gooru.controllers.v2.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.OAuthClient;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.oauth.OAuthService;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = { "v2/lti" })
public class LtiRestV2Controller extends BaseController implements ConstantProperties, ParameterProperties {

	
	@Autowired
	private OAuthService oAuthService;

	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_OAUTH_UPDATE })
	@RequestMapping(method = RequestMethod.PUT, value = "/client")
	public ModelAndView updateLTIClient(HttpServletRequest request, HttpServletResponse response, @RequestBody String data) throws Exception {

		User apiCaller = (User) request.getAttribute(Constants.USER);
		OAuthClient LTIClient = buildLTIClientFromInputParameters(data); 
		 ActionResponseDTO<OAuthClient> responseDTO = oAuthService.updateOAuthClient(LTIClient,apiCaller.getGooruUId());
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
		}
		String [] includes = (String[]) ArrayUtils.addAll(ERROR_INCLUDE, OAUTH_CLIENT_INCLUDES);

		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL,true, includes);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_OAUTH_READ })
	@RequestMapping(method = { RequestMethod.GET }, value = "/client/list")
	public ModelAndView listLTIClientByOrganization(@RequestParam String organizationUId,@RequestParam (required = false)String grantType ,HttpServletRequest request, HttpServletResponse response , @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, 
			@RequestParam (value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit) throws Exception {
		
		String [] includes = (String[]) ArrayUtils.addAll(ERROR_INCLUDE, OAUTH_CLIENT_INCLUDES);
		return toModelAndViewWithIoFilter(this.getOAuthService().listOAuthClientByOrganization(organizationUId, offset, limit, grantType), RESPONSE_FORMAT_JSON, EXCLUDE_ALL,true, includes);

	}
	
	private OAuthClient buildLTIClientFromInputParameters(String data) {
		return JsonDeserializer.deserialize(data, OAuthClient.class);
	}
	public OAuthService getOAuthService() {
		return oAuthService;
	}

}
