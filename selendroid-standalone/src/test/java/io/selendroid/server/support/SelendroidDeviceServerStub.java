/*
 * Copyright 2013 selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.server.support;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SelendroidDeviceServerStub extends NanoHTTPD {
	private TestSessionListener testSessionListener;

	public SelendroidDeviceServerStub(int port) throws IOException {
		super(port, new File("."));
		System.out
				.println("SelendroidDeviceServerStub is started on the following port: "
						+ port);
	}

	public void registerTestSessionListener(
			TestSessionListener testSessionListener) {
		if (testSessionListener == null) {
			throw new IllegalArgumentException(
					"The testSessionListener must not be null.");
		}
		if (this.testSessionListener != null) {
			throw new IllegalStateException(
					"Server does only support one listener and there is already one registered.");
		}
		this.testSessionListener = testSessionListener;
	}

	public Response serve(String uri, String method, Properties header,
			Properties params, Properties files) {
		if (this.testSessionListener == null) {
			throw new IllegalStateException(
					"Server must have one test session listener registered.");
		}
		try {
			if (uri.endsWith("/wd/hub/status") && isGet(method)) {
				return respond(testSessionListener.status(params));
			} else if (uri.endsWith("/wd/hub/session") && isPost(method)) {
				return respond(testSessionListener.createSession(params));
			} else if (uri.endsWith("/wd/hub/session/:sessionId")
					&& isDelete(method)) {
				return respond(testSessionListener.deleteSession(params));
			} else if (uri.endsWith(testSessionListener.uriMapping)) {
				return respond(testSessionListener
						.executeSelendroidRequest(params));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "ERROR OCCURED");
	}

	private boolean isGet(String method) {
		return "GET".equals(method);
	}

	private boolean isPost(String method) {
		return "POST".equals(method);
	}

	private boolean isDelete(String method) {
		return "DELETE".equals(method);
	}

	private Response respond(org.openqa.selendroid.server.Response response) {
		return new Response(HTTP_OK, "application/json", response.toString());
	}
}
