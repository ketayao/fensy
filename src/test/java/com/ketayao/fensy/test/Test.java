package com.ketayao.fensy.test;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {
		//简单认识正则表达式的概念
		/*
		p("abc".matches("..."));
		p("a8729a".replaceAll("//d", "-"));
		Pattern p = Pattern.compile("[a-z]{3}");
		Matcher m = p.matcher("fgh");
		p(m.matches());
		p("fgha".matches("[a-z]{3}"));
		p(Pattern.matches("a*b", "aaaaab"));
		 */

		//初步认识. * + ?
		/*
		p("a".matches("."));
		p("aa".matches("aa"));
		p("aaaa".matches("a*"));
		p("aaaa".matches("a+"));
		p("".matches("a*"));
		p("aaaa".matches("a?"));
		p("".matches("a?"));
		p("a".matches("a?"));
		p("214523145234532".matches("//d{3,100}"));
		p("192.168.0.aaa".matches("//d{1,3}//.//d{1,3}//.//d{1,3}//.//d{1,3}"));
		p("192".matches("[0-2][0-9][0-9]"));
		 */

		//范围
		/*
		p("a".matches("[abc]"));
		p("a".matches("[^abc]"));
		p("A".matches("[a-zA-Z]"));
		p("A".matches("[a-z]|[A-Z]"));
		p("A".matches("[a-z[A-Z]]"));
		p("R".matches("[A-Z&&[RFG]]"));
		 */

		//认识/s /w /d /
		/*
		p(" /n/r/t".matches("//s{4}"));
		p(" ".matches("//S"));
		p("a_8".matches("//w{3}"));
		p("abc888&^%".matches("[a-z]{1,3}//d+[&^#%]+"));
		p("//".matches("////"));
		 */

		//POSIX Style
		//p("a".matches("//p{Lower}"));
		//boundary


		//email
		//p("asdfasdfsafsf@dsdfsdf.com".matches("[//w[.-]]+@[//w[.-]]+//.[//w]+"));
		//matches find lookingAt

		Pattern p = Pattern.compile("\\d{3,5}");
		String s = "123-34345-234-00";
		Matcher m = p.matcher(s);
		p(m.matches());
		m.reset();
		p(m.find());
		p(m.start() + "-" + m.end());
		p(m.find());
		p(m.start() + "-" + m.end());
		p(m.find());
		p(m.start() + "-" + m.end());
		p(m.find());
		//p(m.start() + "-" + m.end());
		p("------------------------");
		p(m.lookingAt());
		p(m.start() + "-" + m.end());
		p(m.lookingAt());
		p(m.start() + "-" + m.end());
		p(m.lookingAt());
		p(m.lookingAt());


		//replacement
		/*
		Pattern p = Pattern.compile("java", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher("java Java JAVa JaVa IloveJAVA you hateJava afasdfasdf");
		StringBuffer buf = new StringBuffer();
		int i=0;
		while(m.find()) {
			i++;
			if(i%2 == 0) {
				m.appendReplacement(buf, "java");
			} else {
				m.appendReplacement(buf, "JAVA");
			}
		}
		m.appendTail(buf);
		p(buf);
		 */

		//group
		/*
		Pattern p = Pattern.compile("(//d{3,5})([a-z]{2})");
		String s = "123aa-34345bb-234cc-00";
		Matcher m = p.matcher(s);
		while(m.find()) {
			p(m.group());
			p(m.group(1));
			p(m.group(2));
		}
		 */

		//qulifiers
		/*
		Pattern p = Pattern.compile(".{3,10}+[0-9]");
		String s = "aaaa5bbbb68";
		Matcher m = p.matcher(s);
		if(m.find())
			p(m.start() + "-" + m.end());
		else 
			p("not match!");
		 */

		//non-capturing groups
		//从右侧开始匹配，并且不会捕获a字符
		/*
		Pattern p = Pattern.compile(".{3}(?=a)");
		String s = "444a663a34b";
		Matcher m = p.matcher(s);
		while(m.find()) {
			p(m.group());
		}
		 */

		//从右侧开始匹配，并且会捕获a字符
		/*
		Pattern p = Pattern.compile(".{3}(?!a)");
		String s = "444a663a343a";
		Matcher m = p.matcher(s);
		while(m.find()) {
			p(m.group());
		}
		 */

		/*
		 * (?=X ) 零宽度正先行断言。仅当子表达式 X 在 此位置的右侧匹配时才继续匹配。
		 * 			例如，/w+(?=/d) 与后跟数字的单词匹配，而不与该数字匹配。此构造不会回溯。 
		 * (?!X) 零宽度负先行断言。仅当子表达式 X 不在 此位置的右侧匹配时才继续匹配。
		 * 			例如，例如，/w+(?!/d) 与后不跟数字的单词匹配，而不与该数字匹配。 
		   (?<=X) 零宽度正后发断言。仅当子表达式 X 在 此位置的左侧匹配时才继续匹配。
		   			例如，(?<=19)99 与跟在 19 后面的 99 的实例匹配。此构造不会回溯。 
		   (?<!X) 零宽度负后发断言。仅当子表达式 X 不在此位置的左侧匹配时才继续匹配。
		   			例如，(?<!19)99 与不跟在 19 后面的 99 的实例匹配 
		 * 
		 */

		//back refenrences
		/*
		Pattern p = Pattern.compile("(//d(//d))//2");
		String s = "122";
		Matcher m = p.matcher(s);
		p(m.matches());
		 */

		//flags的简写
		//Pattern p = Pattern.compile("java", Pattern.CASE_INSENSITIVE);
		//p("Java".matches("(?i)(java)"));
	}

	public static void p(Object o) {
		System.out.println(o);
	}

}