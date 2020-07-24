/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.http.api.worlds;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@RequiredArgsConstructor
public class WorldClient
{
	private final OkHttpClient client;

	public WorldResult lookupWorlds() throws IOException
	{
//		HttpUrl url = RuneLiteAPI.getApiBase().newBuilder()
//			.addPathSegment("worlds.js")
//			.build();
		// Actually curl THIS url: https://api.github.com/repos/runelite/runelite/git/refs/tags
		// Get the last item in the array
		// Get the ref
		// Split on '-' and get the last split

		// Final built URL should be in the form https://api.runelite.net/runelite-<VERSION_NUMBER>/worlds.js

		String json = client.newCall(new Request.Builder().url("https://api.github.com/repos/runelite/runelite/git" +
				"/refs/tags").build()).execute().body().string();

		System.out.println("JSON: " + json);
		GsonBuilder builder = new GsonBuilder();
		Object jsonObj = builder.create().fromJson(json, Object.class);
		List jsonList = ((List) jsonObj);
		String last_ref = (String) ((Map) jsonList.get(jsonList.size() - 1)).get("ref");
		String version_num = last_ref.substring(last_ref.lastIndexOf('-') + 1);

		String url = "https://api.runelite.net/runelite-" + version_num + "/worlds.js";


		Request request = new Request.Builder()
			.url(url)
			.build();

		try (Response response = client.newCall(request).execute())
		{
			if (!response.isSuccessful())
			{
				log.debug("Error looking up worlds: {}", response);
				throw new IOException("unsuccessful response looking up worlds");
			}

			InputStream in = response.body().byteStream();
			return RuneLiteAPI.GSON.fromJson(new InputStreamReader(in), WorldResult.class);
		}
		catch (JsonParseException ex)
		{
			throw new IOException(ex);
		}
	}
}
