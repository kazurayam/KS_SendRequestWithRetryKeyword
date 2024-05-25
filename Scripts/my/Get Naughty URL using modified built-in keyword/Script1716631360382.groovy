// Test Cases/my/Get Naughty URL with built-in keyword

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kazurayam.ks.SendRequestKeywordModifier
import com.kms.katalon.core.webservice.keyword.builtin.SendRequestKeyword

new SendRequestKeywordModifier().modify()
// for debug
List<MetaMethod> methods = SendRequestKeyword.metaClass.getExpandoMethods()
println "methods.size()=" + methods.size()
methods.each { println it };
println "modified the SendRequestKeyword class"


ResponseObject response = WS.sendRequest(findTestObject('Object Repository/naughty'))

WS.comment("status: " + response.getStatusCode())
WS.comment("content-type: " + response.getContentType())
println(response.getResponseBodyContent())

assert response.getContentType().contains("application/json")
