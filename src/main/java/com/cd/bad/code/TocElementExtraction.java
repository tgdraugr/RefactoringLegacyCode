package com.cd.bad.code;

import org.dom4j.Element;

public interface TocElementExtraction {
	String processElement(String xPathExpression, Element element);
}
