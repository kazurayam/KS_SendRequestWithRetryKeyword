package com.kazurayam.ks

import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords


public class WSBuiltInKeywordsModifier {

	public static void modifySendRequest() {

		Closure<ResponseObject> cls = { RequestObject request, FailureHandling flowControl = RunConfiguration.getDefaultFailureHandling() ->
			KzSendRequestKeyword kw = new KzSendRequestKeyword()
			return kw.sendRequestWithRetry(request, flowControl)
		}

		WSBuiltInKeywords.metaClass.static.sendRequest = cls
	}
}
