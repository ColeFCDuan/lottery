package com.lottery.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class LoadResourceUtls {

	private static final String OS = System.getProperty("os.name").toLowerCase();

	private LoadResourceUtls() {
	}

	public static List<String> loadResources(String path) throws IOException {
		if (Objects.isNull(path))
			throw new NullPointerException("path is null");
		path = LoadResourceUtls.class.getResource(path).getPath();
		Path tmp = Paths.get(OS.indexOf("windows") != -1 ? path.substring(1, path.length()) : path);
		return Files.readAllLines(tmp, StandardCharsets.UTF_8);
	}

	public static String translateToString(List<String> list, String dilimiter) {
		if (Objects.isNull(list))
			return null;
		return list.parallelStream()
				.collect((Collector<CharSequence, ?, String>) (Objects.isNull(dilimiter) ? Collectors.joining()
						: Collectors.joining(dilimiter)));
	}

	public static List<String> translateToList(List<String> list, String regex, String replacement) {
		if (Objects.isNull(list))
			return List.of();
		return list.parallelStream().map(t -> t.replaceAll(regex, replacement)).collect(Collectors.toList());
	}

	public static <R> List<R> translateToList(List<String> list, Function<String, R> function) {
		if (Objects.isNull(list))
			return List.of();
		return list.parallelStream().map(function::apply).collect(Collectors.toList());
	}
}
