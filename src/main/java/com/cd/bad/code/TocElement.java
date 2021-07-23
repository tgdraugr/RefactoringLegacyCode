package com.cd.bad.code;

import org.dom4j.Element;

public interface TocElement {
	String processedElement(String xPathExpression, Element element);
}
