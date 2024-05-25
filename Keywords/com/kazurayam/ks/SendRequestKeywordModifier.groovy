package com.kazurayam.ks

import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.builtin.SendRequestKeyword

public class SendRequestKeywordModifier {

	public SendRequestKeywordModifier() {}

	public void modify() {
		SendRequestKeyword.metaClass.sendRequest = { RequestObject request, FailureHandling flowControl ->
			println "calling KzSendRequestKeyword.sendRequestWithRetry method"
			return KzSendRequestKeyword.sendRequestWithRetry(request, flowControl)
		}
	}
}
