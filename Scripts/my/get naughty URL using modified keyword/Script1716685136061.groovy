// Test Cases/my/Get Naughty URL with built-in keyword

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kazurayam.ks.WSBuiltInKeywordsModifier

import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webservice.keyword.builtin.SendRequestKeyword

// modify the implementation of "WS.sendRequest(RequestObject)" method 
// dynamically by Groovy Metaprogramming technique
WSBuiltInKeywordsModifier.modifySendRequest()

ResponseObject response = WS.sendRequest(findTestObject('Object Repository/naughty'))

WS.comment("status: " + response.getStatusCode())
WS.comment("content-type: " + response.getContentType())
println(response.getResponseBodyContent())

assert response.getContentType().contains("application/json")
