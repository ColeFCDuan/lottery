package com.lottery.resource;

import java.util.List;

import com.lottery.util.LoadResourceUtls;

public interface Resource<T> {

	@SuppressWarnings("unchecked")
	default List<T> load(String path) throws Exception {
		return (List<T>) LoadResourceUtls.translateToList(LoadResourceUtls.loadResources(path), t -> t.split("\\s")[3]);
	}

}
