// Test Cases/my/repeat Getting Naughty URL with built-in keyword

import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

for (i in 1..10) {
	WS.callTestCase(findTestCase("my/Get Naughty URL with built-in keyword"), null)
}