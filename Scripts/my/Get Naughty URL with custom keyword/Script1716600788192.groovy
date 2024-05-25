// Test Cases/my/Get Naughty URL with custom keyword

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kazurayam.ks.SendRequestKeyword as WSK

ResponseObject response = WSK.sendRequestWithRetry(findTestObject('Object Repository/naughty'))

WS.comment("status: " + response.getStatusCode())
WS.comment("content-type: " + response.getContentType())
println(response.getResponseBodyContent())

assert response.getContentType().contains("application/json")
