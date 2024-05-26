// Test Cases/my/repeat Getting Naughty URL with built-in keyword

import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

for (i in 1..15) {
	WS.callTestCase(findTestCase("my/get naughty URL using modified keyword"), null)
	WS.delay(1)
}