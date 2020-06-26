/*
 *	Copyright (c) 2017-2020 Software AG, Darmstadt, Germany and/or its licensors
 * 
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 *		http://www.apache.org/licenses/LICENSE-2.0
 * 
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.softwareag.samples;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.softwareag.connectivity.AbstractSimpleTransport;
import com.softwareag.connectivity.Message;
import com.softwareag.connectivity.PluginConstructorParameters.TransportConstructorParameters;
import com.softwareag.connectivity.util.MapExtractor;

public class FileTransport extends AbstractSimpleTransport {
	private final PrintWriter hostToFile;
	private final BufferedReader fileToHost;

	public FileTransport(org.slf4j.Logger logger, TransportConstructorParameters params) throws Exception {
		super(logger, params);
		MapExtractor config = new MapExtractor(params.getConfig(), "config");
		hostToFile = new PrintWriter(config.getStringDisallowEmpty("hostToFile"), StandardCharsets.UTF_8.name());
		fileToHost = new BufferedReader(new InputStreamReader(new FileInputStream(config.getStringDisallowEmpty("fileToHost")), StandardCharsets.UTF_8));
		config.checkNoItemsRemaining();
	}

	public void start() throws Exception {
		while(true) {
			String line = fileToHost.readLine();
			if(line == null) break;

			Message m = new Message(line);
			hostSide.sendBatchTowardsHost(Collections.singletonList(m));
		}
	}

	public void deliverMessageTowardsTransport(Message message) throws Exception {
		hostToFile.println(message.getPayload());
		hostToFile.flush();
	}

	public void shutdown() throws Exception {
		if(hostToFile != null) hostToFile.close();
		if(fileToHost != null) fileToHost.close();
	}
	
	/** Identifies the version of the API this plug-in was built against. */
	public static final String CONNECTIVITY_API_VERSION = com.softwareag.connectivity.ConnectivityPlugin.CONNECTIVITY_API_VERSION;
}
