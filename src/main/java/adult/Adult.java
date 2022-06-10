package adult;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import squidBot.bot.Media;

public class Adult {
	private static JSONArray search(String query) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://api.redtube.com/?data=redtube.Videos.searchVideos&output=json&search="
							+ query + "&thumbsize=medium"))
					.method("GET", HttpRequest.BodyPublishers.noBody()).build();

			HttpResponse<String> response = HttpClient.newHttpClient().send(request,
					HttpResponse.BodyHandlers.ofString());

			return new JSONObject(response.body()).getJSONArray("videos");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			return null;
		}
	}

	public static File adult(String query) {
		String[] parts = query.split(" ");

		int lastParam = -1;
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].startsWith("-")) {
				lastParam = i + 1;
			}
		}

		StringBuilder qb = new StringBuilder();
		StringBuilder pb = new StringBuilder();

		for (int i = 0; i <= lastParam; i++) {
			if(pb.length() != 0) {
				pb.append(' ');
			}
			pb.append(parts[i]);
		}
		
		for (int i = lastParam + 1; i < parts.length; i++) {
			if(qb.length() != 0) {
				qb.append(' ');
			}
			qb.append(parts[i]);
		}
		
		String searchQuery;
		try {
			searchQuery = URLEncoder.encode(qb.toString(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			searchQuery = qb.toString();
		}

		JSONArray list = search(searchQuery);
		String id = list.getJSONObject(0).getJSONObject("video").getString("video_id");

		String params = pb.toString();
		
		try {
			return Media.dlp(params + " https://www.redtube.com/"+id, true, false);
		} catch (UnsupportedEncodingException | InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			return null;
		}
	}
}
