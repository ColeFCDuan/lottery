package com.lottery.resource.impl;

import java.util.ArrayList;
import java.util.List;

import com.lottery.resource.Resource;
import com.lottery.util.LoadResourceUtls;

public class StringResource implements Resource<String> {

	@Override
	public List<String> load(String path) throws Exception {
		List<String> list = LoadResourceUtls.translateToList(LoadResourceUtls.loadResources(path),
				t -> t.split("\\s")[3]);
		list = LoadResourceUtls.translateToList(list, ",|\\\\", "");
		int size = list.size();
		String str = LoadResourceUtls.translateToString(list, null);
		List<String> results = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			results.add(str);
		}
		return results;
	}
}
