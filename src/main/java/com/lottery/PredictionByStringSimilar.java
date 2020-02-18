package com.lottery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lottery.util.LoadResourceUtls;

public class PredictionByStringSimilar {

	/**
	 * 编程之美 计算字符串的相似度 我们定义一套操作方法来把两个不相同的字符串变得相同，具体的操作方法为： 1.修改一个字符（如把“a”替换为“b”）;
	 * 2.增加一个字符（如把“abdd”变为“aebdd”）; 3.删除一个字符（如把“travelling”变为“traveling”）;
	 * 比如，对于“abcdefg”和“abcdef”两个字符串来说，我们认为可以通过增加/减少一个“g”的方式来达到目的。 上面的两种方案，都仅需要一次
	 * 。把这个操作所需要的次数定义为两个字符串的距离，而相似度等于“距离+1”的倒数。 也就是说，“abcdefg”和“abcdef”的距离为1，相似度
	 * 为1/2=0.5。 给定任意两个字符串，你是否能写出一个算法来计算它们的相似度呢？
	 * 
	 * 解答：动态规划+备忘录
	 * 2012-11-04：主要思路还是递归。字符串记为A和B（当前比较的位置记为K，当前距离记为L），从第一个字符开始按位比较，分两种情况：
	 * 1、A和B在第K位的字符相等（L不变）。那好，各自向后移动，继续比较第K+1位
	 * 2、A和B在第K位的字符不相等（L=L+1）。采取递归，作三种操作，看哪种操作最后得到的距离最短：
	 * 一是A和B同时向后移动（相当于A和B同时删除这个字符），继续比较第K+1位 二是A移动B不移动，相当于A删除了这个字符，用剩余的字符与B作比较
	 * 三是A不移动B移动，相当于B删除了这个字符，用剩余的字符与A作比较 递归的好处就是可以递归得到这三种操作到最后得到的距离，哪个是最短
	 * 举个例子，A="abc",B="zbc"。我们可以一眼看出，采用第一种操作算得的距离最短（L=1） 但程序中要递归执行这另外两种操作并比较：
	 * A1="bc",B2="zbc" -->按位比较得到的L=1+3 A2="abc",B2="bc" -->按位比较得到的L=1+3
	 * 因此程序会选择第一种操作，再接着进行第K+1位的比较
	 */
	private final static Logger log = LoggerFactory.getLogger(PredictionByStringSimilar.class);
	private final static String PATH = "/ssq_result.txt";
	private static int[][] record; // 记录子问题的解，0表示子问题未求解

	public static void main(String[] args) throws IOException {
		List<String> list = LoadResourceUtls.translateToList(LoadResourceUtls.loadResources(PATH),
				t -> t.split("\\s")[3]);
		list = LoadResourceUtls.translateToList(list, ",|\\\\", "");
		String dest = LoadResourceUtls.translateToString(list, null);
		System.out.println(dest.length());
		String[] strBB = { dest };
		for (String strB : strBB) {
			log.debug(String.valueOf(jaccard(dest, strB)));
//			log.debug(String.valueOf(SorensenDice(dest, strB)));
//			log.debug(String.valueOf(levenshtein(dest, strB)));
//			log.debug(String.valueOf(hamming(dest, strB)));
//			log.debug(String.valueOf(cos(dest, strB)));
//			// 超级慢
//			int distance = distanceBetween(dest, strB);
//			int distance = minDistance(dest, strB);
//			log.debug(String.valueOf(1.0 / (distance + 1)));
		}
	}
	
	public static float cos(String a, String b) {
        if (a == null || b == null) {
            return 0F;
        }
        Set<Integer> aChar = a.chars().boxed().collect(Collectors.toSet());
        Set<Integer> bChar = b.chars().boxed().collect(Collectors.toSet());

        // 统计字频
        Map<Integer, Integer> aMap = new HashMap<>();
        Map<Integer, Integer> bMap = new HashMap<>();
        for (Integer a1 : aChar) {
            aMap.put(a1, aMap.getOrDefault(a1, 0) + 1);
        }
        for (Integer b1 : bChar) {
            bMap.put(b1, bMap.getOrDefault(b1, 0) + 1);
        }

        // 向量化
        Set<Integer> union = SetUtils.union(aChar, bChar);
        int[] aVec = new int[union.size()];
        int[] bVec = new int[union.size()];
        List<Integer> collect = new ArrayList<>(union);
        for (int i = 0; i < collect.size(); i++) {
            aVec[i] = aMap.getOrDefault(collect.get(i), 0);
            bVec[i] = bMap.getOrDefault(collect.get(i), 0);
        }

        // 分别计算三个参数
        int p1 = 0;
        for (int i = 0; i < aVec.length; i++) {
            p1 += (aVec[i] * bVec[i]);
        }

        float p2 = 0f;
        for (int i : aVec) {
            p2 += (i * i);
        }
        p2 = (float) Math.sqrt(p2);

        float p3 = 0f;
        for (int i : bVec) {
            p3 += (i * i);
        }
        p3 = (float) Math.sqrt(p3);

        return ((float) p1) / (p2 * p3);
    }
	
	public static float hamming(String a, String b) {
        if (a == null || b == null) {
            return 0f;
        }
        if (a.length() != b.length()) {
            return 0f;
        }

        int disCount = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                disCount++;
            }
        }
        return (float) disCount / (float) a.length();
    }

	public static float levenshtein(String a, String b) {
		if (a == null && b == null) {
			return 1f;
		}
		if (a == null || b == null) {
			return 0F;
		}
		int editDistance = editDis(a, b);
		return 1 - ((float) editDistance / Math.max(a.length(), b.length()));
	}

	private static int editDis(String a, String b) {

		int aLen = a.length();
		int bLen = b.length();

		if (aLen == 0)
			return aLen;
		if (bLen == 0)
			return bLen;

		int[][] v = new int[aLen + 1][bLen + 1];
		for (int i = 0; i <= aLen; ++i) {
			for (int j = 0; j <= bLen; ++j) {
				if (i == 0) {
					v[i][j] = j;
				} else if (j == 0) {
					v[i][j] = i;
				} else if (a.charAt(i - 1) == b.charAt(j - 1)) {
					v[i][j] = v[i - 1][j - 1];
				} else {
					v[i][j] = 1 + Math.min(v[i - 1][j - 1], Math.min(v[i][j - 1], v[i - 1][j]));
				}
			}
		}
		return v[aLen][bLen];
	}

	public static float SorensenDice(String a, String b) {
		if (a == null && b == null) {
			return 1f;
		}
		if (a == null || b == null) {
			return 0F;
		}
		Set<Integer> aChars = a.chars().boxed().collect(Collectors.toSet());
		Set<Integer> bChars = b.chars().boxed().collect(Collectors.toSet());
		// 求交集数量
		int intersect = SetUtils.intersection(aChars, bChars).size();
		if (intersect == 0) {
			return 0F;
		}
		// 全集，两个集合直接加起来
		int aSize = aChars.size();
		int bSize = bChars.size();
		return (2 * (float) intersect) / ((float) (aSize + bSize));
	}

	public static float jaccard(String a, String b) {
		if (a == null && b == null) {
			return 1f;
		}
		// 都为空相似度为 1
		if (a == null || b == null) {
			return 0f;
		}
		Set<Integer> aChar = a.chars().boxed().collect(Collectors.toSet());
		Set<Integer> bChar = b.chars().boxed().collect(Collectors.toSet());
		// 交集数量
		int intersection = SetUtils.intersection(aChar, bChar).size();
		if (intersection == 0)
			return 0;
		// 并集数量
		int union = SetUtils.union(aChar, bChar).size();
		return ((float) intersection) / (float) union;
	}

	public static int minDistance(String word1, String word2) {
		if (word1.length() == 0 || word2.length() == 0)
			return word1.length() == 0 ? word2.length() : word1.length();
		int[][] arr = new int[word1.length() + 1][word2.length() + 1];
		for (int i = 0; i <= word1.length(); i++) {
			arr[i][0] = i;
		}
		for (int j = 0; j <= word2.length(); j++) {
			arr[0][j] = j;
		}
		for (int i = 0; i < word1.length(); i++) {
			for (int j = 0; j < word2.length(); j++) {
				if (word1.charAt(i) == word2.charAt(j)) {
					arr[j + 1][i + 1] = arr[j][i];
				} else {
					int replace = arr[i][j] + 1;
					int insert = arr[i][j + 1] + 1;
					int delete = arr[i + 1][j] + 1;

					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					arr[i + 1][j + 1] = min;
				}
			}
		}
		return arr[word1.length()][word2.length()];
	}

	public static int distanceBetween(String strA, String strB) {
		int distance = -1;
		if (strA != null && strB != null) {
			int lenA = strA.length();
			int lenB = strB.length();
			if (lenA == 0 && lenB == 0) {
				distance = 0;
			}
			if (lenA != 0 && lenB == 0) {
				distance = lenA;
			}
			if (lenA == 0 && lenB != 0) {
				distance = lenB;
			}
			if (lenA != 0 && lenB != 0) {
				record = new int[lenA + 1][lenB + 1];
				char[] charArrayA = strA.toCharArray();
				char[] charArrayB = strB.toCharArray();
				distance = distanceHelp(charArrayA, charArrayB, 0, 0, lenA - 1, lenB - 1);
			}
		}
		return distance;
	}

	// endA和endB是不变的，因此记录子问题的解可用record[beginA][beginB]来表示
	public static int distanceHelp(char[] charArrayA, char[] charArrayB, int beginA, int beginB, int endA, int endB) {
		if (beginA > endA) { // 递归出口：A从头到尾每个字符遍历完了，B有两种情况：
			if (beginB > endB) { // 1.B也同时遍历完了，说明这A=B
				return 0;
			} else {
				return endB - beginB + 1; // 2.B还没遍历完，那B剩下的长度就是两个字符串不同的地方，即距离
			}
		}
		if (beginB > endB) {
			if (beginA > endA) {
				return 0;
			} else {
				return endA - beginA + 1;
			}
		}

		int distance = -1;
		if (charArrayA[beginA] == charArrayB[beginB]) {
			distance = record[beginA + 1][beginB + 1];
			if (distance == 0) {
				distance = distanceHelp(charArrayA, charArrayB, beginA + 1, beginB + 1, endA, endB);
			}
		} else {
			int d1 = record[beginA + 1][beginB];
			if (d1 == 0) {
				d1 = distanceHelp(charArrayA, charArrayB, beginA + 1, beginB, endA, endB);
			}
			int d2 = record[beginA][beginB + 1];
			if (d2 == 0) {
				d2 = distanceHelp(charArrayA, charArrayB, beginA, beginB + 1, endA, endB);
			}
			int d3 = record[beginA + 1][beginB + 1];
			if (d3 == 0) {
				d3 = distanceHelp(charArrayA, charArrayB, beginA + 1, beginB + 1, endA, endB);
			}
			distance = min(d1, d2, d3) + 1;
		}
		record[beginA][beginB] = distance;
		return distance;
	}

	private static int min(int x, int... yy) {
		int m = x;
		for (int y : yy) {
			if (y < m) {
				m = y;
			}
		}
		return m;
	}
}
