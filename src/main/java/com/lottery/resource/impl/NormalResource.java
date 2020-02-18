package com.lottery.resource.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lottery.resource.Resource;
import com.lottery.util.LoadResourceUtls;

/**
 * 
 * Desc:
 * 
 * @author: TGJ
 * @since: 1.0.0
 */
public class NormalResource implements Resource<Long> {

	private static final String OS = System.getProperty("os.name").toLowerCase();

	@Override
	public List<Long> load(String path) throws Exception {
		path = LoadResourceUtls.class.getResource(path).getPath();
		Path tmp = Paths.get(OS.indexOf("windows") != -1 ? path.substring(1, path.length()) : path);
		return Files.readAllLines(tmp, StandardCharsets.UTF_8).parallelStream()
				.map(t -> Long.valueOf(Stream.of(t.split("\\s")[3].split(",|\\\\")).collect(Collectors.joining())))
				.collect(Collectors.toList());
	}

}
