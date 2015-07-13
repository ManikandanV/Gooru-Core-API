/////////////////////////////////////////////////////////////
// BaseComponent.java
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
package org.ednovo.gooru.infrastructure.messenger;

import java.util.logging.Level;

import org.ednovo.gooru.application.util.TaxonomyUtil;
import org.ednovo.gooru.core.constant.ConfigConstants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.domain.service.setting.SettingService;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseComponent implements ParameterProperties {
	
	@Autowired
	private SettingService settingService;

	private final Logger logger = LoggerFactory.getLogger(BaseComponent.class);

	protected void releaseClientResources(ClientResource resource, Representation representation) {
		try {
			if (resource != null) {
				resource.release();
			}
			if (representation != null) {
				representation.release();
			}
			resource = null;
			representation = null;
		} catch (Exception e) {
			getLogger().error(e.getMessage());
		}
	}

	protected void createAndRunClientResource(String url) {
		executeClientResource(new ClientResource(getApiPath() + url));
	}

	protected void createAndRunClientResource(String url, Form form) {
		executeClientResource(new ClientResource(getApiPath() + url), form);
	}

	protected void createAndRunClientResource(String path, String url, Form form) {
		executeClientResource(new ClientResource(path + url), form);
	}

	protected void executeClientResource(ClientResource clientResource) {
		executeClientResource(clientResource, null);
	}

	protected void executeClientResource(ClientResource clientResource, Form form) {

		Representation representation = null;
		try {
			clientResource.getLogger().setLevel(Level.WARNING);
			if (form == null) {
				representation = clientResource.get();
			} else {
				representation = clientResource.post(form.getWebRepresentation());
			}

		} catch (Exception exception) {
			getLogger().error(exception.getMessage() , exception);
		} finally {
			releaseClientResources(clientResource, representation);
		}
	}

	protected String getApiPath() {
		return settingService.getConfigSetting(ConfigConstants.GOORU_HOME,0, TaxonomyUtil.GOORU_ORG_UID) + "/" + settingService.getConfigSetting(ConfigConstants.GOORU_SERVICES_ENDPOINT,0, TaxonomyUtil.GOORU_ORG_UID) ;
	}

	protected String getSearchApiPath() {
		return settingService.getConfigSetting(ConfigConstants.GOORU_SEARCH_ENDPOINT,0, TaxonomyUtil.GOORU_ORG_UID) + "/";
	}

	public Logger getLogger() {
		return logger;
	}

	protected abstract class ClientResourceExecuter {

	private ClientResource clientResource = null;

	private Representation representation = null;

		protected ClientResourceExecuter() {
			try {
				run(clientResource, representation);
			} catch (Exception exception) {
				getLogger().error(exception.getMessage());
			} finally {
				releaseClientResources(clientResource, representation);
			}
		}

		public abstract void run(ClientResource clientResource, Representation representation) throws Exception;
	}
}
