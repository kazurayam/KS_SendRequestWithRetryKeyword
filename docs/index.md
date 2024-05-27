# \[Katalon Studio\] Modifying WS.sendRequest keyword to support implicit retry on server error

-   author: kazurayam

-   document version: 0.3.0

-   Katalon Studio version: v9.0.0 Free

-   source project: <https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry>

## Problem to solve

### Application Under Test

This project bundles a HTTP Server that works on the localhost. Later, I will describe how to start the server on your machine. When the server is up, a URL becomes available:

-   <http://localhost:3000/naughty>

I will call this URL as **the naughty URL** for short. The naughty URL has the following characteristics.

\(1\) It returns an HTTP response with STATUS=200 and a JSON body:

    {
      "title": "JavaScript本格入門",
      "price": 3200,
      "publisher": "技術評論社"
    }

![01 01 status=200](https://kazurayam.github.io/KS_modify_SendRequestKeyword_with_retry/images/01_01_status=200.png)

\(2\) It occasionaly returns an HTTP response with STATUS=500 and a HTML body:

    <html>
      <head>
        <meta charset="UTF-8" />
        <title>500 Internal Server Error
        </title>
      </head>
      <body>
        <p>500 Internal Server Error
        </p>
      </body>
    </html>

![01 02 status=500](https://kazurayam.github.io/KS_modify_SendRequestKeyword_with_retry/images/01_02_status=500.png)

\(3\) It returns an Error at random. The probability is approximately 33%. 1 error per 3 requests. You can read the server source in TypeScript [app.ts](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/master/webserver/app.ts) ,Line#64 :

    router.get("/naughty", async (_req: Request, params: Record<string, string>) => {
      const r = randomNumber({ min: 1, max: 100 });
      if (modulo(r, 3) === 0) {
        const html = await Deno.readTextFile(`error.html`);
        return new Response(html, { status: 500, headers: {"content-type": "text/html; charset=utf-8"}});
      } else {
        const html = await Deno.readTextFile(`book.json`);
        return new Response(html, { headers: {"content-type": "application/json; charset=utf-8"}});
      }
    });

### A Test Case with built-in WS.sendRequest keyword fails on Server Error

We have a script [Test Cases/my/get naughty URL using built-in keyword](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/master/Scripts/my/get%20naughty%20URL%20using%20built-in%20keyword/Script1716685092640.groovy) that makes an HTTP request to the naughty URL using the Katalon built-in keyword [WS.sendRequest](https://docs.katalon.com/katalon-studio/keywords/keyword-description-in-katalon-studio/web-service-keywords/ws-send-request).

    // Test Cases/my/get naughty URL with built-in keyword

    import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

    import com.kms.katalon.core.testobject.ResponseObject
    import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

    ResponseObject response = WS.sendRequest(findTestObject('Object Repository/naughty'))

    WS.comment("status: " + response.getStatusCode())
    WS.comment("content-type: " + response.getContentType())
    println(response.getResponseBodyContent())

    assert response.getStatusCode() == 200
    assert response.getContentType().toLowerCase().contains("json")

Please note that this script asserts that the response has the HTTP STATUS "200 OK" and the Content-Type to be "json". If the HTTP STATUS is found to be "500", then this script will fail.

We have another scipt [Test Cases/my/repeat getting naught URL using built-in keyword](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Scripts/my/repeat%20getting%20naughty%20URL%20using%20built-in%20keyword/Script1716685152512.groovy).

    // Test Cases/my/repeat getting naughty URL using built-in keyword

    import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

    import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

    for (i in 1..10) {
        WS.callTestCase(findTestCase("my/get naughty URL using built-in keyword"), null)
        WS.delay(1)
    }

The latter script repeats calling the former script for multiple times (actually 10 times). When I run the latter script, it always stops midway with a failure of the former script. Why the former Test Case script fails? Because the built-in `WS.sendRequest` keyword returns a ResponseObject with HTTP STATUS=500 to the caller when the naughty URL replied an error.

### Problem: WS.sendRequest keyword doesn’t care Server errors

In the real world, some Katalon users developed WebService testing projects which make repetitive calls to `WS.sendRequest` keyword. For example, one has a project that makes 200 hundreds times of call to `WS.sendRequest`.

It is often the case that the AUT (Application Under Test) is still being developed so that is not robust enough. The AUT may respond with STATUS=500 rather often.
The problem is, as soon as the AUT respond an error, the test scirpt that uses `WS.sendRequest` keyword stops. If an error occured at the 100th request, the 101th and following requests would never be carried out. The productivity of WebService testers using Katalon Studio would go down.

![01 03 repeat using builtin keyword](https://kazurayam.github.io/KS_modify_SendRequestKeyword_with_retry/images/01_03_repeat_using_builtin_keyword.png)

### Feature request

I, as a WebService tester using Katalon Studio, want my test to be more robust against possible flakiness of the AUT server. I want the `WS.sendRequest` keyword to retry getting the target URL silently.

## Built-in Web Server as testbed

Here I will explain how to launch the HTTP server as testbed locally on your machine.

### ![02 01 Deno](https://kazurayam.github.io/KS_modify_SendRequestKeyword_with_retry/images/02_01_Deno.png) is required

On your machine, you need to install [Deno](https://deno.com/), the next generation JavaScript runtime. Please follow the installation instruction on their site.

I assume you have a bash Terminal where you can do this:

    $ deno --version
    deno 1.43.6 (release, x86_64-apple-darwin)
    v8 12.4.254.13
    typescript 5.4.5

The exact version does not matter. Any recent version will do.

### Download the project

You can download the zip of this repository from the
[Releases](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/releases/) page. Download the latest one, and unzip it.

### How to start the process

I would write `$PROJECT` to represent the directory where you located the project on your machine.

In the Terminal command line, do the following operations:

    $ cd $PROJECT
    $ cd webserver
    $ /bin/bash ./appstart.sh

    Listening on http://localhost:3000/

That’s it. The server is up. Now, you should be able to get access to the naughty URL:

-   <http://localhost:3000/naughty>

The [webserver/appstart.sh](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/webserver/appstart.sh) is a single line of shell script:

    deno run --allow-net --allow-read --allow-write --allow-env app.ts

The `appstart.sh` runs the `deno run` command while specifying a TypeScript code that creates a HTTP server:

-   [webserver/apps.ts](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/webserver/app.ts)

> It is nice to have a local HTTP server application in a WebUI/WebService Test Automation project. With it you can mimic your AUT and debug your tests. I’ve found that Deno is an easy-to-use but full-fledged platform to create a webserver as testbed.

## Solution

It is ideal if the built-in `WS.sendRequest` keyword in Katalon Studio is changed to be robust against server errors. But I can’t wait for their product development. Here I would show you my custom Groovy codes that give you the second best solution.

### Where to find the source code of com.kms.katalon.core.\*

In the following sections, I will present my Groovy classes. I wrote them based on the source of Katalon Studio. I found the source code of `com.kms.katalon.core.*` in the following directory of my Mac:

    $ pwd
    /Applications/Katalon Studio.app/Contents/Eclipse/configuration/resources/source

    $ tree -l 1 .
    1  [error opening dir]
    .
    ├── com.kms.katalon.core
    │   └── com.kms.katalon.core-sources.jar
    ├── com.kms.katalon.core.cucumber
    │   └── com.kms.katalon.core.cucumber-sources.jar
    ├── com.kms.katalon.core.mobile
    │   └── com.kms.katalon.core.mobile-sources.jar
    ├── com.kms.katalon.core.testng
    │   └── com.kms.katalon.core.testng-sources.jar
    ├── com.kms.katalon.core.webservice
    │   └── com.kms.katalon.core.webservice-sources.jar
    ├── com.kms.katalon.core.webui
    │   └── com.kms.katalon.core.webui-sources.jar
    └── com.kms.katalon.core.windows
        └── com.kms.katalon.core.windows-sources.jar

I unziped the jar files to get the sources in Java or Groovy. On Windows and Linux, you should be able to find the same jar files under the Katalon installed directory.

### Custom Groovy classes

I have developed 2 Groovy classes.

#### com.kazurayam.ks.KzSendRequestKeyword

-   [Keywords/com/kazurayam/ks/KzSendRequestKeyword.groovy](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Keywords/com/kazurayam/ks/KzSendRequestKeyword.groovy)

This class implements an alternative to the built-in `WS.sendRequest(RequestObject, FailureHandling)` keyword. This class send an HTTP Reuest and returns the ResponseObject. The difference is that, when the server returned an HTTP respose with STATUS != 200 OK, then the class makes retry silently.

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

#### com.kazurayam.ks.WSBuiltInKeywordsModifier

-   [Keywords/com/kazurayam/ks/WSBuiltInKeywordsModifier.groovy](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Keywords/com/kazurayam/ks/WSBuiltInKeywordsModifier.groovy)

This class modifies the `sendRequest` method of `com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords` class. Once the `modifySendRequest()` methos is called, the `sendRequest` method will be dynamically changed. The `WS.sendRequest()` call will no more uses the built-in class, but the method uses the `com.kazurayam.ks.KzSendRequestKeyword`.

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

### Descriptions

#### How the custom `sendRequestWithRetry` method works

I have developed 2 Test Cases to demonstrate how I can use the custom class `com.kazurayam.ks.KzSendRequestWithRetry`.

-   [Test Cases/my/get naught URL using custom keyword](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Scripts/my/get%20naughty%20URL%20using%20custom%20keyword/Script1716685104583.groovy)

<!-- -->

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

-   [Test Cases/my/repeat getting naughty URL using custom keyword](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Scripts/my/repeat%20getting%20naughty%20URL%20using%20custom%20keyword/Script1716685168111.groovy)

<!-- -->

    // Test Cases/my/repeat getting naughty URL using modified keyword

    import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

    import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

    for (i in 1..10) {
        WS.callTestCase(findTestCase("my/get naughty URL using custom keyword"), null)
        WS.delay(1)
    }

I ran the latter script which repeats calling the former script for 10 times. The former script gets the naught URL, which of course often responds with error of STATUS=500. But the the `KzSendRequestWithRetry` hide ths server error and silently makes retry. So the later script finished successful.

![03 01 repeat using custom keyword](https://kazurayam.github.io/KS_modify_SendRequestKeyword_with_retry/images/03_01_repeat_using_custom_keyword.png)

#### Want to modify the `WS.sendRequest` keyword for retry

Let me imagine that I have 200 Test Case scripts that calls the `WS.sendRequest` keyword. Now, I have developed a custom keyword `WSK.sendRequestWithRetry`. So, I should rewrite my scripts to use `WSK.sendRequestWithRetry` instead of `WS.sendRequest`. Oops, too much works. I don’t like to do it. Any other idea that requires far less rewrite works?

Here I introduce the `com.kazurayam.ks.WSBuitlInKeywordsModifier`. I have made 2 Test Case scripts to demostrate how to use it.

-   [Test Cases/my/get naughty URL using modified keyword](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Scripts/my/get%20naughty%20URL%20using%20modified%20keyword/Script1716685136061.groovy)

In this script, I call the `WSBuildInKeywordsModifier` to dynamically replace the implementation of `sendRequest(RequestObject,FailureHandling)` of the `com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords` class. The new implementation will link to the `com.kazurayam.ks.KzSendRequestKeyword` class, instead of the built-in `com.kms.katalon.core.webservice.kehyword.builtin.SendRequestKeyword`.

    // Test Cases/my/get naughty URL using modified keyword

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

    assert response.getStatusCode() == 200
    assert response.getContentType().toLowerCase().contains("json")

The most interesting thing in this script is the following 2 lines:

    WSBuiltInKeywordsModifier.modifySendRequest()

    ResponseObject response = WS.sendRequest(findTestObject('Object Repository/naughty'))

Here, the `sendRequest` method of `WSBuiltInKeywords` class is dynamically modified. Then we continu using the built-in `WS.sendRequest` keyword. I do not call `WSK.sendRequestWithRetry` explicitly, but I do call it indirectly.

Again, I made another scpript that calls the former script multiple times.

-   [<https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Scripts/my/repeat%20getting%20naughty%20URL%20using%20modified%20keyword/Script1716685188007.groovy>]()

<!-- -->

    // Test Cases/my/repeat Getting Naughty URL with built-in keyword

    import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

    import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

    for (i in 1..15) {
        WS.callTestCase(findTestCase("my/get naughty URL using modified keyword"), null)
        WS.delay(1)
    }

When I ran the latter script, it just passed. The naughty URL reponded STATUS=500 error sometimes, but the modified `WS.sendRequest` keyword silently ignored the error and performed retry. Goodness.

#### Want to minimize the code change

The final demonstration.

I created a Test Case script

-   [Test Cases/my/modifyBuiltInKeywords](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Scripts/my/modifyBuiltInKeywords/Script1716684372390.groovy)

It is simple.

    // Test Cases/my/modifyBuiltInKeywords

    import com.kazurayam.ks.WSBuiltInKeywordsModifier

    WSBuiltInKeywordsModifier.modifySendRequest()

Now I created a Test Suite that combines 2 Test Cases:

1.  [Test Cases/my/modifyBuiltInKeywords](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Scripts/my/modifyBuiltInKeywords/Script1716684372390.groovy)

2.  [Test Cases/my/repeat getting naughty URL using built-in keyword](https://github.com/kazurayam/KS_modify_SendRequestKeyword_with_retry/blob/develop/Scripts/my/repeat%20getting%20naughty%20URL%20using%20built-in%20keyword/Script1716685152512.groovy)

With the 1st Test Case "modifyBuiltInKeywords" in the Test Suite, the built-in keyword `WS.sendRequest` is replaced with new implementation with retry.

Please note that the 2nd Test Case "get naught URL using built-in keyword" is an old one. Literally I made no change.

When I ran this Test Suite, it just passed.

<figure>
<img src="images/04_01_testsuite.png" alt="04 01 testsuite" />
</figure>

The existing Test Case script performed differently as the result of the 1st script. Therefore, if you have hundreds of Test Cases in Test Suites that use the `WS.sendRequest` keyword, then you can insert the `modifyBuiltInKeywords` script into the Test Suites at the 1st row. That’s all. No need to change your existing Test Cases. The modified `WS.sendRequest` keyword will perform robust against the occasional server errors of STATUS=500.

## Conclusion

The `com.kazurayam.ks.KzSendRequestKeyword` class and the `com.kazurayam.ks.WSBuiltInKeywordsModifier` class enable you to modify the built-in `WS.sendRequest(RequestObject,FailureHandling)` keyword on the fly. The modified `WS.sendRequest` method will be robust against the occational errors responded by the AUT. The volume of code changes required for the existing Test Cases could be small and manageable.
