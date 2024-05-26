// Test Cases/my/repeat getting naughty URL using built-in keyword

import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

for (i in 1..10) {
	WS.callTestCase(findTestCase("my/get naughty URL using built-in keyword"), null)
	WS.delay(1)
}