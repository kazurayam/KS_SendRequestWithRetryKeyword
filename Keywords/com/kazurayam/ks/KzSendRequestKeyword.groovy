package com.kazurayam.ks

import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.helper.WebServiceCommonHelper


public class KzSendRequestKeyword {

	public KzSendRequestKeyword() {}

	public ResponseObject sendRequestWithRetry(
							RequestObject request,
							FailureHandling flowControl=RunConfiguration.getDefaultFailureHandling())
			throws Exception {
		//println "called sendRequestWithRetry(RequestObject, ...)"
		int max = 5
		ResponseObject responseObject
		for (i in 1..max) {
			WebServiceCommonHelper.checkRequestObject(request)
			responseObject = WebServiceCommonHelper.sendRequest(request)
			//println("responseObject.getStatusCode()=" + responseObject.getStatusCode())
			//println("responseObject.getHeaderFields()=" + responseObject.getHeaderFields())

			// check if the responseObject is acceptable or not
			if (condition.call(responseObject)) {
				break  // exit the loop
			}

			// the responseObject is not acceptable, so log error and retry conversation
			println "retry " + i
			// wait a while to be gentle to the server
			Thread.sleep(1000)
		}
		return responseObject
	}

	private Closure condition = { ResponseObject responseObject ->
		return responseObject.getStatusCode() >= 200 && responseObject.getStatusCode() < 300
	}

	public void setCondition(Closure cls) {
		condition = cls
	}
}
