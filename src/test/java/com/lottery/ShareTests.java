package com.lottery;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ShareTests {

//	@Ignore
	@Test
	public void myShareTest() throws IOException, InterruptedException {
		String url = "http://hq.sinajs.cn/list=sh000001,sh600678,sh600068,sh600989";
		DecimalFormat format = new DecimalFormat("0.00");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
		while (true) {
			HttpResponse<String> httpResponse;
			httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(),
					HttpResponse.BodyHandlers.ofString());
			String str = httpResponse.body();
			String[] result = str.split("\n");
			System.out.println(simpleDateFormat.format(new Date()));
			System.out.println(Stream.of(result[0].split(",")).collect(Collectors.joining("\t")));
			for (int i = 1; i < result.length; i++) {
				String[] tmp = result[i].split(",");
				System.out.println(tmp[0].split("\"")[1] + "\t" + tmp[0].split("=\"")[0].split("_")[2] + "\t" + tmp[3]
						+ "\t" + format.format((Float.valueOf(tmp[3]) / Float.valueOf(tmp[2]) - 1) * 100) + "%\t"
						+ (int) Math.floor(Float.valueOf(tmp[10]) / 100) + "\t" + tmp[11]);
			}
			TimeUnit.MILLISECONDS.sleep(1992);
		}
	}

	@Ignore
	@Test
	public void createTest() throws IOException {
		Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareFail.txt"), "hwhw",
				StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	}

	@Ignore
	@Test
	public void updateShareByOkTest() throws IOException {
		List<String> readAllLines = Files.readAllLines(
				Paths.get(ShareTests.class.getResource("shareFail.txt").getPath()), StandardCharsets.UTF_8);
		String result = Files
				.readAllLines(Paths.get(ShareTests.class.getResource("shareOk.txt").getPath()), StandardCharsets.UTF_8)
				.parallelStream().filter(t -> !readAllLines.contains(t)).collect(Collectors.joining("\r\n"));
		Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareDesByOk.txt"), result,
				StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	}

	@Ignore
	@Test
	public void sortTest() throws IOException {
		List<JsonObject> dest = Files
				.readAllLines(Paths.get(ShareTests.class.getResource("shareSuccess.txt").getPath()),
						StandardCharsets.UTF_8)
				.stream().map(t -> JsonParser.parseString(t).getAsJsonObject())
				.filter(t -> !t.getAsJsonObject("result").get("scoresAll").isJsonNull()).sorted((t1, t2) -> {
					JsonObject jsonObject = t1.getAsJsonObject("result");
					JsonElement jsonElement = jsonObject.get("scoresAll");
					float t1Score = jsonElement.getAsFloat();
					jsonObject = t2.getAsJsonObject();
					jsonObject = jsonObject.getAsJsonObject("result");
					jsonElement = jsonObject.get("scoresAll");
					float t2Score = jsonElement.getAsFloat();
					return t1Score == t2Score ? 0 : t1Score > t2Score ? -1 : 1;
				}).collect(Collectors.toList());
		dest.stream().limit(10).map(t -> {
			JsonObject jsonObject = t.getAsJsonObject("result");
			jsonObject.remove("sentiment");
			jsonObject.remove("stockIndex");
			jsonObject.remove("shIndex");
			jsonObject.remove("baseAnalysis");
			jsonObject.remove("blacklist");
			jsonObject.remove("category");
			jsonObject.remove("klineData");
			return t;
		}).collect(Collectors.groupingBy(t -> t.getAsJsonObject("result").get("scoresAll").getAsFloat()))
				.forEach((k, v) -> {
					System.out.println("score: " + k + " -------> " + v);
				});
		Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "result.txt"),
				dest.stream().map(t -> t.toString()).collect(Collectors.joining("\r\n")), StandardCharsets.UTF_8,
				StandardOpenOption.CREATE);
	}

	@Ignore
	@Test
	public void updateShareBySuccessTest() throws IOException {
		Map<String, String> orig = new HashMap<>();
		Files.readAllLines(Paths.get(ShareTests.class.getResource("shareOk.txt").getPath()), StandardCharsets.UTF_8)
				.stream().collect(Collectors.groupingBy(t -> {
					String[] tmp = t.split("\\t");
					return tmp[0] + "_" + tmp[2];
				})).forEach((k, v) -> orig.put(k, v.get(0).split("\\t")[1]));
		String result = Files.readAllLines(Paths.get(ShareTests.class.getResource("shareSuccess.txt").getPath()),
				StandardCharsets.UTF_8).stream().map(t -> {
					JsonObject jsonObject = JsonParser.parseString(t).getAsJsonObject();
					jsonObject = jsonObject.getAsJsonObject("result");
					String name = jsonObject.get("stockName").getAsString();
					String code = jsonObject.get("stockCode").getAsString();
					return name + "\t" + orig.get(name + "_" + code) + "\t" + code;
				}).collect(Collectors.joining("\r\n"));
		Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareDesBySuccess.txt"), result,
				StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	}

	@Ignore
	@Test
	public void allShareTest() throws IOException, InterruptedException {
//		String url = "http://api.money.126.net/data/feed/0600389,money.api?callback=test";
//		url = "http://hq.sinajs.cn/list=sh600389";
		List<String> readAllLines = Files.readAllLines(Paths.get(ShareTests.class.getResource("shareOk.txt").getPath()),
				StandardCharsets.UTF_8);
		StringBuffer sb = new StringBuffer();
		StringBuffer sb1 = new StringBuffer();
		CountDownLatch countDownLatch = new CountDownLatch(readAllLines.size());
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() << 1);
		for (String str : readAllLines) {
			if ("".equals(str.trim())) {
				countDownLatch.countDown();
				continue;
			}
			executorService.execute(() -> {
				String[] tmp = str.split("\\t");
				String market = tmp[1];
				String code = tmp[2];
				String url = "https://gateway.jinyi999.cn/rjhy-stock-diagnosis/api/1/app/1DE3B03F8A42974EA0ABCD009FAC6351EC/summary/stock/diagnosis?dataSourceType=A&market="
						+ market + "&stockCode=" + code + "&period=TWENTY";
				HttpResponse<String> httpResponse;
				try {
					httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(),
							HttpResponse.BodyHandlers.ofString());
					String result = httpResponse.body();
					if (!result.contains(":20002")) {
						sb.append(result + "\r\n");
					} else {
						sb1.append(str + "\r\n");
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				} finally {
					countDownLatch.countDown();
				}
			});
		}
		countDownLatch.await();
		Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareSuccess.txt"), sb.toString(),
				StandardCharsets.UTF_8, StandardOpenOption.CREATE);
		Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareFail.txt"), sb1.toString(),
				StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	}

	@Ignore
	@Test
	public void parseShareTest() throws URISyntaxException, IOException {
		List<String> readAllLines = Files.readAllLines(Paths.get(ShareTests.class.getResource("/share.txt").getPath()),
				StandardCharsets.UTF_8);
		StringBuilder sb = new StringBuilder();
		String prefix = readAllLines.get(0).split("\\)")[0].split("\\(")[1].split("-")[0];
		String content = readAllLines.get(1);
		String[] first = content.split("\\)");
		for (String str : first) {
			String[] tmp = str.split("\\(");
			sb.append(tmp[0]).append("\t").append(prefix).append("\t").append(tmp[1]).append("\r\n");
		}
		prefix = readAllLines.get(2).split("\\)")[0].split("\\(")[1].split("-")[0];
		content = readAllLines.get(3);
		first = content.split("\\)");
		for (String str : first) {
			String[] tmp = str.split("\\(");
			sb.append(tmp[0]).append("\t").append(prefix).append("\t").append(tmp[1]).append("\r\n");
		}
		Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareOk.txt"), sb.toString(),
				StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	}
}
