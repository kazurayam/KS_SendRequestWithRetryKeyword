// Test Cases/my/get naughty URL using custom keyword

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kazurayam.ks.KzSendRequestKeyword

KzSendRequestKeyword kw = new KzSendRequestKeyword()
ResponseObject response = kw.sendRequestWithRetry(findTestObject('Object Repository/naughty'), )

WS.comment("status: " + response.getStatusCode())
WS.comment("content-type: " + response.getContentType())
println(response.getResponseBodyContent())

assert response.getStatusCode() == 200
assert response.getContentType().toLowerCase().contains("json")