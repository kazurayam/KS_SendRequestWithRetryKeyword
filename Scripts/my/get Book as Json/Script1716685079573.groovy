// Test Cases/my/get Book as Json
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

ResponseObject response = WS.sendRequest(findTestObject('Object Repository/BookAsJson'))

WS.comment("status: " + response.getStatusCode())
WS.comment("content-type: " + response.getContentType())
println(response.getResponseBodyContent())

