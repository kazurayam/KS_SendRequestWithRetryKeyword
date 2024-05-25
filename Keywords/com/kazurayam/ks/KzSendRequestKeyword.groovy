package com.kazurayam.ks

import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.keyword.internal.KeywordMain
import com.kms.katalon.core.keyword.internal.SupportLevel
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.common.HarLogger
import com.kms.katalon.core.webservice.constants.StringConstants
import com.kms.katalon.core.webservice.helper.WebServiceCommonHelper
import com.kms.katalon.core.webservice.keyword.internal.WebserviceAbstractKeyword


public class KzSendRequestKeyword {

	public static ResponseObject sendRequestWithRetry(RequestObject request) throws Exception {
		FailureHandling flowControl = RunConfiguration.getDefaultFailureHandling()
		return sendRequestWithRetry(request, flowControl)
	}

	public static ResponseObject sendRequestWithRetry(RequestObject request, FailureHandling flowControl) throws Exception {
		//println "called sendRequestWithRetry(RequestObject, ...)"
		int max = 10
		ResponseObject responseObject
		for (i in 1..max) {
			WebServiceCommonHelper.checkRequestObject(request)
			responseObject = WebServiceCommonHelper.sendRequest(request)

			//println("responseObject.getStatusCode()=" + responseObject.getStatusCode())
			//println("responseObject.getHeaderFields()=" + responseObject.getHeaderFields())

			// check if the status is OK
			if (responseObject.getStatusCode() >= 200 && responseObject.getStatusCode() < 300) {
				// check if the content-type is NOT html
				String contentType = responseObject.getHeaderFields().get('content-type').get(0)
				if (!contentType.contains('html')) {
					break  // exit the loop
				}
			}
			// log error
			println "retry " + i
			// wait a while to be gentle to the server
			Thread.sleep(1000)
			// retry sending request
		}
		return responseObject
	}
}
