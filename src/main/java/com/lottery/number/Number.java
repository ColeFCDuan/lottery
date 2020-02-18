package com.lottery.number;

import java.util.Random;

public interface Number {

	<T> T getNumber(Random random) throws Exception;

	<T> T getNumber() throws Exception;
}
